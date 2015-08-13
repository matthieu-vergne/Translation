package fr.sazaju.vheditor.util;

import java.util.Map;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMetadata;
import fr.sazaju.vheditor.translation.TranslationProject;

/**
 * A {@link Feature} is an action which can be executed in the scope of a given
 * {@link TranslationProject}. Like a {@link TranslationMetadata} for a
 * {@link TranslationEntry}, a {@link Feature} is a {@link TranslationProject}
 * -specific added value, which does not fall in the usual fonctionalities like
 * providing {@link Map}s and saving them.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 */
public interface Feature extends Runnable {

	/**
	 * 
	 * @return the name of the {@link Feature}, which acts as an informal
	 *         identifier
	 */
	public String getName();

	/**
	 * 
	 * @return the description of the feature, which provides details about the
	 *         purpose of the feature and how to use it
	 */
	public String getDescription();

	/**
	 * Execute the {@link Feature} in order to produce the corresponding effect.
	 */
	@Override
	public void run();
}
