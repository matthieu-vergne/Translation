package fr.sazaju.vheditor.translation.impl;

import java.util.Collection;
import java.util.HashSet;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMetadata;

public class SimpleTranslationEntry<Metadata extends TranslationMetadata>
		implements TranslationEntry<Metadata> {

	private final String original;
	private final TranslationReader reader;
	private final TranslationWriter writer;
	private String currentTranslation = null;
	private final Metadata metadata;
	private final Collection<TranslationListener> listeners = new HashSet<>();

	public SimpleTranslationEntry(String original, TranslationReader reader,
			TranslationWriter writer, Metadata metadata) {
		this.reader = reader;
		this.writer = writer;
		this.original = original;
		this.metadata = metadata;
	}

	@Override
	public String getOriginalContent() {
		return original;
	}

	@Override
	public String getStoredTranslation() {
		return reader.read();
	}

	@Override
	public String getCurrentTranslation() {
		if (currentTranslation != null) {
			return currentTranslation;
		} else {
			return getStoredTranslation();
		}
	}

	@Override
	public void setCurrentTranslation(String translation) {
		if (translation == null) {
			throw new IllegalArgumentException("No translation provided");
		} else {
			this.currentTranslation = translation;
			for (TranslationListener listener : listeners) {
				listener.translationUpdated(translation);
			}
		}
	}

	@Override
	public void saveTranslation() {
		writer.write(getCurrentTranslation());
	}

	@Override
	public void resetTranslation() {
		setCurrentTranslation(getStoredTranslation());
	}

	@Override
	public void saveAll() {
		// TODO use a custom saver (minimize overhead)
		saveTranslation();
		getMetadata().saveAll();
	}

	@Override
	public void resetAll() {
		resetTranslation();
		getMetadata().resetAll();
	}

	@Override
	public Metadata getMetadata() {
		return metadata;
	}

	@Override
	public void addTranslationListener(TranslationListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeTranslationListener(TranslationListener listener) {
		listeners.remove(listener);
	}

	/**
	 * A {@link TranslationReader} aims at retrieving the stored translation of
	 * a given {@link TranslationEntry} in order to feed the
	 * {@link SimpleTranslationEntry#getStoredTranslation()} method.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 */
	public static interface TranslationReader {
		public String read();
	}

	/**
	 * A {@link TranslationWriter} aims at replacing the stored translation of a
	 * {@link TranslationEntry} by a new one, providing a way to execute the
	 * {@link SimpleTranslationEntry#saveTranslation()} method.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 */
	public static interface TranslationWriter {
		public void write(String translation);
	}
}
