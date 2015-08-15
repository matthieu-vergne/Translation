package fr.sazaju.vheditor.translation.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.util.EntryFilter;
import fr.sazaju.vheditor.util.Writer;

/**
 * A {@link LoadedMap} provides the basic features to manage a
 * {@link TranslationMap} for which we already have the entries.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 * @param <Entry>
 */
public class LoadedMap<Entry extends TranslationEntry<?>> implements
		TranslationMap<Entry> {

	private static final Writer<LoadedMap<?>> DEFAULT_SAVER = new Writer<LoadedMap<? extends TranslationEntry<?>>>() {

		@Override
		public void write(LoadedMap<? extends TranslationEntry<?>> map) {
			for (TranslationEntry<?> entry : map.entries) {
				entry.saveAll();
			}
		}
	};
	private final List<Entry> entries;
	private final Writer<? super LoadedMap<Entry>> mapSaver;

	/**
	 * Instantiate a {@link LoadedMap} which should manage a known list of
	 * {@link TranslationEntry}s. The overall saving strategy is dedicated to a
	 * specific {@link Writer}.
	 * 
	 * @param entries
	 *            the list of {@link TranslationEntry} of this
	 *            {@link TranslationMap}
	 * @param mapSaver
	 *            the {@link Writer} to use for {@link #saveAll()}
	 */
	public LoadedMap(List<Entry> entries,
			Writer<? super LoadedMap<Entry>> mapSaver) {
		this.entries = entries;
		this.mapSaver = mapSaver;
	}

	/**
	 * Instantiate a {@link LoadedMap} which should manage a known list of
	 * {@link TranslationEntry}s. The overall saving strategy is a naive one:
	 * each {@link TranslationEntry} is saved separately, one after the other.
	 * If you want a smarter saving strategy, use
	 * {@link #FixedListMap(List, Writer)} instead.
	 * 
	 * @param entries
	 *            the list of {@link TranslationEntry} of this
	 *            {@link TranslationMap}
	 */
	public LoadedMap(List<Entry> entries) {
		this(entries, DEFAULT_SAVER);
	}

	@Override
	public Iterator<Entry> iterator() {
		return entries.iterator();
	}

	@Override
	public Entry getEntry(int index) {
		return entries.get(index);
	}

	@Override
	public int size() {
		return entries.size();
	}

	@Override
	public void saveAll() {
		mapSaver.write(this);
	}

	@Override
	public void resetAll() {
		for (Entry entry : entries) {
			entry.resetAll();
		}
	}

	private final Collection<EntryFilter<Entry>> filters = new HashSet<EntryFilter<Entry>>();

	@Override
	public Collection<EntryFilter<Entry>> getEntryFilters() {
		return filters;
	}

	public void addEntryFilter(EntryFilter<Entry> filter) {
		filters.add(filter);
	}

	@Override
	public String toString() {
		return "Map(" + size() + " entries)";
	}
}
