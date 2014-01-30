package com.vh.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

@SuppressWarnings("serial")
public class MapListPanel extends JPanel {

	private final MapContentPanel mapContentPanel;
	private final JTree tree;

	public MapListPanel(MapContentPanel mapContentPanel) {
		this.mapContentPanel = mapContentPanel;

		setLayout(new GridLayout(1, 1));
		setBorder(new EtchedBorder());

		File rootFolder = new File("VH/branches/working");
		tree = new JTree(rootFolder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return !file.isHidden() && file.isFile();
			}
		}));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		final TreeCellRenderer defaultRenderer = tree.getCellRenderer();
		tree.setCellRenderer(new TreeCellRenderer() {

			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object cell, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				Object value;
				if (cell instanceof DynamicUtilTreeNode) {
					value = ((DynamicUtilTreeNode) cell).getUserObject();
				} else if (cell instanceof DefaultMutableTreeNode) {
					value = ((DefaultMutableTreeNode) cell).getUserObject();
				} else {
					value = cell;
				}

				if (value instanceof File) {
					value = ((File) value).getName();
				} else {
					// keep current value
				}

				return defaultRenderer.getTreeCellRendererComponent(tree,
						value, selected, expanded, leaf, row, hasFocus);
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
				// TODO Auto-generated method stub
				if (event.getButton() == MouseEvent.BUTTON1
						&& event.getClickCount() == 2) {
					DynamicUtilTreeNode node = (DynamicUtilTreeNode) tree
							.getSelectionPath().getLastPathComponent();
					File file = (File) node.getUserObject();
					updateContent(file);
				} else {
					// nothing to do
				}
			}

		});

		add(new JScrollPane(tree));
	}
	
	public List<File> getFiles() {
		System.out.println(tree.getModel().getClass());
		return null;
	}

	private void updateContent(File file) {
		mapContentPanel.setFile(file);
	}
}
