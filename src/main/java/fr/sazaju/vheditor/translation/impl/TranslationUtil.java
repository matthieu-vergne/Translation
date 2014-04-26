package fr.sazaju.vheditor.translation.impl;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import fr.sazaju.vheditor.gui.TranslationArea;
import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.vergne.logging.LoggerConfiguration;

// TODO use references at https://www.assembla.com/spaces/VH/wiki/Reference
// TODO find original content in translated versions
// TODO for a content between 「 and 」 or  （ and ） which lacks indentation, indent it with '　'
// TODO replace all "..." (3) by "…" (1)
// TODO identify all the "\." in the original version and place them in the translated version
// TODO generalize the previous points as a "formatting recovery" feature (take the formatting of the original version and apply/adapt it to the translated version) : identify contents, punctuation and '\.' parts, map the original and translated ones, use the formatting of the original on the translated, adapt if needed
// TODO wrap the sentences to reach at best the characters limits.
public class TranslationUtil {

	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	static {
		logger.setLevel(Level.OFF);
	}

	public static Object explicit(char character) {
		return character == '\n' ? "\\n" : character == '\r' ? "\\r"
				: character;
	}

	public static void fillPanel(TranslationMap map, JPanel panel) {
		panel.removeAll();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 0, 0, 0);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		Iterator<? extends TranslationEntry> iterator = map.iteratorUsed();
		fillEntries(panel, constraints, iterator, panel.getBackground());
		iterator = map.iteratorUnused();
		if (iterator.hasNext()) {
			panel.add(new JLabel("# UNUSED TRANSLATABLES"), constraints);
			constraints.gridy++;
			fillEntries(panel, constraints, iterator, Color.MAGENTA);
		} else {
			// no unused entries
		}
	}

	private static void fillEntries(JPanel panel,
			GridBagConstraints constraints,
			Iterator<? extends TranslationEntry> iterator, Color background) {
		while (iterator.hasNext()) {
			final TranslationEntry entry = iterator.next();
			{
				JLabel label = new JLabel("# TEXT STRING");
				label.setOpaque(true);
				label.setBackground(background);
				panel.add(label, constraints);
				constraints.gridy++;
				if (entry.isMarkedAsUntranslated()) {
					label = new JLabel("# UNTRANSLATED");
					label.setBackground(background);
					panel.add(label, constraints);
					constraints.gridy++;
				} else {
					// do not write it
				}
				label = new JLabel("# CONTEXT : " + entry.getContext());
				label.setBackground(background);
				label.setOpaque(true);
				panel.add(label, constraints);
				constraints.gridy++;
				if (entry.getCharLimit(false) != null
						&& entry.getCharLimit(true) != null) {
					label = new JLabel("# ADVICE : "
							+ entry.getCharLimit(false) + " char limit ("
							+ entry.getCharLimit(true) + " if face)");
					label.setOpaque(true);
					label.setBackground(background);
					panel.add(label, constraints);
					constraints.gridy++;
				} else if (entry.getCharLimit(false) != null
						&& entry.getCharLimit(true) == null) {
					label = new JLabel("# ADVICE : "
							+ entry.getCharLimit(false) + " char limit");
					label.setOpaque(true);
					label.setBackground(background);
					panel.add(label, constraints);
					constraints.gridy++;
				} else {
					// no advice
				}
				JTextArea original = new JTextArea(entry.getOriginalVersion());
				original.setEditable(false);
				panel.add(original, constraints);
				constraints.gridy++;
				label = new JLabel("# TRANSLATION ");
				label.setOpaque(true);
				label.setBackground(background);
				panel.add(label, constraints);
				constraints.gridy++;
				panel.add(new TranslationArea(entry), constraints);
				constraints.gridy++;
				label = new JLabel("# END STRING");
				label.setOpaque(true);
				label.setBackground(background);
				panel.add(label, constraints);
			}
			constraints.gridy++;
			if (iterator.hasNext()) {
				JLabel label = new JLabel(" ");
				label.setOpaque(true);
				label.setBackground(background);
				panel.add(label, constraints);
				constraints.gridy++;
			} else {
				// EOF
			}
		}
	}

	@SuppressWarnings("serial")
	public static class ParsingException extends IllegalStateException {

		private final File file;
		private final int line;
		private final String rawMessage;
		private final Exception originalException;

		public ParsingException(File file, int lineNumber, String message) {
			super(file.getName() + ", line " + lineNumber + ": " + message);
			this.file = file;
			this.line = lineNumber;
			this.rawMessage = message;
			this.originalException = null;
		}

		public ParsingException(File file, int lineNumber, Exception e) {
			super(file.getName() + ", line " + lineNumber, e);
			this.file = file;
			this.line = lineNumber;
			this.rawMessage = null;
			this.originalException = e;
		}

		public ParsingException(int lineNumber, String message) {
			super("Line " + lineNumber + ": " + message);
			this.file = null;
			this.line = lineNumber;
			this.rawMessage = message;
			this.originalException = null;
		}

		public ParsingException(int lineNumber, Exception e) {
			super("Line " + lineNumber, e);
			this.file = null;
			this.line = lineNumber;
			this.rawMessage = null;
			this.originalException = e;
		}

		public File getFile() {
			return file;
		}

		public int getLine() {
			return line;
		}

		public String getRawMessage() {
			return rawMessage;
		}

		public Exception getOriginalException() {
			return originalException;
		}
	}
}
