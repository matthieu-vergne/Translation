package fr.sazaju.vheditor.translation.impl;

import java.util.regex.Pattern;

import fr.sazaju.vheditor.translation.TranslationEntry;

public class TranslationUtil {

	/**
	 * 
	 * @return <code>true</code> if both {@link #getCurrentTranslation()} and
	 *         {@link #getOriginalContent()} are empty or if both are not,
	 *         <code>false</code> otherwise
	 */
	public static boolean isActuallyTranslated(TranslationEntry<?> entry) {
		return !entry.getCurrentTranslation().trim().isEmpty()
				|| entry.getOriginalContent().trim().isEmpty()
				|| isNotJapanese(entry.getOriginalContent());
	}

	private static final String hiragana = "\u3041-\u3096";
	private static final String katakana = "\u30A1-\u30FB";
	private static final String katakanaAinu = "\u31F0-\u31FF";
	private static final String kanjiRare = "\u3400-\u4DB5";
	private static final String kanji = "\u4E01-\u9FAF";
	private static final String katakanaHalf = "\uFF65-\uFF9F";
	private static final Pattern japanese = Pattern.compile("[" + hiragana
			+ katakana + katakanaAinu + katakanaHalf + kanji + kanjiRare + "]");

	private static boolean isNotJapanese(String text) {
		return !japanese.matcher(text).find();
	}
}
