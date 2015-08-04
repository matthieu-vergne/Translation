package fr.sazaju.vheditor.translation.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMap;

public class SimpleTranslationMap<Entry extends TranslationEntry<?>> implements
		TranslationMap<Entry> {

	public static interface EntryBuilder<Entry extends TranslationEntry<?>> {
		public Entry build(int index);
	}

	private final EntryBuilder<Entry> builder;
	private final int size;
	private final Map<Integer, Entry> entries = new HashMap<Integer, Entry>();

	public SimpleTranslationMap(EntryBuilder<Entry> builder, int size) {
		this.builder = builder;
		this.size = size;
	}

	@Override
	public Iterator<Entry> iterator() {
		return new Iterator<Entry>() {

			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < size;
			}

			@Override
			public Entry next() {
				Entry entry = retrieveEntry(index);
				index++;
				return entry;
			}

			@Override
			public void remove() {
				throw new RuntimeException("You cannot remove an entry.");
			}
		};
	}

	@Override
	public Entry getEntry(int index) {
		return retrieveEntry(index);
	}

	private Entry retrieveEntry(int index) {
		Entry entry = entries.get(index);
		if (entry == null) {
			entry = builder.build(index);
			entries.put(index, entry);
			return entry;
		} else {
			return entry;
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void saveAll() {
		// TODO use a custom saver (minimize overhead)
		for (Entry entry : this) {
			entry.saveAll();
		}
	}

	@Override
	public void resetAll() {
		for (Entry entry : this) {
			entry.resetAll();
		}
	}

}
