package fr.sazaju.vheditor.parsing.vh.map;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;

import fr.sazaju.vheditor.parsing.vh.map.VHEntry.MapSaver;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.util.EntryFilter;
import fr.vergne.parsing.layer.standard.GreedyMode;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Suite;

public class VHMap extends Suite implements TranslationMap<VHEntry> {

	private static final String ENCODING = "UTF-8";
	private File file;
	private final Collection<EntryFilter<VHEntry>> filters;

	public VHMap(Saver saver) {
		super(new MapHeader(), new EntryLoop(saver), new Option<Suite>(
				new Suite(new UnusedTransLine(), new EntryLoop(saver)),
				GreedyMode.POSSESSIVE));
		this.saver = saver;
		this.filters = new LinkedList<>();
		this.filters.add(new EntryFilter<VHEntry>() {

			@Override
			public String getName() {
				return VHEntry.MARKED_AS_UNTRANSLATED.getName();
			}

			@Override
			public String getDescription() {
				return "Search for entries marked with #UNTRANSLATED.";
			}

			@Override
			public boolean isRelevant(VHEntry entry) {
				return entry.getMetadata().get(VHEntry.MARKED_AS_UNTRANSLATED);
			}

		});
	}

	public VHMap(File file) throws IOException {
		this();
		setBaseFile(file);
	}

	private final Saver saver;

	private static class Saver implements MapSaver {

		VHMap map = null;

		@Override
		public void saveMapFile() {
			try {
				FileUtils.write(map.getBaseFile(), map.getContent(), ENCODING);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public VHMap() {
		this(new Saver());
		saver.map = this;
	}

	@SuppressWarnings("serial")
	public static class EmptyMapException extends IllegalStateException {
		public EmptyMapException(File file) {
			super("The map provided (" + file + ") is empty.");
		}
	}

	public Iterator<? extends VHEntry> iteratorUsed() {
		EntryLoop usedEntries = get(1);
		return usedEntries.iterator();
	}

	public Iterator<? extends VHEntry> iteratorUnused() {
		Option<Suite> option = get(2);
		if (option.isPresent()) {
			EntryLoop unusedEntries = option.getOption().get(1);
			return unusedEntries.iterator();
		} else {
			return new LinkedList<VHEntry>().iterator();
		}
	}

	/**
	 * This method set the base file of this {@link VHMap}. If it is different
	 * from the previous base file, the file is parsed and the content of this
	 * map updated correspondingly. Otherwise, nothing change. If you want to
	 * reset changes made on this map, use the {@link #resetAll()} method.
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

	public VHEntry getUsedEntry(int index) {
		EntryLoop usedEntries = get(1);
		return usedEntries.get(index);
	}

	public VHEntry getUnusedEntry(int index) {
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
	public Iterator<VHEntry> iterator() {
		return new Iterator<VHEntry>() {
			private final Iterator<? extends VHEntry> iterator = iteratorUsed();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public VHEntry next() {
				return iterator.next();
			}

			@Override
			public void remove() {
				iterator.remove();
			}

		};
	}

	@Override
	public VHEntry getEntry(int index) {
		return getUsedEntry(index);
	}

	@Override
	public int size() {
		return sizeUsed();
	}

	@Override
	public void saveAll() {
		for (VHEntry entry : this) {
			entry.pseudoSaveAll();
		}
		saver.saveMapFile();
	}

	@Override
	public void resetAll() {
		for (VHEntry entry : this) {
			entry.resetAll();
		}
	}

	@Override
	public Collection<EntryFilter<VHEntry>> getEntryFilters() {
		return filters;
	}
}
