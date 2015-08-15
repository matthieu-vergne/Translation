package fr.vergne.translation.editor.content;

import java.awt.Component;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.editor.content.EntryComponentFactory.EntryComponent;

public interface EntryComponentFactory<ComposedComponent extends Component & EntryComponent> {

	public ComposedComponent createEntryComponent(TranslationEntry<?> entry);

	public static interface EntryComponent {
		public Component getTranslationComponent();
	}
}
