package fr.vergne.translation.impl;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
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
import fr.vergne.translation.impl.PatternFileMap.PatternEntry;
import fr.vergne.translation.util.Switcher;
import fr.vergne.translation.util.EntryFilter;
import fr.vergne.translation.util.impl.IdentitySwitcher;

public class PatternFileMap implements TranslationMap<PatternEntry> {

	private final File file;
	private final Pattern entryPattern;
	private final Pattern originalPattern;
	private final Pattern translationPattern;
	private final Map<Field<?>, Pattern> fieldPatterns = new HashMap<>();
	private final Map<Field<?>, Switcher<String, ?>> fieldConvertors = new HashMap<>();
	private final Collection<Field<?>> editableFields = new HashSet<>();
	@SuppressWarnings("serial")
	private final List<String> referenceStore = new LinkedList<String>() {
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
			StringBuilder builder = new StringBuilder();
			for (String content : referenceStore) {
				builder.append(content);
			}
			try {
				FileUtils.write(file, builder.toString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			logger.info("File saved.");
		}
	};

	private final Map<Integer, WeakReference<PatternEntry>> entryCache = new HashMap<>();
	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private static final Switcher<String, String> STRING_CONVERTOR = new IdentitySwitcher<>();

	public PatternFileMap(File file, String entryRegex, String originalRegex,
			String translationRegex) {
		this.file = file;
		this.entryPattern = Pattern.compile(entryRegex);
		this.originalPattern = Pattern.compile(originalRegex);
		this.translationPattern = Pattern.compile(translationRegex);
	}

	public <T> void addFieldRegex(Field<T> field, String regex,
			Switcher<String, T> convertor, boolean editable) {
		fieldPatterns.put(field, Pattern.compile(regex));
		fieldConvertors.put(field, convertor);
		if (editable) {
			editableFields.add(field);
		} else {
			editableFields.remove(field);
		}
	}

	public void addFieldRegex(Field<String> field, String regex,
			boolean editable) {
		addFieldRegex(field, regex, STRING_CONVERTOR, editable);
	}

	@Override
	public Iterator<PatternEntry> iterator() {
		final String content;
		try {
			logger.info("Reading " + file + "...");
			content = FileUtils.readFileToString(file);
		} catch (IOException e) {
			throw new RuntimeException();
		}
		return new Iterator<PatternEntry>() {

			private final Matcher entryMatcher = entryPattern.matcher(content);
			private String nextEntry = null;
			private int stored = 0;
			private int storeIndex = -1;
			private int entryIndex = -1;

			@Override
			public boolean hasNext() {
				if (nextEntry != null) {
					// already found
				} else if (entryMatcher.find()) {
					nextEntry = entryMatcher.group();
					int nextStored = entryMatcher.start();
					if (stored < nextStored) {
						String extract = content.substring(stored, nextStored);
						stored = nextStored;
						storeIndex++;
						referenceStore.set(storeIndex, extract);
					} else {
						// nothing between the two entries
					}
				} else {
					// no more
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
						original.find();
						substore.put(original, original.group());
						starts.put(original.start(), original);
						logger.finest("- O: " + original.group());

						Matcher translation = translationPattern
								.matcher(nextEntry);
						translation.find();
						substore.put(translation, translation.group());
						starts.put(translation.start(), translation);
						logger.finest("- T: " + translation.group());

						for (Field<?> field : fieldConvertors.keySet()) {
							Pattern pattern = fieldPatterns.get(field);
							Matcher matcher = pattern.matcher(nextEntry);
							matcher.find();
							substore.put(field, matcher.group());
							starts.put(matcher.start(), field);
							logger.finest("- " + field + ": " + matcher.group());
						}

						/*
						 * EXTRACT ALL ORDERED SUBSTRINGS
						 */

						Map<Object, Integer> storeIndexes = new HashMap<>();
						TreeSet<Integer> sortedStarts = new TreeSet<>(
								starts.keySet());
						int substored = 0;
						String previous = null;
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

						/*
						 * INSTANTIATE ENTRY
						 */

						Map<Field<?>, StoreAccessor<?>> fieldAccessors = new HashMap<>();
						for (Field<?> field : fieldConvertors.keySet()) {
							Integer index = storeIndexes.get(field);
							Switcher<String, ?> convertor = fieldConvertors
									.get(field);
							fieldAccessors.put(field, new StoreAccessor<>(
									referenceStore, modifiedStore, index,
									convertor));
						}
						PatternMetadata metadata = new PatternMetadata(
								fieldAccessors, editableFields, saver);
						Integer index = storeIndexes.get(translation);
						StoreAccessor<String> translationAccessor = new StoreAccessor<>(
								referenceStore, modifiedStore, index,
								STRING_CONVERTOR);
						PatternEntry entry = new PatternEntry(original.group(),
								translationAccessor, metadata, saver);

						entryIndex++;
						entryCache.put(entryIndex, new WeakReference<>(entry));

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
	public PatternEntry getEntry(final int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException(
					"You cannot ask for a negative index: " + index);
		} else {
			WeakReference<PatternEntry> reference = entryCache.get(index);
			if (reference == null || reference.get() == null) {
				int counter = 0;
				for (PatternEntry entry : this) {
					if (counter == index) {
						return entry;
					} else {
						counter++;
					}
				}
				throw new IndexOutOfBoundsException(
						"You cannot ask for an index higher than the size: "
								+ index + " > " + counter);
			} else {
				return reference.get();
			}
		}
	}

	@Override
	public int size() {
		int size = 0;
		Iterator<PatternEntry> iterator = iterator();
		while (iterator.hasNext()) {
			iterator.next();
			size++;
		}
		return size;
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
		@Override
		public <T> T getStored(Field<T> field) {
			return (T) accessors.get(field).getStored();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(Field<T> field) {
			return (T) accessors.get(field).get();
		}

		@Override
		public <T> boolean isEditable(Field<T> field) {
			return editableFields.contains(field);
		}

		@Override
		public <T> void set(Field<T> field, T value)
				throws UneditableFieldException {
			if (isEditable(field)) {
				@SuppressWarnings("unchecked")
				StoreAccessor<T> accessor = (StoreAccessor<T>) accessors
						.get(field);
				accessor.set(value);
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
		}

		@Override
		public <T> void reset(Field<T> field) {
			accessors.get(field).reset();
		}

		@Override
		public void saveAll() {
			saveAllBackgroundOnly();
			saver.run();
		}

		public void saveAllBackgroundOnly() {
			for (Field<?> field : this) {
				accessors.get(field).save();
			}
		}

		@Override
		public void resetAll() {
			for (Field<?> field : this) {
				accessors.get(field).reset();
			}
		}

	}

	public static class PatternEntry implements
			TranslationEntry<PatternMetadata> {

		private final String original;
		private final StoreAccessor<String> translationAccessor;
		private final PatternMetadata metadata;
		private final Collection<TranslationListener> listeners = new HashSet<TranslationListener>();
		private final Runnable saver;

		public PatternEntry(String original,
				StoreAccessor<String> translationAccessor,
				PatternMetadata metadata, Runnable saver) {
			this.original = original;
			this.translationAccessor = translationAccessor;
			this.metadata = metadata;
			this.saver = saver;
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
			this.translationAccessor.set(translation);
		}

		@Override
		public void saveTranslation() {
			translationAccessor.save();
			saver.run();
		}

		@Override
		public void resetTranslation() {
			translationAccessor.reset();
		}

		@Override
		public void saveAll() {
			saveAllBackgroundOnly();
			saver.run();
		}

		public void saveAllBackgroundOnly() {
			translationAccessor.save();
			metadata.saveAllBackgroundOnly();
		}

		@Override
		public void resetAll() {
			translationAccessor.reset();
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

		@Override
		public String toString() {
			return getOriginalContent() + "=" + getCurrentTranslation();
		}
	}

	private static class StoreAccessor<Value> {

		private final List<String> referenceStore;
		private final int storeIndex;
		private final Switcher<String, Value> convertor;
		private final Map<Integer, Object> modifiedStore;

		public StoreAccessor(List<String> referenceStore,
				Map<Integer, Object> modifiedStore, int storeIndex,
				Switcher<String, Value> convertor) {
			this.referenceStore = referenceStore;
			this.modifiedStore = modifiedStore;
			this.storeIndex = storeIndex;
			this.convertor = convertor;
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
			return convertor.switchForth(referenceStore.get(storeIndex));
		}

		public void save() {
			Object value = modifiedStore.get(storeIndex);
			if (value == null) {
				logger.finest("Store update of " + this + ": <same data>");
			} else {
				String newValue = convertor.switchBack(get());
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
}
