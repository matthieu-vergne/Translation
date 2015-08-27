package fr.vergne.translation.editor;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import fr.vergne.collection.filter.Filter;
import fr.vergne.collection.util.NaturalComparator;
import fr.vergne.collection.util.NaturalComparator.Translator;
import fr.vergne.translation.editor.content.MapTreeNode;
import fr.vergne.translation.util.CollectionManager;
import fr.vergne.translation.util.MapInformer;
import fr.vergne.translation.util.MapInformer.MapSummaryListener;
import fr.vergne.translation.util.MapInformer.NoDataException;
import fr.vergne.translation.util.MapNamer;

@SuppressWarnings("serial")
public class ListModel<MapID> extends DefaultTreeModel {

	private final DefaultMutableTreeNode root;
	private final MapInformer<MapID> informer;
	private final Map<MapID, MapTreeNode<MapID>> nodeMap;
	private final CollectionManager<MapID> listManager = new CollectionManager<MapID>();
	private final Collection<MapsChangedListener> listeners = new HashSet<MapsChangedListener>();
	private final Filter<MapID> noFilter;
	private final Filter<MapID> incompleteFilter;
	private final Map<MapNamer<MapID>, Comparator<MapID>> comparators = new HashMap<>();
	private MapNamer<MapID> orderNamer;

	public ListModel(final MapInformer<MapID> informer,
			final Collection<MapNamer<MapID>> namers) {
		super(new DefaultMutableTreeNode("maps"));
		root = (DefaultMutableTreeNode) getRoot();
		this.informer = informer;

		noFilter = new Filter<MapID>() {

			@Override
			public Boolean isSupported(MapID file) {
				return true;
			}
		};
		incompleteFilter = new Filter<MapID>() {

			@Override
			public Boolean isSupported(MapID file) {
				try {
					return informer.getEntriesRemaining(file) > 0;
				} catch (NoDataException e) {
					return true;
				}
			}
		};

		for (final MapNamer<MapID> mapNamer : namers) {
			NaturalComparator<MapID> comparator = new NaturalComparator<MapID>(
					new Translator<MapID>() {

						@Override
						public String toString(MapID file) {
							return mapNamer.getNameFor(file);
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
					 * confused due to their identical names. Although none of
					 * the comparisons computed are 100% safe, having everyone
					 * of them leading to a null comparison is improbable, so it
					 * should be OK most of the time.
					 */
					else {
						comparison = id1.hashCode() - id2.hashCode();
						if (comparison == 0) {
							comparison = id1.toString().compareTo(
									id2.toString());
						}
						return comparison;
					}
				}
			};
			comparators.put(mapNamer, comparator);
			listManager.addCollection(noFilter, comparator);
			listManager.addCollection(incompleteFilter, comparator);
		}
		this.orderNamer = namers.iterator().next();

		nodeMap = new HashMap<MapID, MapTreeNode<MapID>>();

		informer.addMapSummaryListener(new MapSummaryListener<MapID>() {

			@Override
			public void mapSummarized(MapID mapId) {
				Object node = nodeMap.get(mapId);

				Collection<MapID> ids = listManager.getCollection(
						getCurrentFilter(), getCurrentComparator());
				boolean oldContains = ids.contains(mapId);
				int index = -1;
				if (oldContains) {
					index = getIndexOfChild(root, node);
				} else {
					// not in the list
				}

				listManager.recheckElement(mapId);

				boolean newContains = ids.contains(mapId);
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
				} else if (oldContains && !newContains) {
					int[] childIndices = new int[] { index };
					Object[] children = new Object[] { node };
					fireTreeNodesRemoved(root, rootPath, childIndices, children);
				} else if (!oldContains && newContains) {
					int[] childIndices = new int[] { index };
					Object[] children = new Object[] { node };
					fireTreeNodesInserted(root, rootPath, childIndices,
							children);
				} else if (!oldContains && !newContains) {
					// no displayed update
				} else {
					throw new RuntimeException("This case should not happen.");
				}

				// update root
				fireTreeNodesChanged(root, rootPath, new int[0], new Object[0]);
			}
		});
	}

	public void setMaps(Collection<MapID> maps) {
		listManager.clear();
		for (MapID id : maps) {
			listManager.addElement(id);
			nodeMap.put(id, new MapTreeNode<MapID>(root, id));
		}
		fireTreeStructureChanged(root, new Object[] { root }, null, null);
		fireMapsChanged();
	}

	public void removeID(MapID id) {
		listManager.removeElement(id);
		nodeMap.remove(id);
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

	public Collection<MapID> getCurrentMapIDs() {
		return listManager.getCollection(getCurrentFilter(),
				getCurrentComparator());
	}

	private Comparator<MapID> getCurrentComparator() {
		return comparators.get(getOrderNamer());
	}

	private Filter<MapID> getCurrentFilter() {
		return isClearedDisplayed ? noFilter : incompleteFilter;
	}

	public Collection<MapID> getAllMapsIDs() {
		return listManager.getAllElements();
	}

	private boolean isClearedDisplayed;

	public void setClearedDisplayed(boolean isClearedDisplayed) {
		if (isClearedDisplayed != this.isClearedDisplayed) {
			this.isClearedDisplayed = isClearedDisplayed;
			fireTreeStructureChanged(root, new Object[] { root }, null, null);
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
			return nodeMap.get(getFileAt(index));
		} else {
			return super.getChild(parent, index);
		}
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == root) {
			return getCurrentMapIDs().size();
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

	private MapID getFileAt(int index) {
		List<MapID> ids = new LinkedList<>(getCurrentMapIDs());
		return ids.get(index);
	}

	private int getIDIndex(MapID id) {
		return new LinkedList<>(getCurrentMapIDs()).indexOf(id);
	}

	public MapNamer<MapID> getOrderNamer() {
		return orderNamer;
	}

	public void setOrderNamer(MapNamer<MapID> orderNamer) {
		if (!this.orderNamer.equals(orderNamer)) {
			this.orderNamer = orderNamer;
			fireTreeStructureChanged(root, new Object[] { root }, null, null);
		} else {
			// keep as is
		}
	}

	public MapInformer<MapID> getMapInformer() {
		return informer;
	}

	public void requestUpdate() {
		fireTreeStructureChanged(root, new Object[] { root }, null, null);
	}
}
