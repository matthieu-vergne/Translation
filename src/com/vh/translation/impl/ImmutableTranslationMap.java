package com.vh.translation.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vh.translation.TranslationEntry;
import com.vh.translation.TranslationMap;
import com.vh.translation.util.LoggerConfiguration;

// TODO adapt parsing capabilities to mistakes in the files
public class ImmutableTranslationMap implements TranslationMap {

	private String RpgMakerTransPatchVersion = null;
	private final File baseFile;
	private final List<TranslationEntry> entries;
	public static final Logger logger = LoggerConfiguration.getSimpleLogger();

	public ImmutableTranslationMap(File mapFile) throws IOException {
		this.baseFile = mapFile;

		Pattern advicePattern = Pattern
				.compile("^# ADVICE : ([0-9]+) char limit(?: \\(([0-9]+) if face\\))?$");

		entries = new LinkedList<TranslationEntry>();
		FileReader fr = new FileReader(mapFile);
		BufferedReader reader = new BufferedReader(fr);
		String line;
		String content = "";
		SimpleTranslationEntry currentEntry = null;
		boolean inUnusedEntries = false;
		int lineCounter = 0;
		while ((line = reader.readLine()) != null) {
			lineCounter++;
			if (line.startsWith("# RPGMAKER TRANS PATCH FILE VERSION ")) {
				String[] split = line.split(" ");
				RpgMakerTransPatchVersion = split[split.length - 1];
				logger.info("RPG Maker Trans Patch version found: "
						+ RpgMakerTransPatchVersion);
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
					if (matcher.group(2) == null) {
						int limit = Integer.parseInt(matcher.group(1));
						currentEntry.setCharLimitWithoutFace(limit);
						currentEntry.setCharLimitWithFace(limit);
						logger.info("Char limit: " + limit);
					} else {
						int limit1 = Integer.parseInt(matcher.group(1));
						int limit2 = Integer.parseInt(matcher.group(2));
						currentEntry.setCharLimitWithoutFace(limit1);
						currentEntry.setCharLimitWithFace(limit2);
						logger.info("Char limits: " + limit2 + " with face, "
								+ limit1 + " without");
					}
				} else {
					throw new ParsingException(mapFile, lineCounter,
							"Unexpected format: " + line);
				}
			} else if (currentEntry != null && !line.startsWith("# ")) {
				content += line + "\n";
			} else if (line.equals("# TRANSLATION ")) {
				logger.info("Original content:" + contentDisplay(content));
				currentEntry.setOriginalContent(content.trim());
				content = "";
			} else if (line.equals("# END STRING")) {
				logger.info("Translated content:" + contentDisplay(content));
				currentEntry.setTranslationContent(content.trim());
				content = "";
				try {
					entries.add(new ImmutableTranslationEntry(currentEntry));
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
		}

		reader.close();
		fr.close();

		if (RpgMakerTransPatchVersion == null) {
			logger.warning("No RPG Maker Trans Patch found. Not blocking but it could be a problem.");
		} else {
			// version found, so no problem
		}
	}

	private String contentDisplay(String content) {
		return content.trim().isEmpty() ? " empty" : "\n" + content;
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

	@Override
	public Iterator<TranslationEntry> iterator() {
		return entries.iterator();
	}

	@Override
	public File getBaseFile() {
		return baseFile;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + entries.size() + " entries]";
	}
}
