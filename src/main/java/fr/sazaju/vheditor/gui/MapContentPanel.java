package fr.sazaju.vheditor.gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import org.apache.commons.lang3.ArrayUtils;

import fr.sazaju.vheditor.gui.GuiBuilder.EntryPanel;
import fr.sazaju.vheditor.parsing.vh.map.BackedTranslationMap;
import fr.sazaju.vheditor.parsing.vh.map.BackedTranslationMap.EmptyMapException;
import fr.sazaju.vheditor.parsing.vh.map.MapEntry;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.impl.TranslationUtil;
import fr.vergne.logging.LoggerConfiguration;

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

	public MapContentPanel() {
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

	public int getCurrentEntryIndex() {
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
		Component focusOwner = frame.getFocusOwner();
		EntryPanel[] entries = getEntryPanels();
		if (focusOwner instanceof TranslationArea) {
			while (!(focusOwner instanceof EntryPanel)) {
				focusOwner = focusOwner.getParent();
			}
			return Arrays.asList(entries).indexOf(focusOwner);
		} else {
			int count = 0;
			Rectangle visible = mapContentArea.getVisibleRect();
			for (EntryPanel entry : entries) {
				Rectangle bounds = entry.getBounds();
				if (visible.y < bounds.y + bounds.height) {
					return count;
				} else {
					// not yet the searched entry
				}
				count++;
			}
		}

		throw new IllegalStateException("Impossible to find the current entry.");
	}

	public void goToEntry(final int entryIndex) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				EntryPanel[] entries = getEntryPanels();
				int index = Math.min(Math.max(entryIndex, 0),
						entries.length - 1);
				JScrollBar scroll = mapContentScroll.getVerticalScrollBar();
				if (index == 0) {
					scroll.setValue(0);
					entries[0].getTranslationArea().requestFocusInWindow();
				} else {
					Rectangle visible = mapContentArea.getVisibleRect();
					Component target = entries[index];
					visible.y = 0;
					while (target != mapContentArea) {
						visible.y += target.getBounds().y;
						target = target.getParent();
					}
					mapContentArea.scrollRectToVisible(visible);
					entries[index].getTranslationArea().requestFocusInWindow();
				}
			}
		});
	}

	/**
	 * 
	 * @return all the {@link EntryPanel}s, used as well as unused, in their
	 *         current order
	 */
	private EntryPanel[] getEntryPanels() {
		Component[] components = mapContentArea.getComponents();
		if (components.length == 0) {
			return new EntryPanel[0];
		} else {
			Container mapPanel = ((Container) components[0]);
			Component[] entries = ((Container) mapPanel.getComponent(1))
					.getComponents();
			if (mapPanel.getComponentCount() >= 3) {
				Component[] unused = ((Container) mapPanel.getComponent(3))
						.getComponents();
				entries = ArrayUtils.addAll(entries, unused);
			} else {
				// no unused entries
			}
			return Arrays.copyOf(entries, entries.length, EntryPanel[].class);
		}
	}

	public void goToNextUntranslatedEntry(boolean relyOnTags) {
		TreeSet<Integer> untranslatedEntries = new TreeSet<Integer>(
				getUntranslatedEntryIndexes(relyOnTags));
		if (untranslatedEntries.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"All the entries are already translated.");
		} else {
			int currentEntry = getCurrentEntryIndex();
			Integer next = untranslatedEntries.ceiling(currentEntry + 1);
			if (next == null) {
				JOptionPane
						.showMessageDialog(this,
								"End of the entries reached. Search from the beginning.");
				goToEntry(untranslatedEntries.first());
			} else {
				goToEntry(next);
			}
		}
	}

	public void setMap(final File mapFile) {
		setMap(mapFile, 0);
	}

	public void setMap(final File mapFile, final int entryIndex) {
		if (mapFile.equals(map.getBaseFile())) {
			goToEntry(entryIndex);
		} else {
			loadingLabel.setText("Loading map " + mapFile.getName() + "...");
			loading.start();
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						synchronized (map) {
							map.setBaseFile(mapFile);
							// TODO add map title (English label)
							mapTitleField.setText(mapFile.getName());
							mapContentArea.removeAll();
							mapContentArea.add(GuiBuilder
									.instantiateMapGui(map));
							goToEntry(entryIndex);
						}
					} catch (EmptyMapException e) {
						JOptionPane.showMessageDialog(MapContentPanel.this,
								"The map " + mapFile + " is empty.",
								"Empty Map", JOptionPane.WARNING_MESSAGE);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(MapContentPanel.this,
								e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
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
	}

	public TranslationMap<?> getMap() {
		return map;
	}

	private static interface LoadingManager {
		public void start();

		public void stop();
	}

	public void save() {
		logger.info("Applying modifications...");
		for (EntryPanel panel : getEntryPanels()) {
			panel.getTranslationArea().save();
			panel.getTranslationTag().save();
		}
		logger.info("Saving map to " + map.getBaseFile() + "...");
		map.saveAll();
		logger.info("Map saved.");
		for (MapSavedListener listener : listeners) {
			listener.mapSaved(map.getBaseFile());
		}
	}

	public void reset() {
		if (map.getBaseFile() == null
				|| !isModified()
				|| JOptionPane
						.showConfirmDialog(
								this,
								"Are you sure you want to cancel *ALL* the modifications that you have not saved?",
								"Cancel Modifications",
								JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
			return;
		} else {
			// we can go ahead
		}
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

	public Collection<Integer> getUntranslatedEntryIndexes(boolean relyOnTags) {
		Collection<Integer> untranslatedEntries = new LinkedList<Integer>();
		Iterator<MapEntry> iterator = map.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			MapEntry entry = iterator.next();
			if (relyOnTags
					&& entry.getMetadata().get(MapEntry.MARKED_AS_UNTRANSLATED)
					|| !relyOnTags
					&& !TranslationUtil.isActuallyTranslated(entry)) {
				untranslatedEntries.add(count);
			} else {
				// already translated
			}
			count++;
		}
		return untranslatedEntries;
	}

	public boolean isModified() {
		for (EntryPanel entry : getEntryPanels()) {
			for (Component component : entry.getComponents()) {
				if (component instanceof TranslationArea) {
					if (((TranslationArea) component).isModified()) {
						return true;
					} else {
						// look for another one
					}
				} else if (component instanceof TranslationTag) {
					if (((TranslationTag<?>) component).isModified()) {
						return true;
					} else {
						// look for another one
					}
				} else {
					// irrelevant component
				}
			}
		}
		return false;
	}

	private final Collection<MapSavedListener> listeners = new HashSet<MapSavedListener>();

	public void addListener(MapSavedListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MapSavedListener listener) {
		listeners.remove(listener);
	}

	public interface MapSavedListener {

		void mapSaved(File mapFile);

	}

}
