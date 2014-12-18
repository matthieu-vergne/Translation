package fr.sazaju.vheditor.gui.parsing;

import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Suite;

public class MapTitlePage extends Suite {

	public MapTitlePage() {
		super(new Formula("[\\s\\S]*?"), new MapTable(), new Formula(
				"[\\s\\S]*?"));
	}

	public MapTable getTable() {
		return this.<MapTable> get(1);
	}
}
