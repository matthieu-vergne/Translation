package fr.sazaju.vheditor.translation.parsing;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationComment.Field;

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
			entry.getComment().set(Field.MARKED_AS_TRANSLATED, true);
			assertTrue(entry.getComment().get(Field.MARKED_AS_TRANSLATED));
			entry.getComment().set(Field.MARKED_AS_TRANSLATED, false);
			assertFalse(entry.getComment().get(Field.MARKED_AS_TRANSLATED));
			entry.getComment().set(Field.MARKED_AS_TRANSLATED, true);
			assertTrue(entry.getComment().get(Field.MARKED_AS_TRANSLATED));
		}
	}
}
