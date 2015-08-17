package fr.vergne.translation.impl;

import java.util.Collection;
import java.util.HashSet;

import fr.vergne.translation.TranslationMetadata;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.TranslationMetadataTest;
import fr.vergne.translation.impl.PatternFileMap.PatternMetadata;

public class PatternMetadataTest extends TranslationMetadataTest {

	private final PatternFileMapTest mapTest = new PatternFileMapTest();

	@Override
	protected TranslationMetadata createTranslationMetadata() {
		return mapTest.createTranslationMap().getEntry(0).getMetadata();
	}

	@Override
	protected Collection<Field<?>> getNonEditableFields() {
		PatternMetadata metadata = mapTest.createTranslationMap().getEntry(0)
				.getMetadata();
		Collection<Field<?>> fields = new HashSet<>();
		for (Field<?> field : metadata) {
			if (metadata.isEditable(field)) {
				// ignore
			} else {
				fields.add(field);
			}
		}
		return fields;
	}

	@Override
	protected Collection<Field<?>> getEditableFields() {
		PatternMetadata metadata = mapTest.createTranslationMap().getEntry(0)
				.getMetadata();
		Collection<Field<?>> fields = new HashSet<>();
		for (Field<?> field : metadata) {
			if (metadata.isEditable(field)) {
				fields.add(field);
			} else {
				// ignore
			}
		}
		return fields;
	}

	@Override
	protected <T> T getInitialStoredValue(Field<T> field) {
		return mapTest.createTranslationMap().getEntry(0).getMetadata()
				.getStored(field);
	}

	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		return mapTest.createNewEditableFieldValue(field, currentValue);
	}

}
