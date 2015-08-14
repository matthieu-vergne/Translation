package fr.sazaju.vheditor.gui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import fr.sazaju.vheditor.gui.ListModel.MapsChangedListener;
import fr.sazaju.vheditor.gui.tool.MapCellRenderer;
import fr.sazaju.vheditor.gui.tool.MapTreeNode;
import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.translation.TranslationProject;
import fr.sazaju.vheditor.translation.impl.EmptyProject;
import fr.sazaju.vheditor.translation.impl.TranslationUtil;
import fr.sazaju.vheditor.util.Feature;
import fr.sazaju.vheditor.util.MapInformer;
import fr.sazaju.vheditor.util.MapInformer.MapSummaryListener;
import fr.sazaju.vheditor.util.MapInformer.NoDataException;
import fr.sazaju.vheditor.util.MapNamer;
import fr.sazaju.vheditor.util.ProjectLoader;
import fr.vergne.logging.LoggerConfiguration;

@SuppressWarnings("serial")
public class MapListPanel<TEntry extends TranslationEntry<?>, TMap extends TranslationMap<TEntry>, MapID, TProject extends TranslationProject<MapID, TMap>>
		extends JPanel {

	private static final String CONFIG_CLEARED_DISPLAYED = "clearedDisplayed";
	private static final String CONFIG_LABELS_DISPLAYED = "labelsDisplayed";
	private static final String CONFIG_LIST_ORDER = "listOrder";
	private static final String CONFIG_MAP_DIR = "mapDir";
	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private final JTextField folderPathField = new JTextField();
	private final JTree tree;
	private final Map<Order, MapNamer<MapID>> namers = new HashMap<Order, MapNamer<MapID>>();
	private final Map<MapID, MapSummary> mapSummaries = Collections
			.synchronizedMap(new HashMap<MapID, MapSummary>());
	private final Collection<MapSummaryListener<MapID>> mapSummaryListeners = new HashSet<MapSummaryListener<MapID>>();
	private final TreeSet<MapID> currentIDs = new TreeSet<MapID>(
			new Comparator<MapID>() {

				@Override
				public int compare(MapID id1, MapID id2) {
					return id1.toString().compareToIgnoreCase(id2.toString());
				}
			});
	private final ProjectLoader<TProject> projectLoader;
	private TranslationProject<MapID, TMap> project = new EmptyProject<>();
	private final Collection<MapListListener> listeners = new HashSet<MapListListener>();
	private final JPanel featureRow = new JPanel(new FlowLayout());

	public MapListPanel(ProjectLoader<TProject> projectLoader) {
		this.projectLoader = projectLoader;

		final MapInformer<MapID> mapInformer = new MapInformer<MapID>() {

			@Override
			public int getEntriesCount(MapID id) throws NoDataException {
				MapSummary mapSummary = mapSummaries.get(id);
				if (mapSummary == null) {
					throw new NoDataException();
				} else {
					return mapSummary.total;
				}
			}

			@Override
			public int getEntriesRemaining(MapID id) throws NoDataException {
				MapSummary mapSummary = mapSummaries.get(id);
				if (mapSummary == null) {
					throw new NoDataException();
				} else {
					return mapSummary.remaining;
				}
			}

			@Override
			public void addMapSummaryListener(MapSummaryListener<MapID> listener) {
				mapSummaryListeners.add(listener);
			}

			@Override
			public void removeMapSummaryListener(
					MapSummaryListener<MapID> listener) {
				mapSummaryListeners.remove(listener);
			}
		};

		final MapNamer<MapID> labelNamer = new MapNamer<MapID>() {

			@Override
			public String getNameFor(MapID id) {
				String label = project.getMapName(id);
				if (label == null) {
					return "[" + id + "]";
				} else {
					return label;
				}
			}
		};
		namers.put(Order.NAME, labelNamer);
		final MapNamer<MapID> idNamer = new MapNamer<MapID>() {

			@Override
			public String getNameFor(MapID id) {
				return id.toString();
			}
		};
		namers.put(Order.ID, idNamer);

		setBorder(new EtchedBorder());

		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		add(buildFileChooserPanel(), constraints);

		constraints.gridy++;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 1;
		tree = buildTreeComponent(mapInformer, idNamer, labelNamer);
		add(new JScrollPane(tree), constraints);

		constraints.gridy++;
		constraints.fill = GridBagConstraints.NONE;
		constraints.weighty = 0;
		JPanel options = buildQuickOptions(labelNamer, idNamer);
		add(options, constraints);

		configureBackgroundSummarizing();
		String projectPath = Gui.config.getProperty(CONFIG_MAP_DIR, null);
		if (projectPath == null) {
			// nothing to load
		} else {
			loadProjectFrom(new File(projectPath));
		}
	}

	@SuppressWarnings("unchecked")
	private JPanel buildQuickOptions(final MapNamer<MapID> labelNamer,
			final MapNamer<MapID> idNamer) {
		JPanel buttons = new JPanel(new GridLayout(0, 1));
		JPanel options = new JPanel(new FlowLayout());
		buttons.add(options);
		buttons.add(featureRow);

		final JCheckBox displayCleared = new JCheckBox();
		displayCleared.setAction(new AbstractAction("Cleared") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean selected = displayCleared.isSelected();
				Gui.config.setProperty(CONFIG_CLEARED_DISPLAYED, "" + selected);
				((ListModel<MapID>) tree.getModel())
						.setClearedDisplayed(selected);
			}
		});
		displayCleared.setSelected(((ListModel<MapID>) tree.getModel())
				.isClearedDisplayed());
		displayCleared.setToolTipText("Display cleared maps.");
		options.add(displayCleared);

		final JCheckBox displayLabels = new JCheckBox();
		displayLabels.setAction(new AbstractAction("Labels") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean selected = displayLabels.isSelected();
				Gui.config.setProperty(CONFIG_LABELS_DISPLAYED, "" + selected);
				((MapCellRenderer<MapID>) tree.getCellRenderer())
						.setMapNamer(selected ? labelNamer : idNamer);

				((ListModel<MapID>) tree.getModel()).requestUpdate();
			}
		});
		displayLabels.setSelected(Boolean.parseBoolean(Gui.config.getProperty(
				CONFIG_LABELS_DISPLAYED, "false")));
		displayLabels.setToolTipText("Display maps' English labels.");
		options.add(displayLabels);

		final JComboBox<Order> sortingChoice = new JComboBox<>(Order.values());
		sortingChoice.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Order order = (Order) sortingChoice.getSelectedItem();
				Gui.config.setProperty(CONFIG_LIST_ORDER, "" + order);
				((ListModel<MapID>) tree.getModel()).setOrderNamer(namers
						.get(order));
			}
		});
		try {
			sortingChoice.setSelectedItem(Order.valueOf(Gui.config.getProperty(
					CONFIG_LIST_ORDER, Order.ID.toString())));
		} catch (IllegalArgumentException e) {
			sortingChoice.setSelectedItem(Order.ID);
		}
		sortingChoice.setToolTipText("Choose map sorting order.");
		options.add(new JLabel("Sort: "));
		options.add(sortingChoice);

		return buttons;
	}

	@SuppressWarnings("unchecked")
	private void loadProjectFrom(File directory) {
		synchronized (mapSummaries) {
			project = projectLoader.load(directory);

			featureRow.removeAll();
			for (final Feature feature : project.getFeatures()) {
				JButton featuerButton = new JButton();
				featuerButton.setAction(new AbstractAction(feature.getName()) {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						feature.run();
					}
				});
				featuerButton.setToolTipText(feature.getDescription());
				featureRow.add(featuerButton);
			}

			Collection<MapID> newIDs = new LinkedList<>();
			for (MapID id : project) {
				newIDs.add(id);
			}

			if (currentIDs.containsAll(newIDs)
					&& newIDs.containsAll(currentIDs)) {
				// same IDs, don't change
			} else {
				Collection<MapID> removed = new LinkedList<MapID>(currentIDs);
				removed.removeAll(newIDs);
				for (MapID id : removed) {
					mapSummaries.remove(id);
				}
				currentIDs.clear();
				currentIDs.addAll(newIDs);

				Gui.config.setProperty(CONFIG_MAP_DIR, directory.toString());
				folderPathField.setText(directory.toString());
				((ListModel<MapID>) tree.getModel()).setMaps(currentIDs);
			}
		}
	}

	private JPanel buildFileChooserPanel() {
		folderPathField.setEditable(false);
		folderPathField.setText("Map folder...");

		JButton openButton = new JButton(new AbstractAction("Browse") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String path = folderPathField.getText();
				JFileChooser fileChooser = new JFileChooser(new File(path
						.isEmpty() ? "." : path));
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setFileHidingEnabled(true);
				fileChooser.setMultiSelectionEnabled(false);
				int answer = fileChooser.showDialog(MapListPanel.this, "Open");
				if (answer == JFileChooser.APPROVE_OPTION) {
					loadProjectFrom(fileChooser.getSelectedFile());
				} else {
					// do not consider it
				}
			}
		});
		openButton
				.setToolTipText("Select the folder of the translation project.");

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 1;
		panel.add(openButton, constraints);
		constraints.gridx = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		panel.add(folderPathField, constraints);
		return panel;
	}

	private JTree buildTreeComponent(MapInformer<MapID> mapInformer,
			final MapNamer<MapID> idNamer, final MapNamer<MapID> labelNamer) {
		final ListModel<MapID> listModel = new ListModel<MapID>(mapInformer,
				namers.values());
		listModel.setClearedDisplayed(Boolean.parseBoolean(Gui.config
				.getProperty(CONFIG_CLEARED_DISPLAYED, "true")));
		Order order;
		try {
			order = Order.valueOf(Gui.config.getProperty(CONFIG_LIST_ORDER,
					Order.ID.toString()));
		} catch (IllegalArgumentException e) {
			order = Order.ID;
		}
		listModel.setOrderNamer(namers.get(order));
		final JTree tree = new JTree(listModel);

		MapCellRenderer<MapID> cellRenderer = new MapCellRenderer<MapID>(
				tree.getCellRenderer(), mapInformer);
		boolean isLabelDisplayed = Boolean.parseBoolean(Gui.config.getProperty(
				CONFIG_LABELS_DISPLAYED, "false"));
		cellRenderer.setMapNamer(isLabelDisplayed ? labelNamer : idNamer);
		tree.setCellRenderer(cellRenderer);

		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		final TreePath[] selection = new TreePath[1];
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent event) {
				TreePath[] paths = event.getPaths();
				for (TreePath path : paths) {
					if (event.isAddedPath(path)) {
						selection[0] = path;
					} else {
						// do not change
					}
				}
			}
		});
		listModel.addTreeModelListener(new TreeModelListener() {

			private void recoverSelection() {
				/*
				 * Invoke the recovery later in order to ensure it is done after
				 * any other action on the tree, otherwise the selection could
				 * be lost due to another action made done the recovery.
				 */
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (selection[0] != null) {
							TreePath path = selection[0];
							@SuppressWarnings("unchecked")
							MapID id = ((MapTreeNode<MapID>) path
									.getLastPathComponent()).getMapID();
							Collection<MapID> ids = listModel
									.getCurrentMapIDs();
							if (ids.contains(id)) {
								tree.clearSelection();
								tree.setSelectionPath(selection[0]);
							} else {
								// still present
							}
						} else {
							// no selection to recover
						}
					}
				});
			}

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				recoverSelection();
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
				recoverSelection();
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				recoverSelection();
			}

			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				recoverSelection();
			}
		});
		tree.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent event) {
				// nothing to do
			}

			@Override
			public void mousePressed(MouseEvent event) {
				// nothing to do
			}

			@Override
			public void mouseExited(MouseEvent event) {
				// nothing to do
			}

			@Override
			public void mouseEntered(MouseEvent event) {
				// nothing to do
			}

			@SuppressWarnings("unchecked")
			@Override
			public void mouseClicked(MouseEvent event) {
				synchronized (mapSummaries) {
					if (event.getButton() == MouseEvent.BUTTON1
							&& event.getClickCount() == 2) {
						MapID file = getSelectedID(tree);
						updateMapSummary(file, false);
						for (MapListListener listener : listeners) {
							if (listener instanceof MapSelectedListener) {
								((MapSelectedListener<MapID>) listener)
										.mapSelected(file);
							} else {
								// not the right listener
							}
						}
					} else {
						// nothing to do for single click
					}
				}
			}

		});
		tree.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {
				// nothing to do
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				int keyCode = arg0.getKeyCode();
				if (keyCode == KeyEvent.VK_F5) {
					updateMapSummary(getSelectedID(tree), true);
				} else {
					// no action for other keys
				}
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				// nothing to do
			}
		});
		return tree;
	}

	private MapID getSelectedID(final JTree tree) {
		@SuppressWarnings("unchecked")
		MapTreeNode<MapID> node = (MapTreeNode<MapID>) tree.getSelectionPath()
				.getLastPathComponent();
		MapID id = node.getMapID();
		return id;
	}

	private void configureBackgroundSummarizing() {
		// sense the app closing
		final boolean[] isClosed = { false };
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				JFrame frame = (JFrame) SwingUtilities
						.getWindowAncestor(MapListPanel.this);
				if (frame == null) {
					SwingUtilities.invokeLater(this);
				} else {
					frame.addWindowListener(new WindowListener() {

						@Override
						public void windowOpened(WindowEvent arg0) {
							// do nothing
						}

						@Override
						public void windowIconified(WindowEvent arg0) {
							// do nothing
						}

						@Override
						public void windowDeiconified(WindowEvent arg0) {
							// do nothing
						}

						@Override
						public void windowDeactivated(WindowEvent arg0) {
							// do nothing
						}

						@Override
						public void windowClosing(WindowEvent arg0) {
							// do nothing
						}

						@Override
						public void windowClosed(WindowEvent arg0) {
							isClosed[0] = true;
						}

						@Override
						public void windowActivated(WindowEvent arg0) {
							// do nothing
						}

					});
				}
			}
		});

		// create the background task
		final boolean[] isRunning = { false };
		final Runnable backgroundSummary = new Runnable() {

			@Override
			public void run() {
				try {
					isRunning[0] = true;
					MapID id;
					if (isClosed[0] || (id = getWaitingMap()) == null) {
						isRunning[0] = false;
					} else {
						updateMapSummary(id, false);
						SwingUtilities.invokeLater(this);
					}
				} catch (Exception e) {
					isRunning[0] = false;
					throw new RuntimeException(e);
				}
			}

			private MapID getWaitingMap() {
				@SuppressWarnings("unchecked")
				ListModel<MapID> model = (ListModel<MapID>) tree.getModel();
				MapInformer<MapID> mapInformer = model.getMapInformer();

				Iterator<MapID> iterator = model.getCurrentMapIDs().iterator();
				while (iterator.hasNext()) {
					MapID id = iterator.next();
					try {
						mapInformer.getEntriesCount(id);
					} catch (NoDataException e) {
						return id;
					}
				}

				Iterator<MapID> iterator2 = model.getAllMapsIDs().iterator();
				while (iterator2.hasNext()) {
					MapID id = iterator2.next();
					try {
						mapInformer.getEntriesCount(id);
					} catch (NoDataException e) {
						return id;
					}
				}

				return null;
			}
		};

		// sense the necessity to launch the task
		@SuppressWarnings("unchecked")
		ListModel<MapID> model = (ListModel<MapID>) tree.getModel();
		model.addMapsChangedListener(new MapsChangedListener() {

			@Override
			public void mapsChanged() {
				if (isRunning[0]) {
					// already running
				} else {
					SwingUtilities.invokeLater(backgroundSummary);
				}
			}
		});
	}

	public void updateMapSummary(final MapID id, boolean force) {
		synchronized (mapSummaries) {
			if (!force && mapSummaries.get(id) != null) {
				// nothing to load
			} else {
				logger.info("Analysing " + id + "...");
				MapSummary summary = new MapSummary();
				TMap map = project.getMap(id);
				summary.total = map.size();
				summary.remaining = 0;
				Iterator<TEntry> iterator = map.iterator();
				while (iterator.hasNext()) {
					TEntry entry = iterator.next();
					summary.remaining += TranslationUtil
							.isActuallyTranslated(entry) ? 0 : 1;
				}

				mapSummaries.put(id, summary);
				logger.info("Map summarized: " + id);

				for (MapSummaryListener<MapID> listener : mapSummaryListeners) {
					listener.mapSummarized(id);
				}
			}
		}
	}

	private static class MapSummary {
		int total;
		int remaining;

		@Override
		public String toString() {
			return (total - remaining) + "/" + total;
		}
	}

	public void addListener(MapListListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MapListListener listener) {
		listeners.remove(listener);
	}

	public static interface MapListListener {
	}

	public static interface MapSelectedListener<MapID> extends MapListListener {
		public void mapSelected(MapID id);
	}

	public TranslationProject<MapID, ? extends TranslationMap<?>> getProject() {
		return project;
	}

	/**
	 * Sorting used to display the list of maps.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 */
	private static enum Order {
		/**
		 * Order the maps based on their IDs.
		 */
		ID,
		/**
		 * Order the maps based on their name.
		 */
		NAME
	}
}
