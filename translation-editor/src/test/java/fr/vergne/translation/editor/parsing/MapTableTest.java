package fr.vergne.translation.editor.parsing;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class MapTableTest {

	private final File testFolder = new File("src/test/resources");

	@Test
	public void testSize() throws IOException {
		File file = new File(testFolder, "mapTable.html");
		MapTable table = new MapTable();
		table.setContent(FileUtils.readFileToString(file));
		assertEquals(5, table.size());
	}

	@Test
	public void testIterator() throws IOException {
		File file = new File(testFolder, "mapTable.html");
		MapTable table = new MapTable();
		table.setContent(FileUtils.readFileToString(file));
		Iterator<MapRow> iterator = table.iterator();
		assertTrue(iterator.hasNext());
		iterator.next();
		assertTrue(iterator.hasNext());
		iterator.next();
		assertTrue(iterator.hasNext());
		iterator.next();
		assertTrue(iterator.hasNext());
		iterator.next();
		assertTrue(iterator.hasNext());
		iterator.next();
		assertFalse(iterator.hasNext());
		try {
			iterator.next();
			fail("No exception thrown.");
		} catch (NoSuchElementException e) {
		}
	}

}
