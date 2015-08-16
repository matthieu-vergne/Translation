package fr.vergne.translation.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationMapTest;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.impl.PatternFileMap.PatternEntry;
import fr.vergne.translation.util.Switcher;
import fr.vergne.translation.util.impl.SmartStringSwitcher;

public class PatternFileMapTest extends TranslationMapTest<PatternEntry> {

	@Override
	protected TranslationMap<PatternEntry> createTranslationMap() {
		String mapContent = "";
		mapContent += "J=エントリー１|E=Entry 1|F1=1|F2=true|F3=V1\n";
		mapContent += "J=エントリー２|E=Entry 2|F1=2|F2=|F3=V2\n";
		mapContent += "J=エントリー３|E=Entry 3|F1=3|F2=false|F3=\n";
		File file = createTempMap(mapContent);

		String entryRegex = "(?<=\\n|^)[^\\n]+(?=\\n|$)";
		String originalRegex = "(?<=^J=)[^|]+(?=\\|)";
		String translationRegex = "(?<=\\|E=)[^|]+(?=\\|)";
		PatternFileMap map = new PatternFileMap(file, entryRegex,
				originalRegex, translationRegex);

		Field<Integer> field1 = new Field<Integer>("F1");
		String regex1 = "(?<=\\|F1=)[^|]*(?=\\||$)";
		Switcher<String, Integer> convertor1 = new SmartStringSwitcher<>(
				Integer.class);
		map.addFieldRegex(field1, regex1, convertor1, true);

		Field<Boolean> field2 = new Field<Boolean>("F2");
		String regex2 = "(?<=\\|F2=)[^|]*(?=\\||$)";
		Switcher<String, Boolean> convertor2 = new SmartStringSwitcher<>(
				Boolean.class);
		map.addFieldRegex(field2, regex2, convertor2, false);

		Field<String> field3 = new Field<String>("F3");
		String regex3 = "(?<=\\|F3=)[^|]*(?=\\||$)";
		map.addFieldRegex(field3, regex3, true);

		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		if (field.getName().equals("F1")) {
			if (currentValue == null) {
				return (T) (Integer) 0;
			} else {
				return (T) (Integer) (((Integer) currentValue) + 1);
			}
		} else if (field.getName().equals("F2")) {
			if (currentValue == null) {
				return (T) (Boolean) false;
			} else {
				return (T) (Boolean) (!((Boolean) currentValue));
			}
		} else if (field.getName().equals("F3")) {
			if (currentValue == null) {
				return (T) "test";
			} else {
				return (T) ((String) currentValue + "?");
			}
		} else {
			throw new RuntimeException("Unmanaged field: " + field);
		}
	}

	@Test
	public void testMapEntriesComplientWithEntryRegex() {
		String mapContent = "";
		mapContent += "J=エントリー１|E=Entry 1\n";
		mapContent += "J=エントリー２|E=Entry 2\n";
		mapContent += "J=エントリー３|E=Entry 3\n";
		File file = createTempMap(mapContent);

		String entryRegex = "(?<=\\n|^)[^\\n]+(?=\\n|$)";
		PatternFileMap map = new PatternFileMap(file, entryRegex, "J", "E");

		assertEquals(3, map.size());
	}

	@Test
	public void testMapEntriesComplientWithOriginalRegex() {
		String mapContent = "";
		mapContent += "J=エントリー１|E=Entry 1\n";
		mapContent += "J=エントリー２|E=Entry 2\n";
		mapContent += "J=エントリー３|E=Entry 3\n";
		File file = createTempMap(mapContent);

		String entryRegex = "(?<=\\n|^)[^\\n]+(?=\\n|$)";
		String originalRegex = "(?<=^J=)[^|]+(?=\\|)";
		PatternFileMap map = new PatternFileMap(file, entryRegex,
				originalRegex, "E");

		assertEquals("エントリー１", map.getEntry(0).getOriginalContent());
		assertEquals("エントリー２", map.getEntry(1).getOriginalContent());
		assertEquals("エントリー３", map.getEntry(2).getOriginalContent());
	}

	@Test
	public void testMapEntriesComplientWithTranslationRegex() {
		String mapContent = "";
		mapContent += "J=エントリー１|E=Entry 1\n";
		mapContent += "J=エントリー２|E=Entry 2\n";
		mapContent += "J=エントリー３|E=Entry 3\n";
		File file = createTempMap(mapContent);

		String entryRegex = "(?<=\\n|^)[^\\n]+(?=\\n|$)";
		String translationRegex = "(?<=\\|E=)[^|]+$";
		PatternFileMap map = new PatternFileMap(file, entryRegex, "J",
				translationRegex);

		assertEquals("Entry 1", map.getEntry(0).getCurrentTranslation());
		assertEquals("Entry 2", map.getEntry(1).getCurrentTranslation());
		assertEquals("Entry 3", map.getEntry(2).getCurrentTranslation());
	}

	@Test
	public void testMapEntriesComplientWithFieldRegex() {
		String mapContent = "";
		mapContent += "J=エントリー１|E=Entry 1|F1=1|F2=true|F3=V1\n";
		mapContent += "J=エントリー２|E=Entry 2|F1=2|F2=|F3=V2\n";
		mapContent += "J=エントリー３|E=Entry 3|F1=3|F2=false|F3=\n";
		File file = createTempMap(mapContent);

		String entryRegex = "(?<=\\n|^)[^\\n]+(?=\\n|$)";
		PatternFileMap map = new PatternFileMap(file, entryRegex, "J", "E");

		Field<Integer> field1 = new Field<Integer>("F1");
		String regex1 = "(?<=\\|F1=)[^|]*(?=\\||$)";
		Switcher<String, Integer> convertor1 = new SmartStringSwitcher<>(
				Integer.class, "");
		map.addFieldRegex(field1, regex1, convertor1, true);

		Field<Boolean> field2 = new Field<Boolean>("F2");
		String regex2 = "(?<=\\|F2=)[^|]*(?=\\||$)";
		Switcher<String, Boolean> convertor2 = new SmartStringSwitcher<>(
				Boolean.class, "");
		map.addFieldRegex(field2, regex2, convertor2, false);

		Field<String> field3 = new Field<String>("F3");
		String regex3 = "(?<=\\|F3=)[^|]*(?=\\||$)";
		map.addFieldRegex(field3, regex3, true);

		assertEquals((Integer) 1, map.getEntry(0).getMetadata().get(field1));
		assertEquals((Boolean) true, map.getEntry(0).getMetadata().get(field2));
		assertEquals((String) "V1", map.getEntry(0).getMetadata().get(field3));

		assertEquals((Integer) 2, map.getEntry(1).getMetadata().get(field1));
		assertEquals((Boolean) null, map.getEntry(1).getMetadata().get(field2));
		assertEquals((String) "V2", map.getEntry(1).getMetadata().get(field3));

		assertEquals((Integer) 3, map.getEntry(2).getMetadata().get(field1));
		assertEquals((Boolean) false, map.getEntry(2).getMetadata().get(field2));
		assertEquals((String) "", map.getEntry(2).getMetadata().get(field3));
	}

	private File createTempMap(String mapContent) {
		try {
			File file = File.createTempFile("test", ".map");
			FileUtils.write(file, mapContent);
			return file;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
