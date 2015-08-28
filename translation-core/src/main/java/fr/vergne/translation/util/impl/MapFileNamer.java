package fr.vergne.translation.util.impl;

import java.io.File;

import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.util.MapNamer;

/**
 * Simple {@link MapNamer} for file-based {@link TranslationMap}s IDs: the name
 * returned for a given {@link TranslationMap} is the name of the file it
 * corresponds to.
 * 
 * @author Matthieu VERGNE <matthieu.vergne@gmail.com>
 * 
 */
public class MapFileNamer implements MapNamer<File> {

	private final String name;
	private final String description;

	public MapFileNamer(String name, String description) {
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

	@Override
	public String getNameFor(File file) {
		return file.getName();
	}
}
