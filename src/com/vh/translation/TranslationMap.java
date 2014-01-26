package com.vh.translation;

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
	 * This method should return an {@link Iterator} which provides each
	 * {@link TranslationEntry} that we should find in the
	 * {@link TranslationMap} file. Moreover, they should be in the
	 * "right order", meaning that if the {@link TranslationMap} is built from
	 * an existing {@link File}, this {@link Iterator} should provide the
	 * corresponding {@link TranslationEntry} in the same order.
	 */
	public Iterator<TranslationEntry> iterator();

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
}
