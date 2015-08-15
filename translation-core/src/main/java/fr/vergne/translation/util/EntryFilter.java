package fr.vergne.translation.util;

import fr.vergne.translation.TranslationEntry;

public interface EntryFilter<Entry extends TranslationEntry<?>> {

	public String getName();

	public String getDescription();

	public boolean isRelevant(Entry entry);
}
