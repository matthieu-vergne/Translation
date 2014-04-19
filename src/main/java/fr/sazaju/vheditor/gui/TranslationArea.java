package fr.sazaju.vheditor.gui;

import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.translation.impl.SimpleTranslationEntry;

@SuppressWarnings("serial")
public class TranslationArea extends JTextArea {

	private final SimpleTranslationEntry entry;

	public TranslationArea(SimpleTranslationEntry entry) {
		super(entry.getTranslatedVersion());
		this.entry = entry;
		setBorder(new EtchedBorder());
	}

	public void save() {
		entry.setTranslationContent(getText());
	}

	public void reset() {
		setText(entry.getTranslatedVersion());
	}
}
