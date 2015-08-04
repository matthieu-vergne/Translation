package fr.sazaju.vheditor.translation.impl;

import java.util.LinkedList;
import java.util.List;

import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.TranslationMapTest;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationEntry.TranslationReader;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationEntry.TranslationWriter;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMap.EntryBuilder;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMetadata.FieldReader;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMetadata.FieldWriter;

public class SimpleTranslationMapTest extends
		TranslationMapTest<SimpleTranslationEntry<?>> {

	@Override
	protected TranslationMap<SimpleTranslationEntry<?>> createTranslationMap() {
		final List<String> translationStorage = new LinkedList<>();
		for (int index = 0; index < 10; index++) {
			translationStorage.add("Entry " + index);
		}
		final List<Integer> metadataStorage = new LinkedList<>();
		for (int index = 0; index < 10; index++) {
			metadataStorage.add(index);
		}
		return new SimpleTranslationMap<>(
				new EntryBuilder<SimpleTranslationEntry<?>>() {

					@Override
					public SimpleTranslationEntry<?> build(final int index) {
						String original = "エントリー" + index;
						TranslationReader reader = new TranslationReader() {

							@Override
							public String read() {
								return translationStorage.get(index);
							}
						};
						TranslationWriter writer = new TranslationWriter() {

							@Override
							public void write(String translation) {
								translationStorage.set(index, translation);
							}
						};
						SimpleTranslationMetadata metadata = new SimpleTranslationMetadata();
						metadata.configureField(new Field<Integer>("integer"),
								new FieldReader<Integer>() {

									@Override
									public Integer read() {
										return metadataStorage.get(index);
									}
								}, new FieldWriter<Integer>() {

									@Override
									public void write(Integer value) {
										metadataStorage.set(index, value);
									}
								});
						return new SimpleTranslationEntry<SimpleTranslationMetadata>(
								original, reader, writer, metadata);
					}
				}, translationStorage.size());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		return (T) (Integer) (((Integer) currentValue) + 1);
	}
}
