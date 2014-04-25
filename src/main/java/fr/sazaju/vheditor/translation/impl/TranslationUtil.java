package fr.sazaju.vheditor.translation.impl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
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

// TODO find original content in translated versions
// TODO for a content between 「 and 」 or  （ and ） which lacks indentation, indent it with '　'
// TODO replace all "..." (3) by "…" (1)
// TODO identify all the "\." in the original version and place them in the translated version
// TODO generalize the previous points as a "formatting recovery" feature (take the formatting of the original version and apply/adapt it to the translated version) : identify contents, punctuation and '\.' parts, map the original and translated ones, use the formatting of the original on the translated, adapt if needed
// TODO wrap the sentences to reach at best the characters limits.
// TODO use references at https://www.assembla.com/spaces/VH/wiki/Reference
public class TranslationUtil {

	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	static {
		logger.setLevel(Level.OFF);
	}

	public static TranslationMap readMap(File mapFile) throws IOException {
		SimpleTranslationMap map = new SimpleTranslationMap();
		map.setBaseFile(mapFile);

		BufferedReader reader = new BufferedReader(new FileReader(mapFile));
		try {
			String line;
			boolean inUnusedEntries = false;
			int lineCounter = 0;
			int markSize = 100;
			while ((line = reader.readLine()) != null) {
				lineCounter++;
				// FIXME retrieve newline characters
				if (line.startsWith("# RPGMAKER TRANS PATCH FILE VERSION ")) {
					String[] split = line.split(" ");
					map.setRpgMakerTransPatchVersion(split[split.length - 1]);
					logger.info("RPG Maker Trans Patch version found: "
							+ map.getRpgMakerTransPatchVersion());
				} else if (line.equals("# UNUSED TRANSLATABLES")) {
					inUnusedEntries = true;
					logger.warning("Remaining entries are unused");
				} else if (line.equals("# TEXT STRING")) {
					logger.info("Retrieving entry content...");
					reader.reset();
					StringBuilder sb = new StringBuilder();
					boolean endReached = false;
					boolean afterNewline = false;
					boolean isClosing = false;
					lineCounter--;
					int subLineCounter = 1;
					do {
						int codePoint = reader.read();
						char character = (char) codePoint;
						if (!isClosing
								&& sb.toString().endsWith("# END STRING")) {
							isClosing = true;
							logger.finer("- CLOSING!");
						} else if (isClosing && afterNewline) {
							reader.reset();
							endReached = true;
							logger.finer("- FINISHED!");
						} else {
							if (isClosing) {
								logger.finer("- pass: " + explicit(character));
							} else {
								sb.appendCodePoint(codePoint);
								logger.finer("- write: " + explicit(character));
							}
						}
						reader.mark(markSize);
						subLineCounter += character == '\n' ? 1 : 0;
						afterNewline = System.lineSeparator().contains(
								"" + character);
					} while (!endReached);
					subLineCounter -= 2;
					logger.info("Building new entry...");
					SimpleTranslationEntry currentEntry;
					try {
						currentEntry = new SimpleTranslationEntry(sb.toString());
					} catch (ParsingException e) {
						throw new ParsingException(mapFile, lineCounter
								+ e.getLine(), e);
					}
					currentEntry.setUnused(inUnusedEntries);
					try {
						map.getEntries().add(currentEntry);
					} catch (NullPointerException e) {
						throw new ParsingException(mapFile, lineCounter, e);
					}
					lineCounter += subLineCounter;
					logger.info("Entry built.");
				} else if (line.trim().isEmpty()) {
					// empty line between entries, just ignore
				} else {
					throw new ParsingException(mapFile, lineCounter,
							" Unrecognised line: " + line);
				}
				reader.mark(markSize);
			}
		} finally {
			reader.close();
		}

		if (map.getRpgMakerTransPatchVersion() == null) {
			logger.warning("No RPG Maker Trans Patch found. Not blocking but it could be a problem.");
		} else {
			// version found, so no problem
		}

		return map;
	}

	public static Object explicit(char character) {
		return character == '\n' ? "\\n" : character == '\r' ? "\\r"
				: character;
	}

	// TODO generalize to TranslationMap (which does not know patch version)
	public static void writeMap(SimpleTranslationMap map, File mapFile)
			throws IOException {
		PrintStream writer = new PrintStream(mapFile);
		String version = map.getRpgMakerTransPatchVersion();
		if (version != null) {
			// FIXME use original newline character
			writer.println("# RPGMAKER TRANS PATCH FILE VERSION " + version);
		} else {
			// no version to write
		}
		boolean unusedSection = false;
		Iterator<TranslationEntry> iterator = map.iterator();
		while (iterator.hasNext()) {
			TranslationEntry entry = iterator.next();
			if (!unusedSection && entry.isUnused()) {
				writer.println("# UNUSED TRANSLATABLES");
				unusedSection = true;
			} else {
				// not the start of the unused section
			}
			writer.println(entry.getTextualVersion());
			if (iterator.hasNext()) {
				writer.println("");
			} else {
				// EOF
			}
		}
		writer.close();
	}

	public static String map2html(SimpleTranslationMap map) {
		StringBuilder builder = new StringBuilder();
		String version = map.getRpgMakerTransPatchVersion();
		if (version != null) {
			builder.append("# RPGMAKER TRANS PATCH FILE VERSION " + version);
			builder.append("<br/>");
		} else {
			// no version to write
		}
		boolean unusedSection = false;
		Iterator<TranslationEntry> iterator = map.iterator();
		int entryId = 0;
		while (iterator.hasNext()) {
			TranslationEntry entry = iterator.next();
			if (!unusedSection && entry.isUnused()) {
				builder.append("# UNUSED TRANSLATABLES");
				builder.append("<br/>");
				unusedSection = true;
			} else {
				// not the start of the unused section
			}
			{
				builder.append("<a name='" + entryId + "'/>");
				builder.append("# TEXT STRING");
				builder.append("<br/>");
				if (entry.isMarkedAsUntranslated()) {
					builder.append("# UNTRANSLATED");
					builder.append("<br/>");
				} else {
					// do not write it
				}
				builder.append("# CONTEXT : " + entry.getContext());
				builder.append("<br/>");
				if (entry.getCharLimit(false) != null
						&& entry.getCharLimit(true) != null) {
					builder.append("# ADVICE : " + entry.getCharLimit(false)
							+ " char limit (" + entry.getCharLimit(true)
							+ " if face)");
					builder.append("<br/>");
				} else if (entry.getCharLimit(false) != null
						&& entry.getCharLimit(true) == null) {
					builder.append("# ADVICE : " + entry.getCharLimit(false)
							+ " char limit");
					builder.append("<br/>");
				} else {
					// no advice
				}
				builder.append("<pre>");
				builder.append(entry.getOriginalVersion());
				builder.append("</pre>");
				builder.append("# TRANSLATION ");
				builder.append("<br/>");
				builder.append("<textarea rows='4' style='resize:none'>");
				builder.append(entry.getTranslatedVersion());
				builder.append("</textarea>");
				builder.append("<br/>");
				builder.append("# END STRING");
			}
			builder.append("<br/>");
			if (iterator.hasNext()) {
				builder.append("<br/>");
			} else {
				// EOF
			}
			entryId++;
		}
		return builder.toString();
	}

	public static void fillPanel(SimpleTranslationMap map, JPanel panel) {
		panel.removeAll();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 0, 0, 0);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		String version = map.getRpgMakerTransPatchVersion();
		if (version != null) {
			panel.add(new JLabel("# RPGMAKER TRANS PATCH FILE VERSION "
					+ version), constraints);
			constraints.gridy++;
		} else {
			// no version to write
		}
		boolean unusedSection = false;
		Iterator<TranslationEntry> iterator = map.iterator();
		while (iterator.hasNext()) {
			final SimpleTranslationEntry entry = (SimpleTranslationEntry) iterator
					.next();
			if (!unusedSection && entry.isUnused()) {
				panel.add(new JLabel("# UNUSED TRANSLATABLES"), constraints);
				constraints.gridy++;
				unusedSection = true;
			} else {
				// not the start of the unused section
			}
			{
				panel.add(new JLabel("# TEXT STRING"), constraints);
				constraints.gridy++;
				if (entry.isMarkedAsUntranslated()) {
					panel.add(new JLabel("# UNTRANSLATED"), constraints);
					constraints.gridy++;
				} else {
					// do not write it
				}
				panel.add(new JLabel("# CONTEXT : " + entry.getContext()),
						constraints);
				constraints.gridy++;
				if (entry.getCharLimit(false) != null
						&& entry.getCharLimit(true) != null) {
					panel.add(
							new JLabel("# ADVICE : "
									+ entry.getCharLimit(false)
									+ " char limit ("
									+ entry.getCharLimit(true) + " if face)"),
							constraints);
					constraints.gridy++;
				} else if (entry.getCharLimit(false) != null
						&& entry.getCharLimit(true) == null) {
					panel.add(
							new JLabel("# ADVICE : "
									+ entry.getCharLimit(false) + " char limit"),
							constraints);
					constraints.gridy++;
				} else {
					// no advice
				}
				JTextArea original = new JTextArea(entry.getOriginalVersion());
				original.setEditable(false);
				panel.add(original, constraints);
				constraints.gridy++;
				panel.add(new JLabel("# TRANSLATION "), constraints);
				constraints.gridy++;
				panel.add(new TranslationArea(entry), constraints);
				constraints.gridy++;
				panel.add(new JLabel("# END STRING"), constraints);
			}
			constraints.gridy++;
			if (iterator.hasNext()) {
				panel.add(new JLabel(" "), constraints);
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
