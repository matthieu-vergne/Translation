package fr.sazaju.vheditor.parsing.vh.map;

import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class ContentBlock extends Suite {

	public ContentBlock() {
		super(new Formula("(?:[\\r\\n]*(?:[^#\\r\\n]++#?+[^#\\r\\n]*+)?+)*"),
				new Newline());
	}

	public Formula getContentWithoutNewline() {
		return get(0);
	}
}
