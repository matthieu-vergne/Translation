package fr.sazaju.vheditor.translation;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.translation.TranslationMetadata.FieldListener;
import fr.sazaju.vheditor.translation.TranslationMetadata.UneditableFieldException;

public abstract class TranslationMetadataTest {

	protected abstract TranslationMetadata createTranslationMetadata();

	protected abstract Collection<Field<?>> getNonEditableFields();

	protected abstract Collection<Field<?>> getEditableFields();

	protected abstract <T> T getInitialReference(Field<T> field);

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
				getInitialReference(field);
			} catch (Exception e) {
				fail("Exception thrown while asking the reference for " + field);
			}
		}

		Collection<Field<?>> editableFields = getEditableFields();
		assertFalse("null fields are provided as as editable fields",
				editableFields.contains(null));
		assertEquals("some editable fields are used several time",
				editableFields.size(), new HashSet<>(editableFields).size());
		for (Field<?> field : editableFields) {
			try {
				getInitialReference(field);
			} catch (Exception e) {
				fail("Exception thrown while asking the reference for " + field);
			}

			stressNewValues(field);
		}
	}

	private <T> void stressNewValues(Field<T> field) {
		T currentValue = getInitialReference(field);
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
	public void testGetReferenceProperlyRetrievesReferenceValueBeforeModification() {
		TranslationMetadata metadata = createTranslationMetadata();

		for (Field<?> field : getNonEditableFields()) {
			assertEquals(getInitialReference(field),
					metadata.getReference(field));
		}
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialReference(field),
					metadata.getReference(field));
		}
	}

	@Test
	public void testGetReferenceProperlyRetrievesReferenceValueAfterModification() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
			assertEquals(getInitialReference(field),
					metadata.getReference(field));
		}
	}

	@Test
	public void testNonEditableFieldThrowsExceptionOnSet() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getNonEditableFields()) {
			try {
				setToReference(metadata, field);
				fail("No exception thrown");
			} catch (UneditableFieldException e) {
			}
		}
	}

	@Test
	public void testNonEditableFieldProperlyRetrieveReferenceValue() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getNonEditableFields()) {
			assertEquals(getInitialReference(field), metadata.get(field));
		}
	}

	@Test
	public void testEditableFieldProperlyRetrieveReferenceValueBeforeModification() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialReference(field), metadata.get(field));
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
	public void testReferenceProperlyUpdatedAfterSave() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			Object value = change(metadata, field);
			metadata.save(field);
			assertEquals(value, metadata.getReference(field));
		}
	}

	@Test
	public void testEditableFieldProperlyUpdatedAfterReset() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
			metadata.reset(field);
			assertEquals(getInitialReference(field), metadata.get(field));
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
	public void testAllReferencesProperlyUpdatedAfterSaveAll() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
		}
		metadata.saveAll();
		for (Field<?> field : getEditableFields()) {
			assertEquals(metadata.get(field), metadata.getReference(field));
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
	public void testAllReferencesProperlyMaintainedAfterResetAll() {
		TranslationMetadata metadata = createTranslationMetadata();
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
		}
		metadata.resetAll();
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialReference(field),
					metadata.getReference(field));
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
		};
		metadata.addFieldListener(listener);
		metadata.removeFieldListener(listener);
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
			assertTrue(notified.isEmpty());
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
		});
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
			metadata.reset(field);
			assertEquals(getInitialReference(field), notified.get(field));
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
	public void testListenerNotifiedAfterResetAllWhenRegistered() {
		TranslationMetadata metadata = createTranslationMetadata();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		metadata.addFieldListener(new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}
		});
		for (Field<?> field : getEditableFields()) {
			change(metadata, field);
		}
		metadata.resetAll();
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialReference(field), notified.get(field));
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

	private <T> void setToReference(TranslationMetadata metadata, Field<T> field) {
		metadata.set(field, getInitialReference(field));
	}

}
