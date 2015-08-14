package fr.sazaju.vheditor.gui.tool;

import fr.sazaju.vheditor.translation.TranslationProject;

public interface ToolProvider<MapID> {

	public TranslationProject<MapID, ?> getProject();

	public void loadMap(MapID mapId);

	public void loadMapEntry(MapID mapId, int entryIndex);

}
