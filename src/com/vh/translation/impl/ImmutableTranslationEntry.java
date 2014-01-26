package com.vh.translation.impl;

import com.vh.translation.TranslationEntry;

public class ImmutableTranslationEntry implements TranslationEntry {

	private final boolean isMarkedAsUntranslated;
	private final String context;
	private final Integer charLimitWithFace;
	private final Integer charLimitWithoutFace;
	private final String originalContent;
	private final String translationContent;
	private final boolean isUnused;

	public ImmutableTranslationEntry(boolean isMarkedAsUntranslated,
			String context, Integer charLimitWithFace,
			Integer charLimitWithoutFace, String originalContent,
			String translationContent, boolean isUnused) {
		this.isMarkedAsUntranslated = isMarkedAsUntranslated;
		this.context = context;
		this.charLimitWithFace = charLimitWithFace;
		this.charLimitWithoutFace = charLimitWithoutFace;
		this.originalContent = originalContent;
		this.translationContent = translationContent;
		this.isUnused = isUnused;
	}

	public ImmutableTranslationEntry(TranslationEntry entry) {
		this(entry.isMarkedAsUntranslated(), entry.getContext(), entry
				.getCharLimit(true), entry.getCharLimit(false), entry
				.getOriginalVersion(), entry.getTranslatedVersion(), entry
				.isUnused());
	}

	@Override
	public boolean isUnused() {
		return isUnused;
	}

	@Override
	public boolean isMarkedAsUntranslated() {
		return isMarkedAsUntranslated;
	}

	@Override
	public boolean isActuallyTranslated() {
		String original = getOriginalVersion();
		String translated = getTranslatedVersion();
		return original == null && translated == null || original != null
				&& translated != null
				&& original.isEmpty() == translated.isEmpty();
	}

	@Override
	public String getContext() {
		return context;
	}

	@Override
	public Integer getCharLimit(boolean isFacePresent) {
		return isFacePresent ? charLimitWithFace : charLimitWithoutFace;
	}

	@Override
	public String getOriginalVersion() {
		return originalContent;
	}

	@Override
	public String getTranslatedVersion() {
		return translationContent;
	}

}
