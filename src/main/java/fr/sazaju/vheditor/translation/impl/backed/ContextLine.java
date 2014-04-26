package fr.sazaju.vheditor.translation.impl.backed;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Formula;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Newline;

public class ContextLine extends Suite {

	public ContextLine() {
		super(new Atom("# CONTEXT : "), new Formula("[^\r\n]++"), new Newline());
	}

	public Formula getContext() {
		return get(1);
	}
}
