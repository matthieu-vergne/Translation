package fr.sazaju.vheditor.translation.impl;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationEntryTest;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.util.Reader;
import fr.sazaju.vheditor.util.Writer;
import fr.sazaju.vheditor.util.impl.ConstantReader;

public class OnDemandEntryTest extends
		TranslationEntryTest<OnDemandMetadata> {

	@Override
	protected TranslationEntry<OnDemandMetadata> createTranslationEntry() {
		final String[] datastore = { getInitialStoredTranslation() };
		final Integer[] metadatastore = { 10 };
		Reader<String> original = new ConstantReader<String>("エントリー");
		Reader<String> reader = new Reader<String>() {

			@Override
			public String read() {
				return datastore[0];
			}
		};
		Writer<String> writer = new Writer<String>() {

			@Override
			public void write(String translation) {
				datastore[0] = translation;
			}
		};
		OnDemandMetadata metadata = new OnDemandMetadata();
		metadata.configureField(new Field<Integer>("integer"),
				new Reader<Integer>() {

					@Override
					public Integer read() {
						return metadatastore[0];
					}
				}, new Writer<Integer>() {

					@Override
					public void write(Integer value) {
						metadatastore[0] = value;
					}
				});
		return new OnDemandEntry<>(original, reader, writer, metadata);
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
