package fr.vergne.translation.impl;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationEntry.TranslationListener;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationMetadata;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.TranslationMetadata.FieldListener;
import fr.vergne.translation.TranslationProject;
import fr.vergne.translation.util.EntryFilter;
import fr.vergne.translation.util.Feature;
import fr.vergne.translation.util.MapNamer;
import fr.vergne.translation.util.MultiReader;
import fr.vergne.translation.util.Writer;

/**
 * An {@link OnDemandProject} aims at accessing to the resources of a
 * {@link TranslationProject} on demand, thus retrieving each
 * {@link TranslationMap} only when requested.
 * 
 * @author Matthieu VERGNE <matthieu.vergne@gmail.com>
 * 
 * @param <TMapID>
 * @param <TMap>
 */
public class OnDemandProject<TEntry extends TranslationEntry<? extends TranslationMetadata>, TMapID, TMap extends TranslationMap<TEntry>>
		implements TranslationProject<TEntry, TMapID, TMap> {

	private static final Writer<OnDemandProject<?, ?, ? extends TranslationMap<?>>> DEFAULT_SAVER = new Writer<OnDemandProject<?, ?, ? extends TranslationMap<?>>>() {

		@Override
		public void write(
				OnDemandProject<?, ?, ? extends TranslationMap<?>> project) {
			for (TranslationMap<?> map : project.modifiedMaps) {
				map.saveAll();
			}
		}
	};
	private final Set<TMapID> ids;
	private final MultiReader<TMapID, TMap> mapReader;
	private final Writer<? super OnDemandProject<TEntry, TMapID, TMap>> projectSaver;
	private final Map<TMapID, WeakReference<TMap>> cache = new HashMap<TMapID, WeakReference<TMap>>();
	private final Set<TMap> modifiedMaps = new HashSet<>();

	/**
	 * Instantiate an {@link OnDemandProject} for a given set of
	 * {@link TranslationMap}s. Only the IDs of the {@link TranslationMap}s are
	 * stored, the {@link TranslationMap}s themselves are retrieved on demand.
	 * The overall saving strategy is provided by a custom {@link Writer}.
	 * 
	 * @param ids
	 *            the {@link TranslationMap} IDs of this
	 *            {@link TranslationProject}
	 * @param mapReader
	 *            the {@link MultiReader} to use for {@link #getMap(Object)}
	 * @param projectSaver
	 *            the {@link Writer} to use for {@link #saveAll()}
	 */
	public OnDemandProject(Collection<TMapID> ids,
			MultiReader<TMapID, TMap> mapReader,
			Writer<? super OnDemandProject<TEntry, TMapID, TMap>> projectSaver) {
		this.ids = Collections.unmodifiableSet(new LinkedHashSet<>(ids));
		this.mapReader = mapReader;
		this.projectSaver = projectSaver;
	}

	/**
	 * Instantiate an {@link OnDemandProject} for a given set of
	 * {@link TranslationMap}s. Only the IDs of the {@link TranslationMap}s are
	 * stored, the {@link TranslationMap}s themselves are retrieved on demand.
	 * For saving the project, a naive strategy is used: each modified
	 * {@link TranslationMap} is saved separately. If you want a smarter
	 * strategy, use the most extended constructor.
	 * 
	 * @param ids
	 *            the {@link TranslationMap} IDs of this
	 *            {@link TranslationProject}
	 * @param mapReader
	 *            the {@link MultiReader} to use for {@link #getMap(Object)}
	 */
	public OnDemandProject(Collection<TMapID> ids,
			MultiReader<TMapID, TMap> reader) {
		this(ids, reader, DEFAULT_SAVER);
	}

	@Override
	public Iterator<TMapID> iterator() {
		return ids.iterator();
	}

	@Override
	public TMap getMap(TMapID id) {
		WeakReference<TMap> reference = cache.get(id);
		if (reference == null || reference.get() == null) {
			final TMap map = mapReader.read(id);
			TranslationListener translationListener = new TranslationListener() {

				@Override
				public void translationUpdated(String newTranslation) {
					modifiedMaps.add(map);
				}

				@Override
				public void translationStored() {
					// ignored
				}
			};
			FieldListener metadataListener = new FieldListener() {

				@Override
				public <T> void fieldUpdated(Field<T> field, T newValue) {
					modifiedMaps.add(map);
				}

				@Override
				public <T> void fieldStored(Field<T> field) {
					// ignored
				}
			};
			for (TranslationEntry<? extends TranslationMetadata> entry : map) {
				entry.addTranslationListener(translationListener);
				entry.getMetadata().addFieldListener(metadataListener);
			}
			cache.put(id, new WeakReference<TMap>(map));
			return map;
		} else {
			return reference.get();
		}
	}

	@Override
	public int size() {
		return ids.size();
	}

	@Override
	public void saveAll() {
		projectSaver.write(this);
		modifiedMaps.clear();
	}

	@Override
	public void resetAll() {
		for (TMap map : modifiedMaps) {
			map.resetAll();
		}
		modifiedMaps.clear();
	}

	private final Collection<MapNamer<TMapID>> mapNamers = new LinkedHashSet<>();

	@Override
	public Collection<MapNamer<TMapID>> getMapNamers() {
		return mapNamers;
	}

	public void addMapNamer(MapNamer<TMapID> namer) {
		mapNamers.add(namer);
	}

	private final Collection<Feature> features = new LinkedHashSet<>();

	@Override
	public Collection<Feature> getFeatures() {
		return features;
	}

	public void addFeature(Feature feature) {
		features.add(feature);
	}

	private final Collection<EntryFilter<TEntry>> filters = new HashSet<EntryFilter<TEntry>>();

	@Override
	public Collection<EntryFilter<TEntry>> getEntryFilters() {
		return filters;
	}

	public void addEntryFilter(EntryFilter<TEntry> filter) {
		filters.add(filter);
	}
}
