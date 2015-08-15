package fr.vergne.translation.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import fr.vergne.translation.TranslationMetadata;
import fr.vergne.translation.util.Reader;
import fr.vergne.translation.util.Writer;

/**
 * An {@link OnDemandMetadata} allows to retrieve the
 * {@link TranslationMetadata} values on demand, so it does not store them
 * directly. In particular, it does not try to retrieve all of them from the
 * start: they will be retrieved only if requested with
 * {@link #getStored(Field)}.<br/>
 * <br/>
 * This implementation also provides methods to set up editable and non-editable
 * {@link Field}s, namely the <code>configureXxx()</code> methods. The ones
 * taking a {@link Writer} as argument allows to set up editable fields, while
 * the ones having no {@link Writer} argument set up non-editable fields.
 * 
 * @author Matthieu VERGNE <matthieu.vergne@gmail.com>
 * 
 */
public class OnDemandMetadata implements TranslationMetadata {

	private static final Writer<OnDemandMetadata> DEFAULT_SAVER = new Writer<OnDemandMetadata>() {

		@Override
		public void write(OnDemandMetadata element) {
			for (Field<?> field : element.orderedFields) {
				element.save(field);
			}
		}
	};
	private final Map<Field<?>, Object> changedValues = new HashMap<Field<?>, Object>();
	private final Map<Field<?>, Reader<?>> fieldReaders = new HashMap<Field<?>, Reader<?>>();
	private final Map<Field<?>, Writer<?>> fieldSavers = new HashMap<Field<?>, Writer<?>>();
	private final Collection<FieldListener> listeners = new HashSet<>();
	private final Set<Field<?>> orderedFields = new LinkedHashSet<Field<?>>();
	private final Writer<? super OnDemandMetadata> metadataSaver;

	/**
	 * Instantiate an {@link OnDemandMetadata} with a customized saving
	 * strategy.
	 * 
	 * @param saver
	 *            the save strategy to use when calling {@link #saveAll()}
	 */
	public OnDemandMetadata(Writer<? super OnDemandMetadata> saver) {
		this.metadataSaver = saver;
	}

	/**
	 * Instantiate an {@link OnDemandMetadata} with a naive saving strategy:
	 * each modified field is saved separately. If you want to use a smarter
	 * strategy, use {@link #OnDemandMetadata(Writer)} isntead.
	 */
	public OnDemandMetadata() {
		this(DEFAULT_SAVER);
	}

	/**
	 * 
	 * @param field
	 *            the {@link Field} to configure as non-editable
	 * @param reader
	 *            the way to retrieve the stored {@link Field} value
	 */
	public <T> void configureField(Field<T> field, Reader<T> reader) {
		fieldReaders.put(field, reader);
		fieldSavers.remove(field);
		orderedFields.add(field);
	}

	/**
	 * 
	 * @param field
	 *            the {@link Field} to configure as editable
	 * @param reader
	 *            the way to retrieve the stored {@link Field} value
	 * @param writer
	 *            the way to save the {@link Field} value
	 */
	public <T> void configureField(Field<T> field, Reader<T> reader,
			Writer<T> writer) {
		fieldReaders.put(field, reader);
		fieldSavers.put(field, writer);
		orderedFields.add(field);
	}

	@Override
	public Iterator<Field<?>> iterator() {
		return orderedFields.iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getStored(Field<T> field) {
		return (T) fieldReaders.get(field).read();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Field<T> field) {
		if (changedValues.containsKey(field)) {
			return (T) changedValues.get(field);
		} else if (fieldReaders.containsKey(field)) {
			return (T) fieldReaders.get(field).read();
		} else {
			throw new IllegalArgumentException("Unmanaged field: " + field);
		}
	}

	@Override
	public <T> boolean isEditable(Field<T> field) {
		return fieldSavers.containsKey(field);
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
			Writer<T> writer = (Writer<T>) fieldSavers.get(field);
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
				listener.fieldUpdated(field, (T) fieldReaders.get(field).read());
			}
		} else {
			// nothing to reset
		}
	}

	@Override
	public void saveAll() {
		metadataSaver.write(this);
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
			builder.append(field + ": " + get(field) + "|");
		}
		return "[" + builder.toString() + "]";
	}
}
