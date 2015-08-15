package fr.vergne.translation.editor;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import fr.vergne.translation.editor.tool.Tool;

@SuppressWarnings("serial")
public class ToolPanel extends JPanel {

	private final JTabbedPane tabContainer;

	public ToolPanel() {
		setLayout(new GridLayout());
		tabContainer = new JTabbedPane();
		add(tabContainer);
	}

	public void addTool(Tool<?> tool) {
		String target = tool.getTitle();
		for (int i = 0; i < tabContainer.getTabCount(); i++) {
			String title = tabContainer.getTitleAt(i);
			if (target.equals(title)) {
				tabContainer.setSelectedIndex(i);
				return;
			} else {
				// not this one
			}
		}
		tabContainer.add(target, tool.instantiatePanel());
	}
}
