package fr.sazaju.vheditor.translation;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import fr.sazaju.vheditor.util.EntryFilter;

/**
 * A {@link TranslationMap} describes a purpose-dedicated set of
 * {@link TranslationEntry}s. In other words, it represents a set of
 * {@link TranslationEntry}s which are somehow related, typically because they
 * relate to a specific scene or similar, so they order matter.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 * @param <Entry>
 */
public interface TranslationMap<Entry extends TranslationEntry<?>> extends
		Iterable<Entry> {

	/**
	 * This method should return an {@link Iterator} which provides all the
	 * {@link Entry}s that should be translated. Moreover, they should be in the
	 * "right order", meaning that if the {@link TranslationMap} is built from
	 * an existing {@link File}, this {@link Iterator} should provide the
	 * corresponding {@link Entry}s in the same order.
	 */
	@Override
	public Iterator<Entry> iterator();

	/**
	 * This method aims at providing the requested {@link Entry} of the current
	 * {@link TranslationMap}. The first entry is at the index 0 and the entries
	 * are in the same order than for {@link #iterator()}.
	 * 
	 * @param index
	 *            the index of the {@link Entry}
	 * @return the corresponding {@link Entry}
	 */
	public Entry getEntry(int index);

	/**
	 * @return the number of {@link Entry}s in this {@link TranslationMap}
	 */
	public int size();

	/**
	 * This method should be equivalent to calling
	 * {@link TranslationEntry#saveAll()} for each {@link TranslationEntry} of
	 * this {@link TranslationMap} in an atomic way, thus reducing the overhead
	 * of calling each one separately.
	 */
	public void saveAll();

	/**
	 * This method should be equivalent to calling
	 * {@link TranslationEntry#resetAll()} for each {@link TranslationEntry} of
	 * this {@link TranslationMap} in an atomic way, thus reducing the overhead
	 * of calling each one separately.
	 */
	public void resetAll();

	/**
	 * 
	 * @return the {@link EntryFilter}s which can be used to search for
	 *         particular {@link TranslationEntry}s.
	 */
	public Collection<EntryFilter<Entry>> getEntryFilters();
}
