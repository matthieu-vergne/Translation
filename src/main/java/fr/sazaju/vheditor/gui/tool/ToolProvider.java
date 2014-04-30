package fr.sazaju.vheditor.gui.tool;

import java.io.File;
import java.util.Collection;

public interface ToolProvider {

	public Collection<File> getMapFiles();

	public void loadMap(File mapFile);

	public void loadMapEntry(File mapFile, int entryIndex);

}
