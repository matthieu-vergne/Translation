package fr.sazaju.vheditor.gui;

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

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMap;

@SuppressWarnings("serial")
public class MapContentPanel extends JPanel {

	private final JTextArea mapContentArea;
	private TranslationMap map;
	private final JLabel mapTitleField;
	private final MapEntryPanel entryPanel;
	private int entryIndex = 0;

	public MapContentPanel(final MapEntryPanel entryPanel) {
		this.entryPanel = entryPanel;
		configureListeners(entryPanel);

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

	private void configureListeners(final MapEntryPanel entryPanel) {
		// TODO position the caret so we can see the entry
		entryPanel.addListener(new MapEntryPanel.NextEntryListener() {

			@Override
			public void eventGenerated() {
				if (entryIndex < map.size() - 1) {
					entryIndex++;
					entryPanel.setEntry(map.getEntry(entryIndex));
				} else {
					// no more
				}
			}
		});
		entryPanel.addListener(new MapEntryPanel.PreviousEntryListener() {

			@Override
			public void eventGenerated() {
				if (entryIndex > 0) {
					entryIndex--;
					entryPanel.setEntry(map.getEntry(entryIndex));
				} else {
					// no more
				}
			}
		});
		entryPanel.addListener(new MapEntryPanel.FirstEntryListener() {

			@Override
			public void eventGenerated() {
				entryIndex = 0;
				entryPanel.setEntry(map.getEntry(entryIndex));
			}
		});
		entryPanel.addListener(new MapEntryPanel.LastEntryListener() {

			@Override
			public void eventGenerated() {
				entryIndex = map.size() - 1;
				entryPanel.setEntry(map.getEntry(entryIndex));
			}
		});
		entryPanel.addListener(new MapEntryPanel.UntranslatedEntryListener() {

			@Override
			public void eventGenerated() {
				int total = map.size();
				for (int i = 1; i <= total; i++) {
					int actualIndex = (entryIndex + i) % total;
					TranslationEntry entry = map.getEntry(actualIndex);
					if (entry.isActuallyTranslated()) {
						continue;
					} else {
						entryIndex = actualIndex;
						entryPanel.setEntry(entry);
						break;
					}
				}
			}
		});
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
		entryPanel.setEntry(map.iterator().next());
	}

	public TranslationMap getMap() {
		return map;
	}
}
