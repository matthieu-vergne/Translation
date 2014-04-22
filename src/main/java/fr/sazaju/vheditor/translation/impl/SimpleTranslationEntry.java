package fr.sazaju.vheditor.translation.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.output.ByteArrayOutputStream;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.impl.TranslationUtil.ParsingException;
import fr.vergne.logging.LoggerConfiguration;

public class SimpleTranslationEntry implements TranslationEntry {

	private boolean isMarkedAsUntranslated = false;
	private String context = null;
	private Integer charLimitWithFace = null;
	private Integer charLimitWithoutFace = null;
	private String originalContent = null;
	private String translationContent = null;
	private boolean isUnused = false;
	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	static {
		logger.setLevel(Level.OFF);
	}

	public SimpleTranslationEntry() {
	}

	public SimpleTranslationEntry(String textualVersion) throws IOException {
		setTextualVersion(textualVersion);
	}

	public void setTextualVersion(String textualVersion) throws IOException {
		logger.info("Parsing entry...");
		Pattern advicePattern = Pattern
				.compile("^# ADVICE : ([0-9]+) char limit(?: \\(([0-9]+) if face\\))?$");

		SimpleTranslationEntry newEntry = new SimpleTranslationEntry();
		BufferedReader reader = new BufferedReader(new StringReader(
				textualVersion));
		String line;
		String content = "";
		int markSize = 100;
		int lineCounter = 0;
		while ((line = reader.readLine()) != null) {
			try {
				lineCounter++;
				if (line.equals("# TEXT STRING")) {
					// ignore it
				} else if (line.equals("# UNTRANSLATED")) {
					newEntry.setMarkedAsUntranslated(true);
					logger.info("Marked as untranslated entry");
				} else if (line.startsWith("# CONTEXT : ")) {
					String context = line.substring("# CONTEXT : ".length());
					newEntry.setContext(context);
					logger.info("Context: " + context);
				} else if (line.startsWith("# ADVICE : ")) {
					Matcher matcher = advicePattern.matcher(line);
					if (matcher.find()) {
						int limit = Integer.parseInt(matcher.group(1));
						newEntry.setCharLimitWithoutFace(limit);
						logger.info("Char limit: " + limit);
						if (matcher.group(2) != null) {
							limit = Integer.parseInt(matcher.group(2));
							newEntry.setCharLimitWithFace(limit);
							logger.info("Char limit with face: " + limit);
						} else {
							// no char limit with face
						}
					} else {
						throw new ParsingException(lineCounter,
								"Unexpected format: " + line);
					}
				} else if (!line.startsWith("#")) {
					logger.info("Retrieving content...");
					reader.reset();
					StringBuilder sb = new StringBuilder();
					boolean endReached = false;
					boolean afterNewline = false;
					try {
						do {
							int codePoint = reader.read();
							char character = (char) codePoint;
							if (afterNewline && character == '#') {
								reader.reset();
								endReached = true;
								logger.info("X #");
							} else {
								logger.info("- char: "
										+ TranslationUtil.explicit(character));
								sb.appendCodePoint(codePoint);
								reader.mark(markSize);
							}
							lineCounter += character == '\n' ? 1 : 0;
							afterNewline = System.lineSeparator().contains(
									"" + character);
						} while (!endReached);
						lineCounter--;
						content = sb.toString();
						content = content.substring(0, content.length()
								- System.lineSeparator().length());
						logger.info("Content retrieved: "
								+ (content.trim().isEmpty() ? " empty" : System
										.lineSeparator()
										+ content
										+ System.lineSeparator()));
					} catch (Exception e) {
						throw new ParsingException(lineCounter, e);
					}
				} else if (line.equals("# TRANSLATION ")) {
					newEntry.setOriginalContent(content);
					content = "";
					logger.info("Content retrieved used for original");
				} else if (line.equals("# END STRING")) {
					newEntry.setTranslationContent(content);
					content = "";
					logger.info("Content retrieved used for translation");
				} else {
					throw new ParsingException(lineCounter,
							"Unrecognized line: " + line);
				}
				reader.mark(markSize);
			} catch (Exception e) {
				throw new ParsingException(lineCounter, e);
			}
		}

		reader.close();

		setValuesFrom(newEntry);
		logger.info("Entry parsed.");
	}

	@Override
	public String getTextualVersion() {
		OutputStream out = new ByteArrayOutputStream();
		PrintStream writer = new PrintStream(out);

		writer.println("# TEXT STRING");
		if (isMarkedAsUntranslated()) {
			writer.println("# UNTRANSLATED");
		} else {
			// do not write it
		}
		writer.println("# CONTEXT : " + getContext());
		if (getCharLimit(false) != null && getCharLimit(true) != null) {
			writer.println("# ADVICE : " + getCharLimit(false)
					+ " char limit (" + getCharLimit(true) + " if face)");
		} else if (getCharLimit(false) != null && getCharLimit(true) == null) {
			writer.println("# ADVICE : " + getCharLimit(false) + " char limit");
		} else {
			// no advice
		}
		writer.println(getOriginalVersion());
		writer.println("# TRANSLATION ");
		writer.println(getTranslatedVersion());
		writer.print("# END STRING");

		writer.close();
		return out.toString();
	}

	public void setValuesFrom(SimpleTranslationEntry newEntry) {
		isMarkedAsUntranslated = newEntry.isMarkedAsUntranslated;
		context = newEntry.context;
		charLimitWithFace = newEntry.charLimitWithFace;
		charLimitWithoutFace = newEntry.charLimitWithoutFace;
		originalContent = newEntry.originalContent;
		translationContent = newEntry.translationContent;
		isUnused = newEntry.isUnused;
	}

	public void setMarkedAsUntranslated(boolean isMarkedAsUntranslated) {
		this.isMarkedAsUntranslated = isMarkedAsUntranslated;
	}

	@Override
	public boolean isMarkedAsUntranslated() {
		return isMarkedAsUntranslated;
	}

	@Override
	public boolean isActuallyTranslated() {
		String original = getOriginalVersion();
		String translated = getTranslatedVersion();
		return original == null && translated == null || original != null
				&& translated != null
				&& original.isEmpty() == translated.isEmpty();
	}

	public void setContext(String context) {
		this.context = context;
	}

	@Override
	public String getContext() {
		return context;
	}

	public void setCharLimitWithFace(int charLimitWithFace) {
		this.charLimitWithFace = charLimitWithFace;
	}

	public void setCharLimitWithoutFace(int charLimitWithoutFace) {
		this.charLimitWithoutFace = charLimitWithoutFace;
	}

	@Override
	public Integer getCharLimit(boolean isFacePresent) {
		return isFacePresent ? charLimitWithFace : charLimitWithoutFace;
	}

	public void setOriginalContent(String originalContent) {
		this.originalContent = originalContent;
	}

	@Override
	public String getOriginalVersion() {
		return originalContent;
	}

	public void setTranslationContent(String translationContent) {
		this.translationContent = translationContent;
	}

	@Override
	public String getTranslatedVersion() {
		return translationContent;
	}

	public void setUnused(boolean isUnused) {
		this.isUnused = isUnused;
	}

	@Override
	public boolean isUnused() {
		return isUnused;
	}

}
