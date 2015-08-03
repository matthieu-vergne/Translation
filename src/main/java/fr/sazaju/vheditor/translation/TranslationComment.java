package fr.sazaju.vheditor.translation;

/**
 * A {@link TranslationComment} aims at interacting with the specific data
 * contained into the comment of a {@link TranslationEntry}.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 */
public interface TranslationComment {

	/**
	 * 
	 * @param field
	 *            the {@link Field} to retrieve the value for
	 * @return the value of the {@link Field}, <code>null</code> if there is no
	 *         value assigned to it
	 */
	public <T> T get(Field<T> field);

	/**
	 * 
	 * @param field
	 *            the {@link Field} to check
	 * @return <code>true</code> if {@link #set(Field, Object)} can be used with
	 *         this {@link Field}, <code>false</code> otherwise
	 */
	public <T> boolean isEditable(Field<T> field);

	/**
	 * 
	 * @param field
	 *            the {@link Field} to modify
	 * @param value
	 *            the new value of the {@link Field}
	 * @throws UneditableFieldException
	 *             if the field is not editable ({@link #isEditable(Field)}
	 *             returns <code>false</code>)
	 */
	public <T> void set(Field<T> field, T value);

	/**
	 * 
	 * @return the full comment content
	 */
	public String getFullString();

	/**
	 * A {@link Field} represents a specific data which can be retrieved from a
	 * {@link TranslationComment}. Such a data needs to be managed somehow, and
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
