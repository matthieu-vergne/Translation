package fr.sazaju.vheditor.gui;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.lang3.ArrayUtils;

import fr.sazaju.vheditor.util.FileViewManager;
import fr.sazaju.vheditor.util.MapInformer;
import fr.sazaju.vheditor.util.MapInformer.LoadingListener;
import fr.sazaju.vheditor.util.MapInformer.NoDataException;
import fr.sazaju.vheditor.util.MapNamer;
import fr.vergne.collection.filter.Filter;
import fr.vergne.collection.util.NaturalComparator;
import fr.vergne.collection.util.NaturalComparator.Translator;

@SuppressWarnings("serial")
public class ListModel extends DefaultTreeModel {

	private final DefaultMutableTreeNode root;
	private final MapInformer informer;
	private final Map<File, DefaultMutableTreeNode> fileMap;
	private final FileViewManager listManager = new FileViewManager();
	private final Filter<File> noFilter;
	private final Filter<File> incompleteFilter;
	private final Comparator<File> fileComparator;
	private final Comparator<File> labelComparator;
	private Order order = Order.File;

	public ListModel(final MapInformer informer, final MapNamer fileNamer,
			final MapNamer labelNamer) {
		super(new DefaultMutableTreeNode("maps"));
		root = (DefaultMutableTreeNode) getRoot();
		this.informer = informer;

		noFilter = new Filter<File>() {

			@Override
			public Boolean isSupported(File file) {
				return true;
			}
		};
		incompleteFilter = new Filter<File>() {

			@Override
			public Boolean isSupported(File file) {
				try {
					return informer.getEntriesRemaining(file) > 0;
				} catch (NoDataException e) {
					return true;
				}
			}
		};

		fileComparator = new NaturalComparator<File>(new Translator<File>() {

			@Override
			public String toString(File file) {
				return fileNamer.getNameFor(file);
			}
		});
		labelComparator = new NaturalComparator<File>(new Translator<File>() {

			@Override
			public String toString(File file) {
				return labelNamer.getNameFor(file);
			}
		});

		listManager.addCollection(noFilter, fileComparator);
		listManager.addCollection(noFilter, labelComparator);
		listManager.addCollection(incompleteFilter, fileComparator);
		listManager.addCollection(incompleteFilter, labelComparator);

		fileMap = new HashMap<File, DefaultMutableTreeNode>();

		informer.addLoadingListener(new LoadingListener() {

			@Override
			public void mapLoaded(File map) {
				listManager.recheckFile(map);
				// TODO use fireTreeNodesXxx()
			}
		});
	}

	public void setFiles(Collection<File> files) {
		listManager.clearFiles();
		listManager.addAllFiles(files);
		
		for (File file : files) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
			fileMap.put(file, node);
		}
		// TODO use fireTreeNodesXxx()
	}

	public Collection<File> getCurrentFiles() {
		return listManager.getCollection(getCurrentFilter(),
				getCurrentComparator());
	}

	private Comparator<File> getCurrentComparator() {
		return getOrder() == Order.File ? fileComparator : labelComparator;
	}

	private Filter<File> getCurrentFilter() {
		return isClearedDisplayed ? noFilter : incompleteFilter;
	}

	public Collection<File> getAllFiles() {
		return listManager.getAllFiles();
	}

	private boolean isClearedDisplayed;

	public void setClearedDisplayed(boolean isClearedDisplayed) {
		if (isClearedDisplayed != this.isClearedDisplayed) {
			this.isClearedDisplayed = isClearedDisplayed;
			// TODO use fireTreeNodesXxx()
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
			return fileMap.get(getFileAt(index));
		} else {
			return super.getChild(parent, index);
		}
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == root) {
			return getCurrentFiles().size();
		} else {
			return super.getChildCount(parent);
		}
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == root) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) child;
			File file = (File) node.getUserObject();
			return getFileIndex(file);
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

	private File getFileAt(int index) {
		Collection<File> files = getCurrentFiles();
		return (File) files.toArray()[index];
	}

	private int getFileIndex(File file) {
		File[] files = getCurrentFiles().toArray(new File[0]);
		return ArrayUtils.indexOf(files, file);
	}

	/**
	 * Sorting used to display the list of maps.
	 * 
	 * @author Sazaju HITOKAGE <sazaju@gmail.com>
	 * 
	 */
	public static enum Order {
		/**
		 * Order the maps based on their file names.
		 */
		File,
		/**
		 * Order the maps based on their English labels.
		 */
		Label
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		if (order != this.order) {
			this.order = order;
			// TODO use fireTreeNodesXxx()
		} else {
			// keep as is
		}
	}

	public MapInformer getMapInformer() {
		return informer;
	}
}
