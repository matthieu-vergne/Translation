package fr.vergne.translation.editor.tool;

import fr.vergne.translation.TranslationProject;

public interface ToolProvider<MapID> {

	public TranslationProject<MapID, ?> getProject();

	public void loadMap(MapID mapId);

	public void loadMapEntry(MapID mapId, int entryIndex);

}
