package fr.sazaju.vheditor.translation.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationMap;

public class TranslationUtilTest {

	@Test
	public void testReadWriteMap() throws IOException {
		File mapFolder = new File("VH/branches/working/");
		File[] listFiles = mapFolder.listFiles();
		for (File originalFile : listFiles) {
			// TODO consider all *.txt files
			if (originalFile.isFile()
					&& originalFile.getName().startsWith("Map")) {
				System.out.println("Testing " + originalFile.getName() + "...");
				File writtenFile = File.createTempFile(originalFile.getName(),
						".txt");

				TranslationMap map = TranslationUtil.readMap(originalFile);
				TranslationUtil.writeMap((SimpleTranslationMap) map,
						writtenFile);

				String readContent = FileUtils.readFileToString(originalFile);
				String writtenContent = FileUtils.readFileToString(writtenFile);
				assertEquals(originalFile.getName(), readContent,
						writtenContent);

				writtenFile.deleteOnExit();
				writtenFile.delete();
			} else {
				continue;
			}
		}
	}

}
