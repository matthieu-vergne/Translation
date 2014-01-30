package com.vh.gui;

import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import org.apache.commons.io.FileUtils;

import com.vh.translation.TranslationMap;

@SuppressWarnings("serial")
public class MapContentPanel extends JPanel {

	private final JTextArea mapContent;
	private TranslationMap map;

	public MapContentPanel() {
		setLayout(new GridLayout(1, 1));
		setBorder(new EtchedBorder());
		mapContent = new JTextArea();
		add(new JScrollPane(mapContent));
	}

	public void setMap(TranslationMap map) {
		String content;
		try {
			content = FileUtils.readFileToString(map.getBaseFile());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		mapContent.setText(content);
		mapContent.setCaretPosition(0);
		this.map = map;
	}

	public TranslationMap getMap() {
		return map;
	}
}
