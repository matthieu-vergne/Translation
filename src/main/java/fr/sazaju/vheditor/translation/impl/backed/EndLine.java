package fr.sazaju.vheditor.translation.impl.backed;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Loop;
import fr.vergne.parsing.layer.impl.Loop.Generator;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Newline;

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
