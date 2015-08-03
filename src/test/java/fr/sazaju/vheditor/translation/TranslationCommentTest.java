package fr.sazaju.vheditor.translation;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationComment.Field;
import fr.sazaju.vheditor.translation.TranslationComment.FieldListener;
import fr.sazaju.vheditor.translation.TranslationComment.UneditableFieldException;

public abstract class TranslationCommentTest {

	protected abstract TranslationComment createTranslationComment();

	protected abstract Collection<Field<?>> getNonEditableFields();

	protected abstract Collection<Field<?>> getEditableFields();

	protected abstract <T> T getInitialReference(Field<T> field);

	protected abstract <T> T createNewEditableFieldValue(Field<T> field,
			T currentValue);

	@Test
	public void testAbstractMethodsProvideProperValues() {
		Set<TranslationComment> comments = new HashSet<TranslationComment>();
		for (int i = 0; i < 10; i++) {
			comments.add(createTranslationComment());
		}
		assertFalse("null instances are provided as comments",
				comments.contains(null));
		assertEquals(
				"the same comments are reused instead of creating new ones",
				10, comments.size());

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
		TranslationComment comment = createTranslationComment();

		for (Field<?> field : getNonEditableFields()) {
			assertEquals(getInitialReference(field),
					comment.getReference(field));
		}
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialReference(field),
					comment.getReference(field));
		}
	}

	@Test
	public void testGetReferenceProperlyRetrievesReferenceValueAfterModification() {
		TranslationComment comment = createTranslationComment();
		for (Field<?> field : getEditableFields()) {
			change(comment, field);
			assertEquals(getInitialReference(field),
					comment.getReference(field));
		}
	}

	@Test
	public void testNonEditableFieldThrowsExceptionOnSet() {
		TranslationComment comment = createTranslationComment();
		for (Field<?> field : getNonEditableFields()) {
			try {
				setToReference(comment, field);
				fail("No exception thrown");
			} catch (UneditableFieldException e) {
			}
		}
	}

	@Test
	public void testNonEditableFieldProperlyRetrieveReferenceValue() {
		TranslationComment comment = createTranslationComment();
		for (Field<?> field : getNonEditableFields()) {
			assertEquals(getInitialReference(field), comment.get(field));
		}
	}

	@Test
	public void testEditableFieldProperlyRetrieveReferenceValueBeforeModification() {
		TranslationComment comment = createTranslationComment();
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialReference(field), comment.get(field));
		}
	}

	@Test
	public void testEditableFieldProperlyRetrieveUpdatedValueAfterModification() {
		TranslationComment comment = createTranslationComment();
		for (Field<?> field : getEditableFields()) {
			Object value = change(comment, field);
			assertEquals(value, comment.get(field));
		}
	}

	@Test
	public void testReferenceProperlyUpdatedAfterSave() {
		TranslationComment comment = createTranslationComment();
		for (Field<?> field : getEditableFields()) {
			Object value = change(comment, field);
			comment.save(field);
			assertEquals(value, comment.getReference(field));
		}
	}

	@Test
	public void testEditableFieldProperlyUpdatedAfterReset() {
		TranslationComment comment = createTranslationComment();
		for (Field<?> field : getEditableFields()) {
			change(comment, field);
			comment.reset(field);
			assertEquals(getInitialReference(field), comment.get(field));
		}
	}

	@Test
	public void testAllChangesProperlyMaintainedAfterSaveAll() {
		TranslationComment comment = createTranslationComment();
		Map<Field<?>, Object> values = new HashMap<Field<?>, Object>();
		for (Field<?> field : getEditableFields()) {
			values.put(field, change(comment, field));
		}
		comment.saveAll();
		for (Field<?> field : getEditableFields()) {
			assertEquals(values.get(field), comment.get(field));
		}
	}

	@Test
	public void testAllReferencesProperlyUpdatedAfterSaveAll() {
		TranslationComment comment = createTranslationComment();
		for (Field<?> field : getEditableFields()) {
			change(comment, field);
		}
		comment.saveAll();
		for (Field<?> field : getEditableFields()) {
			assertEquals(comment.get(field), comment.getReference(field));
		}
	}

	@Test
	public void testAllChangesProperlyDiscardedAfterResetAll() {
		TranslationComment comment = createTranslationComment();
		Map<Field<?>, Object> values = new HashMap<Field<?>, Object>();
		for (Field<?> field : getEditableFields()) {
			values.put(field, change(comment, field));
		}
		comment.resetAll();
		for (Field<?> field : getEditableFields()) {
			Object value = values.get(field);
			if (value != null) {
				assertFalse(value.equals(comment.get(field)));
			} else {
				assertFalse(value == comment.get(field));
			}
		}
	}

	@Test
	public void testAllReferencesProperlyMaintainedAfterResetAll() {
		TranslationComment comment = createTranslationComment();
		for (Field<?> field : getEditableFields()) {
			change(comment, field);
		}
		comment.resetAll();
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialReference(field),
					comment.getReference(field));
		}
	}

	@Test
	public void testListenerNotifiedAfterSetWhenRegistered() {
		TranslationComment comment = createTranslationComment();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		comment.addFieldListener(new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}
		});
		for (Field<?> field : getEditableFields()) {
			Object value = change(comment, field);
			assertEquals(value, notified.get(field));
		}
	}

	@Test
	public void testListenerNotNotifiedAfterSetWhenUnregistered() {
		TranslationComment comment = createTranslationComment();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		FieldListener listener = new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}
		};
		comment.addFieldListener(listener);
		comment.removeFieldListener(listener);
		for (Field<?> field : getEditableFields()) {
			change(comment, field);
			assertTrue(notified.isEmpty());
		}
	}

	@Test
	public void testListenerNotifiedAfterResetWhenRegistered() {
		TranslationComment comment = createTranslationComment();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		comment.addFieldListener(new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}
		});
		for (Field<?> field : getEditableFields()) {
			change(comment, field);
			comment.reset(field);
			assertEquals(getInitialReference(field), notified.get(field));
		}
	}

	@Test
	public void testListenerNotNotifiedAfterResetWhenUnregistered() {
		TranslationComment comment = createTranslationComment();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		FieldListener listener = new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}
		};
		comment.addFieldListener(listener);
		comment.removeFieldListener(listener);
		for (Field<?> field : getEditableFields()) {
			change(comment, field);
			comment.reset(field);
			assertTrue(notified.isEmpty());
		}
	}

	@Test
	public void testListenerNotifiedAfterResetAllWhenRegistered() {
		TranslationComment comment = createTranslationComment();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		comment.addFieldListener(new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}
		});
		for (Field<?> field : getEditableFields()) {
			change(comment, field);
		}
		comment.resetAll();
		for (Field<?> field : getEditableFields()) {
			assertEquals(getInitialReference(field), notified.get(field));
		}
	}

	@Test
	public void testListenerNotNotifiedAfterResetAllWhenUnregistered() {
		TranslationComment comment = createTranslationComment();
		final Map<Field<?>, Object> notified = new HashMap<Field<?>, Object>();
		FieldListener listener = new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				notified.put(field, newValue);
			}
		};
		comment.addFieldListener(listener);
		comment.removeFieldListener(listener);
		for (Field<?> field : getEditableFields()) {
			change(comment, field);
		}
		comment.resetAll();
		assertTrue(notified.isEmpty());
	}

	private <T> T change(TranslationComment comment, Field<T> field) {
		T value = createNewEditableFieldValue(field, comment.get(field));
		comment.set(field, value);
		return value;
	}

	private <T> void setToReference(TranslationComment comment, Field<T> field) {
		comment.set(field, getInitialReference(field));
	}

}
