package fr.vergne.translation.impl;

import java.util.LinkedList;
import java.util.List;

import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationMapTest;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.util.MultiReader;
import fr.vergne.translation.util.Reader;
import fr.vergne.translation.util.Writer;
import fr.vergne.translation.util.impl.ConstantReader;

public class OnDemandMapTest extends TranslationMapTest<OnDemandEntry<?>> {

	@Override
	protected TranslationMap<OnDemandEntry<?>> createTranslationMap() {
		final List<String> translationStorage = new LinkedList<>();
		for (int index = 0; index < 10; index++) {
			translationStorage.add("Entry " + index);
		}
		final List<Integer> metadataStorage = new LinkedList<>();
		for (int index = 0; index < 10; index++) {
			metadataStorage.add(index);
		}
		return new OnDemandMap<>(new ConstantReader<Integer>(
				translationStorage.size()),
				new MultiReader<Integer, OnDemandEntry<?>>() {

					@Override
					public OnDemandEntry<?> read(final Integer index) {
						Reader<String> original = new ConstantReader<String>(
								"エントリー" + index);
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
						return new OnDemandEntry<OnDemandMetadata>(original,
								reader, writer, metadata);
					}
				});
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		return (T) (Integer) (((Integer) currentValue) + 1);
	}
}
