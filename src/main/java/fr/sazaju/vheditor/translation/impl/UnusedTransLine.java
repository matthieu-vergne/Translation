package fr.sazaju.vheditor.translation.impl;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Newline;

public class UnusedTransLine extends Suite {

	public UnusedTransLine() {
		super(new Atom("# UNUSED TRANSLATABLES"), new Newline());
	}
}
