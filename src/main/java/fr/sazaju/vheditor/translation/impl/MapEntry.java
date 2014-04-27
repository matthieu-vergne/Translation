package fr.sazaju.vheditor.translation.impl;

import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Option;

public class MapEntry extends Suite implements TranslationEntry {

	public MapEntry() {
		super(new StartLine(), new Option<UntranslatedLine>(
				new UntranslatedLine()), new ContextLine(),
				new Option<AdviceLine>(new AdviceLine()), new ContentBlock(),
				new TranslationLine(), new ContentBlock(), new EndLine());
	}

	@Override
	public boolean isMarkedAsUntranslated() {
		Option<UntranslatedLine> option = get(1);
		return option.isPresent();
	}

	@Override
	public boolean isActuallyTranslated() {
		return !getTranslatedVersion().trim().isEmpty()
				|| getOriginalVersion().trim().isEmpty()
				|| isNotJapanese(getOriginalVersion());
	}

	private static final String hiragana = "\u3041-\u3096";
	private static final String katakana = "\u30A1-\u30FB";
	private static final String katakanaAinu = "\u31F0-\u31FF";
	private static final String kanjiRare = "\u3400-\u4DB5";
	private static final String kanji = "\u4E01-\u9FAF";
	private static final String katakanaHalf = "\uFF65-\uFF9F";
	private static final Pattern japanese = Pattern.compile("[" + hiragana
			+ katakana + katakanaAinu + katakanaHalf + kanji + kanjiRare + "]");

	private boolean isNotJapanese(String text) {
		return !japanese.matcher(text).find();
	}

	@Override
	public String getContext() {
		ContextLine context = get(2);
		return context.getContext().getContent();
	}

	@Override
	public Integer getCharLimit(boolean isFacePresent) {
		Option<AdviceLine> option = get(3);
		if (option.isPresent()) {
			AdviceLine advice = option.getOption();
			try {
				return isFacePresent ? Integer.parseInt(advice.getFaceLimit()
						.getContent()) : Integer.parseInt(advice
						.getGeneralLimit().getContent());
			} catch (NoSuchElementException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public String getOriginalVersion() {
		ContentBlock block = get(4);
		return block.getContentWithoutNewline().getContent();
	}

	@Override
	public String getTranslatedVersion() {
		ContentBlock block = get(6);
		return block.getContentWithoutNewline().getContent();
	}

	@Override
	public void setTranslatedVersion(String translation) {
		ContentBlock block = get(6);
		block.getContentWithoutNewline().setContent(translation);
	}
}
