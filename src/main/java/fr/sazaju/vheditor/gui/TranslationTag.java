package fr.sazaju.vheditor.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.sazaju.vheditor.translation.TranslationEntry;

@SuppressWarnings("serial")
public class TranslationTag extends JPanel {

	private final TranslationEntry entry;
	private final boolean isMarked;

	public TranslationTag(final TranslationEntry entry) {
		this.entry = entry;
		this.isMarked = entry.isMarkedAsUntranslated();

		final JLabel tag = new JLabel() {
			@Override
			public String getText() {
				return entry.isMarkedAsUntranslated() ? "# UNTRANSLATED"
						: "<html><s># UNTRANSLATED</s></html>";
			}
		};

		JButton toggleButton = new JButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				entry.setMarkedAsUntranslated(!entry.isMarkedAsUntranslated());
				/*
				 * Set a different text than the current value to force the
				 * generation of the update event.
				 */
				tag.setText("" + entry.isMarkedAsUntranslated());
			}
		}) {
			@Override
			public String getText() {
				return entry.isMarkedAsUntranslated() ? "-" : "+";
			}
		};

		setLayout(new FlowLayout(FlowLayout.LEADING));
		add(tag);
		add(toggleButton);
	}

	public boolean isModified() {
		return entry.isMarkedAsUntranslated() != isMarked;
	}
}
