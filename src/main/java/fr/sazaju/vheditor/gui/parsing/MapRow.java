package fr.sazaju.vheditor.gui.parsing;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.Suite;

public class MapRow extends Suite {

	public MapRow() {
		super(new Atom("<tr>"), new NewlineLoop(), new Loop<MapCell>(
				new Loop.Generator<MapCell>() {

					@Override
					public MapCell generates() {
						return new MapCell();
					}
				}, 5), new Atom("</tr>"), new NewlineLoop());
	}

	public String getId() {
		return getCellLoop().get(0).getCellContent();
	}

	public String getJapaneseLabel() {
		return getCellLoop().get(1).getCellContent();
	}

	public String getEnglishDescription() {
		return getCellLoop().get(2).getCellContent();
	}

	public String getEnglishLabel() {
		return getCellLoop().get(3).getCellContent();
	}

	public int getRevision() {
		return Integer.parseInt(getCellLoop().get(4).getCellContent());
	}

	private Loop<MapCell> getCellLoop() {
		return this.<Loop<MapCell>> get(2);
	}
}
