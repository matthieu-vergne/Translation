package fr.sazaju.vheditor.parsing.vh;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.vheditor.parsing.vh.map.VHEntry;
import fr.sazaju.vheditor.parsing.vh.map.VHMap;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.translation.TranslationProject;
import fr.sazaju.vheditor.translation.TranslationProjectTest;

public class VHProjectTest extends TranslationProjectTest<File, VHMap> {

	@Override
	protected TranslationProject<File, VHMap> createTranslationProject() {
		File tempDirectory;
		try {
			tempDirectory = File.createTempFile("vhTest", "");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		tempDirectory.delete();
		tempDirectory.mkdir();

		File templateDirectory = new File("VH/branches/working");
		FileFilter fileFilter;
		if (templateDirectory.list().length > 50) {
			fileFilter = new FileFilter() {

				@Override
				public boolean accept(File file) {
					/*
					 * Restrict to a reduced set of small files + the biggest
					 * file, otherwise it takes an eternity to test.
					 */
					return file.length() < 1000
							|| file.getName().equals(
									"RPG_RT_COMMONEVENTDATA.txt");
				}
			};
		} else {
			fileFilter = new FileFilter() {

				@Override
				public boolean accept(File file) {
					/*
					 * The map files are already reduced, so use all of them.
					 */
					return true;
				}
			};
		}

		try {
			FileUtils.copyDirectory(templateDirectory, tempDirectory,
					fileFilter);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new VHProject(tempDirectory);
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
	public void testAllVHFilesAreRetrieved() {
		File vhDirectory = new File("VH/branches/working");
		VHProject project = new VHProject(vhDirectory);
		int simpleMapCounter = 0;
		List<String> specialMaps = new LinkedList<>();
		specialMaps.add("RPG_RT_ATTRIBUTEDATA.txt");
		specialMaps.add("RPG_RT_COMMONEVENTDATA.txt");
		specialMaps.add("RPG_RT_CONDITIONDATA.txt");
		specialMaps.add("RPG_RT_HERODATA.txt");
		specialMaps.add("RPG_RT_ITEMDATA.txt");
		specialMaps.add("RPG_RT_MONSTERDATA.txt");
		specialMaps.add("RPG_RT_MONSTERPARTIES.txt");
		specialMaps.add("RPG_RT_SKILLDATA.txt");
		specialMaps.add("RPG_RT_STRINGDATA.txt");
		for (File file : project) {
			if (file.getName().matches("^Map[0-9]+\\.txt$")) {
				simpleMapCounter++;
			} else if (specialMaps.contains(file.getName())) {
				specialMaps.remove(file.getName());
			} else {
				fail("Consider a file which is not a map: " + file);
			}
		}
		assertTrue("only " + simpleMapCounter + " simple maps retrieved",
				simpleMapCounter > 600);
		assertTrue("some special maps are not retrieved: " + specialMaps,
				specialMaps.isEmpty());
	}
}
