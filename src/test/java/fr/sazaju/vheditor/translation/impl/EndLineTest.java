package fr.sazaju.vheditor.translation.impl;

import java.util.Arrays;

import org.junit.Test;

public class EndLineTest {

	@Test
	public void testSetGetContent() {
		EndLine end = new EndLine();
		end.setContent("# END STRING");
		for (String newline : Arrays.asList("\n", "\r", "\n\r", "\r\n")) {
			end.setContent("# END STRING" + newline);
		}
	}

}
