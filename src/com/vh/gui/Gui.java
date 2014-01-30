package com.vh.gui;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class Gui extends JFrame {

	private final MapListPanel mapListPanel;
	private final MapContentPanel mapContentPanel;
	private final JLabel mapEntryPanel;
	private final JLabel toolsPanel;

	public Gui() {
		setTitle("VH Translation Tool");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		mapContentPanel = new MapContentPanel();
		mapListPanel = new MapListPanel(mapContentPanel);
		mapEntryPanel = new JLabel("Entry");
		toolsPanel = new JLabel("Tools");

		final JSplitPane middleSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		middleSplit.setLeftComponent(mapContentPanel);
		middleSplit.setRightComponent(mapEntryPanel);
		middleSplit.setResizeWeight(0.5);

		final JSplitPane rightSplit = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT);
		rightSplit.setLeftComponent(middleSplit);
		rightSplit.setRightComponent(toolsPanel);
		rightSplit.setResizeWeight(0.5);

		final JSplitPane rootSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rootSplit.setLeftComponent(mapListPanel);
		rootSplit.setRightComponent(rightSplit);
		rootSplit.setResizeWeight(1.0 / 3);

		setLayout(new GridLayout(1, 1));
		add(rootSplit);

		pack();
		rootSplit.setDividerLocation(rootSplit.getResizeWeight());
	}

	public static void main(String[] args) {
		new Runnable() {
			public void run() {
				new Gui().setVisible(true);
			}
		}.run();
	}
}
