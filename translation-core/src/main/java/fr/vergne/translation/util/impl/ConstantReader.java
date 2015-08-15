package fr.vergne.translation.util.impl;

import fr.vergne.translation.util.Reader;

/**
 * A {@link ConstantReader} is a {@link Reader} based on a constant value.
 * 
 * @author Matthieu VERGNE <matthieu.vergne@gmail.com>
 * 
 * @param <Value>
 */
public class ConstantReader<Value> implements Reader<Value> {

	private final Value value;

	public ConstantReader(Value value) {
		this.value = value;
	}

	@Override
	public Value read() {
		return value;
	}

}
