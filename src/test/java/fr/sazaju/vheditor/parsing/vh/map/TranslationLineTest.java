package fr.sazaju.vheditor.parsing.vh.map;

import java.util.Arrays;

import org.junit.Test;

public class TranslationLineTest {

	@Test
	public void testSetGetContent() {
		TranslationLine trans = new TranslationLine();
		for (String newline : Arrays.asList("\n", "\r", "\n\r", "\r\n")) {
			trans.setContent("# TRANSLATION " + newline);
		}
	}

}
