package fr.sazaju.vheditor.translation.impl.backed;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Newline;

public class StartLine extends Suite {

	public StartLine() {
		super(new Atom("# TEXT STRING"), new Newline());
	}
}
