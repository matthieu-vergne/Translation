package fr.sazaju.vheditor.translation;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationMetadata.Field;

public abstract class TranslationMapTest<Entry extends TranslationEntry<? extends TranslationMetadata>> {

	protected abstract TranslationMap<Entry> createTranslationMap();

	protected abstract <T> T createNewEditableFieldValue(Field<T> field,
			T currentValue);

	@Test
	public void testAbstractMethodsProvideProperValues() {
		Set<TranslationMap<Entry>> maps = new HashSet<TranslationMap<Entry>>();
		for (int i = 0; i < 10; i++) {
			maps.add(createTranslationMap());
		}
		assertFalse("null instances are provided as maps", maps.contains(null));
		assertEquals("the same maps are reused instead of creating new ones",
				10, maps.size());

		TranslationMap<Entry> map = createTranslationMap();
		for (Entry entry : map) {
			TranslationMetadata metadata = entry.getMetadata();
			for (Field<?> field : metadata) {
				if (metadata.isEditable(field)) {
					stressNewValues(metadata, field);
				} else {
					// ignore
				}
			}
		}
	}

	private <T> void stressNewValues(TranslationMetadata metadata,
			Field<T> field) {
		T currentValue = metadata.get(field);
		for (int i = 0; i < 10; i++) {
			T nextValue;
			try {
				nextValue = createNewEditableFieldValue(field, currentValue);
			} catch (Exception e) {
				fail("Exception thrown while asking a new value for " + field);
				return;
			}
			String errorMessage = "the same value (" + currentValue
					+ ") is returned when asking for a new one for " + field;
			if (nextValue == null) {
				assertFalse(errorMessage, nextValue == currentValue);
			} else {
				assertFalse(errorMessage, nextValue.equals(currentValue));
			}
		}
	}

	@Test
	public void testIteratorNotNull() {
		TranslationMap<Entry> map = createTranslationMap();
		Iterator<Entry> iterator = map.iterator();
		assertNotNull(iterator);
	}

	@Test
	public void testIteratorProvidesAtLeastOneEntry() {
		TranslationMap<Entry> map = createTranslationMap();
		Iterator<Entry> iterator = map.iterator();
		assertTrue(iterator.hasNext());
		assertNotNull(iterator.next());
	}

	@Test
	public void testIteratorProvidesNoNullEntry() {
		TranslationMap<Entry> map = createTranslationMap();
		Iterator<Entry> iterator = map.iterator();
		while (iterator.hasNext()) {
			assertNotNull(iterator.next());
		}
	}

	@Test
	public void testSizeFitsIteratorEntries() {
		TranslationMap<Entry> map = createTranslationMap();
		Iterator<Entry> iterator = map.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			iterator.next();
			count++;
		}
		assertEquals(count, map.size());
	}

	@Test
	public void testGetEntryOrderedLikeIterator() {
		TranslationMap<Entry> map = createTranslationMap();
		Iterator<Entry> iterator = map.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			assertEquals(iterator.next(), map.getEntry(index));
			index++;
		}
	}

	@Test
	public void testTranslationModificationsProperlyMaintainedAfterSaveAll() {
		TranslationMap<Entry> map = createTranslationMap();
		List<String> savedTranslations = new LinkedList<>();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			String translation = entry.getCurrentTranslation();
			translation += "?";
			entry.setCurrentTranslation(translation);
			savedTranslations.add(translation);
		}
		map.saveAll();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			String savedTranslation = savedTranslations.get(index);
			assertEquals(savedTranslation, entry.getCurrentTranslation());
		}
	}

	@Test
	public void testTranslationModificationsProperlySavedAfterSaveAll() {
		TranslationMap<Entry> map = createTranslationMap();
		List<String> savedTranslations = new LinkedList<>();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			String translation = entry.getCurrentTranslation();
			translation += "?";
			entry.setCurrentTranslation(translation);
			savedTranslations.add(translation);
		}
		map.saveAll();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			String savedTranslation = savedTranslations.get(index);
			assertEquals(savedTranslation, entry.getStoredTranslation());
		}
	}

	@Test
	public void testTranslationModificationsProperlyDiscardedAfterResetAll() {
		TranslationMap<Entry> map = createTranslationMap();
		List<String> initialTranslations = new LinkedList<>();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			String translation = entry.getCurrentTranslation();
			initialTranslations.add(translation);
			translation += "?";
			entry.setCurrentTranslation(translation);
		}
		map.resetAll();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			String initialTranslation = initialTranslations.get(index);
			assertEquals(initialTranslation, entry.getCurrentTranslation());
		}
	}

	@Test
	public void testMetadataModificationsProperlyMaintainedAfterSaveAll() {
		TranslationMap<Entry> map = createTranslationMap();
		List<Map<Field<?>, Object>> modifiedFields = new LinkedList<>();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			TranslationMetadata metadata = entry.getMetadata();
			Map<Field<?>, Object> modifications = new HashMap<Field<?>, Object>();
			for (Field<?> field : metadata) {
				if (metadata.isEditable(field)) {
					Object value = change(metadata, field);
					modifications.put(field, value);
				} else {
					// ignore
				}
			}
			modifiedFields.add(modifications);
		}
		map.saveAll();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			TranslationMetadata metadata = entry.getMetadata();
			Map<Field<?>, Object> modifications = modifiedFields.get(index);
			for (Field<?> field : modifications.keySet()) {
				assertEquals(modifications.get(field), metadata.get(field));
			}
		}
	}

	@Test
	public void testMetadataModificationsProperlySavedAfterSaveAll() {
		TranslationMap<Entry> map = createTranslationMap();
		List<Map<Field<?>, Object>> modifiedFields = new LinkedList<>();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			TranslationMetadata metadata = entry.getMetadata();
			Map<Field<?>, Object> modifications = new HashMap<Field<?>, Object>();
			for (Field<?> field : metadata) {
				if (metadata.isEditable(field)) {
					Object value = change(metadata, field);
					modifications.put(field, value);
				} else {
					// ignore
				}
			}
			modifiedFields.add(modifications);
		}
		map.saveAll();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			TranslationMetadata metadata = entry.getMetadata();
			Map<Field<?>, Object> modifications = modifiedFields.get(index);
			for (Field<?> field : modifications.keySet()) {
				assertEquals(modifications.get(field),
						metadata.getStored(field));
			}
		}
	}

	@Test
	public void testMetadataModificationsProperlyDiscardedAfterResetAll() {
		TranslationMap<Entry> map = createTranslationMap();
		List<Map<Field<?>, Object>> initialFields = new LinkedList<>();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			TranslationMetadata metadata = entry.getMetadata();
			Map<Field<?>, Object> initialValues = new HashMap<Field<?>, Object>();
			for (Field<?> field : metadata) {
				if (metadata.isEditable(field)) {
					initialValues.put(field, metadata.get(field));
					change(metadata, field);
				} else {
					// ignore
				}
			}
			initialFields.add(initialValues);
		}
		map.resetAll();
		for (int index = 0; index < map.size(); index++) {
			Entry entry = map.getEntry(index);
			TranslationMetadata metadata = entry.getMetadata();
			Map<Field<?>, Object> initialValues = initialFields.get(index);
			for (Field<?> field : initialValues.keySet()) {
				assertEquals(initialValues.get(field), metadata.get(field));
			}
		}
	}

	private <T> T change(TranslationMetadata metadata, Field<T> field) {
		T value = createNewEditableFieldValue(field, metadata.get(field));
		metadata.set(field, value);
		return value;
	}
}
