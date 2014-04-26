package fr.sazaju.vheditor.translation;

import java.io.File;
import java.util.Iterator;

/**
 * A {@link TranslationMap} describes a complete map {@link File}.
 * 
 * @author sazaju
 * 
 */
public interface TranslationMap {

	/**
	 * This method should return an {@link Iterator} which provides all the
	 * {@link TranslationEntry}s that should be used. Moreover, they should be
	 * in the "right order", meaning that if the {@link TranslationMap} is built
	 * from an existing {@link File}, this {@link Iterator} should provide the
	 * corresponding {@link TranslationEntry}s in the same order
	 */
	public Iterator<? extends TranslationEntry> iteratorUsed();

	/**
	 * This method should return an {@link Iterator} which provides all the
	 * {@link TranslationEntry}s that should <b>NOT</b> be used. These are
	 * entries that should appear after a "# UNUSED TRANSLATABLES" line in a map
	 * file. Moreover, they should be in the "right order", meaning that if the
	 * {@link TranslationMap} is built from an existing {@link File}, this
	 * {@link Iterator} should provide the corresponding
	 * {@link TranslationEntry}s in the same order
	 */
	public Iterator<? extends TranslationEntry> iteratorUnused();

	/**
	 * An instance of {@link TranslationMap} is generally used to manage a map
	 * file which already exists in the game. Thus, this method should provide
	 * the {@link File} in which the content of the {@link TranslationMap} is
	 * supposed to appear.
	 * 
	 * 
	 * @return the {@link File} on which the {@link TranslationMap} is based on
	 */
	public File getBaseFile();

	/**
	 * Save the current content of the {@link TranslationMap} to the base file (
	 * {@link #getBaseFile()}).
	 */
	public void save();

	/**
	 * This method aims at providing the requested {@link TranslationEntry} of
	 * the current {@link TranslationMap}. Only the used
	 * {@link TranslationEntry}s (as provided by {@link #iteratorUsed()}) should
	 * be provided by this method.
	 * 
	 * @param index
	 *            the index of the used {@link TranslationEntry}
	 * @return the corresponding {@link TranslationEntry}
	 */
	public TranslationEntry getUsedEntry(int index);

	/**
	 * This method aims at providing the requested {@link TranslationEntry} of
	 * the current {@link TranslationMap}. Only the unused
	 * {@link TranslationEntry}s (as provided by {@link #iteratorUnused()})
	 * should be provided by this method.
	 * 
	 * @param index
	 *            the index of the used {@link TranslationEntry}
	 * @return the corresponding {@link TranslationEntry}
	 */
	public TranslationEntry getUnusedEntry(int index);

	/**
	 * This method should provide the number of {@link TranslationEntry}s which
	 * are actually used in this {@link TranslationMap}. These are all the
	 * {@link TranslationEntry}s provided by {@link #iteratorUsed()}.
	 * 
	 * @return the number of used {@link TranslationEntry}s in this
	 *         {@link TranslationMap}
	 */
	public int sizeUsed();

	/**
	 * This method should provide the number of {@link TranslationEntry}s which
	 * are <b>NOT</b> used in this {@link TranslationMap}. These are all the
	 * {@link TranslationEntry}s provided by {@link #iteratorUnused()}.
	 * 
	 * @return the number of unused {@link TranslationEntry}s in this
	 *         {@link TranslationMap}
	 */
	public int sizeUnused();
}
