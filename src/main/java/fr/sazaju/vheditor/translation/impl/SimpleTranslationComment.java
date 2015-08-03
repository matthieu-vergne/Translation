package fr.sazaju.vheditor.translation.impl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import fr.sazaju.vheditor.translation.TranslationComment;

/**
 * This implementation of {@link TranslationComment} provides a method to setup
 * editable and non-editable {@link Field}s, respectively:
 * <ul>
 * <li>{@link #configureField(Field, FieldReader)}</li>
 * <li>{@link #configureField(Field, FieldReader, FieldWriter)}</li>
 * </ul>
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 */
public class SimpleTranslationComment implements TranslationComment {

	private Map<Field<?>, FieldReader<?>> readers = new HashMap<Field<?>, FieldReader<?>>();
	private Map<Field<?>, FieldWriter<?>> writers = new HashMap<Field<?>, FieldWriter<?>>();
	private Set<Field<?>> orderedFields = new LinkedHashSet<Field<?>>();

	/**
	 * A {@link FieldReader} aims at retrieving the value of a given
	 * {@link Field} in order to feed the
	 * {@link SimpleTranslationComment#get(Field)} method.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 * @param <T>
	 */
	public static interface FieldReader<T> {
		public T read();
	}

	/**
	 * A {@link FieldWriter} aims at replacing the current value of a
	 * {@link Field} by a new one, providing a way to execute the
	 * {@link SimpleTranslationComment#set(Field, Object)} method.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 * @param <T>
	 */
	public static interface FieldWriter<T> {
		public void write(T value);
	}

	/**
	 * 
	 * @param field
	 *            the {@link Field} to configure as non-editable
	 * @param reader
	 *            the way to retrieve the {@link Field} value
	 */
	public <T> void configureField(Field<T> field, FieldReader<T> reader) {
		readers.put(field, reader);
		writers.remove(field);
		orderedFields.add(field);
	}

	/**
	 * 
	 * @param field
	 *            the {@link Field} to configure as editable
	 * @param reader
	 *            the way to retrieve the {@link Field} value
	 * @param writer
	 *            the way to update the {@link Field} value
	 */
	public <T> void configureField(Field<T> field, FieldReader<T> reader,
			FieldWriter<T> writer) {
		readers.put(field, reader);
		writers.put(field, writer);
		orderedFields.add(field);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Field<T> field) {
		return (T) readers.get(field).read();
	}

	@Override
	public <T> boolean isEditable(Field<T> field) {
		return writers.containsKey(field);
	}

	@Override
	public <T> void set(Field<T> field, T value) {
		@SuppressWarnings("unchecked")
		FieldWriter<T> writer = (FieldWriter<T>) writers.get(field);
		writer.write(value);
	}

	@Override
	public String getFullString() {
		StringBuilder builder = new StringBuilder();
		for (Field<?> field : orderedFields) {
			Object value = readers.get(field).read();
			builder.append(field + ": " + value + "\n");
		}
		return builder.toString();
	}
}
