package fr.sazaju.vheditor.translation.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import fr.sazaju.vheditor.translation.TranslationMetadata;

/**
 * This implementation of {@link TranslationMetadata} provides a method to setup
 * editable and non-editable {@link Field}s, respectively:
 * <ul>
 * <li>{@link #configureField(Field, FieldReader)}</li>
 * <li>{@link #configureField(Field, FieldReader, FieldWriter)}</li>
 * </ul>
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 */
public class SimpleTranslationMetadata implements TranslationMetadata {

	private final Map<Field<?>, Object> changedValues = new HashMap<Field<?>, Object>();
	private final Map<Field<?>, FieldReader<?>> readers = new HashMap<Field<?>, FieldReader<?>>();
	private final Map<Field<?>, FieldWriter<?>> writers = new HashMap<Field<?>, FieldWriter<?>>();
	private final Collection<FieldListener> listeners = new HashSet<>();
	private final Set<Field<?>> orderedFields = new LinkedHashSet<Field<?>>();

	/**
	 * A {@link FieldReader} aims at retrieving the reference value of a given
	 * {@link Field} in order to feed the
	 * {@link SimpleTranslationMetadata#getReference(Field)} method.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 * @param <T>
	 */
	public static interface FieldReader<T> {
		public T read();
	}

	/**
	 * A {@link FieldWriter} aims at replacing the reference value of a
	 * {@link Field} by a new one, providing a way to execute the
	 * {@link SimpleTranslationMetadata#save(Field)} method.
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
	public <T> T getReference(Field<T> field) {
		return (T) readers.get(field).read();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Field<T> field) {
		if (changedValues.containsKey(field)) {
			return (T) changedValues.get(field);
		} else {
			return (T) readers.get(field).read();
		}
	}

	@Override
	public <T> boolean isEditable(Field<T> field) {
		return writers.containsKey(field);
	}

	@Override
	public <T> void set(Field<T> field, T value)
			throws UneditableFieldException {
		if (isEditable(field)) {
			changedValues.put(field, value);
			for (FieldListener listener : listeners) {
				listener.fieldUpdated(field, value);
			}
		} else {
			throw new UneditableFieldException(field);
		}
	}

	@Override
	public void addFieldListener(FieldListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeFieldListener(FieldListener listener) {
		listeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void save(Field<T> field) {
		if (changedValues.containsKey(field)) {
			FieldWriter<T> writer = (FieldWriter<T>) writers.get(field);
			writer.write((T) changedValues.get(field));
		} else {
			// nothing to save
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void reset(Field<T> field) {
		if (changedValues.containsKey(field)) {
			changedValues.remove(field);
			for (FieldListener listener : listeners) {
				listener.fieldUpdated(field, (T) readers.get(field).read());
			}
		} else {
			// nothing to reset
		}
	}

	@Override
	public void saveAll() {
		// TODO use a custom saver (minimize overhead)
		for (Field<?> field : orderedFields) {
			save(field);
		}
	}

	@Override
	public void resetAll() {
		for (Field<?> field : orderedFields) {
			reset(field);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Field<?> field : orderedFields) {
			Object value = readers.get(field).read();
			builder.append(field + ": " + value + "\n");
		}
		return builder.toString();
	}
}
