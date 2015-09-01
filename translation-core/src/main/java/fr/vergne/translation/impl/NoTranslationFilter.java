package fr.vergne.translation.impl;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.util.EntryFilter;

public class NoTranslationFilter<TEntry extends TranslationEntry<?>> implements
		EntryFilter<TEntry> {

	@Override
	public String getName() {
		return "No translation";
	}

	@Override
	public String getDescription() {
		return "Search for entries which have some original content but no translation.";
	}

	@Override
	public boolean isRelevant(TEntry entry) {
		return !TranslationUtil.isActuallyTranslated(entry);
	}

	@Override
	public String toString() {
		return getName();
	}
}
