package fr.sazaju.vheditor.translation.impl;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationEntryTest;
import fr.sazaju.vheditor.translation.TranslationMetadata;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationEntry.TranslationReader;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationEntry.TranslationWriter;

public class SimpleTranslationEntryTest extends TranslationEntryTest {

	@Override
	protected TranslationEntry createTranslationEntry() {
		String original = "エントリー";
		final String[] datastore = { getInitialStoredTranslation() };
		TranslationReader reader = new TranslationReader() {

			@Override
			public String read() {
				return datastore[0];
			}
		};
		TranslationWriter writer = new TranslationWriter() {

			@Override
			public void write(String translation) {
				datastore[0] = translation;
			}
		};
		TranslationMetadata metadata = new SimpleTranslationMetadata();
		return new SimpleTranslationEntry(original, reader, writer, metadata);
	}

	@Override
	protected String getInitialStoredTranslation() {
		return "Entry";
	}

	@Override
	protected String createNewTranslation(String currentTranslation) {
		return currentTranslation + "?";
	}

}
