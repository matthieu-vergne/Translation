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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import fr.sazaju.vheditor.gui.parsing.MapLabelPage;
import fr.sazaju.vheditor.gui.parsing.MapRow;
import fr.sazaju.vheditor.gui.parsing.MapTable;
import fr.sazaju.vheditor.gui.tool.MapCellRenderer;
import fr.sazaju.vheditor.gui.tool.MapTreeNode;
import fr.sazaju.vheditor.parsing.vh.map.BackedTranslationMap;
import fr.sazaju.vheditor.parsing.vh.map.BackedTranslationMap.EmptyMapException;
import fr.sazaju.vheditor.parsing.vh.map.MapEntry;
import fr.sazaju.vheditor.translation.impl.TranslationUtil;
import fr.sazaju.vheditor.util.MapInformer;
import fr.sazaju.vheditor.util.MapInformer.MapSummaryListener;
import fr.sazaju.vheditor.util.MapInformer.NoDataException;
import fr.sazaju.vheditor.util.MapNamer;
import fr.vergne.logging.LoggerConfiguration;
import fr.vergne.parsing.layer.exception.ParsingException;

@SuppressWarnings("serial")
public class MapListPanel extends JPanel {

	private static final String CONFIG_LABEL_SOURCE = "labelSource";
	private static final String CONFIG_LABEL_PREFIX = "label.";
	private static final String CONFIG_LABEL_LAST_UPDATE = "lastLabelUpdate";
	private static final String CONFIG_CLEARED_DISPLAYED = "clearedDisplayed";
	private static final String CONFIG_LABELS_DISPLAYED = "labelsDisplayed";
	private static final String CONFIG_LIST_ORDER = "listOrder";
	private static final String CONFIG_MAP_DIR = "mapDir";
	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private final JTextField folderPathField = new JTextField();
	private final JTree tree;
	private final Map<Order, MapNamer<File>> namers = new HashMap<Order, MapNamer<File>>();
	private final Map<String, String> mapLabels = Collections
			.synchronizedMap(new HashMap<String, String>());
	private final Map<File, MapSummary> mapSummaries = Collections
			.synchronizedMap(new HashMap<File, MapSummary>());
	// TODO generalize to any type, not only File
	private final Collection<MapSummaryListener<File>> mapSummaryListeners = new HashSet<MapSummaryListener<File>>();
	private final TreeSet<File> currentFiles = new TreeSet<File>(
			new Comparator<File>() {

				@Override
				public int compare(File f1, File f2) {
					return f1.getName().compareToIgnoreCase(f2.getName());
				}
			});

	public MapListPanel() {
		// TODO generalize to any type, not only File
		final MapInformer<File> mapInformer = new MapInformer<File>() {

			@Override
			public String getLabel(File mapFile) throws NoDataException {
				String label = retrieveMapLabel(mapFile);
				if (label == null) {
					throw new NoDataException();
				} else {
					return label;
				}
			}

			@Override
			public int getEntriesCount(File mapFile) throws NoDataException {
				MapSummary mapSummary = mapSummaries.get(mapFile);
				if (mapSummary == null) {
					throw new NoDataException();
				} else {
					return mapSummary.total;
				}
			}

			@Override
			public int getEntriesRemaining(File mapFile) throws NoDataException {
				MapSummary mapSummary = mapSummaries.get(mapFile);
				if (mapSummary == null) {
					throw new NoDataException();
				} else {
					return mapSummary.remaining;
				}
			}

			@Override
			public void addMapSummaryListener(MapSummaryListener<File> listener) {
				mapSummaryListeners.add(listener);
			}

			@Override
			public void removeMapSummaryListener(
					MapSummaryListener<File> listener) {
				mapSummaryListeners.remove(listener);
			}
		};

		// TODO generalize to any type, not only File
		final MapNamer<File> labelNamer = new MapNamer<File>() {

			@Override
			public String getNameFor(File file) {
				String label;
				try {
					label = mapInformer.getLabel(file);
				} catch (NoDataException e) {
					label = null;
				}
				if (label == null || !label.matches("[^a-zA-Z]*[a-zA-Z]+.*")) {
					return "[" + file.getName() + "]";
				} else {
					return label;
				}
			}
		};
		namers.put(Order.LABEL, labelNamer);
		// TODO generalize to any type, not only File
		final MapNamer<File> fileNamer = new MapNamer<File>() {

			@Override
			public String getNameFor(File file) {
				return file.getName();
			}
		};
		namers.put(Order.FILE, fileNamer);

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
		tree = buildTreeComponent(mapInformer, fileNamer, labelNamer);
		add(new JScrollPane(tree), constraints);

		constraints.gridy++;
		constraints.fill = GridBagConstraints.NONE;
		constraints.weighty = 0;
		JPanel options = buildQuickOptions(labelNamer, fileNamer);
		add(options, constraints);

		configureBackgroundSummarizing();
		updateFiles(new File(Gui.config.getProperty(CONFIG_MAP_DIR, "")));
	}

	// TODO generalize to any type, not only File
	@SuppressWarnings("unchecked")
	private JPanel buildQuickOptions(final MapNamer<File> labelNamer,
			final MapNamer<File> fileNamer) {
		JPanel buttons = new JPanel(new GridLayout(0, 1));
		JPanel row1 = new JPanel(new FlowLayout());
		JPanel row2 = new JPanel(new FlowLayout());
		buttons.add(row1);
		buttons.add(row2);

		final JCheckBox displayCleared = new JCheckBox();
		displayCleared.setAction(new AbstractAction("Cleared") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean selected = displayCleared.isSelected();
				Gui.config.setProperty(CONFIG_CLEARED_DISPLAYED, "" + selected);
				// TODO generalize to any type, not only File
				((ListModel<File>) tree.getModel())
						.setClearedDisplayed(selected);
			}
		});
		// TODO generalize to any type, not only File
		displayCleared.setSelected(((ListModel<File>) tree.getModel())
				.isClearedDisplayed());
		displayCleared.setToolTipText("Display cleared maps.");
		row1.add(displayCleared);

		final JCheckBox displayLabels = new JCheckBox();
		displayLabels.setAction(new AbstractAction("Labels") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean selected = displayLabels.isSelected();
				Gui.config.setProperty(CONFIG_LABELS_DISPLAYED, "" + selected);
				((MapCellRenderer) tree.getCellRenderer())
						.setMapNamer(selected ? labelNamer : fileNamer);

				((ListModel<File>) tree.getModel()).requestUpdate();
			}
		});
		displayLabels.setSelected(Boolean.parseBoolean(Gui.config.getProperty(
				CONFIG_LABELS_DISPLAYED, "false")));
		displayLabels.setToolTipText("Display maps' English labels.");
		row1.add(displayLabels);

		final JButton labelSource = new JButton();
		labelSource.setAction(new AbstractAction("Source") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String source = Gui.config.getProperty(CONFIG_LABEL_SOURCE);
				Object answer = JOptionPane
						.showInputDialog(
								MapListPanel.this,
								"Please provide the location of the page describing the labels (URL or local file):",
								"Label Source", JOptionPane.QUESTION_MESSAGE,
								null, null, source);
				if (answer == null || source.equals(answer)) {
					// no change requested
				} else if (((String) answer).isEmpty()) {
					displayError("An empty location is of no use, so the change is cancelled.");
				} else {
					logger.info("Label source set: " + answer);
					Gui.config.setProperty(CONFIG_LABEL_SOURCE,
							answer.toString());
					Gui.config.setProperty(CONFIG_LABEL_LAST_UPDATE, "" + 0);
				}
			}
		});
		labelSource
				.setToolTipText("Configure the source where to load the maps' labels from.");
		row2.add(labelSource);

		final JButton updateLabels = new JButton();
		updateLabels.setAction(new AbstractAction("Update") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					loadLabels(true);
					mapLabels.clear();
					// TODO generalize to any type, not only File
					((ListModel<File>) tree.getModel()).requestUpdate();
				} catch (Exception e) {
					displayError(e.getMessage());
				}
			}
		});
		updateLabels
				.setToolTipText("Request the update of the labels from the label source.");
		row2.add(updateLabels);

		final JComboBox<Order> sortingChoice = new JComboBox<>(Order.values());
		sortingChoice.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Order order = (Order) sortingChoice.getSelectedItem();
				Gui.config.setProperty(CONFIG_LIST_ORDER, "" + order);
				// TODO generalize to any type, not only File
				((ListModel<File>) tree.getModel()).setOrderNamer(namers
						.get(order));
			}
		});
		// TODO generalize to any type, not only File
		sortingChoice.setSelectedItem(((ListModel<File>) tree.getModel())
				.getOrderNamer());
		sortingChoice.setToolTipText("Choose map sorting order.");
		row1.add(new JLabel("Sort: "));
		row1.add(sortingChoice);

		return buttons;
	}

	@SuppressWarnings("unchecked")
	private void updateFiles(File folder) {
		synchronized (mapSummaries) {
			List<File> newFiles = Arrays.asList(retrieveFiles(folder));
			if (currentFiles.containsAll(newFiles)
					&& newFiles.containsAll(currentFiles)) {
				// same files, don't change
			} else {
				Collection<File> removed = new LinkedList<File>(currentFiles);
				removed.removeAll(newFiles);
				for (File file : removed) {
					mapSummaries.remove(file);
				}
				currentFiles.clear();
				currentFiles.addAll(newFiles);

				Gui.config.setProperty(CONFIG_MAP_DIR, folder.toString());
				folderPathField.setText(folder.toString());
				// TODO generalize to any type, not only File
				((ListModel<File>) tree.getModel()).setMaps(currentFiles);
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
					updateFiles(fileChooser.getSelectedFile());
				} else {
					// do not consider it
				}
			}
		});
		openButton.setToolTipText("Select the folder where are the map files.");

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

	private File[] retrieveFiles(File rootFolder) {
		File[] files = rootFolder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return !file.isHidden() && file.isFile();
			}
		});
		if (files == null) {
			files = new File[0];
		} else if (files.length == 0) {
			// nothing to sort
		} else {
			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					return f1.getName().compareToIgnoreCase(f2.getName());
				}
			});
		}
		return files;
	}

	// TODO generalize to any type, not only File
	private JTree buildTreeComponent(MapInformer<File> mapInformer,
			final MapNamer<File> fileNamer, final MapNamer<File> labelNamer) {
		// TODO generalize to any type, not only File
		final ListModel<File> listModel = new ListModel<File>(mapInformer,
				namers.values());
		listModel.setClearedDisplayed(Boolean.parseBoolean(Gui.config
				.getProperty(CONFIG_CLEARED_DISPLAYED, "true")));
		Order order = Order.valueOf(Gui.config.getProperty(CONFIG_LIST_ORDER,
				Order.FILE.toString()).toUpperCase());
		listModel.setOrderNamer(namers.get(order));
		final JTree tree = new JTree(listModel);

		MapCellRenderer cellRenderer = new MapCellRenderer(
				tree.getCellRenderer(), mapInformer);
		boolean isLabelDisplayed = Boolean.parseBoolean(Gui.config.getProperty(
				CONFIG_LABELS_DISPLAYED, "false"));
		cellRenderer.setMapNamer(isLabelDisplayed ? labelNamer : fileNamer);
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
							// TODO generalize to any type, not only File
							File file = ((MapTreeNode<File>) path
									.getLastPathComponent()).getMapID();
							Collection<File> files = listModel
									.getCurrentMapIDs();
							if (files.contains(file)) {
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

			@Override
			public void mouseClicked(MouseEvent event) {
				synchronized (mapSummaries) {
					if (event.getButton() == MouseEvent.BUTTON1
							&& event.getClickCount() == 2) {
						File file = getSelectedFile(tree);
						updateMapSummary(file, false);
						for (MapListListener listener : listeners) {
							if (listener instanceof FileSelectedListener) {
								((FileSelectedListener) listener)
										.fileSelected(file);
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
					updateMapSummary(getSelectedFile(tree), true);
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

	protected String retrieveMapLabel(File mapFile) {
		String mapName = mapFile.getName();
		logger.finest("Retrieving label for " + mapName + "...");
		if (mapLabels.containsKey(mapName)) {
			// already loaded
		} else {
			try {
				loadLabels(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mapLabels
					.put(mapName,
							Gui.config.getProperty(CONFIG_LABEL_PREFIX
									+ mapName, null));
		}
		String mapLabel = mapLabels.get(mapName);
		logger.finest("Label retrieved: " + mapName + " = " + mapLabel);
		return mapLabel;
	}

	private void loadLabels(boolean force) {
		if (Gui.config.containsKey(CONFIG_LABEL_SOURCE)) {
			// source already configured
		} else {
			Gui.config.setProperty(CONFIG_LABEL_SOURCE,
					"https://www.assembla.com/spaces/VH/wiki/Map_List");
		}
		long lastUpdate = Long.parseLong(Gui.config.getProperty(
				CONFIG_LABEL_LAST_UPDATE, "0"));
		if (!force && System.currentTimeMillis() < lastUpdate + 86400000) {
			// not old enough
		} else {
			String source = Gui.config.getProperty(CONFIG_LABEL_SOURCE);
			URL url;
			try {
				url = new URL(source);
			} catch (MalformedURLException e) {
				try {
					url = new File(source).toURI().toURL();
				} catch (MalformedURLException e1) {
					throw new RuntimeException("Malformed URL: " + source, e);
				}
			}
			try {
				loadLabelsFrom(url);
			} finally {
				Gui.config.setProperty(CONFIG_LABEL_LAST_UPDATE,
						"" + System.currentTimeMillis());
			}
		}
	}

	private void loadLabelsFrom(URL pageUrl) {
		logger.info("Loading page from " + pageUrl + "...");
		String pageContent;
		try {
			URLConnection connection = pageUrl.openConnection();
			Pattern pattern = Pattern
					.compile("text/html;\\s+charset=([^\\s]+)\\s*");
			Matcher matcher = pattern.matcher(connection.getContentType());
			String charset = matcher.matches() ? matcher.group(1) : "UTF-8";
			Reader reader = new InputStreamReader(connection.getInputStream(),
					charset);
			StringBuilder buffer = new StringBuilder();
			int ch;
			while ((ch = reader.read()) >= 0) {
				buffer.append((char) ch);
			}
			pageContent = buffer.toString();
			reader.close();
		} catch (Exception e) {
			throw new RuntimeException("Impossible to read the source "
					+ pageUrl, e);
		}

		logger.info("Parsing content...");
		MapLabelPage mapLabelPage = new MapLabelPage();
		try {
			mapLabelPage.setContent(pageContent);
		} catch (ParsingException e) {
			throw new RuntimeException("Impossible to find map labels in "
					+ pageUrl, e);
		}
		logger.info("Content parsed.");

		logger.info("Saving labels...");
		MapTable table = mapLabelPage.getTable();
		int total = table.size();
		for (MapRow row : table) {
			String name = "Map" + row.getId() + ".txt";
			String label = row.getEnglishLabel();
			Gui.config.setProperty(CONFIG_LABEL_PREFIX + name, label);
		}
		logger.info("Labels saved: " + total);
	}

	private void displayError(String message) {
		JOptionPane.showOptionDialog(this, message, "Loading Failed",
				JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null,
				new Object[] { "OK" }, "OK");
	}

	private File getSelectedFile(final JTree tree) {
		// TODO generalize to any type, not only File
		@SuppressWarnings("unchecked")
		MapTreeNode<File> node = (MapTreeNode<File>) tree.getSelectionPath()
				.getLastPathComponent();
		File file = node.getMapID();
		return file;
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
					File file;
					if (isClosed[0] || (file = getWaitingMap()) == null) {
						isRunning[0] = false;
					} else {
						updateMapSummary(file, false);
						SwingUtilities.invokeLater(this);
					}
				} catch (Exception e) {
					isRunning[0] = false;
					throw new RuntimeException(e);
				}
			}

			private File getWaitingMap() {
				// TODO generalize to any type, not only File
				@SuppressWarnings("unchecked")
				ListModel<File> model = (ListModel<File>) tree.getModel();
				// TODO generalize to any type, not only File
				MapInformer<File> mapInformer = model.getMapInformer();

				Iterator<File> iterator = model.getCurrentMapIDs().iterator();
				while (iterator.hasNext()) {
					File file = iterator.next();
					try {
						mapInformer.getEntriesCount(file);
					} catch (NoDataException e) {
						return file;
					}
				}

				Iterator<File> iterator2 = model.getAllMapsIDs().iterator();
				while (iterator2.hasNext()) {
					File file = iterator2.next();
					try {
						mapInformer.getEntriesCount(file);
					} catch (NoDataException e) {
						return file;
					}
				}

				return null;
			}
		};

		// sense the necessity to launch the task
		// TODO generalize to any type, not only File
		@SuppressWarnings("unchecked")
		ListModel<File> model = (ListModel<File>) tree.getModel();
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

	public void updateMapSummary(final File file, boolean force) {
		synchronized (mapSummaries) {
			if (!force && mapSummaries.get(file) != null) {
				// nothing to load
			} else {
				logger.info("Analysing " + file.getName() + "...");
				MapSummary summary = new MapSummary();
				try {
					BackedTranslationMap map = new BackedTranslationMap(file);
					summary.total = map.size();
					summary.remaining = 0;
					Iterator<MapEntry> iterator = map.iterator();
					while (iterator.hasNext()) {
						MapEntry entry = iterator.next();
						summary.remaining += TranslationUtil
								.isActuallyTranslated(entry) ? 0 : 1;
					}
				} catch (ParsingException e) {
					summary = null;
				} catch (EmptyMapException e) {
					summary.total = 0;
					summary.remaining = 0;
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (summary == null) {
					mapSummaries.remove(file);
					// TODO generalize to any type, not only File
					@SuppressWarnings("unchecked")
					ListModel<File> model = (ListModel<File>) tree.getModel();
					model.removeID(file);
					logger.warning("This file is not a map: " + file);
				} else {
					mapSummaries.put(file, summary);
					logger.info("File summarized: " + file);

					// TODO generalize to any type, not only File
					for (MapSummaryListener<File> listener : mapSummaryListeners) {
						listener.mapSummarized(file);
					}
				}
			}
		}
	}

	private static class MapSummary {
		int total;
		int remaining;
	}

	private final Collection<MapListListener> listeners = new HashSet<MapListListener>();

	public void addListener(MapListListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MapListListener listener) {
		listeners.remove(listener);
	}

	public static interface MapListListener {
	}

	public static interface FileSelectedListener extends MapListListener {
		public void fileSelected(File file);
	}

	public Collection<File> getFiles() {
		return currentFiles;
	}

	/**
	 * Sorting used to display the list of maps.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 */
	// TODO generalize to any type, not only File
	private static enum Order {
		/**
		 * Order the maps based on their file names.
		 */
		FILE,
		/**
		 * Order the maps based on their English labels.
		 */
		LABEL
	}
}
