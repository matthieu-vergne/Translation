package fr.vergne.translation.util;

/**
 * A {@link Switcher} aims at providing an easy way to switch between two
 * representations of a value.
 * 
 * @author Matthieu VERGNE <matthieu.vergne@gmail.com>
 * 
 * @param <T1>
 *            the first representation type
 * @param <T2>
 *            the second representation type
 */
public interface Switcher<T1, T2> {
	/**
	 * 
	 * @param value
	 *            the value in the first representation
	 * @return the value in the second representation
	 */
	public T2 switchForth(T1 value);

	/**
	 * 
	 * @param value
	 *            the value in the second representation
	 * @return the value in the first representation
	 */
	public T1 switchBack(T2 value);

}
