package fr.vergne.translation.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;

import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.TranslationProject;
import fr.vergne.translation.TranslationProjectTest;
import fr.vergne.translation.impl.PatternFileMap.PatternEntry;
import fr.vergne.translation.util.MultiReader;
import fr.vergne.translation.util.Switcher;
import fr.vergne.translation.util.impl.SmartStringSwitcher;

public class MapFilesProjectTest extends
		TranslationProjectTest<PatternEntry, File, PatternFileMap> {

	private final File testDirectory = new File("src/test/resources/project");

	@Override
	protected TranslationProject<PatternEntry, File, PatternFileMap> createTranslationProject() {
		Collection<File> files = new LinkedList<>();
		try {
			for (String map : new String[] { "Map1.txt", "Map2.txt", "Map3.txt" }) {
				File original = new File(testDirectory, map);
				File copy = File.createTempFile("test", map);
				FileUtils.copyFile(original, copy);
				files.add(copy);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final String entryRegex = "# TEXT STRING\\n.*?\\n# END STRING";
		String textLineRegex = "(|[^#\\n][^\\n]*)";
		String textRegex = textLineRegex + "(\\n" + textLineRegex + ")*";
		final String originalRegex = "(?=\\n)" + textRegex
				+ "(?=\\n# TRANSLATION )";
		final String translationRegex = "(?<=# TRANSLATION \\n)" + textRegex
				+ "(?=\\n)";

		final Field<Integer> field1 = new Field<>("Limit (no face)");
		final String regex1 = "(?<=# ADVICE : )\\d+(?=\\D)";
		final Switcher<String, Integer> convertor1 = new SmartStringSwitcher<>(
				Integer.class);

		final Field<Integer> field2 = new Field<>("Limit (face)");
		final String regex2 = "(?<=# ADVICE : \\d{1,4} char limit \\()\\d+(?=\\D)";
		final Switcher<String, Integer> convertor2 = new SmartStringSwitcher<>(
				Integer.class);
		MultiReader<File, PatternFileMap> mapReader = new MultiReader<File, PatternFileMap>() {

			@Override
			public PatternFileMap read(File file) {
				PatternFileMap map = new PatternFileMap(file, entryRegex,
						originalRegex, translationRegex);
				map.addFieldRegex(field1, regex1, convertor1, true);
				map.addFieldRegex(field2, regex2, convertor2, false);
				return map;
			}
		};
		return new MapFilesProject<>(files, mapReader);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		if (field.getName().equals("Limit (no face)")) {
			return (T) (Integer) (((Integer) currentValue) + 1);
		} else {
			throw new RuntimeException("The field " + field
					+ " is not supposed to be editable");
		}
	}

}
