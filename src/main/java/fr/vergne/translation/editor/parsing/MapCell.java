package fr.vergne.translation.editor.parsing;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Suite;

public class MapCell extends Suite {

	public MapCell() {
		super(new Atom("<td>"), new Formula("[\\s\\S]*?"), new Atom("</td>"),
				new NewlineLoop());
	}

	public String getCellContent() {
		String content = get(1).getContent();
		content = content.replaceAll("[\r\n]", " ");
		content = content.replaceAll("<br/?>", " ");
		content = content.replaceAll(" +", " ");
		content = content.replaceAll("<[^>]+>", "");
		return content.trim();
	}
}
