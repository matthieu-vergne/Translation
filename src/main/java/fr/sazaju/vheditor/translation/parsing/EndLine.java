package fr.sazaju.vheditor.translation.parsing;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.Loop.Generator;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class EndLine extends Suite {

	public EndLine() {
		super(new Atom("# END STRING"), new Loop<Newline>(
				new Generator<Newline>() {

					@Override
					public Newline generates() {
						return new Newline();
					}
				}, 0,2));
	}
}
