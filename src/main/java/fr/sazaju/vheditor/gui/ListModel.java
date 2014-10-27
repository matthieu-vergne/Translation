package fr.sazaju.vheditor.gui;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import fr.sazaju.vheditor.gui.MapListPanel.MapDescriptor;

@SuppressWarnings("serial")
public class ListModel extends DefaultTreeModel {

	private final DefaultMutableTreeNode root;
	private final Map<File, MapDescriptor> mapDescriptors;
	private List<File> files;
	private boolean isClearedDisplayed;

	public ListModel(Map<File, MapDescriptor> mapDescriptors,
			boolean isClearedDisplayed) {
		super(new DefaultMutableTreeNode("maps"));
		root = (DefaultMutableTreeNode) getRoot();
		this.mapDescriptors = mapDescriptors;
		setClearedDisplayed(isClearedDisplayed);
	}

	public void setFiles(Collection<File> files) {
		this.files = new LinkedList<File>(files);
		root.removeAllChildren();
		for (File file : files) {
			root.add(new DefaultMutableTreeNode(file));
		}
	}

	public List<File> getFiles() {
		return new LinkedList<File>(files);
	}

	public void setClearedDisplayed(boolean isClearedDisplayed) {
		this.isClearedDisplayed = isClearedDisplayed;
	}

	public boolean isClearedDisplayed() {
		return isClearedDisplayed;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (!isClearedDisplayed && parent == root) {
			Iterator<File> iterator = files.iterator();
			int count = -1;
			index++;
			while (iterator.hasNext()) {
				File file = iterator.next();
				count++;
				MapDescriptor descriptor = mapDescriptors.get(file);
				if (descriptor != null && descriptor.remaining == 0) {
					// ignore this node
				} else {
					index--;
					if (index == 0) {
						return super.getChild(parent, count);
					} else {
						// not yet
					}
				}
			}
			throw new NoSuchElementException();
		} else {
			return super.getChild(parent, index);
		}
	}

	@Override
	public int getChildCount(Object parent) {
		if (!isClearedDisplayed && parent == root) {
			Iterator<File> iterator = files.iterator();
			int count = 0;
			while (iterator.hasNext()) {
				File file = iterator.next();
				MapDescriptor descriptor = mapDescriptors.get(file);
				if (descriptor != null && descriptor.remaining == 0) {
					// ignore this node
				} else {
					count++;
				}
			}
			return count;
		} else {
			return super.getChildCount(parent);
		}
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		for (int i = 0; i < getChildCount(parent); i++) {
			if (child == getChild(parent, i)) {
				return i;
			} else {
				// not yet
			}
		}
		throw new NoSuchElementException("No child " + child + " for parent "
				+ parent);
	}

}
