package com.vh.translation.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vh.translation.TranslationEntry;
import com.vh.translation.TranslationMap;
import com.vh.util.LoggerConfiguration;

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

	public static TranslationMap readMap(File mapFile) throws IOException {
		SimpleTranslationMap map = new SimpleTranslationMap();
		map.setBaseFile(mapFile);

		BufferedReader reader = new BufferedReader(new FileReader(mapFile));
		String line;
		boolean inUnusedEntries = false;
		int lineCounter = 0;
		int markSize = 100;
		while ((line = reader.readLine()) != null) {
			lineCounter++;
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
					if (!isClosing && sb.toString().endsWith("# END STRING")) {
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

		reader.close();

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
