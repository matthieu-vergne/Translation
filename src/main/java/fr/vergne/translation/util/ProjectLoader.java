package fr.vergne.translation.util;

import java.io.File;

import fr.vergne.translation.TranslationProject;

public interface ProjectLoader<Project extends TranslationProject<?, ?>> {

	public Project load(File directory);
}
