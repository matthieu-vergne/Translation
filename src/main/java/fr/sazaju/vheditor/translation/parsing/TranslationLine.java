package fr.sazaju.vheditor.translation.parsing;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class TranslationLine extends Suite {

	public TranslationLine() {
		super(new Atom("# TRANSLATION "), new Newline());
	}
}
