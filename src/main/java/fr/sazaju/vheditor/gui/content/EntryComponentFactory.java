package fr.sazaju.vheditor.gui.content;

import java.awt.Component;

import fr.sazaju.vheditor.gui.content.EntryComponentFactory.EntryComponent;
import fr.sazaju.vheditor.translation.TranslationEntry;

public interface EntryComponentFactory<ComposedComponent extends Component & EntryComponent> {

	public ComposedComponent createEntryComponent(TranslationEntry<?> entry);

	public static interface EntryComponent {
		public Component getTranslationComponent();
	}
}
