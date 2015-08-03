package fr.sazaju.vheditor.translation.impl;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationComment;
import fr.sazaju.vheditor.translation.TranslationComment.Field;
import fr.sazaju.vheditor.translation.TranslationCommentTest;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationComment.FieldWriter;

public class SimpleTranslationCommentTest extends TranslationCommentTest {

	private final Field<Integer> nonEditable0 = new Field<>("Field 0");
	private final Field<Integer> editable0 = new Field<>("Field 1");
	private final Field<Integer> nonEditable1 = new Field<>("Field 2");
	private final Field<Integer> editable1 = new Field<>("Field 3");

	@Override
	protected TranslationComment createTranslationComment() {
		SimpleTranslationComment comment = new SimpleTranslationComment();
		final Integer[] datastore = { getInitialReference(nonEditable0),
				getInitialReference(nonEditable1),
				getInitialReference(editable0), getInitialReference(editable1) };
		comment.configureField(nonEditable0,
				new SimpleTranslationComment.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[0];
					}
				});
		comment.configureField(nonEditable1,
				new SimpleTranslationComment.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[2];
					}
				});
		comment.configureField(editable0,
				new SimpleTranslationComment.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[1];
					}
				}, new FieldWriter<Integer>() {

					@Override
					public void write(Integer value) {
						datastore[1] = value;
					}
				});
		comment.configureField(editable1,
				new SimpleTranslationComment.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[3];
					}
				}, new FieldWriter<Integer>() {

					@Override
					public void write(Integer value) {
						datastore[3] = value;
					}
				});
		return comment;
	}

	@Override
	protected Collection<Field<?>> getEditableFields() {
		Collection<Field<?>> fields = new LinkedList<Field<?>>();
		fields.add(editable0);
		fields.add(editable1);
		return fields;
	}

	@Override
	protected Collection<Field<?>> getNonEditableFields() {
		Collection<Field<?>> fields = new LinkedList<Field<?>>();
		fields.add(nonEditable0);
		fields.add(nonEditable1);
		return fields;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T getInitialReference(Field<T> field) {
		return (T) (Integer) 10;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		return (T) (Integer) (((Integer) currentValue) + 1);
	}

	@Test
	public void testEditableFieldIdentifiedThroughWriter() {
		SimpleTranslationComment comment = new SimpleTranslationComment();
		final Integer[] datastore = { 0, 1, 2, 3 };
		comment.configureField(nonEditable0,
				new SimpleTranslationComment.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[0];
					}
				});
		comment.configureField(editable0,
				new SimpleTranslationComment.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[1];
					}
				}, new FieldWriter<Integer>() {

					@Override
					public void write(Integer value) {
						datastore[1] = value;
					}
				});
		comment.configureField(nonEditable1,
				new SimpleTranslationComment.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[2];
					}
				});
		comment.configureField(editable1,
				new SimpleTranslationComment.FieldReader<Integer>() {

					@Override
					public Integer read() {
						return datastore[3];
					}
				}, new FieldWriter<Integer>() {

					@Override
					public void write(Integer value) {
						datastore[3] = value;
					}
				});

		assertEquals(false, comment.isEditable(nonEditable0));
		assertEquals(false, comment.isEditable(nonEditable1));
		assertEquals(true, comment.isEditable(editable0));
		assertEquals(true, comment.isEditable(editable1));
	}

}
