package fr.sazaju.vheditor.translation;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.util.Feature;

public abstract class TranslationProjectTest<TMapID, TMap extends TranslationMap<? extends TranslationEntry<? extends TranslationMetadata>>> {

	protected abstract TranslationProject<TMapID, TMap> createTranslationProject();

	protected abstract <T> T createNewEditableFieldValue(Field<T> field,
			T currentValue);

	@Test
	public void testAbstractMethodsProvideProperValues() {
		Set<TranslationProject<TMapID, TMap>> projects = new HashSet<TranslationProject<TMapID, TMap>>();
		for (int i = 0; i < 10; i++) {
			projects.add(createTranslationProject());
		}
		assertFalse("null instances are provided as projects",
				projects.contains(null));
		assertEquals(
				"the same projects are reused instead of creating new ones",
				10, projects.size());

		TranslationProject<TMapID, TMap> project = createTranslationProject();
		for (TMapID id : project) {
			TMap map = project.getMap(id);
			for (TranslationEntry<?> entry : map) {
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
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		Iterator<TMapID> iterator = project.iterator();
		assertNotNull(iterator);
	}

	@Test
	public void testIteratorProvidesAtLeastOneEntry() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		Iterator<TMapID> iterator = project.iterator();
		assertTrue(iterator.hasNext());
		assertNotNull(iterator.next());
	}

	@Test
	public void testIteratorProvidesNoNullEntry() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		Iterator<TMapID> iterator = project.iterator();
		while (iterator.hasNext()) {
			assertNotNull(iterator.next());
		}
	}

	@Test
	public void testSizeFitsIteratorEntries() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		Iterator<TMapID> iterator = project.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			iterator.next();
			count++;
		}
		assertEquals(count, project.size());
	}

	@Test
	public void testGetMapProvidesProperMap() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		Iterator<TMapID> iterator = project.iterator();
		while (iterator.hasNext()) {
			TMapID id = iterator.next();
			TMap map = project.getMap(id);
			assertNotNull(map);
		}
	}

	@Test
	public void testTranslationModificationsProperlyMaintainedAfterSaveAll() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		Map<TMapID, List<String>> savedTranslations = new HashMap<TMapID, List<String>>();
		for (TMapID id : project) {
			TMap map = project.getMap(id);
			List<String> mapTranslations = new LinkedList<>();
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> entry = map
						.getEntry(index);
				String translation = entry.getCurrentTranslation();
				translation += "?";
				entry.setCurrentTranslation(translation);
				mapTranslations.add(translation);
			}
			savedTranslations.put(id, mapTranslations);
		}
		project.saveAll();
		for (Entry<TMapID, List<String>> entry : savedTranslations.entrySet()) {
			TMapID id = entry.getKey();
			List<String> mapTranslations = entry.getValue();
			TMap map = project.getMap(id);
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> tEntry = map
						.getEntry(index);
				String savedTranslation = mapTranslations.get(index);
				assertEquals(savedTranslation, tEntry.getCurrentTranslation());
			}
		}
	}

	@Test
	public void testTranslationModificationsProperlySavedAfterSaveAll() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		Map<TMapID, List<String>> savedTranslations = new HashMap<TMapID, List<String>>();
		for (TMapID id : project) {
			TMap map = project.getMap(id);
			List<String> mapTranslations = new LinkedList<>();
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> entry = map
						.getEntry(index);
				String translation = entry.getCurrentTranslation();
				translation += "?";
				entry.setCurrentTranslation(translation);
				mapTranslations.add(translation);
			}
			savedTranslations.put(id, mapTranslations);
		}
		project.saveAll();
		for (Entry<TMapID, List<String>> entry : savedTranslations.entrySet()) {
			TMapID id = entry.getKey();
			List<String> mapTranslations = entry.getValue();
			TMap map = project.getMap(id);
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> tEntry = map
						.getEntry(index);
				String savedTranslation = mapTranslations.get(index);
				assertEquals(savedTranslation, tEntry.getStoredTranslation());
			}
		}
	}

	@Test
	public void testTranslationModificationsProperlyDiscardedAfterResetAll() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		Map<TMapID, List<String>> initialTranslations = new HashMap<TMapID, List<String>>();
		for (TMapID id : project) {
			TMap map = project.getMap(id);
			List<String> mapTranslations = new LinkedList<>();
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> entry = map
						.getEntry(index);
				String translation = entry.getCurrentTranslation();
				mapTranslations.add(translation);
				translation += "?";
				entry.setCurrentTranslation(translation);
			}
			initialTranslations.put(id, mapTranslations);
		}
		project.resetAll();
		for (Entry<TMapID, List<String>> entry : initialTranslations.entrySet()) {
			TMapID id = entry.getKey();
			List<String> mapTranslations = entry.getValue();
			TMap map = project.getMap(id);
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> tEntry = map
						.getEntry(index);
				String initialTranslation = mapTranslations.get(index);
				assertEquals(initialTranslation, tEntry.getCurrentTranslation());
			}
		}
	}

	@Test
	public void testMetadataModificationsProperlyMaintainedAfterSaveAll() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		Map<TMapID, List<Map<Field<?>, Object>>> savedMetadata = new HashMap<TMapID, List<Map<Field<?>, Object>>>();
		for (TMapID id : project) {
			TMap map = project.getMap(id);
			List<Map<Field<?>, Object>> modifiedFields = new LinkedList<>();
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> entry = map
						.getEntry(index);
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
			savedMetadata.put(id, modifiedFields);
		}
		project.saveAll();
		for (Entry<TMapID, List<Map<Field<?>, Object>>> entry : savedMetadata
				.entrySet()) {
			TMapID id = entry.getKey();
			List<Map<Field<?>, Object>> modifiedFields = entry.getValue();
			TMap map = project.getMap(id);
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> tEntry = map
						.getEntry(index);
				TranslationMetadata metadata = tEntry.getMetadata();
				Map<Field<?>, Object> modifications = modifiedFields.get(index);
				for (Field<?> field : modifications.keySet()) {
					assertEquals(modifications.get(field), metadata.get(field));
				}
			}
		}
	}

	@Test
	public void testMetadataModificationsProperlySavedAfterSaveAll() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		Map<TMapID, List<Map<Field<?>, Object>>> savedMetadata = new HashMap<TMapID, List<Map<Field<?>, Object>>>();
		for (TMapID id : project) {
			TMap map = project.getMap(id);
			List<Map<Field<?>, Object>> modifiedFields = new LinkedList<>();
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> entry = map
						.getEntry(index);
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
			savedMetadata.put(id, modifiedFields);
		}
		project.saveAll();
		for (Entry<TMapID, List<Map<Field<?>, Object>>> entry : savedMetadata
				.entrySet()) {
			TMapID id = entry.getKey();
			List<Map<Field<?>, Object>> modifiedFields = entry.getValue();
			TMap map = project.getMap(id);
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> tEntry = map
						.getEntry(index);
				TranslationMetadata metadata = tEntry.getMetadata();
				Map<Field<?>, Object> modifications = modifiedFields.get(index);
				for (Field<?> field : modifications.keySet()) {
					assertEquals(modifications.get(field),
							metadata.getStored(field));
				}
			}
		}
	}

	@Test
	public void testMetadataModificationsProperlyDiscardedAfterResetAll() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		Map<TMapID, List<Map<Field<?>, Object>>> initialMetadata = new HashMap<TMapID, List<Map<Field<?>, Object>>>();
		for (TMapID id : project) {
			TMap map = project.getMap(id);
			List<Map<Field<?>, Object>> initialFields = new LinkedList<>();
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> entry = map
						.getEntry(index);
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
			initialMetadata.put(id, initialFields);
		}
		project.resetAll();
		for (Entry<TMapID, List<Map<Field<?>, Object>>> entry : initialMetadata
				.entrySet()) {
			TMapID id = entry.getKey();
			List<Map<Field<?>, Object>> initialFields = entry.getValue();
			TMap map = project.getMap(id);
			for (int index = 0; index < map.size(); index++) {
				TranslationEntry<? extends TranslationMetadata> tEntry = map
						.getEntry(index);
				TranslationMetadata metadata = tEntry.getMetadata();
				Map<Field<?>, Object> initialValues = initialFields.get(index);
				for (Field<?> field : initialValues.keySet()) {
					assertEquals(initialValues.get(field), metadata.get(field));
				}
			}
		}
	}

	@Test
	public void testProjectFeaturesHaveProperNames() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		for (Feature feature : project.getFeatures()) {
			assertNotNull(feature.getName());
		}
	}

	@Test
	public void testProjectFeaturesDoNotThrowExceptionOnDescription() {
		TranslationProject<TMapID, TMap> project = createTranslationProject();
		for (Feature feature : project.getFeatures()) {
			feature.getDescription();
		}
	}

	private <T> T change(TranslationMetadata metadata, Field<T> field) {
		T value = createNewEditableFieldValue(field, metadata.get(field));
		metadata.set(field, value);
		return value;
	}
}
