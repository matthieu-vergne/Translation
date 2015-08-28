package fr.vergne.translation.editor;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationProject;
import fr.vergne.translation.editor.ListModel.MapsChangedListener;
import fr.vergne.translation.editor.content.MapCellRenderer;
import fr.vergne.translation.editor.content.MapTreeNode;
import fr.vergne.translation.editor.tool.ToolProvider;
import fr.vergne.translation.impl.EmptyProject;
import fr.vergne.translation.impl.TranslationUtil;
import fr.vergne.translation.util.MapInformer;
import fr.vergne.translation.util.MapInformer.MapSummaryListener;
import fr.vergne.translation.util.MapInformer.NoDataException;

@SuppressWarnings("serial")
public class MapListPanel<TEntry extends TranslationEntry<?>, TMap extends TranslationMap<TEntry>, MapID, TProject extends TranslationProject<MapID, TMap>>
		extends JPanel {

	public static final Logger logger = Logger.getLogger(MapListPanel.class
			.getName());
	private final JTree tree;
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
	private TranslationProject<MapID, TMap> currentProject = new EmptyProject<>();
	private final Collection<MapSelectedListener<MapID>> listeners = new HashSet<>();

	public MapListPanel(final ToolProvider<MapID> toolProvider) {
		final MapInformer<MapID> mapInformer = new MapInformer<MapID>() {

			@Override
			public String getName(MapID mapId) throws NoDataException {
				return toolProvider.getMapNamer().getNameFor(mapId);
			}

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
			public boolean isModified(MapID id) throws NoDataException {
				MapSummary mapSummary = mapSummaries.get(id);
				if (mapSummary == null) {
					throw new NoDataException();
				} else {
					return mapSummary.isModified;
				}
			}

			@Override
			public int getAllEntriesCount() throws NoDataException {
				int total = 0;
				for (MapID id : currentIDs) {
					MapSummary summary = mapSummaries.get(id);
					if (summary == null) {
						throw new NoDataException();
					} else {
						total += summary.total;
					}
				}
				return total;
			}

			@Override
			public int getAllEntriesRemaining() throws NoDataException {
				int remaining = 0;
				for (MapID id : currentIDs) {
					MapSummary summary = mapSummaries.get(id);
					if (summary == null) {
						throw new NoDataException();
					} else {
						remaining += summary.remaining;
					}
				}
				return remaining;
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

		setLayout(new GridLayout(1, 1));
		tree = buildTreeComponent(mapInformer);
		add(new JScrollPane(tree));

		configureBackgroundSummarizing();
	}

	private JTree buildTreeComponent(MapInformer<MapID> mapInformer) {
		final ListModel<MapID> listModel = new ListModel<MapID>(mapInformer);
		final JTree tree = new JTree(listModel);

		MapCellRenderer<MapID> cellRenderer = new MapCellRenderer<MapID>(
				tree.getCellRenderer(), mapInformer);
		tree.setCellRenderer(cellRenderer);

		tree.setRootVisible(true);
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
							Object selectedNode = path.getLastPathComponent();
							if (selectedNode instanceof MapTreeNode) {
								@SuppressWarnings("unchecked")
								MapID id = ((MapTreeNode<MapID>) selectedNode)
										.getMapID();
								Collection<MapID> ids = listModel
										.getCurrentMapIDs();
								if (ids.contains(id)) {
									tree.clearSelection();
									tree.setSelectionPath(selection[0]);
								} else {
									// still present
								}
							} else {
								// not a map, ignore its selection
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
						MapID id = getSelectedID(tree);
						if (id != null) {
							updateMapSummary(id, false);
							for (MapSelectedListener<MapID> listener : listeners) {
								listener.mapSelected(id);
							}
						} else {
							// nothing to do
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
					MapID id = getSelectedID(tree);
					if (id != null) {
						updateMapSummary(id, true);
					} else {
						// nothing to do
					}
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
		Object source = tree.getSelectionPath().getLastPathComponent();
		if (source instanceof MapTreeNode) {
			@SuppressWarnings("unchecked")
			MapTreeNode<MapID> node = (MapTreeNode<MapID>) source;
			MapID id = node.getMapID();
			return id;
		} else {
			return null;
		}
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

	@SuppressWarnings("unchecked")
	public void setProject(TranslationProject<MapID, TMap> currentProject) {
		this.currentProject = currentProject;

		Collection<MapID> newIDs = new LinkedList<>();
		for (MapID id : currentProject) {
			newIDs.add(id);
		}

		if (currentIDs.containsAll(newIDs) && newIDs.containsAll(currentIDs)) {
			// same IDs, don't change
		} else {
			Collection<MapID> removed = new LinkedList<MapID>(currentIDs);
			removed.removeAll(newIDs);
			for (MapID id : removed) {
				mapSummaries.remove(id);
			}
			currentIDs.clear();
			currentIDs.addAll(newIDs);

			((ListModel<MapID>) tree.getModel()).setMaps(currentIDs);
		}
	}

	public TranslationProject<MapID, TMap> getProject() {
		return currentProject;
	}

	public void updateMapSummary(final MapID id, boolean force) {
		synchronized (mapSummaries) {
			if (!force && mapSummaries.get(id) != null) {
				// nothing to load
			} else {
				logger.finest("Summarizing " + id + "...");
				MapSummary summary = new MapSummary();
				TMap map = currentProject.getMap(id);
				summary.total = map.size();
				summary.remaining = 0;
				Iterator<TEntry> iterator = map.iterator();
				while (iterator.hasNext()) {
					TEntry entry = iterator.next();
					summary.remaining += TranslationUtil
							.isActuallyTranslated(entry) ? 0 : 1;
				}
				mapSummaries.put(id, summary);
				logger.finest("Map summarized: " + summary);

				for (MapSummaryListener<MapID> listener : mapSummaryListeners) {
					listener.mapSummarized(id);
				}
			}
		}
	}

	private static class MapSummary {
		int total;
		int remaining;
		boolean isModified;

		@Override
		public String toString() {
			String status = isModified ? "modified" : "saved";
			return (total - remaining) + "/" + total + "(" + status + ")";
		}
	}

	public void addMapSelectedListener(MapSelectedListener<MapID> listener) {
		listeners.add(listener);
	}

	public void removeMapSelectedListener(MapSelectedListener<MapID> listener) {
		listeners.remove(listener);
	}

	public static interface MapSelectedListener<MapID> {
		public void mapSelected(MapID id);
	}

	public void setModifiedStatus(MapID id, boolean isModified) {
		mapSummaries.get(id).isModified = isModified;
		for (MapSummaryListener<MapID> listener : mapSummaryListeners) {
			listener.mapSummarized(id);
		}
	}

	@SuppressWarnings("unchecked")
	public void requestUpdate() {
		((ListModel<MapID>) tree.getModel()).requestUpdate();
	}

	@SuppressWarnings("unchecked")
	public void setClearedDisplayed(boolean isDisplayed) {
		((ListModel<MapID>) tree.getModel()).setClearedDisplayed(isDisplayed);
	}

}
