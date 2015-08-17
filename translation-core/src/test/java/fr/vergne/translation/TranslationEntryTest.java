package fr.vergne.translation;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import fr.vergne.translation.TranslationEntry.TranslationListener;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.TranslationMetadata.FieldListener;

public abstract class TranslationEntryTest<Metadata extends TranslationMetadata> {

	protected abstract TranslationEntry<Metadata> createTranslationEntry();

	protected abstract String getInitialStoredTranslation();

	protected abstract String createNewTranslation(String currentTranslation);

	protected abstract <T> T createNewEditableFieldValue(Field<T> field,
			T currentValue);

	@Test
	public void testAbstractMethodsProvideProperValues() {
		Set<TranslationEntry<Metadata>> entries = new HashSet<TranslationEntry<Metadata>>();
		for (int i = 0; i < 10; i++) {
			entries.add(createTranslationEntry());
		}
		assertFalse("null instances are provided as entries",
				entries.contains(null));
		assertEquals(
				"the same entries are reused instead of creating new ones", 10,
				entries.size());

		try {
			getInitialStoredTranslation();
		} catch (Exception e) {
			fail("Exception thrown while asking the stored translation");
		}
		assertNotNull("a translation can be empty, but not null",
				getInitialStoredTranslation());

		String currentTranslation = getInitialStoredTranslation();
		for (int i = 0; i < 10; i++) {
			String nextTranslation;
			try {
				nextTranslation = createNewTranslation(currentTranslation);
			} catch (Exception e) {
				fail("Exception thrown while asking a new translation");
				return;
			}
			assertNotNull("a translation can be empty, but not null",
					nextTranslation);
			String errorMessage = "the same translation (" + currentTranslation
					+ ") is returned when asking for a new one";
			assertFalse(errorMessage,
					nextTranslation.equals(currentTranslation));
		}

		Metadata metadata = createTranslationEntry().getMetadata();
		for (Field<?> field : metadata) {
			if (metadata.isEditable(field)) {
				stressNewValues(metadata, field);
			} else {
				// ignore
			}
		}
	}

	private <T> void stressNewValues(Metadata metadata, Field<T> field) {
		T currentValue = metadata.get(field);
		for (int i = 0; i < 10; i++) {
			T nextValue;
			try {
				nextValue = createNewEditableFieldValue(field, currentValue);
			} catch (Exception e) {
				fail("Exception thrown while asking a new value for " + field
						+ " with " + currentValue);
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
	public void testGetStoredProperlyRetrievesStoredTranslationBeforeModification() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		assertEquals(getInitialStoredTranslation(),
				entry.getStoredTranslation());
	}

	@Test
	public void testGetStoredProperlyRetrievesStoredTranslationAfterModification() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		assertEquals(getInitialStoredTranslation(),
				entry.getStoredTranslation());
	}

	@Test
	public void testGetCurrentProperlyRetrievesStoredTranslationBeforeModification() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		assertEquals(getInitialStoredTranslation(),
				entry.getCurrentTranslation());
	}

	@Test
	public void testGetCurrentProperlyRetrievesUpdatedTranslationAfterModification() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		assertEquals(translation, entry.getCurrentTranslation());
	}

	@Test
	public void testSetCurrentThrowsExceptionOnNullTranslation() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		try {
			entry.setCurrentTranslation(null);
			fail("No exception thrown");
		} catch (Exception e) {
		}
	}

	@Test
	public void testStoredProperlyUpdatedAfterSaveTranslation() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.saveTranslation();
		assertEquals(translation, entry.getStoredTranslation());
	}

	@Test
	public void testGetCurrentProperlyUpdatedAfterResetTranslation() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetTranslation();
		assertEquals(getInitialStoredTranslation(),
				entry.getCurrentTranslation());
	}

	@Test
	public void testTranslationProperlyMaintainedAfterSaveAll() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.saveAll();
		assertEquals(translation, entry.getCurrentTranslation());
	}

	@Test
	public void testStoredProperlyUpdatedAfterSaveAll() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.saveAll();
		assertEquals(entry.getCurrentTranslation(),
				entry.getStoredTranslation());
	}

	@Test
	public void testMetadataProperlyMaintainedAfterSaveAll() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		Metadata metadata = entry.getMetadata();

		Map<Field<?>, Object> savedValues = new HashMap<>();
		for (Field<?> field : metadata) {
			if (metadata.isEditable(field)) {
				Object value = change(metadata, field);
				savedValues.put(field, value);
			} else {
				// ignore
			}
		}
		assertFalse("Unable to apply the test: no field modified",
				savedValues.isEmpty());
		entry.saveAll();
		for (Field<?> field : savedValues.keySet()) {
			Object value = savedValues.get(field);
			assertEquals(field.toString(), value, entry.getMetadata()
					.get(field));
		}
	}

	@Test
	public void testMetadataProperlySavedAfterSaveAll() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		Metadata metadata = entry.getMetadata();

		Map<Field<?>, Object> savedValues = new HashMap<>();
		for (Field<?> field : metadata) {
			if (metadata.isEditable(field)) {
				Object value = change(metadata, field);
				savedValues.put(field, value);
			} else {
				// ignore
			}
		}
		assertFalse("Unable to apply the test: no field modified",
				savedValues.isEmpty());
		entry.saveAll();
		for (Field<?> field : savedValues.keySet()) {
			Object value = savedValues.get(field);
			assertEquals(field.toString(), value, entry.getMetadata()
					.getStored(field));
		}
	}

	@Test
	public void testTranslationProperlyDiscardedAfterResetAll() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetAll();
		assertFalse(translation.equals(entry.getCurrentTranslation()));
	}

	@Test
	public void testStoredProperlyMaintainedAfterResetAll() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetAll();
		assertEquals(getInitialStoredTranslation(),
				entry.getStoredTranslation());
	}

	@Test
	public void testMetadataProperlyDiscardedAfterResetAll() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		Metadata metadata = entry.getMetadata();

		Map<Field<?>, Object> values = new HashMap<>();
		for (Field<?> field : metadata) {
			if (metadata.isEditable(field)) {
				values.put(field, metadata.get(field));
				change(metadata, field);
			} else {
				// ignore
			}
		}
		entry.resetAll();
		for (Field<?> field : values.keySet()) {
			Object value = values.get(field);
			assertEquals(value, entry.getMetadata().get(field));
		}
	}

	@Test
	public void testListenerNotifiedAfterSetCurrentWhenRegistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		final String[] notified = { null };
		entry.addTranslationListener(new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}

			@Override
			public void translationStored() {
				// ignore
			}
		});
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		assertEquals(translation, notified[0]);
	}

	@Test
	public void testListenerNotNotifiedAfterSetCurrentWhenUnregistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		final String[] notified = { null };
		TranslationListener listener = new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}

			@Override
			public void translationStored() {
				// ignore
			}
		};
		entry.addTranslationListener(listener);
		entry.removeTranslationListener(listener);
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		assertNull(notified[0]);
	}

	public void testListenerNotifiedAfterSaveTranslationWhenRegistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		final Boolean[] notified = { false };
		entry.addTranslationListener(new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				// ignore
			}

			@Override
			public void translationStored() {
				notified[0] = true;
			}
		});

		notified[0] = false;
		entry.setCurrentTranslation(getInitialStoredTranslation()+"?");
		entry.saveTranslation();
		assertEquals(true, notified[0]);
	}

	public void testListenerNotNotifiedAfterSaveTranslationWhenUnregistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		final Boolean[] notified = { false };
		TranslationListener listener = new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				// ignore
			}

			@Override
			public void translationStored() {
				notified[0] = true;
			}
		};
		entry.addTranslationListener(listener);
		entry.removeTranslationListener(listener);

		notified[0] = false;
		entry.setCurrentTranslation(getInitialStoredTranslation()+"?");
		entry.saveTranslation();
		assertEquals(false, notified[0]);
	}

	public void testListenerNotifiedAfterSaveAllWhenRegistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		final Boolean[] notified = { false };
		entry.addTranslationListener(new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				// ignore
			}

			@Override
			public void translationStored() {
				notified[0] = true;
			}
		});

		notified[0] = false;
		entry.setCurrentTranslation(getInitialStoredTranslation()+"?");
		entry.saveAll();
		assertEquals(true, notified[0]);
	}

	public void testListenerNotNotifiedAfterSaveAllWhenUnregistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		final Boolean[] notified = { false };
		TranslationListener listener = new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				// ignore
			}

			@Override
			public void translationStored() {
				notified[0] = true;
			}
		};
		entry.addTranslationListener(listener);
		entry.removeTranslationListener(listener);

		notified[0] = false;
		entry.setCurrentTranslation(getInitialStoredTranslation()+"?");
		entry.saveAll();
		assertEquals(false, notified[0]);
	}

	@Test
	public void testListenerNotifiedAfterResetTranslationWhenRegistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		final String[] notified = { null };
		entry.addTranslationListener(new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}

			@Override
			public void translationStored() {
				// ignored
			}
		});
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetTranslation();
		assertEquals(entry.getStoredTranslation(), notified[0]);
	}

	@Test
	public void testListenerNotNotifiedAfterResetTranslationWhenUnregistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		final String[] notified = { null };
		TranslationListener listener = new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}

			@Override
			public void translationStored() {
				// ignored
			}
		};
		entry.addTranslationListener(listener);
		entry.removeTranslationListener(listener);
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetTranslation();
		assertNull(notified[0]);
	}

	@Test
	public void testListenerNotifiedAfterResetAllWhenRegistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		final String[] notified = { null };
		entry.addTranslationListener(new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}

			@Override
			public void translationStored() {
				// ignore
			}
		});
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetAll();
		assertEquals(entry.getStoredTranslation(), notified[0]);
	}

	@Test
	public void testListenerNotNotifiedAfterResetAllWhenUnregistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		final String[] notified = { null };
		TranslationListener listener = new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}

			@Override
			public void translationStored() {
				// ignore
			}
		};
		entry.addTranslationListener(listener);
		entry.removeTranslationListener(listener);
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetAll();
		assertNull(notified[0]);
	}

	@Test
	public void testFieldListenerNotifiedAfterSaveAllWhenRegistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		Metadata metadata = entry.getMetadata();
		final Collection<Field<?>> notified = new HashSet<Field<?>>();
		metadata.addFieldListener(new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				// ignore
			}

			@Override
			public <T> void fieldStored(Field<T> field) {
				notified.add(field);
			}
		});
		Collection<Field<?>> expected = new HashSet<Field<?>>();
		for (Field<?> field : metadata) {
			if (metadata.isEditable(field)) {
				change(metadata, field);
				expected.add(field);
			} else {
				// ignore
			}
		}
		entry.saveAll();
		for (Field<?> field : metadata) {
			if (metadata.isEditable(field)) {
				assertTrue("No notification received for " + field,
						notified.contains(field));
			} else {
				// not important if not notifed
			}
		}
	}

	@Test
	public void testFieldListenerNotNotifiedAfterSaveAllWhenUnregistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		Metadata metadata = entry.getMetadata();
		final Collection<Field<?>> notified = new HashSet<Field<?>>();
		FieldListener listener = new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				// ignore
			}

			@Override
			public <T> void fieldStored(Field<T> field) {
				notified.add(field);
			}
		};
		metadata.addFieldListener(listener);
		metadata.removeFieldListener(listener);
		for (Field<?> field : metadata) {
			if (metadata.isEditable(field)) {
				change(metadata, field);
			} else {
				// ignore
			}
		}
		entry.saveAll();
		for (Field<?> field : metadata) {
			assertFalse("Notification still received for " + field,
					notified.contains(field));
		}
	}

	@Test
	public void testFieldListenerNotifiedAfterResetAllWhenRegistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		Metadata metadata = entry.getMetadata();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		metadata.addFieldListener(new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}

			@Override
			public <T> void fieldStored(Field<T> field) {
				// ignore
			}
		});
		Map<Field<?>, Object> resetValues = new HashMap<>();
		for (Field<?> field : metadata) {
			if (metadata.isEditable(field)) {
				resetValues.put(field, metadata.getStored(field));
				Object nonResetValue = change(metadata, field);
				notified.put(field, nonResetValue);
			} else {
				// ignore
			}
		}
		entry.resetAll();
		for (Field<?> field : resetValues.keySet()) {
			assertEquals(resetValues.get(field), notified.get(field));
		}
	}

	@Test
	public void testFieldNotNotifiedAfterResetAllWhenUnregistered() {
		TranslationEntry<Metadata> entry = createTranslationEntry();
		Metadata metadata = entry.getMetadata();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		FieldListener listener = new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}

			@Override
			public <T> void fieldStored(Field<T> field) {
				// ignore
			}
		};
		metadata.addFieldListener(listener);
		metadata.removeFieldListener(listener);
		Map<Field<?>, Object> nonResetValues = new HashMap<>();
		for (Field<?> field : metadata) {
			if (metadata.isEditable(field)) {
				Object nonResetValue = change(metadata, field);
				notified.put(field, nonResetValue);
				nonResetValues.put(field, nonResetValue);
			} else {
				// ignore
			}
		}
		entry.resetAll();
		for (Field<?> field : nonResetValues.keySet()) {
			assertEquals(nonResetValues.get(field), notified.get(field));
		}
	}

	private <T> T change(Metadata metadata, Field<T> field) {
		T value = createNewEditableFieldValue(field, metadata.get(field));
		metadata.set(field, value);
		return value;
	}
}
