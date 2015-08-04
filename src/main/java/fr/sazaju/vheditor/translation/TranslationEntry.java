package fr.sazaju.vheditor.translation;

/**
 * A {@link TranslationEntry} describes a single entry to translate, so it
 * relates an original content ({@link #getOriginalContent()}) to a translation
 * ({@link #getCurrentTranslation()}). Optionally, some {@link Metadata} can be
 * provided through {@link #getMetadata()} to enrich the description of the
 * {@link TranslationEntry}, like the limits of characters, a description of the
 * context, etc.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 */
public interface TranslationEntry<Metadata extends TranslationMetadata> {

	/**
	 * 
	 * @return the untranslated content
	 */
	public String getOriginalContent();

	/**
	 * The stored translation is the reference translation to consider. Upon
	 * instantiation, {@link #getCurrentTranslation()} should return the same
	 * translation, before to diverge by using
	 * {@link #setCurrentTranslation(String)}. In the case of divergence,
	 * calling {@link #resetAll()} should align the current translation on the
	 * stored one, while calling {@link #saveAll()} should align the stored
	 * translation on the current one.
	 * 
	 * @return the stored translation
	 */
	public String getStoredTranslation();

	/**
	 * 
	 * @return the current translation
	 * @see #getStoredTranslation()
	 */
	public String getCurrentTranslation();

	/**
	 * This method allows to update the current translation of a given
	 * {@link TranslationEntry}. After modification, any
	 * {@link TranslationListener} registered through
	 * {@link #addTranslationListener(TranslationListener)} should be notified
	 * of the new translation.
	 * 
	 * @param translation
	 *            the new translation
	 * @see #getCurrentTranslation()
	 * @see #getStoredTranslation()
	 */
	public void setCurrentTranslation(String translation);

	/**
	 * Saving a translation leads to align the stored translation to the current
	 * one. After the process, {@link #getStoredTranslation()} should return the
	 * same result than {@link #getCurrentTranslation()}. This method should
	 * also lead to update the storage (usually a file or database) on which
	 * this {@link TranslationEntry} is based on.
	 */
	public void saveTranslation();

	/**
	 * Resetting a translation leads to align the current translation to the
	 * stored one. After the process, {@link #getCurrentTranslation()} should
	 * return the same result than {@link #getStoredTranslation()}. This method
	 * should be a way to recover the same content than the storage (usually a
	 * file or database) on which this {@link TranslationEntry} is based on.
	 * After the reset, any {@link TranslationListener} registered through
	 * {@link #addTranslationListener(TranslationListener)} should be notified
	 * of the new current translation.
	 */
	public void resetTranslation();

	/**
	 * This method should be equivalent to calling {@link #saveTranslation()}
	 * and {@link Metadata#saveAll()} in an atomic way, thus reducing the
	 * overhead of calling each method separately.
	 */
	public void saveAll();

	/**
	 * This method should be equivalent to calling {@link #resetTranslation()}
	 * and {@link Metadata#resetAll()} in an atomic way, thus reducing the
	 * overhead of calling each method separately.
	 */
	public void resetAll();

	/**
	 * 
	 * @return a {@link Metadata} providing extra information on this
	 *         {@link TranslationEntry}
	 */
	public Metadata getMetadata();

	/**
	 * A {@link TranslationListener} allows to be notified when the translation
	 * of a {@link TranslationEntry} is updated. To be notified, the
	 * {@link TranslationListener} should have been provided to
	 * {@link TranslationEntry#addTranslationListener(TranslationListener)} .
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 */
	public static interface TranslationListener {
		public void translationUpdated(String newTranslation);
	}

	/**
	 * 
	 * @param listener
	 *            the {@link TranslationListener} to register to this
	 *            {@link TranslationEntry}
	 */
	public void addTranslationListener(TranslationListener listener);

	/**
	 * 
	 * @param listener
	 *            the {@link TranslationListener} to unregister from this
	 *            {@link TranslationEntry}
	 */
	public void removeTranslationListener(TranslationListener listener);
}
