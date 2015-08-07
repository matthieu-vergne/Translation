package fr.sazaju.vheditor.translation.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import fr.sazaju.vheditor.parsing.vh.map.VHEntry;
import fr.sazaju.vheditor.parsing.vh.map.VHMap;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.translation.TranslationProject;
import fr.sazaju.vheditor.translation.TranslationProjectTest;
import fr.sazaju.vheditor.util.MultiReader;

public class MapFilesProjectTest extends TranslationProjectTest<File, VHMap> {

	private final File testDirectory = new File("src/test/resources/project");

	@Override
	protected TranslationProject<File, VHMap> createTranslationProject() {
		File file1 = new File(testDirectory, "Map1.txt");
		File file2 = new File(testDirectory, "Map2.txt");
		File file3 = new File(testDirectory, "Map3.txt");
		Collection<File> files = Arrays.asList(file1, file2, file3);
		MultiReader<File, VHMap> mapReader = new MultiReader<File, VHMap>() {

			@Override
			public VHMap read(File file) {
				try {
					return new VHMap(file);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		return new MapFilesProject<>(files, mapReader);
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

}
