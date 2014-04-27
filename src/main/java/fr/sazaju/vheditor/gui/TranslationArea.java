package fr.sazaju.vheditor.gui;

import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.translation.TranslationEntry;

@SuppressWarnings("serial")
public class TranslationArea extends JTextArea {

	private final TranslationEntry entry;

	public TranslationArea(TranslationEntry entry) {
		super(entry.getTranslatedVersion());
		this.entry = entry;
		setBorder(new EtchedBorder());
	}

	public void save() {
		entry.setTranslatedVersion(getText());
	}

	public void reset() {
		setText(entry.getTranslatedVersion());
	}

	public boolean isModified() {
		return !getText().equals(entry.getTranslatedVersion());
	}
}
