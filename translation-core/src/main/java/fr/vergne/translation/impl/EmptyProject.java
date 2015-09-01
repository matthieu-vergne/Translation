package fr.vergne.translation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationProject;
import fr.vergne.translation.util.EntryFilter;
import fr.vergne.translation.util.Feature;
import fr.vergne.translation.util.MapNamer;

public class EmptyProject<Entry extends TranslationEntry<?>, MapID, Map extends TranslationMap<Entry>>
		implements TranslationProject<Entry, MapID, Map> {

	@Override
	public Iterator<MapID> iterator() {
		return Collections.<MapID> emptyList().iterator();
	}

	@Override
	public Map getMap(MapID id) {
		throw new RuntimeException("This project is empty, there is no map "
				+ id);
	}

	@Override
	public Collection<MapNamer<MapID>> getMapNamers() {
		return Collections.emptyList();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public void saveAll() {
		// nothing to save
	}

	@Override
	public void resetAll() {
		// nothing to reset
	}

	@Override
	public Collection<Feature> getFeatures() {
		return Collections.emptyList();
	}

	@Override
	public Collection<EntryFilter<Entry>> getEntryFilters() {
		return Collections.emptyList();
	}
}
