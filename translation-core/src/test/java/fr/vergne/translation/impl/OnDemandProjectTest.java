package fr.vergne.translation.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.TranslationProject;
import fr.vergne.translation.TranslationProjectTest;
import fr.vergne.translation.util.MultiReader;
import fr.vergne.translation.util.Reader;
import fr.vergne.translation.util.Writer;
import fr.vergne.translation.util.impl.ConstantReader;

public class OnDemandProjectTest
		extends
		TranslationProjectTest<OnDemandEntry<OnDemandMetadata>, String, OnDemandMap<OnDemandEntry<OnDemandMetadata>>> {

	@Override
	protected TranslationProject<OnDemandEntry<OnDemandMetadata>, String, OnDemandMap<OnDemandEntry<OnDemandMetadata>>> createTranslationProject() {
		Collection<String> ids = Arrays.asList("Map1", "Map2", "Map3");
		final Map<String, List<String>> allTranslationStorage = new HashMap<>();
		final Map<String, List<Integer>> allMetadataStorage = new HashMap<>();
		for (String id : ids) {
			List<String> translationStorage = new LinkedList<>();
			for (int index = 0; index < 10; index++) {
				translationStorage.add("Entry " + index);
			}
			allTranslationStorage.put(id, translationStorage);
			final List<Integer> metadataStorage = new LinkedList<>();
			for (int index = 0; index < 10; index++) {
				metadataStorage.add(index);
			}
			allMetadataStorage.put(id, metadataStorage);
		}
		MultiReader<String, OnDemandMap<OnDemandEntry<OnDemandMetadata>>> builder = new MultiReader<String, OnDemandMap<OnDemandEntry<OnDemandMetadata>>>() {

			@Override
			public OnDemandMap<OnDemandEntry<OnDemandMetadata>> read(String id) {
				final List<String> translationStorage = allTranslationStorage
						.get(id);
				final List<Integer> metadataStorage = allMetadataStorage
						.get(id);
				MultiReader<Integer, OnDemandEntry<OnDemandMetadata>> entryBuilder = new MultiReader<Integer, OnDemandEntry<OnDemandMetadata>>() {

					@Override
					public OnDemandEntry<OnDemandMetadata> read(
							final Integer index) {
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
				};
				return new OnDemandMap<>(new ConstantReader<Integer>(
						translationStorage.size()), entryBuilder);
			}
		};
		return new OnDemandProject<>(ids, builder);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		return (T) (Integer) (((Integer) currentValue) + 1);
	}

}
