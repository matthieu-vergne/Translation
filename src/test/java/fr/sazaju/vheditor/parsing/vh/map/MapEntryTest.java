package fr.sazaju.vheditor.parsing.vh.map;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationEntryTest;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;

public class MapEntryTest extends TranslationEntryTest {

	private final File testFolder = new File("src/test/resources");
	private final FileFilter entryFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isFile() && file.getName().endsWith(".entry");
		}
	};

	@Override
	protected TranslationEntry createTranslationEntry() {
		try {
			File templateFile = new File(testFolder, "translation.entry");
			File entryFile = File.createTempFile("translation", ".entry");
			FileUtils.copyFile(templateFile, entryFile);
			MapEntry entry = new MapEntry();
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
			MapEntry entry = new MapEntry();
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

	@Test
	public void testTextualVersionMap() throws IOException {
		MapEntry entry = new MapEntry();
		for (File entryFile : testFolder.listFiles(entryFilter)) {
			String original = FileUtils.readFileToString(entryFile);
			entry.setContent(original);
			String reBuilt = entry.getContent();
			assertEquals(entryFile.getName(), original, reBuilt);
		}
	}

	@Test
	public void testTranslatedTag() throws IOException {
		MapEntry entry = new MapEntry();
		for (File entryFile : testFolder.listFiles(entryFilter)) {
			String original = FileUtils.readFileToString(entryFile);
			entry.setContent(original);
			entry.getMetadata().set(Field.MARKED_AS_TRANSLATED, true);
			assertTrue(entry.getMetadata().get(Field.MARKED_AS_TRANSLATED));
			entry.getMetadata().set(Field.MARKED_AS_TRANSLATED, false);
			assertFalse(entry.getMetadata().get(Field.MARKED_AS_TRANSLATED));
			entry.getMetadata().set(Field.MARKED_AS_TRANSLATED, true);
			assertTrue(entry.getMetadata().get(Field.MARKED_AS_TRANSLATED));
		}
	}
}
