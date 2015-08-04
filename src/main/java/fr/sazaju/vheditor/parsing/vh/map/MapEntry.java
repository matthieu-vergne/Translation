package fr.sazaju.vheditor.parsing.vh.map;

import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMetadata;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMetadata;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMetadata.FieldReader;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMetadata.FieldWriter;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Suite;

public class MapEntry extends Suite implements TranslationEntry {

	private static final Field<String> CONTEXT = new Field<String>("Context");
	private final SimpleTranslationMetadata metadata;
	private String reference;
	private final Collection<TranslationListener> listeners = new HashSet<>();;

	public MapEntry() {
		super(new StartLine(), new Option<UntranslatedLine>(
				new UntranslatedLine()), new ContextLine(),
				new Option<AdviceLine>(new AdviceLine()), new ContentBlock(),
				new TranslationLine(), new ContentBlock(), new EndLine());
		metadata = new SimpleTranslationMetadata();
		metadata.configureField(Field.MARKED_AS_TRANSLATED,
				new FieldReader<Boolean>() {

					@Override
					public Boolean read() {
						// FIXME retrieve from the file
						Option<UntranslatedLine> option = get(1);
						return !option.isPresent();
					}
				}, new FieldWriter<Boolean>() {

					@Override
					public void write(Boolean isMarkedAsTranslated) {
						// FIXME write to the file
						Option<UntranslatedLine> option = get(1);
						option.setContent(!isMarkedAsTranslated ? "# UNTRANSLATED\n"
								: "");
					}
				});
		metadata.configureField(CONTEXT, new FieldReader<String>() {

			@Override
			public String read() {
				ContextLine context = get(2);
				return context.getContext().getContent();
			}
		});
		metadata.configureField(Field.CHAR_LIMIT_FACE,
				new FieldReader<Integer>() {

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
		metadata.configureField(Field.CHAR_LIMIT_NO_FACE,
				new FieldReader<Integer>() {

					@Override
					public Integer read() {
						Option<AdviceLine> option = get(3);
						if (option.isPresent()) {
							AdviceLine advice = option.getOption();
							try {
								return Integer.parseInt(advice
										.getGeneralLimit().getContent());
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
		reference = getCurrentTranslation();
	}

	@Override
	public String getOriginalContent() {
		ContentBlock block = get(4);
		return block.getContentWithoutNewline().getContent();
	}

	@Override
	public String getReferenceTranslation() {
		return reference;
	}

	@Override
	public String getCurrentTranslation() {
		ContentBlock block = get(6);
		return block.getContentWithoutNewline().getContent();
	}

	@Override
	public void setCurrentTranslation(String translation) {
		ContentBlock block = get(6);
		block.getContentWithoutNewline().setContent(translation);
		for (TranslationListener listener : listeners) {
			listener.translationUpdated(translation);
		}
	}

	@Override
	public void saveTranslation() {
		// FIXME write to the file
	}

	@Override
	public void resetTranslation() {
		setCurrentTranslation(reference);
	}

	@Override
	public void saveAll() {
		// FIXME write to the file
	}

	@Override
	public void resetAll() {
		resetTranslation();
		metadata.resetAll();
	}

	@Override
	public TranslationMetadata getMetadata() {
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
}
