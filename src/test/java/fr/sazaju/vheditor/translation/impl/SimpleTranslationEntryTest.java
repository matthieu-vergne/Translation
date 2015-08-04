package fr.sazaju.vheditor.translation.impl;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationEntryTest;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationEntry.TranslationReader;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationEntry.TranslationWriter;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMetadata.FieldReader;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMetadata.FieldWriter;

public class SimpleTranslationEntryTest extends
		TranslationEntryTest<SimpleTranslationMetadata> {

	@Override
	protected TranslationEntry<SimpleTranslationMetadata> createTranslationEntry() {
		String original = "エントリー";
		final String[] datastore = { getInitialStoredTranslation() };
		final Integer[] metadatastore = { 10 };
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
		SimpleTranslationMetadata metadata = new SimpleTranslationMetadata();
		metadata.configureField(new Field<Integer>("integer"),
				new FieldReader<Integer>() {

					@Override
					public Integer read() {
						return metadatastore[0];
					}
				}, new FieldWriter<Integer>() {

					@Override
					public void write(Integer value) {
						metadatastore[0] = value;
					}
				});
		return new SimpleTranslationEntry<>(original, reader, writer, metadata);
	}

	@Override
	protected String getInitialStoredTranslation() {
		return "Entry";
	}

	@Override
	protected String createNewTranslation(String currentTranslation) {
		return currentTranslation + "?";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		return (T) (Integer) (((Integer) currentValue) + 1);
	}
}
