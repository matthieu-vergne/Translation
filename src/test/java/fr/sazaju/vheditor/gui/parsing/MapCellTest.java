package fr.sazaju.vheditor.gui.parsing;

import static org.junit.Assert.*;

import org.junit.Test;

public class MapCellTest {

	@Test
	public void testAdmitNewlines() {
		MapCell mapCell = new MapCell();
		mapCell.setContent("<td>\ncont\nent\n</td>");
	}

	@Test
	public void testAdmitJapanese() {
		MapCell mapCell = new MapCell();
		mapCell.setContent("<td>宿屋主人部屋</td>");
	}

	@Test
	public void testAdmitEmpty() {
		MapCell mapCell = new MapCell();
		mapCell.setContent("<td></td>");
	}

	@Test
	public void testRemoveLinks() {
		MapCell mapCell = new MapCell();
		mapCell.setContent("<td><a class=\"wiki_link\" title=\"MapID\" href=\"https://www.assembla.com/wiki/show/VH/MapID\">MapID</a></td>");
		assertEquals("MapID", mapCell.getCellContent());
	}

	@Test
	public void testRemoveNewLines() {
		MapCell mapCell = new MapCell();
		mapCell.setContent("<td>a<br>b</td>");
		assertEquals("a b", mapCell.getCellContent());
	}

	@Test
	public void testTrim() {
		MapCell mapCell = new MapCell();
		mapCell.setContent("<td>  a     </td>");
		assertEquals("a", mapCell.getCellContent());
		mapCell.setContent("<td>  <br/>  a <br>\t</td>");
		assertEquals("a", mapCell.getCellContent());
	}

}
