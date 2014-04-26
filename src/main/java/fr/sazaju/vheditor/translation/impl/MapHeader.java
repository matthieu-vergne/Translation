package fr.sazaju.vheditor.translation.impl;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Newline;

public class MapHeader extends Suite {

	public MapHeader() {
		super(new Atom("# RPGMAKER TRANS PATCH FILE VERSION 2.0"),
				new Newline());
	}
}
