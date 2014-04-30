package fr.sazaju.vheditor.gui.tool;

import javax.swing.JPanel;

public interface Tool {
	
	public void setToolProvider(ToolProvider provider);

	public String getTitle();

	public JPanel instantiatePanel();
}
