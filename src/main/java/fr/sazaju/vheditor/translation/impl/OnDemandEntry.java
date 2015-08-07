package fr.sazaju.vheditor.translation.impl;

import java.util.Collection;
import java.util.HashSet;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMetadata;
import fr.sazaju.vheditor.util.Reader;
import fr.sazaju.vheditor.util.Writer;

/**
 * an {@link OnDemandEntry} retrieves the stored texts (original version and
 * translation) on demand, meaning that it does not directly store them. Only
 * the current translation is stored.
 * 
 * @author Sazaju HITOKAGE <sazaju@gmail.com>
 * 
 * @param <Metadata>
 */
public class OnDemandEntry<Metadata extends TranslationMetadata> implements
		TranslationEntry<Metadata> {

	private static final Writer<? super OnDemandEntry<? extends TranslationMetadata>> DEFAULT_SAVER = new Writer<OnDemandEntry<? extends TranslationMetadata>>() {

		@Override
		public void write(OnDemandEntry<? extends TranslationMetadata> entry) {
			entry.saveTranslation();
			entry.getMetadata().saveAll();
		}
	};
	private final Reader<? extends String> originalReader;
	private final Reader<? extends String> translationReader;
	private final Writer<? super String> translationSaver;
	private String currentTranslation;
	private final Metadata metadata;
	private final Writer<? super OnDemandEntry<Metadata>> entrySaver;
	private final Collection<TranslationListener> listeners = new HashSet<>();

	/**
	 * Instantiate an {@link OnDemandEntry} with custom data retrieval and
	 * saving strategies. This is the most open solution to adapt to many
	 * situations.
	 * 
	 * @param originalReader
	 *            the {@link Reader} to use for {@link #getOriginalContent()}
	 * @param translationReader
	 *            the {@link Reader} to use for {@link #getStoredTranslation()}
	 * @param translationSaver
	 *            the {@link Writer} to use for {@link #saveTranslation()}
	 * @param metadata
	 *            the {@link TranslationMetadata} of this
	 *            {@link TranslationEntry}
	 * @param entrySaver
	 *            the {@link Writer} to use for {@link #saveAll()}
	 */
	public OnDemandEntry(Reader<? extends String> originalReader,
			Reader<? extends String> translationReader,
			Writer<? super String> translationSaver, Metadata metadata,
			Writer<? super OnDemandEntry<Metadata>> entrySaver) {
		this.translationReader = translationReader;
		this.translationSaver = translationSaver;
		this.originalReader = originalReader;
		this.metadata = metadata;
		this.currentTranslation = translationReader.read();
		this.entrySaver = entrySaver;
	}

	/**
	 * Instantiate a {@link OnDemandEntry} with a naive saving strategy for
	 * {@link #saveAll()}: first save the translation with
	 * {@link #saveTranslation()}, then save the metadata with
	 * {@link TranslationMetadata#saveAll()}. If you want a smarter strategy,
	 * use the most extended constructor.
	 * 
	 * @param originalReader
	 *            the {@link Reader} to use for {@link #getOriginalContent()}
	 * @param translationReader
	 *            the {@link Reader} to use for {@link #getStoredTranslation()}
	 * @param translationSaver
	 *            the {@link Writer} to use for {@link #saveTranslation()}
	 * @param metadata
	 *            the {@link TranslationMetadata} of this
	 *            {@link TranslationEntry}
	 */
	public OnDemandEntry(Reader<? extends String> originalReader,
			Reader<? extends String> translationReader,
			Writer<? super String> translationSaver, Metadata metadata) {
		this(originalReader, translationReader, translationSaver, metadata,
				DEFAULT_SAVER);
	}

	@Override
	public String getOriginalContent() {
		return originalReader.read();
	}

	@Override
	public String getStoredTranslation() {
		return translationReader.read();
	}

	@Override
	public String getCurrentTranslation() {
		return currentTranslation;
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
		translationSaver.write(getCurrentTranslation());
	}

	@Override
	public void resetTranslation() {
		setCurrentTranslation(getStoredTranslation());
	}

	@Override
	public void saveAll() {
		entrySaver.write(this);
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

	@Override
	public String toString() {
		String original = format(getOriginalContent());
		String translation = format(getCurrentTranslation());
		return "\"" + original + "\" => \"" + translation + "\"";
	}

	private String format(String text) {
		String oneLineText = "";
		for (String line : text.split("\n")) {
			oneLineText += line.trim() + " ";
		}
		return oneLineText.trim();
	}
}
