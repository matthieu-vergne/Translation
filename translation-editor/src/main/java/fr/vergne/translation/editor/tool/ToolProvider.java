package fr.vergne.translation.editor.tool;

import fr.vergne.translation.TranslationProject;
import fr.vergne.translation.util.MapNamer;

public interface ToolProvider<MapID> {

	public TranslationProject<?, MapID, ?> getProject();

	public MapNamer<MapID> getMapNamer();

	public void loadMap(MapID mapId);

	public void loadMapEntry(MapID mapId, int entryIndex);

}
