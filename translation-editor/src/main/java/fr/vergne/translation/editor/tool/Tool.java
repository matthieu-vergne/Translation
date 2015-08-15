package fr.vergne.translation.editor.tool;

import javax.swing.JPanel;

public interface Tool<MapID> {

	public void setToolProvider(ToolProvider<MapID> provider);

	public String getTitle();

	public JPanel instantiatePanel();
}
