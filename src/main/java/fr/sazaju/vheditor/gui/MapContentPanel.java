package fr.sazaju.vheditor.gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.gui.content.EntryComponentFactory;
import fr.sazaju.vheditor.gui.content.EntryComponentFactory.EnrichedComponent;
import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationEntry.TranslationListener;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.TranslationMetadata.Field;
import fr.sazaju.vheditor.translation.TranslationMetadata.FieldListener;
import fr.sazaju.vheditor.translation.impl.TranslationUtil;
import fr.vergne.logging.LoggerConfiguration;

@SuppressWarnings("serial")
public class MapContentPanel<EntryComponent extends Component & EnrichedComponent>
		extends JPanel {

	private final JPanel mapContentArea;
	private final JPanel mapLoadingArea;
	private final JScrollPane mapContentScroll;
	private final JLabel mapTitleField;
	private TranslationMap<?> map;
	private final LoadingManager loading;
	private final JLabel loadingLabel;
	public Logger logger = LoggerConfiguration.getSimpleLogger();
	private EntryComponentFactory<EntryComponent> entryFactory;
	private final List<EntryComponent> entryComponents = new LinkedList<>();
	private boolean isMapModified = false;
	private EntryComponent lastFocusedEntry;
	private final Field<Boolean> untranslatedField;

	public MapContentPanel(EntryComponentFactory<EntryComponent> entryFactory,
			Field<Boolean> untranslatedField) {
		this.entryFactory = entryFactory;
		this.untranslatedField = untranslatedField;

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
		return entryComponents.indexOf(lastFocusedEntry);
	}

	public void goToEntry(final int entryIndex) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				int index = Math.min(Math.max(entryIndex, 0), map.size() - 1);
				JScrollBar scroll = mapContentScroll.getVerticalScrollBar();
				if (index == 0) {
					scroll.setValue(0);
					entryComponents.get(0).getTranslationComponent()
							.requestFocusInWindow();
				} else {
					Rectangle visible = mapContentArea.getVisibleRect();
					EntryComponent entryComponent = entryComponents.get(index);
					Component target = entryComponent;
					visible.y = 0;
					while (target != mapContentArea) {
						visible.y += target.getBounds().y;
						target = target.getParent();
					}
					mapContentArea.scrollRectToVisible(visible);
					entryComponent.getTranslationComponent()
							.requestFocusInWindow();
				}
			}
		});
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

	public void setMap(TranslationMap<?> map, String name) {
		setMap(map, name, 0);
	}

	public void setMap(final TranslationMap<?> map, final String name,
			final int entryIndex) {
		if (this.map != null && this.map.equals(map)) {
			goToEntry(entryIndex);
		} else {
			loadingLabel.setText("Loading map " + name + "...");
			loading.start();
			this.map = map;
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					synchronized (map) {
						// TODO add map title (English label)
						mapTitleField.setText(name);
						mapContentArea.removeAll();
						entryComponents.clear();

						mapContentArea.setLayout(new GridLayout(map.size(), 1));
						TranslationListener translationListener = new TranslationListener() {

							@Override
							public void translationUpdated(String newTranslation) {
								isMapModified = true;
							}
						};
						FieldListener fieldListener = new FieldListener() {

							@Override
							public <T> void fieldUpdated(Field<T> field,
									T newValue) {
								isMapModified = true;
							}
						};
						for (TranslationEntry<?> entry : map) {
							final EntryComponent entryComponent = entryFactory
									.createEntryComponent(entry);
							mapContentArea.add(entryComponent);
							entryComponents.add(entryComponent);
							entryComponent.getTranslationComponent()
									.addFocusListener(new FocusListener() {

										@Override
										public void focusLost(FocusEvent e) {
											// nothing to do
										}

										@Override
										public void focusGained(FocusEvent e) {
											lastFocusedEntry = entryComponent;
										}
									});

							entry.addTranslationListener(translationListener);
							entry.getMetadata().addFieldListener(fieldListener);
						}

						goToEntry(entryIndex);
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
		logger.info("Saving map " + map + "...");
		map.saveAll();
		logger.info("Map saved.");
		for (MapSavedListener listener : listeners) {
			listener.mapSaved(map);
		}
	}

	public void reset() {
		if (map == null
				|| !isMapModified()
				|| JOptionPane
						.showConfirmDialog(
								this,
								"Are you sure you want to cancel *ALL* the modifications that you have not saved?",
								"Cancel Modifications",
								JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
			return;
		} else {
			logger.info("Resetting map " + map + "...");
			map.resetAll();
			logger.info("Map reset.");
		}
	}

	// TODO separate tag-based and content-based
	// TODO make tag-based unavailable if field not provided
	public Collection<Integer> getUntranslatedEntryIndexes(boolean relyOnTags) {
		Collection<Integer> untranslatedEntries = new LinkedList<Integer>();
		Iterator<? extends TranslationEntry<?>> iterator = map.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			TranslationEntry<?> entry = iterator.next();
			if (relyOnTags && entry.getMetadata().get(untranslatedField)
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

	private final Collection<MapSavedListener> listeners = new HashSet<MapSavedListener>();

	public void addListener(MapSavedListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MapSavedListener listener) {
		listeners.remove(listener);
	}

	public static interface MapSavedListener {

		void mapSaved(TranslationMap<?> map);

	}

	public void setEntryComponentFactory(
			EntryComponentFactory<EntryComponent> entryFactory) {
		if (entryFactory == null) {
			throw new IllegalArgumentException("No entry factory provided: "
					+ entryFactory);
		} else {
			this.entryFactory = entryFactory;
		}
	}

	public EntryComponentFactory<EntryComponent> getEntryComponentFactory() {
		return entryFactory;
	}

	public boolean isMapModified() {
		return isMapModified;
	}
}
