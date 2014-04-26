package fr.sazaju.vheditor.translation.impl.backed;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Newline;

public class TranslationLine extends Suite {

	public TranslationLine() {
		super(new Atom("# TRANSLATION "), new Newline());
	}
}
