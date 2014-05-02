package fr.sazaju.vheditor.translation.parsing;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Newline;

public class UntranslatedLine extends Suite {

	public UntranslatedLine() {
		super(new Atom("# UNTRANSLATED"), new Newline());
	}
}
