package com.vh.translation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vh.translation.impl.SimpleTranslationEntry;
import com.vh.translation.impl.SimpleTranslationMap;
import com.vh.util.LoggerConfiguration;

// TODO for a content between 「 and 」 or  （ and ） which lacks indentation, indent it with '　'
// TODO replace all "..." (3) by "…" (1)
// TODO identify all the "\." in the original version and place them in the translated version
// TODO generalize the previous points as a "formatting recovery" feature (take the formatting of the original version and apply/adapt it to the translated version) : identify contents, punctuation and '\.' parts, map the original and translated ones, use the formatting of the original on the translated, adapt if needed
// TODO wrap the sentences to reach at best the characters limits.
public class TranslationUtil {

	public static final Logger logger = LoggerConfiguration.getSimpleLogger();

	public static TranslationMap readMap(File mapFile) throws IOException {
		SimpleTranslationMap map = new SimpleTranslationMap();
		map.setBaseFile(mapFile);

		Pattern advicePattern = Pattern
				.compile("^# ADVICE : ([0-9]+) char limit(?: \\(([0-9]+) if face\\))?$");

		BufferedReader reader = new BufferedReader(new FileReader(mapFile));
		String line;
		String content = "";
		SimpleTranslationEntry currentEntry = null;
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
				currentEntry = new SimpleTranslationEntry();
				currentEntry.setUnused(inUnusedEntries);
				logger.info("Building new entry...");
			} else if (line.equals("# UNTRANSLATED")) {
				currentEntry.setMarkedAsUntranslated(true);
				logger.info("Marked as untranslated entry");
			} else if (line.startsWith("# CONTEXT : ")) {
				String context = line.substring("# CONTEXT : ".length());
				currentEntry.setContext(context);
				logger.info("Context: " + context);
			} else if (line.startsWith("# ADVICE : ")) {
				Matcher matcher = advicePattern.matcher(line);
				if (matcher.find()) {
					int limit = Integer.parseInt(matcher.group(1));
					currentEntry.setCharLimitWithoutFace(limit);
					logger.info("Char limit: " + limit);
					if (matcher.group(2) != null) {
						limit = Integer.parseInt(matcher.group(2));
						currentEntry.setCharLimitWithFace(limit);
						logger.info("Char limit with face: " + limit);
					} else {
						// no char limit with face
					}
				} else {
					throw new ParsingException(mapFile, lineCounter,
							"Unexpected format: " + line);
				}
			} else if (currentEntry != null && !line.startsWith("#")) {
				logger.info("Retrieving content...");
				reader.reset();
				StringBuilder sb = new StringBuilder();
				boolean endReached = false;
				boolean afterNewline = false;
				do {
					int codePoint = reader.read();
					char character = (char) codePoint;
					if (afterNewline && character == '#') {
						reader.reset();
						endReached = true;
						logger.info("X #");
					} else {
						sb.appendCodePoint(codePoint);
						reader.mark(markSize);
						logger.info("- char: " + explicit(character));
					}
					afterNewline = System.lineSeparator().contains(
							"" + character);
				} while (!endReached);
				content = sb.toString();
				content = content.substring(0, content.length()
						- System.lineSeparator().length());
				logger.info("Content retrieved: "
						+ (content.trim().isEmpty() ? " empty" : System
								.lineSeparator()
								+ content
								+ System.lineSeparator()));
			} else if (line.equals("# TRANSLATION ")) {
				currentEntry.setOriginalContent(content);
				content = "";
				logger.info("Content retrieved used for original");
			} else if (line.equals("# END STRING")) {
				currentEntry.setTranslationContent(content);
				content = "";
				logger.info("Content retrieved used for translation");
				try {
					map.getEntries().add(currentEntry);
				} catch (NullPointerException e) {
					throw new ParsingException(mapFile, lineCounter, e);
				}
				currentEntry = null;
				logger.info("Entry built");
			} else if (currentEntry == null && line.trim().isEmpty()) {
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

	private static Object explicit(char character) {
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
			writer.println("# TEXT STRING");
			if (entry.isMarkedAsUntranslated()) {
				writer.println("# UNTRANSLATED");
			} else {
				// do not write it
			}
			writer.println("# CONTEXT : " + entry.getContext());
			if (entry.getCharLimit(false) != null
					&& entry.getCharLimit(true) != null) {
				writer.println("# ADVICE : " + entry.getCharLimit(false)
						+ " char limit (" + entry.getCharLimit(true)
						+ " if face)");
			} else if (entry.getCharLimit(false) != null
					&& entry.getCharLimit(true) == null) {
				writer.println("# ADVICE : " + entry.getCharLimit(false)
						+ " char limit");
			} else {
				// no advice
			}
			writer.println(entry.getOriginalVersion());
			writer.println("# TRANSLATION ");
			writer.println(entry.getTranslatedVersion());
			writer.println("# END STRING");
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
		public ParsingException(File file, int lineNumber, String message) {
			super(file.getName() + ", line " + lineNumber + ": " + message);
		}

		public ParsingException(File file, int lineNumber, Exception e) {
			super(file.getName() + ", line " + lineNumber, e);
		}
	}
}