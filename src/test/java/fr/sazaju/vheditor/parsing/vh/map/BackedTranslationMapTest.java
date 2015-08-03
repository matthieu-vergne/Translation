package fr.sazaju.vheditor.parsing.vh.map;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.vheditor.parsing.vh.map.BackedTranslationMap.EmptyMapException;

public class BackedTranslationMapTest {

	@Test
	public void testReadWriteMap() throws IOException {
		File mapFolder = new File("VH/branches/working/");
		File[] listFiles = mapFolder.listFiles();
		Arrays.sort(listFiles, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareToIgnoreCase(f2.getName());
			}
		});
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
		assertEquals("TOTAL OVERFLOW: " + overflowCounter + "/" + mapCounter,
				0, overflowCounter);
	}

}
