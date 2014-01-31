package com.vh.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

@SuppressWarnings("serial")
public class MapToolsPanel extends JPanel {

	public MapToolsPanel() {
		setBorder(new EtchedBorder());

		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = 0;
		constraints.gridy = 0;
		add(new JLabel("Tools"), constraints);
	}
}
