package fr.sazaju.vheditor.util;

import fr.sazaju.vheditor.translation.TranslationEntry;

public interface EntryFilter<Entry extends TranslationEntry<?>> {

	public String getName();
	
	public String getDescription();

	public boolean isRelevant(Entry entry);
}
