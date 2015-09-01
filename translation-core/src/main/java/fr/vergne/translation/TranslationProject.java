package fr.vergne.translation;

import java.util.Collection;
import java.util.Iterator;

import fr.vergne.translation.util.EntryFilter;
import fr.vergne.translation.util.Feature;
import fr.vergne.translation.util.MapNamer;

public interface TranslationProject<Entry extends TranslationEntry<?>, MapID, Map extends TranslationMap<Entry>>
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
	 * This method aims at providing the different naming strategies relevant to
	 * this {@link TranslationProject}. Several naming strategies may be used
	 * for instance because they provide different facilities: use file names to
	 * retrieve the data on the file system, use titles to identify the purpose
	 * of the {@link TranslationMap}, etc.
	 * 
	 * @return the {@link MapNamer}s relevant for this project.
	 */
	public Collection<MapNamer<MapID>> getMapNamers();

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

	/**
	 * 
	 * @return the {@link EntryFilter}s which can be used to search for
	 *         particular {@link TranslationEntry}s.
	 */
	public Collection<EntryFilter<Entry>> getEntryFilters();
}
