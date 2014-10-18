package fr.sazaju.vheditor.translation.parsing;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class MapEntryTest {

	private final File testFolder = new File(".");
	private final FileFilter entryFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isFile() && file.getName().endsWith(".entry");
		}
	};

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
	public void testUntranslatedTag() throws IOException {
		MapEntry entry = new MapEntry();
		for (File entryFile : testFolder.listFiles(entryFilter)) {
			String original = FileUtils.readFileToString(entryFile);
			entry.setContent(original);
			entry.setMarkedAsUntranslated(true);
			assertTrue(entry.isMarkedAsUntranslated());
			entry.setMarkedAsUntranslated(false);
			assertFalse(entry.isMarkedAsUntranslated());
			entry.setMarkedAsUntranslated(true);
			assertTrue(entry.isMarkedAsUntranslated());
		}
	}
}