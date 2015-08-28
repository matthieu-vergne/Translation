package fr.vergne.translation.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import fr.vergne.collection.filter.Filter;
import fr.vergne.collection.util.NaturalComparator;
import fr.vergne.collection.util.NaturalComparator.Translator;
import fr.vergne.translation.editor.content.MapTreeNode;
import fr.vergne.translation.util.MapInformer;
import fr.vergne.translation.util.MapInformer.MapSummaryListener;
import fr.vergne.translation.util.MapInformer.NoDataException;

@SuppressWarnings("serial")
public class ListModel<MapID> extends DefaultTreeModel {

	private static final Logger logger = Logger.getLogger(ListModel.class
			.getName());
	private final DefaultMutableTreeNode root;
	private final MapInformer<MapID> informer;
	private final Map<MapID, MapTreeNode<MapID>> nodeMap;
	private final Collection<MapsChangedListener> listeners = new HashSet<MapsChangedListener>();
	private final Filter<MapID> withClearedFilter;
	private final Filter<MapID> withoutClearedFilter;
	private List<MapID> completeList = new LinkedList<>();
	private List<MapID> currentList = new LinkedList<>();
	private boolean isClearedDisplayed;
	private NaturalComparator<MapID> comparator;

	public ListModel(final MapInformer<MapID> informer) {
		super(new DefaultMutableTreeNode("maps"));
		root = (DefaultMutableTreeNode) getRoot();
		this.informer = informer;

		withClearedFilter = new Filter<MapID>() {

			@Override
			public Boolean isSupported(MapID file) {
				return true;
			}
		};
		withoutClearedFilter = new Filter<MapID>() {

			@Override
			public Boolean isSupported(MapID file) {
				try {
					return informer.getEntriesRemaining(file) > 0;
				} catch (NoDataException e) {
					return true;
				}
			}
		};

		comparator = new NaturalComparator<MapID>(new Translator<MapID>() {

			@Override
			public String toString(MapID id) {
				try {
					return informer.getName(id);
				} catch (NoDataException e) {
					throw new RuntimeException(e);
				}
			}
		}) {
			@Override
			public int compare(MapID id1, MapID id2) {
				int comparison = super.compare(id1, id2);
				if (comparison != 0) {
					return comparison;
				} else if (id1.equals(id2)) {
					return 0;
				}
				/*
				 * The last case enforces that two different IDs are not
				 * confused due to their identical names. Although none of the
				 * comparisons computed are 100% safe, having everyone of them
				 * leading to a null comparison is improbable, so it should be
				 * OK most of the time.
				 */
				else {
					comparison = id1.hashCode() - id2.hashCode();
					if (comparison == 0) {
						comparison = id1.toString().compareTo(id2.toString());
					}
					return comparison;
				}
			}
		};

		nodeMap = new HashMap<MapID, MapTreeNode<MapID>>();

		informer.addMapSummaryListener(new MapSummaryListener<MapID>() {

			@Override
			public void mapSummarized(MapID mapId) {
				logger.finest("Revising tree after summary...");
				Object node = nodeMap.get(mapId);

				boolean oldContains = currentList.contains(mapId);
				int index = -1;
				if (oldContains) {
					index = getIndexOfChild(root, node);
				} else {
					// not in the list
				}

				boolean newContains = getCurrentFilter().isSupported(mapId);
				if (index == -1 && newContains) {
					index = getIndexOfChild(root, node);
				} else {
					// not in the list nor necessary
				}

				Object[] rootPath = new Object[] { root };
				if (oldContains && newContains) {
					int[] childIndices = new int[] { index };
					Object[] children = new Object[] { node };
					fireTreeNodesChanged(root, rootPath, childIndices, children);
					logger.finest("Update " + mapId);
				} else if (oldContains && !newContains) {
					currentList.remove(index);
					int[] childIndices = new int[] { index };
					Object[] children = new Object[] { node };
					fireTreeNodesRemoved(root, rootPath, childIndices, children);
					logger.finest("Remove " + mapId);
				} else if (!oldContains && newContains) {
					currentList.add(index, mapId);
					int[] childIndices = new int[] { index };
					Object[] children = new Object[] { node };
					fireTreeNodesInserted(root, rootPath, childIndices,
							children);
					logger.finest("Add " + mapId);
				} else if (!oldContains && !newContains) {
					// no display update
				} else {
					throw new RuntimeException("This case should not happen.");
				}

				// update root
				fireTreeNodesChanged(root, rootPath, new int[0], new Object[0]);
				logger.finest("Tree revised.");
			}
		});
	}

	public void setMaps(Collection<MapID> maps) {
		completeList = new LinkedList<>(maps);
		currentList = rebuildCurrentList();
		for (MapID id : maps) {
			nodeMap.put(id, new MapTreeNode<MapID>(root, id));
		}
		fireTreeStructureChanged(root, new Object[] { root }, null, null);
		fireMapsChanged();
	}

	public static interface MapsChangedListener {
		public void mapsChanged();
	}

	public void addMapsChangedListener(MapsChangedListener listener) {
		listeners.add(listener);
	}

	public void removeMapsChangedListener(MapsChangedListener listener) {
		listeners.remove(listener);
	}

	private void fireMapsChanged() {
		for (MapsChangedListener listener : listeners) {
			listener.mapsChanged();
		}
	}

	public List<MapID> getCurrentMapIDs() {
		return currentList;
	}

	private List<MapID> rebuildCurrentList() {
		logger.finer("Rebuilding actual map list...");
		List<MapID> ids = new LinkedList<>();
		Filter<MapID> filter = getCurrentFilter();
		for (MapID id : completeList) {
			if (filter.isSupported(id)) {
				logger.finest("- Add " + id);
				ids.add(id);
			} else {
				// unwanted ID
				logger.finest("- Ignore " + id);
			}
		}
		logger.finer("Sorting actual map list...");
		Collections.sort(ids, comparator);
		logger.finer("Map list built.");
		return ids;
	}

	private Filter<MapID> getCurrentFilter() {
		return isClearedDisplayed ? withClearedFilter : withoutClearedFilter;
	}

	public Collection<MapID> getAllMapsIDs() {
		return Collections.unmodifiableCollection(completeList);
	}

	public void setClearedDisplayed(boolean isClearedDisplayed) {
		if (isClearedDisplayed != this.isClearedDisplayed) {
			this.isClearedDisplayed = isClearedDisplayed;
			requestUpdate();
		} else {
			// keep as is
		}
	}

	public boolean isClearedDisplayed() {
		return isClearedDisplayed;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == root) {
			return nodeMap.get(getIDAt(index));
		} else {
			return super.getChild(parent, index);
		}
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == root) {
			return currentList.size();
		} else {
			return super.getChildCount(parent);
		}
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == root) {
			@SuppressWarnings("unchecked")
			MapTreeNode<MapID> node = (MapTreeNode<MapID>) child;
			MapID id = node.getMapID();
			return getIDIndex(id);
		} else {
			return super.getIndexOfChild(parent, child);
		}
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node == getRoot()) {
			return false;
		} else {
			return true;
		}
	}

	private MapID getIDAt(int index) {
		return currentList.get(index);
	}

	private int getIDIndex(MapID id) {
		return currentList.indexOf(id);
	}

	public MapInformer<MapID> getMapInformer() {
		return informer;
	}

	public void requestUpdate() {
		logger.finest("Update full tree");
		currentList = rebuildCurrentList();
		fireTreeStructureChanged(root, new Object[] { root }, null, null);
	}
}
