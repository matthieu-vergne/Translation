package fr.sazaju.vheditor.translation.impl.backed;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.vheditor.translation.impl.backed.BackedTranslationMap.EmptyMapException;

public class BackedTranslationMapTest {

	@Test
	public void testReadWriteMap() throws IOException {
		File mapFolder = new File("VH/branches/working/");
		File[] listFiles = mapFolder.listFiles();
		int mapCounter = 0;
		int overflowCounter = 0;
		for (File originalFile : listFiles) {
			if (originalFile.isFile()) {
				mapCounter++;
				System.out.println("Testing " + originalFile.getName() + "...");
				File writtenFile = File.createTempFile(originalFile.getName(),
						".txt");

				String originalContent = FileUtils
						.readFileToString(originalFile);
				int numberEntries = originalContent.replaceAll("[ß]", "")
						.replaceAll("# TEXT STRING", "ß")
						.replaceAll("[^ß]", "").length();

				try {
					BackedTranslationMap map = new BackedTranslationMap(
							originalFile);
					assertEquals(numberEntries,
							map.sizeUsed() + map.sizeUnused());
					FileUtils.write(writtenFile, map.getContent());
				} catch (StackOverflowError e) {
					overflowCounter++;
					System.out.println("OVERFLOW " + overflowCounter);
					continue;
				} catch (EmptyMapException e) {
					// ignore empty maps
				}

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
		System.out.println("TOTAL OVERFLOW: " + overflowCounter + "/"
				+ mapCounter);
	}

}
