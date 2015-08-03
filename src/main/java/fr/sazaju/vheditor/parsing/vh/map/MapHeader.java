package fr.sazaju.vheditor.parsing.vh.map;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class MapHeader extends Suite {

	public MapHeader() {
		super(new Atom("# RPGMAKER TRANS PATCH FILE VERSION 2.0"),
				new Newline());
	}
}
