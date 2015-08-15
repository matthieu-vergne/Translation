package fr.vergne.translation.util.impl;

import fr.vergne.translation.util.Feature;

public abstract class SimpleFeature implements Feature {

	private final String name;
	private final String description;

	public SimpleFeature(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
