package com.vh.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import org.apache.commons.io.FileUtils;

import com.vh.translation.TranslationMap;

@SuppressWarnings("serial")
public class MapContentPanel extends JPanel {

	private final JTextArea mapContentArea;
	private TranslationMap map;
	private final JLabel mapTitleField;

	public MapContentPanel() {
		setBorder(new EtchedBorder());

		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.CENTER;
		mapTitleField = new JLabel(" ");
		add(mapTitleField, constraints);

		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		mapContentArea = new JTextArea();
		add(new JScrollPane(mapContentArea), constraints);
	}

	public void setMap(TranslationMap map) {
		String content;
		File mapFile = map.getBaseFile();
		try {
			content = FileUtils.readFileToString(mapFile);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		mapTitleField.setText(mapFile.getName());
		mapContentArea.setText(content);
		mapContentArea.setCaretPosition(0);
		this.map = map;
	}

	public TranslationMap getMap() {
		return map;
	}
}
