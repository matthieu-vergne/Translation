package fr.sazaju.vheditor.parsing.vh.map;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationMetadata.Field;

public class MapEntryTest {

	private final File testFolder = new File("src/test/resources");
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
