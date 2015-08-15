package fr.sazaju.vheditor.parsing.vh.map;

import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;

import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.impl.OnDemandMetadata;
import fr.vergne.translation.util.Reader;
import fr.vergne.translation.util.Writer;

public class VHEntry extends Suite implements
		TranslationEntry<OnDemandMetadata> {

	public static final Field<Boolean> MARKED_AS_UNTRANSLATED = new Field<Boolean>(
			"Untranslated tag");
	public static final Field<Integer> CHAR_LIMIT_FACE = new Field<Integer>(
			"Char limit (face)");
	public static final Field<Integer> CHAR_LIMIT_NO_FACE = new Field<Integer>(
			"Char limit (no face)");
	public static final Field<String> CONTEXT = new Field<String>("Context");
	private final OnDemandMetadata metadata;
	private String translation = null;
	private final Collection<TranslationListener> listeners = new HashSet<>();;
	private final MapSaver saver;

	public VHEntry(final MapSaver saver) {
		super(new StartLine(), new Option<UntranslatedLine>(
				new UntranslatedLine()), new ContextLine(),
				new Option<AdviceLine>(new AdviceLine()), new ContentBlock(),
				new TranslationLine(), new ContentBlock(), new EndLine());
		this.saver = saver;
		this.metadata = new OnDemandMetadata();
		this.metadata.configureField(MARKED_AS_UNTRANSLATED,
				new Reader<Boolean>() {

					@Override
					public Boolean read() {
						Option<UntranslatedLine> option = get(1);
						return option.isPresent();
					}
				}, new Writer<Boolean>() {

					@Override
					public void write(Boolean isMarkedAsTranslated) {
						applyUntranslatedTag(isMarkedAsTranslated);
						saver.saveMapFile();
					}

				});
		this.metadata.configureField(CONTEXT, new Reader<String>() {

			@Override
			public String read() {
				ContextLine context = get(2);
				return context.getContext().getContent();
			}
		});
		this.metadata.configureField(CHAR_LIMIT_FACE, new Reader<Integer>() {

			@Override
			public Integer read() {
				Option<AdviceLine> option = get(3);
				if (option.isPresent()) {
					AdviceLine advice = option.getOption();
					try {
						return Integer.parseInt(advice.getFaceLimit()
								.getContent());
					} catch (NoSuchElementException e) {
						return null;
					}
				} else {
					return null;
				}
			}
		});
		this.metadata.configureField(CHAR_LIMIT_NO_FACE, new Reader<Integer>() {

			@Override
			public Integer read() {
				Option<AdviceLine> option = get(3);
				if (option.isPresent()) {
					AdviceLine advice = option.getOption();
					try {
						return Integer.parseInt(advice.getGeneralLimit()
								.getContent());
					} catch (NoSuchElementException e) {
						return null;
					}
				} else {
					return null;
				}
			}
		});
	}

	@Override
	protected void setInternalContent(String content) {
		super.setInternalContent(content);
		translation = null;
	}

	@Override
	public String getOriginalContent() {
		ContentBlock block = get(4);
		return block.getContentWithoutNewline().getContent();
	}

	@Override
	public String getStoredTranslation() {
		ContentBlock block = get(6);
		return block.getContentWithoutNewline().getContent();
	}

	@Override
	public String getCurrentTranslation() {
		if (translation == null) {
			return getStoredTranslation();
		} else {
			return translation;
		}
	}

	@Override
	public void setCurrentTranslation(String translation) {
		if (translation == null) {
			throw new IllegalArgumentException("Null translation provided");
		} else {
			this.translation = translation;
			for (TranslationListener listener : listeners) {
				listener.translationUpdated(translation);
			}
		}
	}

	@Override
	public void saveTranslation() {
		applyTranslation();
		saver.saveMapFile();
	}

	private void applyTranslation() {
		ContentBlock block = get(6);
		block.getContentWithoutNewline().setContent(getCurrentTranslation());
	}

	@Override
	public void resetTranslation() {
		translation = null;
		for (TranslationListener listener : listeners) {
			listener.translationUpdated(getCurrentTranslation());
		}
	}

	public void pseudoSaveAll() {
		applyTranslation();
		applyUntranslatedTag(getMetadata().get(MARKED_AS_UNTRANSLATED));
	}

	@Override
	public void saveAll() {
		pseudoSaveAll();
		saver.saveMapFile();
	}

	@Override
	public void resetAll() {
		resetTranslation();
		metadata.resetAll();
	}

	@Override
	public OnDemandMetadata getMetadata() {
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

	public static interface MapSaver {
		public void saveMapFile();
	}

	private void applyUntranslatedTag(Boolean isMarkedAsTranslated) {
		Option<UntranslatedLine> option = get(1);
		option.setContent(isMarkedAsTranslated ? "# UNTRANSLATED\n" : "");
	}
}
