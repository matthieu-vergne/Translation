package fr.vergne.translation.impl;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationEntryTest;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.impl.PatternFileMap.PatternMetadata;

public class PatternEntryTest extends TranslationEntryTest<PatternMetadata> {

	private final PatternFileMapTest mapTest = new PatternFileMapTest();

	@Override
	protected TranslationEntry<PatternMetadata> createTranslationEntry() {
		return mapTest.createTranslationMap().getEntry(0);
	}

	@Override
	protected String getInitialStoredTranslation() {
		return mapTest.createTranslationMap().getEntry(0)
				.getStoredTranslation();
	}

	@Override
	protected String createNewTranslation(String currentTranslation) {
		return currentTranslation + ".";
	}

	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		return mapTest.createNewEditableFieldValue(field, currentValue);
	}

}
