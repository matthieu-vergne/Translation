package fr.sazaju.vheditor.translation.impl.backed;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class ContextLineTest {

	@Test
	public void testSetGetContent() {
		for (String newline : Arrays.asList("\n", "\r", "\n\r", "\r\n")) {
			ContextLine context = new ContextLine();
			context.setContent("# CONTEXT : Dialogue/Message/FaceUnknown"
					+ newline);
			assertEquals("Dialogue/Message/FaceUnknown", context.getContext()
					.getContent());
		}
	}

}
