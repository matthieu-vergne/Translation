package fr.sazaju.vheditor.util;

import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.TranslationProject;

/**
 * A {@link MapNamer} allows to associate a name to a {@link TranslationMap}.
 * Because {@link TranslationMap}s are retrieved from a
 * {@link TranslationProject} through its {@link MapID}, the {@link MapNamer}
 * associate the name to this {@link MapID} too, which allows to know the name
 * of the {@link TranslationMap} without needing to load it.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 * @param <MapID>
 */
public interface MapNamer<MapID> {

	/**
	 * 
	 * @param id
	 *            the ID of the {@link TranslationMap}
	 * @return the name of the {@link TranslationMap}
	 */
	public String getNameFor(MapID id);
}
