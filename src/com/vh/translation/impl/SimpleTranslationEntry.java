package com.vh.translation.impl;

import com.vh.translation.TranslationEntry;

public class SimpleTranslationEntry implements TranslationEntry {

	private boolean isMarkedAsUntranslated = false;
	private String context = null;
	private Integer charLimitWithFace = null;
	private Integer charLimitWithoutFace = null;
	private String originalContent = null;
	private String translationContent = null;
	private boolean isUnused = false;

	public void setMarkedAsUntranslated(boolean isMarkedAsUntranslated) {
		this.isMarkedAsUntranslated = isMarkedAsUntranslated;
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

	public void setContext(String context) {
		this.context = context;
	}

	@Override
	public String getContext() {
		return context;
	}

	public void setCharLimitWithFace(int charLimitWithFace) {
		this.charLimitWithFace = charLimitWithFace;
	}

	public void setCharLimitWithoutFace(int charLimitWithoutFace) {
		this.charLimitWithoutFace = charLimitWithoutFace;
	}

	@Override
	public Integer getCharLimit(boolean isFacePresent) {
		return isFacePresent ? charLimitWithFace : charLimitWithoutFace;
	}

	public void setOriginalContent(String originalContent) {
		this.originalContent = originalContent;
	}

	@Override
	public String getOriginalVersion() {
		return originalContent;
	}

	public void setTranslationContent(String translationContent) {
		this.translationContent = translationContent;
	}

	@Override
	public String getTranslatedVersion() {
		return translationContent;
	}

	public void setUnused(boolean isUnused) {
		this.isUnused = isUnused;
	}

	@Override
	public boolean isUnused() {
		return isUnused;
	}

}
