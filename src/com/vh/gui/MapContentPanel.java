package com.vh.gui;

import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import org.apache.commons.io.FileUtils;

@SuppressWarnings("serial")
public class MapContentPanel extends JPanel {

	private final JTextArea mapContent;

	public MapContentPanel() {
		setLayout(new GridLayout(1, 1));
		setBorder(new EtchedBorder());
		mapContent = new JTextArea();
		add(new JScrollPane(mapContent));
	}

	public void setFile(File file) {
		String content;
		try {
			content = FileUtils.readFileToString(file);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		mapContent.setText(content);
		mapContent.setCaretPosition(0);
	}
}
