package fr.vergne.translation.editor.content;

import java.awt.Component;

import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.editor.content.MapComponentFactory.MapComponent;

public interface MapComponentFactory<ComposedComponent extends Component & MapComponent> {

	public ComposedComponent createMapComponent(TranslationMap<?> map);

	public static interface MapComponent {
		public EntryComponentFactory.EntryComponent getEntryComponent(int index);
	}
}
