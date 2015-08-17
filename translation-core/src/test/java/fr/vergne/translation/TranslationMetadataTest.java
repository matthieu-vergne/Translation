package fr.vergne.translation;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.TranslationMetadata.FieldListener;
import fr.vergne.translation.TranslationMetadata.UneditableFieldException;

public abstract class TranslationMetadataTest {

	protected abstract TranslationMetadata createTranslationMetadata();

	protected abstract Collection<Field<?>> getNonEditableFields();

	protected abstract Collection<Field<?>> getEditableFields();

	protected abstract <T> T getInitialStoredValue(Field<T> field);

	protected abstract <T> T createNewEditableFieldValue(Field<T> field,
			T currentValue);

	@Test
	public void testAbstractMethodsProvideProperValues() {
		Set<TranslationMetadata> metadatas = new HashSet<TranslationMetadata>();
		for (int i = 0; i < 10; i++) {
			metadatas.add(createTranslationMetadata());
		}
		assertFalse("null instances are provided as metadatas",
				metadatas.contains(null));
		assertEquals(
				"the same metadatas are reused instead of creating new ones",
				10, metadatas.size());

		Collection<Field<?>> nonEditableFields = getNonEditableFields();
		assertFalse("null fields are provided as as non-editable fields",
				nonEditableFields.contains(null));
		assertEquals("some non-editable fields are used several time",
				nonEditableFields.size(),
				new HashSet<>(nonEditableFields).size());
		for (Field<?> field : nonEditableFields) {
			try {
				getInitialStoredValue(field);
			} catch (Exception e) {
				throw new RuntimeException(
						"Exception thrown while asking the stored value of "
								+ field, e);
			}
		}

		Collection<Field<?>> editableFields = getEditableFields();
		assertFalse("null fields are provided as as editable fields",
				editableFields.contains(null));
		assertEquals("some editable fields are used several time",
				editableFields.size(), new HashSet<>(editableFields).size());
		assertTrue("there is shared fields between the non-editables "
				+ nonEditableFields + " and the editables " + editableFields,
				Collections.disjoint(nonEditableFields, editableFields));
		for (Field<?> field : editableFields) {
			try {
				getInitialStoredValue(field);
			} catch (Exception e) {
				fail("Exception thrown while asking the stored value of "
						+ field);
			}

			stressNewValues(field);
		}
	}

	private <T> void stressNewValues(Field<T> field) {
		T currentValue = getInitialStoredValue(field);
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
	public void testIteratorDoesNotProvideSeveralTimesTheSameField() {
		TranslationMetadata metadata = createTranslationMetadata();
		Collection<Field<?>> fields = new HashSet<>();
		for (Field<?> field : metadata) {
			assertFalse(field + " already seen: " + fields,
					fields.contains(field));
			fields.add(field);
		}
	}

	@Test
	public void testIteratorDoesNotProvideFieldsOutOfEditableAndNonEditableFields() {
		TranslationMetadata metadata = createTranslationMetadata();
		Collection<Field<?>> editableFields = getEditableFields();
		Collection<Field<?>> nonEditableFields = getNonEditableFields();
		for (Field<?> field : metadata) {
			assertTrue(editableFields.contains(field)
					|| nonEditableFields.contains(field));
		}
	}

	@Test
	public void testIteratorProvidesFieldsWhichHaveStoredValue() {
		TranslationMetadata metadata = createTranslationMetadata();
		Collection<Field<?>> fields = new HashSet<>();
		for (Field<?> field : getNonEditableFields()) {
			if (metadata.getStored(field) != null) {
				fields.add(field);
			} else {
				// ignore
			}
		}
		for (Field<?> field : getEditableFields()) {
			if (metadata.getStored(field) != null) {
				fields.add(field);
			} else {
				// ignore
			}
		}

		for (Field<?> field : metadata) {
			fields.remove(field);
		}

		assertTrue(
				"some fields are not provided while they have a stored value: "
						+ fields, fields.isEmpty());
	}

	@Test
	public void testIteratorProvidesFieldsWhichHaveCurrentValue() {
		TranslationMetadata metadata = createTranslationMetadata();
		Collection<Field<?>> fields = new HashSet<>();
		for (Field<?> field : getNonEditableFields()) {
			if (metadata.get(field) != null) {
				fields.add(field);
			} else {
				// ignore
			}
		}
		for (Field<?> field : getEditableFields()) {
			if (metadata.get(field) != null) {
				// already filled
			} else {
				change(metadata, field);
			}
			fields.add(field);
		}

		for (Field<?> field : metadata) {
			fields.remove(field);
		}

		assertTrue(
				"some fields are not provided while they have a current value: "
						+ fields, fields.isEmpty());
	}

	@Test
	public void testGetStoredProperlyRetrievesStoredValueBeforeModification() {
		TranslationMetadata metadata = createTranslationMetadata();

		for (Field<?> field : getNonEditableFields()) {
			assertEquals(getInitialStoredValue(field),
					metadata.getStored(field));
		}
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialStoredValue(field),
					metadata.getStored(field));
		}
	}

	@Test
	public void testGetStoredProperlyRetrievesStoredValueAfterModification() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
			assertEquals(getInitialStoredValue(field),
					metadata.getStored(field));
		}
	}

	@Test
	public void testNonEditableFieldThrowsExceptionOnSet() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getNonEditableFields()) {
			try {
				setToStoredValue(metadata, field);
				fail("No exception thrown");
			} catch (UneditableFieldException e) {
			}
		}
	}

	@Test
	public void testNonEditableFieldProperlyRetrieveStoredValue() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getNonEditableFields()) {
			assertEquals(getInitialStoredValue(field), metadata.get(field));
		}
	}

	@Test
	public void testEditableFieldProperlyRetrieveStoredValueBeforeModification() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialStoredValue(field), metadata.get(field));
		}
	}

	@Test
	public void testEditableFieldProperlyRetrieveUpdatedValueAfterModification() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			Object value = change(metadata, field);
			assertEquals(value, metadata.get(field));
		}
	}

	@Test
	public void testStoredValueProperlyUpdatedAfterSave() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			Object value = change(metadata, field);
			metadata.save(field);
			assertEquals(value, metadata.getStored(field));
		}
	}

	@Test
	public void testEditableFieldProperlyUpdatedAfterReset() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
			metadata.reset(field);
			assertEquals(getInitialStoredValue(field), metadata.get(field));
		}
	}

	@Test
	public void testAllChangesProperlyMaintainedAfterSaveAll() {
		TranslationMetadata metadata = createTranslationMetadata();
		Map<Field<?>, Object> values = new HashMap<Field<?>, Object>();
		for (Field<?> field : getEditableFields()) {
			values.put(field, change(metadata, field));
		}
		metadata.saveAll();
		for (Field<?> field : getEditableFields()) {
			assertEquals(values.get(field), metadata.get(field));
		}
	}

	@Test
	public void testAllStoredValuesProperlyUpdatedAfterSaveAll() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
		}
		metadata.saveAll();
		for (Field<?> field : getEditableFields()) {
			assertEquals(metadata.get(field), metadata.getStored(field));
		}
	}

	@Test
	public void testAllChangesProperlyDiscardedAfterResetAll() {
		TranslationMetadata metadata = createTranslationMetadata();
		Map<Field<?>, Object> values = new HashMap<Field<?>, Object>();
		for (Field<?> field : getEditableFields()) {
			values.put(field, change(metadata, field));
		}
		metadata.resetAll();
		for (Field<?> field : getEditableFields()) {
			Object value = values.get(field);
			if (value != null) {
				assertFalse(value.equals(metadata.get(field)));
			} else {
				assertFalse(value == metadata.get(field));
			}
		}
	}

	@Test
	public void testAllStoredValuesProperlyMaintainedAfterResetAll() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
		}
		metadata.resetAll();
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialStoredValue(field),
					metadata.getStored(field));
		}
	}

	@Test
	public void testListenerNotifiedAfterSetWhenRegistered() {
		TranslationMetadata metadata = createTranslationMetadata();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		metadata.addFieldListener(new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}

			@Override
			public <T> void fieldStored(Field<T> field) {
				// ignored
			}
		});
		for (Field<?> field : getEditableFields()) {
			Object value = change(metadata, field);
			assertEquals(value, notified.get(field));
		}
	}

	@Test
	public void testListenerNotNotifiedAfterSetWhenUnregistered() {
		TranslationMetadata metadata = createTranslationMetadata();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		FieldListener listener = new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}

			@Override
			public <T> void fieldStored(Field<T> field) {
				// ignored
			}
		};
		metadata.addFieldListener(listener);
		metadata.removeFieldListener(listener);
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
			assertTrue(notified.isEmpty());
		}
	}

	@Test
	public void testListenerNotifiedAfterSaveWhenRegistered() {
		TranslationMetadata metadata = createTranslationMetadata();
		final Collection<Field<?>> notified = new HashSet<Field<?>>();
		metadata.addFieldListener(new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				// ignored
			}

			@Override
			public <T> void fieldStored(Field<T> field) {
				notified.add(field);
			}
		});
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
			metadata.save(field);
			assertTrue("No notification received for " + field,
					notified.contains(field));
		}
	}

	@Test
	public void testListenerNotNotifiedAfterSaveWhenUnregistered() {
		TranslationMetadata metadata = createTranslationMetadata();
		final Collection<Field<?>> notified = new HashSet<Field<?>>();
		FieldListener listener = new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				// ignored
			}

			@Override
			public <T> void fieldStored(Field<T> field) {
				notified.add(field);
			}
		};
		metadata.addFieldListener(listener);
		metadata.removeFieldListener(listener);
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
			metadata.save(field);
			assertFalse("Notification still received for " + field,
					notified.contains(field));
		}
	}

	@Test
	public void testListenerNotifiedAfterResetWhenRegistered() {
		TranslationMetadata metadata = createTranslationMetadata();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		metadata.addFieldListener(new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}

			@Override
			public <T> void fieldStored(Field<T> field) {
				// ignored
			}
		});
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
			metadata.reset(field);
			assertEquals(getInitialStoredValue(field), notified.get(field));
		}
	}

	@Test
	public void testListenerNotNotifiedAfterResetWhenUnregistered() {
		TranslationMetadata metadata = createTranslationMetadata();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		FieldListener listener = new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}

			@Override
			public <T> void fieldStored(Field<T> field) {
				// ignored
			}
		};
		metadata.addFieldListener(listener);
		metadata.removeFieldListener(listener);
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
			metadata.reset(field);
			assertTrue(notified.isEmpty());
		}
	}

	@Test
	public void testListenerNotifiedAfterSaveAllWhenRegistered() {
		TranslationMetadata metadata = createTranslationMetadata();
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
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
		}
		metadata.saveAll();
		for (Field<?> field : getEditableFields()) {
			assertTrue("No notification received for " + field,
					notified.contains(field));
		}
	}

	@Test
	public void testListenerNotNotifiedAfterSaveAllWhenUnregistered() {
		TranslationMetadata metadata = createTranslationMetadata();
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
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
		}
		metadata.saveAll();
		for (Field<?> field : metadata) {
			assertFalse("Notification still received for " + field,
					notified.contains(field));
		}
	}

	@Test
	public void testListenerNotifiedAfterResetAllWhenRegistered() {
		TranslationMetadata metadata = createTranslationMetadata();
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
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
		}
		metadata.resetAll();
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialStoredValue(field), notified.get(field));
		}
	}

	@Test
	public void testListenerNotNotifiedAfterResetAllWhenUnregistered() {
		TranslationMetadata metadata = createTranslationMetadata();
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
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
		}
		metadata.resetAll();
		assertTrue(notified.isEmpty());
	}

	private <T> T change(TranslationMetadata metadata, Field<T> field) {
		T value = createNewEditableFieldValue(field, metadata.get(field));
		metadata.set(field, value);
		return value;
	}

	private <T> void setToStoredValue(TranslationMetadata metadata,
			Field<T> field) {
		metadata.set(field, getInitialStoredValue(field));
	}

}
