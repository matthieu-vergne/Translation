package fr.sazaju.vheditor.translation.impl;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationEntry.TranslationListener;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.translation.TranslationMetadata.FieldListener;
import fr.sazaju.vheditor.util.MultiReader;
import fr.sazaju.vheditor.util.Reader;
import fr.sazaju.vheditor.util.Writer;

/**
 * An {@link OnDemandMap} allows to retrieve the {@link TranslationEntry}s on
 * demand, so without storing all of them. If you already have them, you may
 * prefer to use a {@link LoadedMap}.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 * @param <Entry>
 */
public class OnDemandMap<Entry extends TranslationEntry<?>> implements
		TranslationMap<Entry> {

	private static final Writer<OnDemandMap<?>> DEFAULT_SAVER = new Writer<OnDemandMap<? extends TranslationEntry<?>>>() {

		@Override
		public void write(OnDemandMap<? extends TranslationEntry<?>> map) {
			for (TranslationEntry<?> entry : map.modifiedEntries) {
				entry.saveAll();
			}
		}
	};
	private final Reader<? extends Integer> sizeReader;
	private final MultiReader<? super Integer, ? extends Entry> entryReader;
	private final Writer<? super OnDemandMap<Entry>> mapSaver;
	private final Map<Integer, WeakReference<Entry>> cache = new HashMap<Integer, WeakReference<Entry>>();
	private final Set<Entry> modifiedEntries = new HashSet<Entry>();

	/**
	 * Instantiate an {@link OnDemandMap} which should manage a given number of
	 * {@link TranslationEntry}s. Each {@link TranslationEntry} is retrieved on
	 * demand through a specific {@link MultiReader}, and the overall saving
	 * strategy is dedicated to a specific {@link Writer}.
	 * 
	 * @param sizeReader
	 *            the {@link Reader} to use for {@link #size()}
	 * @param entryReader
	 *            the {@link MultiReader} to use for {@link #getEntry(int)}
	 * @param mapSaver
	 *            the {@link Writer} to use for {@link #saveAll()}
	 */
	public OnDemandMap(Reader<? extends Integer> sizeReader,
			MultiReader<? super Integer, ? extends Entry> entryReader,
			Writer<? super OnDemandMap<Entry>> mapSaver) {
		this.sizeReader = sizeReader;
		this.entryReader = entryReader;
		this.mapSaver = mapSaver;
	}

	/**
	 * Instantiate an {@link OnDemandMap} which should manage a given number of
	 * {@link TranslationEntry}s. Each {@link TranslationEntry} is retrieved on
	 * demand through a specific {@link MultiReader}. The overall saving
	 * strategy is a naive one: each modified {@link TranslationEntry} is saved
	 * separately. If you want a smarter saving strategy, use the most extended
	 * constructor.
	 * 
	 * @param sizeReader
	 *            the {@link Reader} to use for {@link #size()}
	 * @param entryReader
	 *            the {@link MultiReader} to use for {@link #getEntry(int)}
	 */
	public OnDemandMap(Reader<? extends Integer> sizeReader,
			MultiReader<? super Integer, ? extends Entry> entryReader) {
		this(sizeReader, entryReader, DEFAULT_SAVER);
	}

	@Override
	public Iterator<Entry> iterator() {
		return new Iterator<Entry>() {

			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public Entry next() {
				Entry entry = retrieveEntry(index);
				index++;
				return entry;
			}

			@Override
			public void remove() {
				throw new RuntimeException("You cannot remove an entry.");
			}
		};
	}

	@Override
	public Entry getEntry(int index) {
		return retrieveEntry(index);
	}

	private Entry retrieveEntry(int index) {
		WeakReference<Entry> reference = cache.get(index);
		if (reference == null || reference.get() == null) {
			final Entry entry = entryReader.read(index);
			entry.addTranslationListener(new TranslationListener() {

				@Override
				public void translationUpdated(String newTranslation) {
					modifiedEntries.add(entry);
				}
			});
			entry.getMetadata().addFieldListener(new FieldListener() {

				@Override
				public <T> void fieldUpdated(Field<T> field, T newValue) {
					modifiedEntries.add(entry);
				}
			});
			cache.put(index, new WeakReference<>(entry));
			return entry;
		} else {
			return reference.get();
		}
	}

	@Override
	public int size() {
		return sizeReader.read();
	}

	@Override
	public void saveAll() {
		mapSaver.write(this);
		modifiedEntries.clear();
	}

	@Override
	public void resetAll() {
		for (Entry entry : modifiedEntries) {
			entry.resetAll();
		}
		modifiedEntries.clear();
	}

	@Override
	public String toString() {
		return "Map(" + sizeReader + " entries, " + modifiedEntries.size()
				+ " modified)";
	}
}
