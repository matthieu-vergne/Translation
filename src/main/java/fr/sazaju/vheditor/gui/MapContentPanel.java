package fr.sazaju.vheditor.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMap;
import fr.sazaju.vheditor.translation.impl.TranslationUtil;

@SuppressWarnings("serial")
public class MapContentPanel extends JPanel {

	private final JEditorPane mapContentArea;
	private TranslationMap map;
	private final JLabel mapTitleField;
	private int entryIndex = 0;

	public MapContentPanel(final MapToolsPanel toolsPanel) {
		configureListeners(toolsPanel);

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
		mapContentArea = new JEditorPane();
		mapContentArea.setContentType("text/html");
		mapContentArea.setEditable(false);
		add(new JScrollPane(mapContentArea), constraints);
	}

	private void configureListeners(final MapToolsPanel toolsPanel) {
		// TODO retrieve current index from current view
		toolsPanel.addListener(new MapToolsPanel.NextEntryListener() {

			@Override
			public void eventGenerated() {
				if (entryIndex < map.size() - 1) {
					entryIndex++;
					mapContentArea.scrollToReference("" + entryIndex);
				} else {
					// no more
				}
			}
		});
		toolsPanel.addListener(new MapToolsPanel.PreviousEntryListener() {

			@Override
			public void eventGenerated() {
				if (entryIndex > 0) {
					entryIndex--;
					mapContentArea.scrollToReference("" + entryIndex);
				} else {
					// no more
				}
			}
		});
		toolsPanel.addListener(new MapToolsPanel.FirstEntryListener() {

			@Override
			public void eventGenerated() {
				entryIndex = 0;
				mapContentArea.scrollToReference("" + entryIndex);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.LastEntryListener() {

			@Override
			public void eventGenerated() {
				entryIndex = map.size() - 1;
				mapContentArea.scrollToReference("" + entryIndex);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.UntranslatedEntryListener() {

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
						mapContentArea.scrollToReference("" + entryIndex);
						break;
					}
				}
			}
		});
	}

	public void setMap(TranslationMap map) {
		mapTitleField.setText(map.getBaseFile().getName());
		mapContentArea.setText(TranslationUtil
				.map2html((SimpleTranslationMap) map));
		mapContentArea.setCaretPosition(0);
		this.map = map;
	}

	public TranslationMap getMap() {
		return map;
	}
}
