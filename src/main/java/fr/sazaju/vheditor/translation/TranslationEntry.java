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
	 * @return <code>true</code> if the line "# UNTRANSLATED" is present,
	 *         <code>false</code> otherwise
	 */
	public boolean isMarkedAsUntranslated();

	/**
	 * 
	 * @param isMarkedAsUntranslated
	 *            <code>true</code> if the line should be marked as
	 *            "# UNTRANSLATED", <code>false</code> otherwise
	 */
	public void setMarkedAsUntranslated(boolean isMarkedAsUntranslated);

	/**
	 * 
	 * @return <code>true</code> if both {@link #getTranslatedVersion()} and
	 *         {@link #getOriginalVersion()} are empty or if both are not,
	 *         <code>false</code> otherwise
	 */
	public boolean isActuallyTranslated();

	/**
	 * 
	 * @return the context of the entry
	 */
	public String getContext();

	/**
	 * 
	 * @param isFacePresent
	 *            tell if we ask for the limit with the face present or not
	 * @return the maximum amount of characters per line, <code>null</code> if
	 *         it is not known
	 */
	public Integer getCharLimit(boolean isFacePresent);

	/**
	 * 
	 * @return the Japanese content
	 */
	public String getOriginalVersion();

	/**
	 * 
	 * @return the English content
	 */
	public String getTranslatedVersion();

	/**
	 * 
	 * @param translation
	 *            the new English content
	 */
	public void setTranslatedVersion(String translation);
}
