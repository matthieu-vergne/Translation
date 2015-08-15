package fr.vergne.translation.editor.parsing;

import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Suite;

public class MapLabelPage extends Suite {

	public MapLabelPage() {
		super(new Formula("[\\s\\S]*?"), new MapTable(), new Formula(
				"[\\s\\S]*?"));
	}

	public MapTable getTable() {
		return this.<MapTable> get(1);
	}
}
