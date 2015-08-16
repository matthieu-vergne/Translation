package fr.vergne.translation.util.impl;

import fr.vergne.translation.util.Switcher;

/**
 * A {@link SmartStringSwitcher} is a {@link Switcher} dedicated to switching
 * between a {@link String} and another representation. It uses a simple
 * switching strategy by relying on a usual implementation for the given class.
 * 
 * @author Matthieu VERGNE <matthieu.vergne@gmail.com>
 * 
 * @param <T>
 */
public class SmartStringSwitcher<T> implements Switcher<String, T> {

	private final Switcher<String, T> internalConvertor;
	private final String nullString;

	/**
	 * 
	 * @param nonStringClass
	 *            the type of the second representation
	 * @param nullString
	 *            the {@link String} to map to the <code>null</code> value of
	 *            the second representation
	 */
	public SmartStringSwitcher(Class<T> nonStringClass, String nullString) {
		this.internalConvertor = chooseConvertor(nonStringClass);
		this.nullString = nullString;
	}

	/**
	 * Equivalent to {@link #SmartStringSwitcher(Class, String)} with
	 * <code>null</code> for the null-string argument.
	 */
	public SmartStringSwitcher(Class<T> nonStringClass) {
		this(nonStringClass, null);
	}

	@Override
	public T switchForth(String value) {
		if (value == nullString || value != null && value.equals(nullString)) {
			return null;
		} else {
			return internalConvertor.switchForth(value);
		}
	}

	@Override
	public String switchBack(T value) {
		if (value == null) {
			return nullString;
		} else {
			return internalConvertor.switchBack(value);
		}
	}

	@SuppressWarnings("unchecked")
	private Switcher<String, T> chooseConvertor(Class<T> nonStringClass) {
		if (Integer.class.isAssignableFrom(nonStringClass)) {
			return (Switcher<String, T>) new Switcher<String, Integer>() {

				@Override
				public Integer switchForth(String value) {
					return Integer.parseInt(value);
				}

				@Override
				public String switchBack(Integer value) {
					return value.toString();
				}
			};
		} else if (Double.class.isAssignableFrom(nonStringClass)) {
			return (Switcher<String, T>) new Switcher<String, Double>() {

				@Override
				public Double switchForth(String value) {
					return Double.parseDouble(value);
				}

				@Override
				public String switchBack(Double value) {
					return value.toString();
				}
			};
		} else if (Boolean.class.isAssignableFrom(nonStringClass)) {
			return (Switcher<String, T>) new Switcher<String, Boolean>() {

				@Override
				public Boolean switchForth(String value) {
					return Boolean.parseBoolean(value);
				}

				@Override
				public String switchBack(Boolean value) {
					return value.toString();
				}
			};
		} else {
			throw new IllegalArgumentException(
					"Impossible to setup the convertor for " + nonStringClass);
		}
	}

}
