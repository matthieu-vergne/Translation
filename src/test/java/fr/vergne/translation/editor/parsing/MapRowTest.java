package fr.vergne.translation.editor.parsing;

import static org.junit.Assert.*;

import org.junit.Test;

public class MapRowTest {

	@Test
	public void testAdmitNewlines() {
		MapRow mapRow = new MapRow();
		mapRow.setContent("<tr>\n<td>\n0001\n</td>\n<td>\n野盗の巣穴への道\n</td>\n<td>\nBandit's Cave -- \n</td>\n<td>\nRoad to Bandit's cave\n</td>\n<td>\n130210\n</td>\n</tr>");
	}

	@Test
	public void testExamples() {
		MapRow mapRow = new MapRow();
		mapRow.setContent("<tr><td><p>0001</p></td><td>野盗の巣穴への道<br></td><td>Bandit's Cave -- </td><td>Road to Bandit's cave <br></td><td>130210</td></tr>");
		assertEquals("0001", mapRow.getId());
		assertEquals("野盗の巣穴への道", mapRow.getJapaneseLabel());
		assertEquals("Bandit's Cave --", mapRow.getEnglishDescription());
		assertEquals("Road to Bandit's cave", mapRow.getEnglishLabel());
		assertEquals(130210, mapRow.getRevision());
	}

}
