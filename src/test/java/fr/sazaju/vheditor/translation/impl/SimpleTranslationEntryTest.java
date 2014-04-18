package fr.sazaju.vheditor.translation.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class SimpleTranslationEntryTest {

	private final File testFolder = new File("test");
	private final FileFilter entryFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isFile() && file.getName().endsWith(".entry");
		}
	};

	@Test
	public void testTextualVersionMap() throws IOException {
		for (File entryFile : testFolder.listFiles(entryFilter)) {
			String original = FileUtils.readFileToString(entryFile);
			String reBuilt = new SimpleTranslationEntry(original)
					.getTextualVersion();
			assertEquals(entryFile.getName(), original, reBuilt);
		}
	}

}
