package fr.sazaju.vheditor.parsing.vh.map;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class StartLine extends Suite {

	public StartLine() {
		super(new Atom("# TEXT STRING"), new Newline());
	}
}
