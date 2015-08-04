package fr.sazaju.vheditor.parsing.vh.map;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;

import fr.sazaju.vheditor.parsing.vh.map.MapEntry.MapSaver;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.vergne.parsing.layer.standard.GreedyMode;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Suite;

public class BackedTranslationMap extends Suite implements
		TranslationMap<MapEntry> {

	private static final String ENCODING = "UTF-8";
	private File file;

	public BackedTranslationMap(File file) throws IOException {
		this();
		setBaseFile(file);
	}

	public BackedTranslationMap(Saver saver) {
		super(new MapHeader(), new EntryLoop(saver), new Option<Suite>(
				new Suite(new UnusedTransLine(), new EntryLoop(saver)),
				GreedyMode.POSSESSIVE));
		this.saver = saver;
	}

	private final Saver saver;

	private static class Saver implements MapSaver {

		BackedTranslationMap map = null;

		@Override
		public void saveMapFile() {
			try {
				FileUtils.write(map.getBaseFile(), map.getContent(), ENCODING);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public BackedTranslationMap() {
		this(new Saver());
		saver.map = this;
	}

	@SuppressWarnings("serial")
	public static class EmptyMapException extends IllegalStateException {
		public EmptyMapException(File file) {
			super("The map provided (" + file + ") is empty.");
		}
	}

	public Iterator<? extends MapEntry> iteratorUsed() {
		EntryLoop usedEntries = get(1);
		return usedEntries.iterator();
	}

	public Iterator<? extends MapEntry> iteratorUnused() {
		Option<Suite> option = get(2);
		if (option.isPresent()) {
			EntryLoop unusedEntries = option.getOption().get(1);
			return unusedEntries.iterator();
		} else {
			return new LinkedList<MapEntry>().iterator();
		}
	}

	/**
	 * This method set the base file of this {@link BackedTranslationMap}. If it
	 * is different from the previous base file, the file is parsed and the
	 * content of this map updated correspondingly. Otherwise, nothing change.
	 * If you want to reset changes made on this map, use the
	 * {@link #resetAll()} method.
	 * 
	 * @param file
	 *            the map file to parse
	 * @throws IOException
	 */
	public void setBaseFile(File file) throws IOException {
		if (file.equals(this.file)) {
			// do not reparse it
		} else {
			String content = FileUtils.readFileToString(file, ENCODING);
			if (content.trim().isEmpty()) {
				throw new EmptyMapException(file);
			} else {
				setContent(content);
				this.file = file;
			}
		}
	}

	public File getBaseFile() {
		return file;
	}

	public MapEntry getUsedEntry(int index) {
		EntryLoop usedEntries = get(1);
		return usedEntries.get(index);
	}

	public MapEntry getUnusedEntry(int index) {
		Option<Suite> option = get(2);
		if (option.isPresent()) {
			EntryLoop unusedEntries = option.getOption().get(1);
			return unusedEntries.get(index);
		} else {
			throw new NoSuchElementException(
					"This map does not have unused entries.");
		}
	}

	public int sizeUsed() {
		EntryLoop usedEntries = get(1);
		return usedEntries.size();
	}

	public int sizeUnused() {
		Option<Suite> option = get(2);
		if (option.isPresent()) {
			EntryLoop unusedEntries = option.getOption().get(1);
			return unusedEntries.size();
		} else {
			return 0;
		}
	}

	@Override
	public Iterator<MapEntry> iterator() {
		return new Iterator<MapEntry>() {
			private final Iterator<? extends MapEntry> iterator = iteratorUsed();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public MapEntry next() {
				return iterator.next();
			}

			@Override
			public void remove() {
				iterator.remove();
			}

		};
	}

	@Override
	public MapEntry getEntry(int index) {
		return getUsedEntry(index);
	}

	@Override
	public int size() {
		return sizeUsed();
	}

	@Override
	public void saveAll() {
		for (MapEntry entry : this) {
			entry.pseudoSaveAll();
		}
		saver.saveMapFile();
	}

	@Override
	public void resetAll() {
		for (MapEntry entry : this) {
			entry.resetAll();
		}
	}
}
