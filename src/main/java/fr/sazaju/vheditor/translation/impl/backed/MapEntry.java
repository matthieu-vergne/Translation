package fr.sazaju.vheditor.translation.impl.backed;

import java.util.NoSuchElementException;

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
		return !getTranslatedVersion().isEmpty()
				|| getOriginalVersion().isEmpty();
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
