package fr.sazaju.vheditor.translation;

import java.util.Iterator;

import fr.sazaju.vheditor.translation.TranslationMetadata.Field;

/**
 * A {@link TranslationMetadata} aims at storing any additional data which would
 * enrich a {@link TranslationEntry}. Typically, the context of the
 * {@link TranslationEntry} and the constraints of the translation, like a limit
 * of characters, are the kind of data which can be stored in a
 * {@link TranslationMetadata}.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 */
public interface TranslationMetadata extends Iterable<Field<?>> {

	/**
	 * Provides all the {@link Field}s managed so far. If a {@link Field} is
	 * provided by this {@link Iterator}, it does not mean that its (stored)
	 * value is not <code>null</code>, so it can provide more {@link Field}s
	 * than expected. Similarly, if a {@link Field} is not provided by this
	 * {@link Iterator} it does not mean that it cannot be used, so it is not a
	 * way to retrieve all the possible {@link Field}s of this
	 * {@link TranslationMetadata}.
	 */
	@Override
	public Iterator<Field<?>> iterator();

	/**
	 * The stored value of a {@link Field} corresponds to a reference. Upon
	 * instantiation, {@link #get(Field)} should return the same value, before
	 * to diverge by using {@link #set(Field, Object)}. In the case of
	 * divergence, calling {@link #reset(Field)} should align the current value
	 * on the stored one, while calling {@link #save(Field)} should align the
	 * stored value on the current one.
	 * 
	 * @param field
	 *            the {@link Field} to retrieve the stored value for
	 * @return the stored value for the {@link Field}, which can be
	 *         <code>null</code>
	 */
	public <T> T getStored(Field<T> field);

	/**
	 * 
	 * @param field
	 *            the {@link Field} to retrieve the value for
	 * @return the value of the {@link Field}, <code>null</code> if there is no
	 *         value assigned to it
	 * @see #getStored(Field)
	 */
	public <T> T get(Field<T> field);

	/**
	 * 
	 * @param field
	 *            the {@link Field} to check
	 * @return <code>true</code> if {@link #set(Field, Object)} can be used with
	 *         this {@link Field}, <code>false</code> otherwise
	 * @see #set(Field, Object)
	 */
	public <T> boolean isEditable(Field<T> field);

	/**
	 * This method allows to update the current value of a given {@link Field}.
	 * After modification, any {@link FieldListener} registered through
	 * {@link #addFieldListener(FieldListener)} should be notified of the new
	 * value.
	 * 
	 * @param field
	 *            the {@link Field} to modify
	 * @param value
	 *            the new value of the {@link Field}
	 * @throws UneditableFieldException
	 *             if the field is not editable ({@link #isEditable(Field)}
	 *             returns <code>false</code>)
	 * @see #isEditable(Field)
	 * @see #get(Field)
	 * @see #getStored(Field)
	 */
	public <T> void set(Field<T> field, T value)
			throws UneditableFieldException;

	/**
	 * A {@link FieldListener} allows to be notified when a {@link Field} of a
	 * {@link TranslationMetadata} is updated. To be notified, the
	 * {@link FieldListener} should have been provided to
	 * {@link TranslationMetadata#addFieldListener(FieldListener)} .
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 */
	public static interface FieldListener {
		public <T> void fieldUpdated(Field<T> field, T newValue);
	}

	/**
	 * 
	 * @param listener
	 *            the {@link FieldListener} to register to this
	 *            {@link TranslationMetadata}
	 */
	public void addFieldListener(FieldListener listener);

	/**
	 * 
	 * @param listener
	 *            the {@link FieldListener} to unregister from this
	 *            {@link TranslationMetadata}
	 */
	public void removeFieldListener(FieldListener listener);

	/**
	 * Saving a {@link Field} leads to align its stored value to its current
	 * value. After the process, {@link #getStored(Field)} should return the
	 * same result than {@link #get(Field)}. This also means that the storage
	 * (usually a file or database) on which this {@link TranslationMetadata} is
	 * based on should be updated.
	 * 
	 * @param field
	 *            the {@link Field} to save
	 */
	public <T> void save(Field<T> field);

	/**
	 * Resetting a {@link Field} leads to align its current value to its stored
	 * value. After the process, {@link #get(Field)} should return the same
	 * result than {@link #getStored(Field)}. This method should be a way to
	 * recover the same content than the storage (usually a file or database) on
	 * which this {@link TranslationMetadata} is based on. After the reset, any
	 * {@link FieldListener} registered through
	 * {@link #addFieldListener(FieldListener)} should be notified of the new
	 * values of the reset {@link Field}s.
	 * 
	 * @param field
	 *            the {@link Field} to reset
	 */
	public <T> void reset(Field<T> field);

	/**
	 * This method should be equivalent to calling {@link #save(Field)} on all
	 * the {@link Field}s in an atomic way, thus reducing the overhead of
	 * calling it for each {@link Field} separately.
	 */
	public void saveAll();

	/**
	 * This method should be equivalent to calling {@link #reset(Field)} on all
	 * the {@link Field}s in an atomic way, thus reducing the overhead of
	 * calling it for each {@link Field} separately.
	 */
	public void resetAll();

	/**
	 * A {@link Field} represents a specific data which can be retrieved from a
	 * {@link TranslationMetadata}.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 * @param <T>
	 *            the type of value assigned to the given field
	 */
	public static class Field<T> {

		private String name;

		/**
		 * Instantiate a {@link Field} with a given display name. This display
		 * name can be retrieved from the {@link #toString()} method.
		 * 
		 * @param name
		 *            the name to use when displaying the {@link Field}
		 */
		public Field(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	/**
	 * Exception thrown when one tries to edit a non-editable {@link Field}.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 */
	@SuppressWarnings("serial")
	public static class UneditableFieldException extends RuntimeException {
		public UneditableFieldException(Field<?> field) {
			super("The field " + field + " is not editable");
		}
	}
}
