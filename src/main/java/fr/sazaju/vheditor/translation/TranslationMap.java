package fr.sazaju.vheditor.translation;

import java.io.File;
import java.util.Iterator;

/**
 * A {@link TranslationMap} describes a complete map {@link File}.
 * 
 * @author sazaju
 * 
 */
public interface TranslationMap extends Iterable<TranslationEntry> {

	/**
	 * This method should return an {@link Iterator} which provides all the
	 * {@link TranslationEntry}s that should be translated. Moreover, they
	 * should be in the "right order", meaning that if the
	 * {@link TranslationMap} is built from an existing {@link File}, this
	 * {@link Iterator} should provide the corresponding
	 * {@link TranslationEntry}s in the same order.
	 */
	@Override
	public Iterator<TranslationEntry> iterator();

	/**
	 * This method aims at providing the requested {@link TranslationEntry} of
	 * the current {@link TranslationMap}. The first entry is at the index 0 and
	 * the entries are in the same order than for {@link #iterator()}.
	 * 
	 * @param index
	 *            the index of the {@link TranslationEntry}
	 * @return the corresponding {@link TranslationEntry}
	 */
	public TranslationEntry getEntry(int index);

	/**
	 * @return the number of {@link TranslationEntry}s in this
	 *         {@link TranslationMap}
	 */
	public int size();

	/**
	 * Save the current content of the {@link TranslationMap} to the
	 * corresponding file(s).
	 */
	public void save();

}
