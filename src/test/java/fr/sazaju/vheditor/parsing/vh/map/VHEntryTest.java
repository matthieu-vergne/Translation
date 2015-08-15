package fr.sazaju.vheditor.parsing.vh.map;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.vheditor.parsing.vh.map.VHEntry.MapSaver;
import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationEntryTest;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.impl.OnDemandMetadata;

public class VHEntryTest extends TranslationEntryTest<OnDemandMetadata> {

	private final File testFolder = new File("src/test/resources");
	private final FileFilter entryFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isFile() && file.getName().endsWith(".entry");
		}
	};

	@Override
	protected TranslationEntry<OnDemandMetadata> createTranslationEntry() {
		try {
			File templateFile = new File(testFolder, "translation.entry");
			final File entryFile = File.createTempFile("translation", ".entry");
			FileUtils.copyFile(templateFile, entryFile);
			final VHEntry[] entryContainer = { null };
			MapSaver saver = new MapSaver() {

				@Override
				public void saveMapFile() {
					try {
						FileUtils.write(entryFile,
								entryContainer[0].getContent());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			};
			VHEntry entry = new VHEntry(saver);
			entryContainer[0] = entry;
			entry.setContent(FileUtils.readFileToString(entryFile));
			return entry;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected String getInitialStoredTranslation() {
		try {
			File entryFile = new File(testFolder, "translation.entry");
			VHEntry entry = new VHEntry(null);
			entry.setContent(FileUtils.readFileToString(entryFile));
			return entry.getStoredTranslation();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected String createNewTranslation(String currentTranslation) {
		return currentTranslation + ".";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		if (field == VHEntry.MARKED_AS_UNTRANSLATED) {
			return (T) (Boolean) !((Boolean) currentValue);
		} else {
			throw new RuntimeException("The field " + field
					+ " is not supposed to be editable");
		}
	}

	@Test
	public void testTextualVersionMap() throws IOException {
		VHEntry entry = new VHEntry(null);
		for (File entryFile : testFolder.listFiles(entryFilter)) {
			String original = FileUtils.readFileToString(entryFile);
			entry.setContent(original);
			String reBuilt = entry.getContent();
			assertEquals(entryFile.getName(), original, reBuilt);
		}
	}

	@Test
	public void testTranslatedTag() throws IOException {
		VHEntry entry = new VHEntry(null);
		for (File entryFile : testFolder.listFiles(entryFilter)) {
			String original = FileUtils.readFileToString(entryFile);
			entry.setContent(original);
			entry.getMetadata().set(VHEntry.MARKED_AS_UNTRANSLATED, true);
			assertTrue(entry.getMetadata().get(VHEntry.MARKED_AS_UNTRANSLATED));
			entry.getMetadata().set(VHEntry.MARKED_AS_UNTRANSLATED, false);
			assertFalse(entry.getMetadata().get(VHEntry.MARKED_AS_UNTRANSLATED));
			entry.getMetadata().set(VHEntry.MARKED_AS_UNTRANSLATED, true);
			assertTrue(entry.getMetadata().get(VHEntry.MARKED_AS_UNTRANSLATED));
		}
	}
}
