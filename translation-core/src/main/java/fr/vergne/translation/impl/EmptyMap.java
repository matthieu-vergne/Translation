package fr.vergne.translation.impl;

import java.util.Collections;
import java.util.Iterator;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMap;

public class EmptyMap<Entry extends TranslationEntry<?>> implements
		TranslationMap<Entry> {

	@Override
	public Iterator<Entry> iterator() {
		return Collections.<Entry> emptyList().iterator();
	}

	@Override
	public Entry getEntry(int index) {
		throw new RuntimeException(
				"This map is empty, no entry should be requested.");
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

}
