package fr.sazaju.vheditor.translation.impl.backed;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class AdviceLineTest {

	@Test
	public void testSetGetContent() {
		AdviceLine advice = new AdviceLine();
		for (String newline : Arrays.asList("\n", "\r", "\n\r", "\r\n")) {
			advice.setContent("# ADVICE : 49 char limit (35 if face)" + newline);
			assertEquals("49", advice.getGeneralLimit().getContent());
			assertTrue(advice.hasFaceLimit());
			assertEquals("35", advice.getFaceLimit().getContent());
			
			advice.setContent("# ADVICE : 31 char limit" + newline);
			assertEquals("31", advice.getGeneralLimit().getContent());
			assertFalse(advice.hasFaceLimit());
		}
	}

}
