package fr.sazaju.vheditor.gui.content;

import java.awt.Component;

import fr.sazaju.vheditor.gui.content.MapComponentFactory.MapComponent;
import fr.sazaju.vheditor.translation.TranslationMap;

public interface MapComponentFactory<ComposedComponent extends Component & MapComponent> {

	public ComposedComponent createMapComponent(TranslationMap<?> map);

	public static interface MapComponent {
		public EntryComponentFactory.EntryComponent getEntryComponent(int index);
	}
}
