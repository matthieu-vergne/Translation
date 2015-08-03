package fr.sazaju.vheditor.gui.tool;

import java.util.Enumeration;

import javax.swing.tree.TreeNode;

public class MapTreeNode<MapID> implements TreeNode {

	private final TreeNode parent;
	private final MapID mapId;

	public MapTreeNode(TreeNode parent, MapID mapId) {
		this.parent = parent;
		this.mapId = mapId;
	}

	public MapID getMapID() {
		return mapId;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		throw new RuntimeException("No children");
	}

	@Override
	public int getChildCount() {
		return 0;
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		return -1;
	}

	@Override
	public boolean getAllowsChildren() {
		return false;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration children() {
		throw new RuntimeException("No children");
	}
}
