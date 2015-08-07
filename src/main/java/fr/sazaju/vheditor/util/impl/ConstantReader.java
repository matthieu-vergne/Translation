package fr.sazaju.vheditor.util.impl;

import fr.sazaju.vheditor.util.Reader;

/**
 * A {@link ConstantReader} is a {@link Reader} based on a constant value.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
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
