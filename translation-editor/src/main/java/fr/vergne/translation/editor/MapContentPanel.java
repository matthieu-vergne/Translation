package fr.vergne.translation.editor;

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
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import fr.vergne.collection.MultiMap;
import fr.vergne.collection.impl.HashMultiMap;
import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationEntry.TranslationListener;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationMetadata;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.TranslationMetadata.FieldListener;
import fr.vergne.translation.editor.content.EntryComponentFactory.EntryComponent;
import fr.vergne.translation.editor.content.MapComponentFactory;
import fr.vergne.translation.editor.content.MapComponentFactory.MapComponent;
import fr.vergne.translation.editor.tool.ToolProvider;
import fr.vergne.translation.impl.EmptyMap;

@SuppressWarnings("serial")
public class MapContentPanel<MapID> extends JPanel {

	public static final Logger logger = Logger.getLogger(MapContentPanel.class
			.getName());
	private final JPanel mapContentArea;
	private final JPanel mapLoadingArea;
	private final JScrollPane mapContentScroll;
	private final JLabel mapTitleField;
	private final LoadingManager loading;
	private final JLabel loadingLabel;
	private final MapComponentFactory<?> mapFactory;
	private MapComponent mapComponent;
	private TranslationMap<?> currentMap = new EmptyMap<>();
	private MapID currentMapId;
	private Collection<TranslationEntry<?>> modifiedEntries = new HashSet<>();
	private MultiMap<TranslationEntry<?>, Field<?>> modifiedFields = new HashMultiMap<TranslationEntry<?>, Field<?>>() {
		public boolean remove(TranslationEntry<?> key, Field<?> value) {
			boolean changed = super.remove(key, value);
			if (get(key).isEmpty()) {
				remove(key);
			} else {
				// remaining stuff to remember
			}
			return changed;
		};
	};
	private int lastFocusedEntryIndex;
	private final Collection<MapUpdateListener<MapID>> updateListeners = new HashSet<MapUpdateListener<MapID>>();
	private ToolProvider<MapID> toolProvider;

	public MapContentPanel(ToolProvider<MapID> toolProvider,
			MapComponentFactory<?> mapFactory) {
		this.mapFactory = mapFactory;
		this.toolProvider = toolProvider;

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
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		mapTitleField = new JLabel(" ", JLabel.CENTER);
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
		return lastFocusedEntryIndex;
	}

	public void goToEntry(final int entryIndex) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final int index = Math.min(Math.max(entryIndex, 0),
						currentMap.size() - 1);
				if (index == 0) {
					mapContentScroll.getVerticalScrollBar().setValue(0);
				} else {
					Rectangle visible = mapContentArea.getVisibleRect();
					Component target = (Component) mapComponent
							.getEntryComponent(index);
					visible.y = 0;
					while (target != mapContentArea) {
						visible.y += target.getBounds().y;
						target = target.getParent();
					}
					mapContentArea.scrollRectToVisible(visible);
				}
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						mapComponent.getEntryComponent(index)
								.getTranslationComponent()
								.requestFocusInWindow();
					}
				});
			}
		});
	}

	public void setMap(final MapID mapId, final int entryIndex) {
		if (this.currentMapId != null && this.currentMapId.equals(mapId)) {
			goToEntry(entryIndex);
		} else {
			alignStoredAndCurrentValues();

			loadingLabel.setText("Loading map " + mapId + "...");
			loading.start();
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					synchronized (currentMap) {
						TranslationMap<?> newMap = toolProvider.getProject()
								.getMap(mapId);
						for (final TranslationEntry<?> entry : newMap) {
							entry.addTranslationListener(new TranslationListener() {

								@Override
								public void translationUpdated(
										String newTranslation) {
									if (entry.getStoredTranslation().equals(
											newTranslation)) {
										modifiedEntries.remove(entry);
									} else {
										modifiedEntries.add(entry);
									}
									notifyUpdateListeners();
								}

								@Override
								public void translationStored() {
									modifiedEntries.remove(entry);
									notifyUpdateListeners();
								}
							});
							final TranslationMetadata metadata = entry
									.getMetadata();
							metadata.addFieldListener(new FieldListener() {

								@Override
								public <T> void fieldUpdated(Field<T> field,
										T newValue) {
									if (metadata.getStored(field).equals(
											newValue)) {
										modifiedFields.remove(entry, field);
									} else {
										modifiedFields.add(entry, field);
									}
									notifyUpdateListeners();
								}

								@Override
								public <T> void fieldStored(Field<T> field) {
									modifiedFields.remove(entry, field);
									notifyUpdateListeners();
								}
							});
						}
						MapContentPanel.this.currentMap = newMap;
						MapContentPanel.this.currentMapId = mapId;

						String name = toolProvider.getProject().getMapName(
								mapId);
						mapTitleField.setText("<html><center>" + mapId + "<br>"
								+ name + "</center></html>");

						mapComponent = mapFactory.createMapComponent(newMap);
						for (int index = 0; index < newMap.size(); index++) {
							EntryComponent entryComponent = mapComponent
									.getEntryComponent(index);
							final int entryIndex = index;
							entryComponent.getTranslationComponent()
									.addFocusListener(new FocusListener() {

										@Override
										public void focusLost(FocusEvent e) {
											// nothing to do
										}

										@Override
										public void focusGained(FocusEvent e) {
											lastFocusedEntryIndex = entryIndex;
										}
									});
						}

						mapContentArea.removeAll();
						mapContentArea.add((Component) mapComponent);

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

	public void alignStoredAndCurrentValues() throws CancellationException {
		if (isMapModified()) {
			int answer = JOptionPane.showOptionDialog(MapContentPanel.this,
					"The map has been modified. Would you like to save it?",
					"Save the Current Map?", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null, new String[] { "Yes",
							"No", "Cancel" }, "Cancel");
			if (answer == JOptionPane.YES_OPTION) {
				save();
			} else if (answer == JOptionPane.NO_OPTION) {
				reset(true);
			} else if (answer == JOptionPane.CANCEL_OPTION) {
				throw new CancellationException("Alignment cancelled");
			} else {
				throw new IllegalStateException("Unmanaged answer: " + answer);
			}
		} else {
			// nothing modified, go ahead
		}
	}

	public TranslationMap<?> getMap() {
		return currentMap;
	}

	public boolean isMapModified() {
		return !(modifiedEntries.isEmpty() && modifiedFields.isEmpty());
	}

	public void save() {
		logger.info("Saving map " + currentMap + "...");
		currentMap.saveAll();
		deleteSavingInfo();
		logger.info("Map saved.");
	}

	private void deleteSavingInfo() {
		modifiedEntries.clear();
		modifiedFields.clear();
		notifyUpdateListeners();
	}

	private void notifyUpdateListeners() {
		boolean isModified = isMapModified();
		for (MapUpdateListener<MapID> listener : updateListeners) {
			listener.mapModified(currentMapId, isModified);
		}
	}

	public void reset() {
		reset(null);
	}

	public void reset(Boolean autoAnswer) {
		if (currentMap == null
				|| !isMapModified()
				|| (autoAnswer != null && autoAnswer == false)
				|| (autoAnswer == null && JOptionPane
						.showConfirmDialog(
								this,
								"Are you sure you want to cancel *ALL* the modifications that you have not saved?",
								"Cancel Modifications",
								JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)) {
			return;
		} else {
			logger.info("Resetting map " + currentMap + "...");
			currentMap.resetAll();
			deleteSavingInfo();
			logger.info("Map reset.");
		}
	}

	public void addUpdateListener(MapUpdateListener<MapID> listener) {
		updateListeners.add(listener);
	}

	public void removeUpdateListener(MapUpdateListener<MapID> listener) {
		updateListeners.remove(listener);
	}

	public static interface MapUpdateListener<MapID> {

		void mapModified(MapID id, boolean isDifferentFromStore);

	}

	private static interface LoadingManager {
		public void start();

		public void stop();
	}
}
