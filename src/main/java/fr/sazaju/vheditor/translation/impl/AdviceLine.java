package fr.sazaju.vheditor.translation.impl;

import java.util.NoSuchElementException;

import fr.vergne.parsing.layer.impl.Atom;
import fr.vergne.parsing.layer.impl.Formula;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Newline;
import fr.vergne.parsing.layer.impl.base.Option;

public class AdviceLine extends Suite {

	public AdviceLine() {
		/*
		 * some maps have mispelling that we have to preserve, thus we use
		 * formulas rather than atoms.
		 */
		super(new Formula("# ADVICE : \\??+"), new Formula("[0-9]++"),
				new Formula(" char limi?+t"), new Option<Suite>(new Suite(
						new Atom(" ("), new Formula("[0-9]++"), new Atom(
								" if face)"))), new Newline());
		// super(new Atom("# ADVICE : "), new IntNumber(),
		// new Atom(" char limit"), new Option<Suite>(new Suite(new Atom(
		// " ("), new IntNumber(), new Atom(" if face)"))),
		// new Newline());
	}

	public Formula getGeneralLimit() {
		return get(1);
	}

	public boolean hasFaceLimit() {
		Option<Suite> option = get(3);
		return option.isPresent();
	}

	public Formula getFaceLimit() {
		Option<Suite> option = get(3);
		if (option.isPresent()) {
			return option.getOption().get(1);
		} else {
			throw new NoSuchElementException();
		}
	}
}
