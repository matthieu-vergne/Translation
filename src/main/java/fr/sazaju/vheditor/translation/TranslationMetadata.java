package fr.sazaju.vheditor.translation;

/**
 * A {@link TranslationMetadata} aims at interacting with the specific data
 * contained into the comment of a {@link TranslationEntry}.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 */
public interface TranslationMetadata {

	/**
	 * The reference value of a {@link Field} is the initial value to consider.
	 * Upon instantiation, {@link #get(Field)} should return the same value,
	 * before to diverge by using {@link #set(Field, Object)}. In the case of
	 * divergence, calling {@link #reset(Field)} should align the current value
	 * on the reference value, while calling {@link #save(Field)} should align
	 * the reference value on the current value.
	 * 
	 * @param field
	 *            the {@link Field} to retrieve the value for
	 * @return the value of reference for the {@link Field}, which can be
	 *         <code>null</code>
	 */
	public <T> T getReference(Field<T> field);

	/**
	 * 
	 * @param field
	 *            the {@link Field} to retrieve the value for
	 * @return the value of the {@link Field}, <code>null</code> if there is no
	 *         value assigned to it
	 * @see #getReference(Field)
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
	 * @see #getReference(Field)
	 */
	public <T> void set(Field<T> field, T value) throws UneditableFieldException;

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
	 * Saving a {@link Field} leads to align its reference value to its current
	 * value. After the process, {@link #getReference(Field)} should return the
	 * same result than {@link #get(Field)}. This method should also lead to
	 * update the storage (usually a file) on which this
	 * {@link TranslationMetadata} is based on.
	 * 
	 * @param field
	 *            the {@link Field} to save
	 */
	public <T> void save(Field<T> field);

	/**
	 * Resetting a {@link Field} leads to align its current value to its
	 * reference value. After the process, {@link #get(Field)} should return the
	 * same result than {@link #getReference(Field)}. This method should be a
	 * way to recover the same content than the storage (usually a file) on
	 * which this {@link TranslationMetadata} is based on. After the reset, any
	 * {@link FieldListener} registered through
	 * {@link #addFieldListener(FieldListener)} should be notified of the new
	 * values of the resetted {@link Field}s.
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
	 * {@link TranslationMetadata}. Such a data needs to be managed somehow, and
	 * {@link Field}s recognised by the editor are provided as static members of
	 * this class, like {@link #MARKED_AS_TRANSLATED}.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 * @param <T>
	 *            the type of value assigned to the given field
	 */
	public static class Field<T> {
		// FIXME replace generic fields by map-specific fields
		public static final Field<Boolean> MARKED_AS_TRANSLATED = new Field<Boolean>(
				"Translated mark");
		public static final Field<Integer> CHAR_LIMIT_FACE = new Field<Integer>(
				"Char limit (face)");
		public static final Field<Integer> CHAR_LIMIT_NO_FACE = new Field<Integer>(
				"Char limit (no face)");

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
