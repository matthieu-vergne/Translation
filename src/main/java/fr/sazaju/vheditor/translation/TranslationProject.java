package fr.sazaju.vheditor.translation;

import java.util.Iterator;

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
	 * @return the number of {@link Map}s in this {@link TranslationProject}
	 */
	public int size();

	/**
	 * This method should be equivalent to calling
	 * {@link TranslationMap#saveAll()} for each {@link TranslationMap} of this
	 * {@link TranslationProject} in an atomic way, thus reducing the
	 * overhead of calling each one separately.
	 */
	public void saveAll();

	/**
	 * This method should be equivalent to calling
	 * {@link TranslationMap#resetAll()} for each {@link TranslationMap} of this
	 * {@link TranslationProject} in an atomic way, thus reducing the
	 * overhead of calling each one separately.
	 */
	public void resetAll();
}
