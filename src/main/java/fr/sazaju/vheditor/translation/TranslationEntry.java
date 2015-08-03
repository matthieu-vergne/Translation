package fr.sazaju.vheditor.translation;

/**
 * A {@link TranslationEntry} describes a single entry to translate.
 * 
 * @author sazaju
 * 
 */
public interface TranslationEntry {

	/**
	 * 
	 * @return the untranslated content
	 */
	public String getOriginalVersion();

	/**
	 * 
	 * @return the current translated content
	 */
	public String getTranslatedVersion();

	/**
	 * 
	 * @param translation
	 *            the new translated content
	 */
	public void setTranslatedVersion(String translation);

	/**
	 * 
	 * @return a {@link TranslationComment} providing extra information on this
	 *         {@link TranslationEntry}
	 */
	public TranslationComment getComment();
}
