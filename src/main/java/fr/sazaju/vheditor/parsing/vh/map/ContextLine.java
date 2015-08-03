package fr.sazaju.vheditor.parsing.vh.map;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class ContextLine extends Suite {

	public ContextLine() {
		super(new Atom("# CONTEXT : "), new Formula("[^\r\n]++"), new Newline());
	}

	public Formula getContext() {
		return get(1);
	}
}
