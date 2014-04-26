package fr.sazaju.vheditor.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.impl.BackedTranslationMap;
import fr.sazaju.vheditor.translation.impl.BackedTranslationMap.EmptyMapException;
import fr.vergne.logging.LoggerConfiguration;

// TODO if there is modifications, ask for confirmation/save before to load a new one or close the app
// TODO update list panel info when saving
// TODO use references at https://www.assembla.com/spaces/VH/wiki/Reference
// TODO find original content in translated versions
// TODO for a content between 「 and 」 or  （ and ） which lacks indentation, indent it with '　'
// TODO replace all "..." (3) by "…" (1)
// TODO identify all the "\." in the original version and place them in the translated version
// TODO generalize the previous points as a "formatting recovery" feature (take the formatting of the original version and apply/adapt it to the translated version) : identify contents, punctuation and '\.' parts, map the original and translated ones, use the formatting of the original on the translated, adapt if needed
// TODO wrap the sentences to reach at best the characters limits.
@SuppressWarnings("serial")
public class MapContentPanel extends JPanel {

	private final JPanel mapContentArea;
	private final JPanel mapLoadingArea;
	private final JScrollPane mapContentScroll;
	private final JLabel mapTitleField;
	private final BackedTranslationMap map = new BackedTranslationMap();
	private final LoadingManager loading;
	private final JLabel loadingLabel;
	public Logger logger = LoggerConfiguration.getSimpleLogger();

	public MapContentPanel(final MapToolsPanel toolsPanel) {
		configureListeners(toolsPanel);

		setBorder(new EtchedBorder());

		final CardLayout contentSwitcher = new CardLayout();
		setLayout(contentSwitcher);
		loading = new LoadingManager() {

			@Override
			public void start() {
				contentSwitcher.last(MapContentPanel.this);
			}

			@Override
			public void stop() {
				contentSwitcher.first(MapContentPanel.this);
			}
		};

		JPanel contentWrapper = new JPanel();
		contentWrapper.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.CENTER;
		mapTitleField = new JLabel(" ");
		contentWrapper.add(mapTitleField, constraints);

		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		mapContentArea = new JPanel();
		mapContentArea.setLayout(new GridLayout(1, 1));
		mapContentScroll = new JScrollPane(mapContentArea);
		mapContentScroll.getVerticalScrollBar().setUnitIncrement(15);
		contentWrapper.add(mapContentScroll, constraints);
		add(contentWrapper);

		mapLoadingArea = new JPanel();
		mapLoadingArea.setLayout(new GridBagLayout());
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		loadingLabel = new JLabel("Loading...");
		mapLoadingArea.add(loadingLabel, constraints);
		add(mapLoadingArea);
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
				goToEntry(map.sizeUsed() - 1);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.UntranslatedEntryListener() {

			@Override
			public void eventGenerated() {
				int total = map.sizeUsed();
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
					TranslationEntry entry = map.getUsedEntry(entryIndex);
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
					map.save();
					logger.info("Map saved.");
				} catch (Exception e) {
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
		final int index = Math.min(Math.max(entryIndex, 0), map.sizeUsed() - 1);
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

	public void setMap(final File mapFile) {
		loadingLabel.setText("Loading map " + mapFile.getName() + "...");
		loading.start();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					synchronized (map) {
						map.setBaseFile(mapFile);
						mapTitleField.setText(mapFile.getName());
						fillPanel(map, mapContentArea);
						goToEntry(0);
					}
				} catch (EmptyMapException e) {
					JOptionPane.showMessageDialog(MapContentPanel.this,
							"The map " + mapFile + " is empty.", "Empty Map",
							JOptionPane.WARNING_MESSAGE);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(MapContentPanel.this,
							e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						loading.stop();
					}
				});
			}
		});
	}

	public static void fillPanel(TranslationMap map, JPanel panel) {
		panel.removeAll();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 0, 0, 0);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		Iterator<? extends TranslationEntry> iterator = map.iteratorUsed();
		fillEntries(panel, constraints, iterator, panel.getBackground());
		iterator = map.iteratorUnused();
		if (iterator.hasNext()) {
			panel.add(new JLabel(" "), constraints);
			constraints.gridy++;
			panel.add(new JLabel("# UNUSED TRANSLATABLES"), constraints);
			constraints.gridy++;
			fillEntries(panel, constraints, iterator, Color.MAGENTA);
		} else {
			// no unused entries
		}
	}

	private static void fillEntries(JPanel panel,
			GridBagConstraints constraints,
			Iterator<? extends TranslationEntry> iterator, Color background) {
		while (iterator.hasNext()) {
			final TranslationEntry entry = iterator.next();
			{
				JLabel label = new JLabel("# TEXT STRING");
				label.setOpaque(true);
				label.setBackground(background);
				panel.add(label, constraints);
				constraints.gridy++;
				if (entry.isMarkedAsUntranslated()) {
					label = new JLabel("# UNTRANSLATED");
					label.setBackground(background);
					panel.add(label, constraints);
					constraints.gridy++;
				} else {
					// do not write it
				}
				label = new JLabel("# CONTEXT : " + entry.getContext());
				label.setBackground(background);
				label.setOpaque(true);
				panel.add(label, constraints);
				constraints.gridy++;
				if (entry.getCharLimit(false) != null
						&& entry.getCharLimit(true) != null) {
					label = new JLabel("# ADVICE : "
							+ entry.getCharLimit(false) + " char limit ("
							+ entry.getCharLimit(true) + " if face)");
					label.setOpaque(true);
					label.setBackground(background);
					panel.add(label, constraints);
					constraints.gridy++;
				} else if (entry.getCharLimit(false) != null
						&& entry.getCharLimit(true) == null) {
					label = new JLabel("# ADVICE : "
							+ entry.getCharLimit(false) + " char limit");
					label.setOpaque(true);
					label.setBackground(background);
					panel.add(label, constraints);
					constraints.gridy++;
				} else {
					// no advice
				}
				JTextArea original = new JTextArea(entry.getOriginalVersion());
				original.setEditable(false);
				panel.add(original, constraints);
				constraints.gridy++;
				label = new JLabel("# TRANSLATION ");
				label.setOpaque(true);
				label.setBackground(background);
				panel.add(label, constraints);
				constraints.gridy++;
				panel.add(new TranslationArea(entry), constraints);
				constraints.gridy++;
				label = new JLabel("# END STRING");
				label.setOpaque(true);
				label.setBackground(background);
				panel.add(label, constraints);
			}
			constraints.gridy++;
			if (iterator.hasNext()) {
				JLabel label = new JLabel(" ");
				label.setOpaque(true);
				label.setBackground(background);
				panel.add(label, constraints);
				constraints.gridy++;
			} else {
				// EOF
			}
		}
	}

	public TranslationMap getMap() {
		return map;
	}

	private static interface LoadingManager {
		public void start();

		public void stop();
	}
}
