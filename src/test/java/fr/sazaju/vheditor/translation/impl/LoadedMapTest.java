package fr.sazaju.vheditor.translation.impl;

import java.util.LinkedList;
import java.util.List;

import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.TranslationMapTest;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.util.Reader;
import fr.sazaju.vheditor.util.Writer;
import fr.sazaju.vheditor.util.impl.ConstantReader;

public class LoadedMapTest extends
		TranslationMapTest<OnDemandEntry<OnDemandMetadata>> {

	@Override
	protected TranslationMap<OnDemandEntry<OnDemandMetadata>> createTranslationMap() {
		final List<String> translationStorage = new LinkedList<>();
		for (int index = 0; index < 10; index++) {
			translationStorage.add("Entry " + index);
		}
		final List<Integer> metadataStorage = new LinkedList<>();
		for (int index = 0; index < 10; index++) {
			metadataStorage.add(index);
		}
		List<OnDemandEntry<OnDemandMetadata>> entries = new LinkedList<>();
		for (int i = 0; i < 10; i++) {
			final int index = i;
			Reader<String> original = new ConstantReader<String>("エントリー"
					+ index);
			Reader<String> reader = new Reader<String>() {

				@Override
				public String read() {
					return translationStorage.get(index);
				}
			};
			Writer<String> writer = new Writer<String>() {

				@Override
				public void write(String translation) {
					translationStorage.set(index, translation);
				}
			};
			OnDemandMetadata metadata = new OnDemandMetadata();
			metadata.configureField(new Field<Integer>("integer"),
					new Reader<Integer>() {

						@Override
						public Integer read() {
							return metadataStorage.get(index);
						}
					}, new Writer<Integer>() {

						@Override
						public void write(Integer value) {
							metadataStorage.set(index, value);
						}
					});
			entries.add(new OnDemandEntry<OnDemandMetadata>(original, reader,
					writer, metadata));
		}
		return new LoadedMap<>(entries);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		return (T) (Integer) (((Integer) currentValue) + 1);
	}

}
