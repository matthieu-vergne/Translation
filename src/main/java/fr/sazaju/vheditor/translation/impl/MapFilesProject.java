package fr.sazaju.vheditor.translation.impl;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationEntry.TranslationListener;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.TranslationMetadata;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.translation.TranslationMetadata.FieldListener;
import fr.sazaju.vheditor.translation.TranslationProject;
import fr.sazaju.vheditor.util.MultiReader;
import fr.sazaju.vheditor.util.Writer;

/**
 * A {@link MapFilesProject} is a {@link TranslationProject} where each
 * {@link TranslationMap} corresponds to a given {@link File}. In other words,
 * one can make a 1-to-1 mapping between such a {@link File} and a
 * {@link TranslationMap}. If more than one {@link File} store a single
 * {@link TranslationMap}, one {@link File} stores more than one
 * {@link TranslationMap}, or something else than a {@link File} is used to
 * build a {@link TranslationMap}, then a different implementation should be
 * used.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 */
public class MapFilesProject<TMap extends TranslationMap<? extends TranslationEntry<? extends TranslationMetadata>>>
		implements TranslationProject<File, TMap> {

	private static final Writer<MapFilesProject<? extends TranslationMap<?>>> DEFAULT_SAVER = new Writer<MapFilesProject<? extends TranslationMap<?>>>() {

		@Override
		public void write(MapFilesProject<? extends TranslationMap<?>> project) {
			for (TranslationMap<?> map : project.modifiedMaps) {
				map.saveAll();
			}
		}
	};
	private final Set<File> files;
	private final MultiReader<? super File, ? extends TMap> mapReader;
	private final Writer<? super MapFilesProject<TMap>> projectSaver;
	private final Map<File, WeakReference<TMap>> cache = new HashMap<>();
	private final Set<TMap> modifiedMaps = new HashSet<>();

	/**
	 * Instantiate a {@link MapFilesProject} to cover a given set of
	 * {@link File}s.
	 * 
	 * @param files
	 *            the set of {@link File}s covered by this
	 *            {@link MapFilesProject}
	 * @param mapReader
	 *            the {@link MultiReader} used for {@link #getMap(File)}
	 * @param projectSaver
	 *            the {@link Writer} to use for {@link #saveAll()}
	 */
	public MapFilesProject(Collection<File> files,
			MultiReader<? super File, ? extends TMap> mapReader,
			Writer<? super MapFilesProject<TMap>> projectSaver) {
		if (files == null || files.isEmpty()) {
			throw new IllegalArgumentException("No files provided: " + files);
		} else {
			this.files = Collections
					.unmodifiableSet(new LinkedHashSet<>(files));
			this.mapReader = mapReader;
			this.projectSaver = projectSaver;
		}
	}

	/**
	 * Instantiate a {@link MapFilesProject} to cover a given set of
	 * {@link File}s. The overall saving strategy is a naive one: each modified
	 * {@link TranslationMap} is saved separately. If you want a smarter
	 * strategy, use the most extended constructor.
	 * 
	 * @param files
	 *            the set of {@link File}s covered by this
	 *            {@link MapFilesProject}
	 * @param mapReader
	 *            the {@link MultiReader} used for {@link #getMap(File)}
	 */
	public MapFilesProject(Collection<File> files,
			MultiReader<? super File, ? extends TMap> mapReader) {
		this(files, mapReader, DEFAULT_SAVER);
	}

	@Override
	public Iterator<File> iterator() {
		return files.iterator();
	}

	@Override
	public TMap getMap(File file) {
		WeakReference<TMap> reference = cache.get(file);
		if (reference == null || reference.get() == null) {
			final TMap map = mapReader.read(file);
			TranslationListener translationListener = new TranslationListener() {

				@Override
				public void translationUpdated(String newTranslation) {
					modifiedMaps.add(map);
				}
			};
			FieldListener metadataListener = new FieldListener() {

				@Override
				public <T> void fieldUpdated(Field<T> field, T newValue) {
					modifiedMaps.add(map);
				}
			};
			for (TranslationEntry<? extends TranslationMetadata> entry : map) {
				entry.addTranslationListener(translationListener);
				entry.getMetadata().addFieldListener(metadataListener);
			}
			cache.put(file, new WeakReference<TMap>(map));
			return map;
		} else {
			return reference.get();
		}
	}

	@Override
	public int size() {
		return files.size();
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

	private final Map<File, String> mapNames = new HashMap<>();

	@Override
	public String getMapName(File id) {
		return mapNames.get(id);
	}

	public void setMapName(File id, String name) {
		mapNames.put(id, name);
	}
	
	
	private final Collection<JButton> features = new LinkedHashSet<>();
	
	@Override
	public Collection<JButton> getExtraFeatures() {
		return features;
	}
	
	public void addExtraFeature(JButton feature) {
		features.add(feature);
	}
}
