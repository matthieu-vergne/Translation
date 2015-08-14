package fr.sazaju.vheditor.gui.content;

import java.awt.Component;

import fr.sazaju.vheditor.gui.content.EntryComponentFactory.EnrichedComponent;
import fr.sazaju.vheditor.translation.TranslationEntry;

public interface EntryComponentFactory<EntryComponent extends Component & EnrichedComponent> {

	public EntryComponent createEntryComponent(TranslationEntry<?> entry);

	public static interface EnrichedComponent {
		public Component getTranslationComponent();
	}
}
