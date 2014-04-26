package fr.sazaju.vheditor.translation.impl;

import fr.vergne.parsing.layer.impl.Formula;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Newline;

public class ContentBlock extends Suite {

	public ContentBlock() {
		super(new Formula("(?:[\\r\\n]*(?:[^#\\r\\n]++#?+[^#\\r\\n]*+)?+)*"),
				new Newline());
	}

	public Formula getContentWithoutNewline() {
		return get(0);
	}
}
