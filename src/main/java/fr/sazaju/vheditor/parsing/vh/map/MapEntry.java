package fr.sazaju.vheditor.parsing.vh.map;

import java.util.NoSuchElementException;

import fr.sazaju.vheditor.translation.TranslationMetadata;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMetadata;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMetadata.FieldReader;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMetadata.FieldWriter;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Suite;

public class MapEntry extends Suite implements TranslationEntry {

	private static final Field<String> CONTEXT = new Field<String>("Context");
	private final SimpleTranslationMetadata metadata;

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
						Option<UntranslatedLine> option = get(1);
						return !option.isPresent();
					}
				}, new FieldWriter<Boolean>() {

					@Override
					public void write(Boolean isMarkedAsTranslated) {
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
	public String getOriginalVersion() {
		ContentBlock block = get(4);
		return block.getContentWithoutNewline().getContent();
	}

	@Override
	public String getTranslatedVersion() {
		ContentBlock block = get(6);
		return block.getContentWithoutNewline().getContent();
	}

	@Override
	public void setTranslatedVersion(String translation) {
		ContentBlock block = get(6);
		block.getContentWithoutNewline().setContent(translation);
	}

	@Override
	public TranslationMetadata getMetadata() {
		return metadata;
	}
}
