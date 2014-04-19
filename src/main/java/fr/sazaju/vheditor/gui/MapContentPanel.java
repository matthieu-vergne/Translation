package fr.sazaju.vheditor.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.impl.SimpleTranslationMap;
import fr.sazaju.vheditor.translation.impl.TranslationUtil;
import fr.sazaju.vheditor.util.LoggerConfiguration;

@SuppressWarnings("serial")
public class MapContentPanel extends JPanel {

	private final JPanel mapContentArea;
	private final JScrollPane mapContentScroll;
	private final JLabel mapTitleField;
	private TranslationMap map;
	public Logger logger = LoggerConfiguration.getSimpleLogger();

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
		mapContentArea = new JPanel();
		mapContentArea.setLayout(new GridLayout(1, 1));
		mapContentScroll = new JScrollPane(mapContentArea);
		add(mapContentScroll, constraints);
	}

	private void configureListeners(final MapToolsPanel toolsPanel) {
		toolsPanel.addListener(new MapToolsPanel.NextEntryListener() {

			@Override
			public void eventGenerated() {
				goToEntry(getDisplayedEntryIndex() + 1);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.PreviousEntryListener() {

			@Override
			public void eventGenerated() {
				goToEntry(getDisplayedEntryIndex() - 1);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.FirstEntryListener() {

			@Override
			public void eventGenerated() {
				goToEntry(0);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.LastEntryListener() {

			@Override
			public void eventGenerated() {
				goToEntry(map.size() - 1);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.UntranslatedEntryListener() {

			@Override
			public void eventGenerated() {
				int total = map.size();
				int entryIndex = getDisplayedEntryIndex();
				for (int i = 1; i <= total; i++) {
					entryIndex++;
					if (entryIndex == total) {
						JOptionPane
								.showMessageDialog(MapContentPanel.this,
										"End of the entries reached. Search from the beginning.");
						entryIndex %= total;
					} else {
						// just continue the search
					}
					TranslationEntry entry = map.getEntry(entryIndex);
					if (entry.isActuallyTranslated()) {
						continue;
					} else {
						goToEntry(entryIndex);
						return;
					}
				}
				JOptionPane.showMessageDialog(MapContentPanel.this,
						"All the entries are already translated.");
			}
		});
		toolsPanel.addListener(new MapToolsPanel.SaveMapListener() {

			@Override
			public void eventGenerated() {
				for (Component component : mapContentArea.getComponents()) {
					if (component instanceof TranslationArea) {
						((TranslationArea) component).save();
					} else {
						// irrelevant component
					}
				}

				File targetFile = map.getBaseFile();
				logger.info("Saving map to " + targetFile + "...");
				try {
					TranslationUtil.writeMap((SimpleTranslationMap) map,
							targetFile);
					logger.info("Map saved.");
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(MapContentPanel.this,
							"The modifications could not be saved to "
									+ targetFile
									+ ". Look at the log for more details.");
				}
			}
		});
		toolsPanel.addListener(new MapToolsPanel.ResetMapListener() {

			@Override
			public void eventGenerated() {
				setVisible(false);
				Rectangle visible = mapContentArea.getVisibleRect();
				final JComponent reference = (JComponent) mapContentArea
						.getComponentAt(0, visible.y);
				final int offset = reference.getVisibleRect().y;
				for (Component component : mapContentArea.getComponents()) {
					if (component instanceof TranslationArea) {
						TranslationArea area = (TranslationArea) component;
						area.reset();
					} else {
						// irrelevant component
					}
				}
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						Rectangle visible = mapContentArea.getVisibleRect();
						visible.y = reference.getBounds().y + offset;
						mapContentArea.scrollRectToVisible(visible);
						setVisible(true);
					}
				});
			}
		});
	}

	protected int getDisplayedEntryIndex() {
		int count = 0;
		Rectangle visible = mapContentArea.getVisibleRect();
		for (Component component : mapContentArea.getComponents()) {
			if (component instanceof JLabel) {
				JLabel label = (JLabel) component;
				String text = label.getText();
				if (text.equals("# TEXT STRING")) {
					Rectangle bounds = label.getBounds();
					if (visible.y <= bounds.y) {
						return count;
					} else {
						// not yet the searched entry
					}
					count++;
				} else {
					// irrelevant component
				}
			} else {
				// irrelevant component
			}
		}
		throw new IllegalStateException("Impossible to find the current entry.");
	}

	protected void goToEntry(final int entryIndex) {
		final int index = Math.min(Math.max(entryIndex, 0), map.size() - 1);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				JScrollBar scroll = mapContentScroll.getVerticalScrollBar();
				if (index == 0) {
					scroll.setValue(0);
				} else {
					int count = 0;
					Rectangle visible = mapContentArea.getVisibleRect();
					for (Component component : mapContentArea.getComponents()) {
						if (component instanceof JLabel) {
							JLabel label = (JLabel) component;
							String text = label.getText();
							if (text.equals("# TEXT STRING")) {
								Rectangle bounds = label.getBounds();
								if (count == index) {
									visible.y = bounds.y;
									mapContentArea.scrollRectToVisible(visible);
									return;
								} else {
									// not yet the searched entry
								}
								count++;
							} else {
								// irrelevant component
							}
						} else {
							// irrelevant component
						}
					}
				}
			}
		});
	}

	public void setMap(TranslationMap map) {
		this.map = map;
		mapTitleField.setText(map.getBaseFile().getName());
		TranslationUtil.fillPanel((SimpleTranslationMap) map, mapContentArea);
		goToEntry(0);
	}

	public TranslationMap getMap() {
		return map;
	}
}
