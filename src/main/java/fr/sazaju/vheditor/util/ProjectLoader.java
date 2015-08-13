package fr.sazaju.vheditor.util;

import java.io.File;

import fr.sazaju.vheditor.translation.TranslationProject;

public interface ProjectLoader<Project extends TranslationProject<?, ?>> {

	public Project load(File directory);
}
