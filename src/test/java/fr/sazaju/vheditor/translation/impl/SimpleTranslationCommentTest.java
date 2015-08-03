package fr.sazaju.vheditor.translation.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationComment.Field;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationComment.FieldReader;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationComment.FieldWriter;

public class SimpleTranslationCommentTest {

	@Test
	public void testNonEditableFieldsProperlyRetrieveValues() {
		SimpleTranslationComment comment = new SimpleTranslationComment();
		final Boolean[] data = { null };
		comment.configureField(Field.MARKED_AS_TRANSLATED,
				new SimpleTranslationComment.FieldReader<Boolean>() {

					@Override
					public Boolean read() {
						return data[0];
					}
				});

		assertEquals(null, comment.get(Field.MARKED_AS_TRANSLATED));

		data[0] = true;
		assertEquals(true, comment.get(Field.MARKED_AS_TRANSLATED));

		data[0] = false;
		assertEquals(false, comment.get(Field.MARKED_AS_TRANSLATED));

		data[0] = null;
		assertEquals(null, comment.get(Field.MARKED_AS_TRANSLATED));
	}

	@Test
	public void testEditableFieldsProperlyRetrieveValues() {
		SimpleTranslationComment comment = new SimpleTranslationComment();
		final Object[] data = { null };
		comment.configureField(Field.MARKED_AS_TRANSLATED,
				new FieldReader<Boolean>() {

					@Override
					public Boolean read() {
						return (Boolean) data[0];
					}
				}, new FieldWriter<Boolean>() {

					@Override
					public void write(Boolean value) {
						// write nothing for this test
					}
				});

		assertEquals(null, comment.get(Field.MARKED_AS_TRANSLATED));

		data[0] = true;
		assertEquals(true, comment.get(Field.MARKED_AS_TRANSLATED));

		data[0] = false;
		assertEquals(false, comment.get(Field.MARKED_AS_TRANSLATED));

		data[0] = null;
		assertEquals(null, comment.get(Field.MARKED_AS_TRANSLATED));
	}

	@Test
	public void testEditableFieldsProperlyUpdateValues() {
		SimpleTranslationComment comment = new SimpleTranslationComment();
		final Object[] data = { null };
		comment.configureField(Field.MARKED_AS_TRANSLATED,
				new FieldReader<Boolean>() {

					@Override
					public Boolean read() {
						// read nothing for this test
						return null;
					}
				}, new FieldWriter<Boolean>() {

					@Override
					public void write(Boolean value) {
						data[0] = value;
					}
				});

		comment.set(Field.MARKED_AS_TRANSLATED, false);
		assertEquals(false, data[0]);

		comment.set(Field.MARKED_AS_TRANSLATED, null);
		assertEquals(null, data[0]);

		comment.set(Field.MARKED_AS_TRANSLATED, true);
		assertEquals(true, data[0]);
	}

}
