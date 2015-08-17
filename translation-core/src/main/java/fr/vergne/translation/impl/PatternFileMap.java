package fr.vergne.translation.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import fr.vergne.logging.LoggerConfiguration;
import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationMetadata;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.TranslationMetadata.FieldListener;
import fr.vergne.translation.impl.PatternFileMap.PatternEntry;
import fr.vergne.translation.util.EntryFilter;
import fr.vergne.translation.util.Switcher;
import fr.vergne.translation.util.impl.IdentitySwitcher;

public class PatternFileMap implements TranslationMap<PatternEntry> {

	private final File file;
	private final Pattern entryPattern;
	private final Pattern originalPattern;
	private final Pattern translationPattern;
	private final Map<Field<?>, Pattern> fieldPatterns = new HashMap<>();
	private final Map<Field<?>, Switcher<String, ?>> fieldSwitchers = new HashMap<>();
	private final Collection<Field<?>> editableFields = new HashSet<>();
	@SuppressWarnings("serial")
	private final List<String> referenceStore = new ArrayList<String>() {
		public boolean add(String content) {
			throw new UnsupportedOperationException();
		};

		public void add(int index, String content) {
			throw new UnsupportedOperationException();
		};

		public String set(int index, String content) {
			if (index == size()) {
				super.add(content);
				return null;
			} else {
				return super.set(index, content);
			}
		};
	};
	private final Map<Integer, Object> modifiedStore = new HashMap<>();
	private final Runnable saver = new Runnable() {

		@Override
		public void run() {
			logger.info("Saving " + file + "...");
			Charset charset = Charset.forName("UTF-8");
			try {
				FileOutputStream out = new FileOutputStream(file);
				for (String content : referenceStore) {
					out.write(content.getBytes(charset));
				}
				out.close();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			logger.info("File saved.");
		}
	};

	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private static final Switcher<String, String> STRING_SWITCHER = new IdentitySwitcher<>();

	public PatternFileMap(File file, String entryRegex, String originalRegex,
			String translationRegex) {
		this.file = file;
		this.entryPattern = Pattern.compile(entryRegex);
		this.originalPattern = Pattern.compile(originalRegex);
		this.translationPattern = Pattern.compile(translationRegex);
	}

	private List<PatternEntry> entries = null;

	private List<PatternEntry> getEntries() {
		if (this.entries != null) {
			// reuse
		} else {
			Iterator<PatternEntry> iterator = startIterator();
			List<PatternEntry> tempEntries = new LinkedList<>();
			while (iterator.hasNext()) {
				PatternEntry entry = iterator.next();
				tempEntries.add(entry);
			}
			this.entries = new ArrayList<>(tempEntries);
		}
		return this.entries;
	}

	public <T> void addFieldRegex(Field<T> field, String regex,
			Switcher<String, T> switcher, boolean editable) {
		fieldPatterns.put(field, Pattern.compile(regex));
		fieldSwitchers.put(field, switcher);
		if (editable) {
			editableFields.add(field);
		} else {
			editableFields.remove(field);
		}
	}

	public void addFieldRegex(Field<String> field, String regex,
			boolean editable) {
		addFieldRegex(field, regex, STRING_SWITCHER, editable);
	}

	@Override
	public Iterator<PatternEntry> iterator() {
		return getEntries().iterator();
	}

	private Iterator<PatternEntry> startIterator() {
		logger.info("Reading " + file + "...");
		final String content;
		try {
			content = FileUtils.readFileToString(file);
		} catch (IOException e) {
			throw new RuntimeException();
		}
		return new Iterator<PatternEntry>() {

			private final Matcher entryMatcher = entryPattern.matcher(content);
			private String nextEntry = null;
			private int stored = 0;
			private int storeIndex = -1;

			@Override
			public boolean hasNext() {
				if (nextEntry != null) {
					// already found
				} else if (entryMatcher.find()) {
					logger.finer("Find new entry, processing...");
					nextEntry = entryMatcher.group();
					int nextStored = entryMatcher.start();
					if (stored < nextStored) {
						String extract = content.substring(stored, nextStored);
						stored = nextStored;
						storeIndex++;
						referenceStore.set(storeIndex, extract);
						logger.finer("Text before entry stored (" + stored
								+ " chars, " + (storeIndex + 1) + " pieces).");
					} else {
						// nothing between the two entries
					}
				} else {
					// no more
					if (stored < content.length()) {
						String extract = content.substring(stored);
						stored = content.length();
						storeIndex++;
						referenceStore.set(storeIndex, extract);
						logger.finer("End of map stored (" + stored
								+ " chars, " + (storeIndex + 1) + " pieces).");
					} else {
						// nothing after the entries
					}
				}
				return nextEntry != null;
			}

			@Override
			public PatternEntry next() {
				if (hasNext()) {
					try {
						/*
						 * EXTRACT USED SUBSTRINGS
						 */

						logger.finest("ENTRY: " + nextEntry);
						Map<Object, String> substore = new HashMap<>();
						@SuppressWarnings("serial")
						Map<Integer, Object> starts = new HashMap<Integer, Object>() {
							@Override
							public Object put(Integer key, Object value) {
								Object old = super.put(key, value);
								if (old != null) {
									throw new RuntimeException(value
											+ " overlaps with " + old);
								} else {
									return old;
								}
							}
						};

						Matcher original = originalPattern.matcher(nextEntry);
						if (original.find()) {
							substore.put(original, original.group());
							starts.put(original.start(), original);
							logger.finest("- O: " + original.group());
						} else {
							throw new ParsingException(
									"Impossible to find original version in entry: "
											+ nextEntry);
						}

						Matcher translation = translationPattern
								.matcher(nextEntry);
						if (translation.find()) {
							substore.put(translation, translation.group());
							starts.put(translation.start(), translation);
							logger.finest("- T: " + translation.group());
						} else {
							throw new ParsingException(
									"Impossible to find translated version in entry: "
											+ nextEntry);
						}

						for (Field<?> field : fieldSwitchers.keySet()) {
							Pattern pattern = fieldPatterns.get(field);
							Matcher matcher = pattern.matcher(nextEntry);
							if (matcher.find()) {
								String group = matcher.group();
								substore.put(field, group);
								starts.put(matcher.start(), field);
								Switcher<String, ?> switcher = fieldSwitchers
										.get(field);
								logger.finest("- " + field + ": \"" + group
										+ "\" -> "
										+ switcher.switchForth(group));
							} else {
								throw new ParsingException(
										"Impossible to find field " + field
												+ " in entry: " + nextEntry);
							}
						}

						/*
						 * EXTRACT ALL ORDERED SUBSTRINGS
						 */

						Map<Object, Integer> storeIndexes = new HashMap<>();
						TreeSet<Integer> sortedStarts = new TreeSet<>(
								starts.keySet());
						int substored = 0;
						String previous = null;
						final int firstIndex = storeIndex + 1;
						for (Integer start : sortedStarts) {
							if (substored < start) {
								storeIndex++;
								referenceStore.set(storeIndex,
										nextEntry.substring(substored, start));
								substored = start;
							} else if (substored > start) {
								String current = substore
										.get(starts.get(start));
								throw new RuntimeException(current
										+ " overlaps with " + previous);
							} else {
								// nothing unused before
							}

							Object object = starts.get(start);
							String current = substore.get(object);
							storeIndex++;
							referenceStore.set(storeIndex, current);
							storeIndexes.put(object, storeIndex);
							substored += current.length();

							previous = current;
						}
						if (substored < stored + nextEntry.length()) {
							storeIndex++;
							referenceStore.set(storeIndex,
									nextEntry.substring(substored));
						} else {
							// nothing unused after
						}
						stored += nextEntry.length();
						final int lastIndex = storeIndex;

						/*
						 * INSTANTIATE ENTRY
						 */

						Map<Field<?>, StoreAccessor<?>> fieldAccessors = new HashMap<>();
						for (Field<?> field : fieldSwitchers.keySet()) {
							Integer index = storeIndexes.get(field);
							Switcher<String, ?> switcher = fieldSwitchers
									.get(field);
							fieldAccessors.put(field, new StoreAccessor<>(
									referenceStore, modifiedStore, index,
									switcher));
						}
						PatternMetadata metadata = new PatternMetadata(
								fieldAccessors, editableFields, saver);
						Integer index = storeIndexes.get(translation);
						StoreAccessor<String> translationAccessor = new StoreAccessor<>(
								referenceStore, modifiedStore, index,
								STRING_SWITCHER);
						StringRebuilder rebuilder = new StringRebuilder(
								referenceStore, firstIndex, lastIndex);
						PatternEntry entry = new PatternEntry(original.group(),
								translationAccessor, metadata, saver, rebuilder);
						logger.finer("Entry stored (" + stored + " chars, "
								+ (storeIndex + 1) + " pieces).");

						return entry;
					} finally {
						nextEntry = null;
					}
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"You cannot remove an entry from this map.");
			}
		};
	}

	@Override
	public PatternEntry getEntry(int index) {
		return getEntries().get(index);
	}

	public String getBeforeEntry(int index) {
		return getBetweenEntries(index - 1, index);
	}

	public String getAfterEntry(int index) {
		return getBetweenEntries(index, index + 1);
	}

	public String getBetweenEntries(int indexFrom, int indexTo) {
		int storeStart;
		if (indexFrom < 0) {
			storeStart = 0;
		} else if (indexFrom < size()) {
			storeStart = getEntry(indexFrom).getLastIndex() + 1;
		} else {
			throw new IndexOutOfBoundsException("The first index (" + indexFrom
					+ ") cannot be higher then the last entry (" + (size() - 1)
					+ ")");
		}

		int storeEnd;
		if (indexTo >= size()) {
			storeEnd = referenceStore.size() - 1;
		} else if (indexTo > indexFrom) {
			storeEnd = getEntry(indexTo).getFirstIndex() - 1;
		} else {
			throw new IndexOutOfBoundsException("The first index (" + indexFrom
					+ ") should be inferior to the second (" + indexTo + ")");
		}

		return new StringRebuilder(referenceStore, storeStart, storeEnd)
				.toString();
	}

	@Override
	public int size() {
		return getEntries().size();
	}

	@Override
	public void saveAll() {
		for (PatternEntry entry : this) {
			entry.saveAllBackgroundOnly();
		}
		saver.run();
	}

	@Override
	public void resetAll() {
		for (PatternEntry entry : this) {
			entry.resetAll();
		}
	}

	private final Collection<EntryFilter<PatternEntry>> filters = new HashSet<>();

	@Override
	public Collection<EntryFilter<PatternEntry>> getEntryFilters() {
		return filters;
	}

	public void addEntryFilter(EntryFilter<PatternEntry> filter) {
		filters.add(filter);
	}

	public static class PatternMetadata implements TranslationMetadata {

		private final Map<Field<?>, StoreAccessor<?>> accessors;
		private final Collection<Field<?>> editableFields;
		private final Collection<FieldListener> listeners = new HashSet<>();
		private final Runnable saver;

		public PatternMetadata(Map<Field<?>, StoreAccessor<?>> fieldAccessors,
				Collection<Field<?>> editableFields, Runnable saver) {
			this.accessors = fieldAccessors;
			this.editableFields = editableFields;
			this.saver = saver;
		}

		@Override
		public Iterator<Field<?>> iterator() {
			return accessors.keySet().iterator();
		}

		@SuppressWarnings("unchecked")
		private <T> StoreAccessor<T> getAccessor(Field<T> field) {
			StoreAccessor<?> accessor = accessors.get(field);
			if (accessor == null) {
				throw new IllegalArgumentException("Unknown field: " + field);
			} else {
				return (StoreAccessor<T>) accessor;
			}
		}

		@Override
		public <T> T getStored(Field<T> field) {
			return getAccessor(field).getStored();
		}

		@Override
		public <T> T get(Field<T> field) {
			return getAccessor(field).get();
		}

		@Override
		public <T> boolean isEditable(Field<T> field) {
			return editableFields.contains(field);
		}

		@Override
		public <T> void set(Field<T> field, T value)
				throws UneditableFieldException {
			if (isEditable(field)) {
				getAccessor(field).set(value);
				for (FieldListener listener : listeners) {
					listener.fieldUpdated(field, value);
				}
			} else {
				throw new UneditableFieldException(field);
			}
		}

		@Override
		public void addFieldListener(FieldListener listener) {
			listeners.add(listener);
		}

		@Override
		public void removeFieldListener(FieldListener listener) {
			listeners.remove(listener);
		}

		@Override
		public <T> void save(Field<T> field) {
			accessors.get(field).save();
			saver.run();
			for (FieldListener listener : listeners) {
				listener.fieldStored(field);
			}
		}

		@Override
		public <T> void reset(Field<T> field) {
			accessors.get(field).reset();
			for (FieldListener listener : listeners) {
				listener.fieldUpdated(field, getStored(field));
			}
		}

		@Override
		public void saveAll() {
			saveAllBackgroundOnly();
			saver.run();
			for (FieldListener listener : listeners) {
				for (Field<?> field : this) {
					listener.fieldStored(field);
				}
			}
		}

		public void saveAllBackgroundOnly() {
			for (Field<?> field : this) {
				accessors.get(field).save();
			}
		}

		@Override
		public void resetAll() {
			for (Field<?> field : this) {
				reset(field);
			}
		}

		@Override
		public String toString() {
			Map<Field<?>, Object> map = new HashMap<>();
			for (Field<?> field : this) {
				map.put(field, get(field));
			}
			return map.toString();
		}

	}

	public static class PatternEntry implements
			TranslationEntry<PatternMetadata> {

		private final String original;
		private final StoreAccessor<String> translationAccessor;
		private final PatternMetadata metadata;
		private final Collection<TranslationListener> listeners = new HashSet<TranslationListener>();
		private final Runnable saver;
		private final StringRebuilder rebuilder;

		public PatternEntry(String original,
				StoreAccessor<String> translationAccessor,
				PatternMetadata metadata, Runnable saver,
				StringRebuilder rebuilder) {
			this.original = original;
			this.translationAccessor = translationAccessor;
			this.metadata = metadata;
			this.saver = saver;
			this.rebuilder = rebuilder;
		}

		private int getFirstIndex() {
			return rebuilder.firstIndex;
		}

		private int getLastIndex() {
			return rebuilder.lastIndex;
		}

		@Override
		public String getOriginalContent() {
			return original;
		}

		@Override
		public String getStoredTranslation() {
			return translationAccessor.getStored();
		}

		@Override
		public String getCurrentTranslation() {
			return translationAccessor.get();
		}

		@Override
		public void setCurrentTranslation(String translation) {
			if (translation == null) {
				throw new IllegalArgumentException("No translation provided: "
						+ translation);
			} else {
				translationAccessor.set(translation);
				for (TranslationListener listener : listeners) {
					listener.translationUpdated(translation);
				}
			}
		}

		@Override
		public void saveTranslation() {
			translationAccessor.save();
			saver.run();
		}

		@Override
		public void resetTranslation() {
			translationAccessor.reset();
			for (TranslationListener listener : listeners) {
				listener.translationUpdated(getStoredTranslation());
			}
		}

		@Override
		public void saveAll() {
			saveAllBackgroundOnly();
			saver.run();
			for (TranslationListener listener : listeners) {
				listener.translationStored();
			}
			for (FieldListener listener : metadata.listeners) {
				for (Field<?> field : metadata) {
					listener.fieldStored(field);
				}
			}
		}

		public void saveAllBackgroundOnly() {
			translationAccessor.save();
			metadata.saveAllBackgroundOnly();
		}

		@Override
		public void resetAll() {
			resetTranslation();
			metadata.resetAll();
		}

		@Override
		public PatternMetadata getMetadata() {
			return metadata;
		}

		@Override
		public void addTranslationListener(TranslationListener listener) {
			listeners.add(listener);
		}

		@Override
		public void removeTranslationListener(TranslationListener listener) {
			listeners.remove(listener);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (obj instanceof PatternEntry) {
				PatternEntry e = (PatternEntry) obj;
				return translationAccessor == e.translationAccessor;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return translationAccessor.hashCode();
		}

		public String rebuildString() {
			return rebuilder.toString();
		}

		@Override
		public String toString() {
			return getOriginalContent() + "=" + getCurrentTranslation();
		}
	}

	private static class StoreAccessor<Value> {

		private final List<String> referenceStore;
		private final int storeIndex;
		private final Switcher<String, Value> switcher;
		private final Map<Integer, Object> modifiedStore;

		public StoreAccessor(List<String> referenceStore,
				Map<Integer, Object> modifiedStore, int storeIndex,
				Switcher<String, Value> switcher) {
			this.referenceStore = referenceStore;
			this.modifiedStore = modifiedStore;
			this.storeIndex = storeIndex;
			this.switcher = switcher;
		}

		@SuppressWarnings("unchecked")
		public Value get() {
			Object value = modifiedStore.get(storeIndex);
			if (value == null) {
				return getStored();
			} else {
				return (Value) value;
			}
		}

		public void set(Value value) {
			logger.info("Value update of " + this + ": \"" + get() + "\" -> \""
					+ value + "\"");
			modifiedStore.put(storeIndex, value);
		}

		private Value getStored() {
			return switcher.switchForth(referenceStore.get(storeIndex));
		}

		public void save() {
			Object value = modifiedStore.get(storeIndex);
			if (value == null) {
				logger.finest("Store update of " + this + ": <same data>");
			} else {
				String newValue = switcher.switchBack(get());
				logger.finest("Retrieve string of " + this + ": \"" + get()
						+ "\" -> \"" + newValue + "\"");
				logger.finest("Store update of " + this + ": \""
						+ referenceStore.get(storeIndex) + "\" -> \""
						+ newValue + "\"");
				referenceStore.set(storeIndex, newValue);
			}
		}

		public void reset() {
			logger.finest("Restore value of " + this + ": \"" + get()
					+ "\" -> \"" + getStored() + "\"");
			modifiedStore.remove(storeIndex);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (obj instanceof StoreAccessor) {
				StoreAccessor<?> e = (StoreAccessor<?>) obj;
				return referenceStore == e.referenceStore
						&& storeIndex == e.storeIndex;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return referenceStore.hashCode() + storeIndex;
		}

		@Override
		public String toString() {
			return "SA" + storeIndex;
		}
	}

	public static class StringRebuilder {
		private final List<String> store;
		private final int firstIndex;
		private final int lastIndex;

		public StringRebuilder(List<String> store, int firstIndex, int lastIndex) {
			this.store = store;
			this.firstIndex = firstIndex;
			this.lastIndex = lastIndex;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			for (String content : store.subList(firstIndex, lastIndex + 1)) {
				builder.append(content);
			}
			return builder.toString();
		}
	}

	@SuppressWarnings("serial")
	public static class ParsingException extends RuntimeException {
		public ParsingException(String message) {
			super(message);
		}

		public ParsingException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
