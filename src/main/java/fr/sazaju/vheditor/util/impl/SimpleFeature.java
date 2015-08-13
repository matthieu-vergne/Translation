package fr.sazaju.vheditor.util.impl;

import fr.sazaju.vheditor.util.Feature;

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
