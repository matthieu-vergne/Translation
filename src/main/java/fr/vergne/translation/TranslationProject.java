package fr.vergne.translation;

import java.util.Collection;
import java.util.Iterator;

import fr.vergne.translation.util.Feature;

public interface TranslationProject<MapID, Map extends TranslationMap<?>>
		extends Iterable<MapID> {

	/**
	 * This method should return an {@link Iterator} which provides the IDs of
	 * all the {@link Map}s that should be translated.
	 */
	@Override
	public Iterator<MapID> iterator();

	/**
	 * This method aims at retrieving a specific {@link Map} from the
	 * {@link TranslationProject}.
	 * 
	 * @param id
	 *            the identifer of the {@link Map}
	 * @return the corresponding {@link Map}
	 */
	public Map getMap(MapID id);

	/**
	 * This method allows to know the purpose of a given {@link Map}. A
	 * {@link Map} corresponding to a set of {@link TranslationEntry}s which
	 * share the same purpose, this purpose should be known in some way. Because
	 * this purpose is decided before to create the {@link Map} (the {@link Map}
	 * is made in a given way <i>because</i> we want it to have a given
	 * purpose), thus it is an information which relates to the full
	 * {@link TranslationProject}, which represents the context in which this
	 * purpose makes sense. For practicality, the purpose is represented as a
	 * name for the {@link Map}, so it can be used easily for display.
	 * 
	 * @return the name of a given {@link Map}
	 */
	public String getMapName(MapID id);

	/**
	 * @return the number of {@link Map}s in this {@link TranslationProject}
	 */
	public int size();

	/**
	 * This method should be equivalent to calling
	 * {@link TranslationMap#saveAll()} for each {@link TranslationMap} of this
	 * {@link TranslationProject} in an atomic way, thus reducing the overhead
	 * of calling each one separately.
	 */
	public void saveAll();

	/**
	 * This method should be equivalent to calling
	 * {@link TranslationMap#resetAll()} for each {@link TranslationMap} of this
	 * {@link TranslationProject} in an atomic way, thus reducing the overhead
	 * of calling each one separately.
	 */
	public void resetAll();

	/**
	 * 
	 * @return the additional {@link Feature}s provided by this
	 *         {@link TranslationProject}
	 */
	public Collection<Feature> getFeatures();
}
