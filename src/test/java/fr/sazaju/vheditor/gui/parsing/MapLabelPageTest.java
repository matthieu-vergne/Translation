package fr.sazaju.vheditor.gui.parsing;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class MapLabelPageTest {

	private final File testFolder = new File("src/test/resources");

	@Test
	public void testSize() throws IOException {
		File file = new File(testFolder, "Map List.html");
		MapLabelPage page = new MapLabelPage();
		page.setContent(FileUtils.readFileToString(file, "UTF-8"));
		assertEquals(661, page.getTable().size());
	}

}
