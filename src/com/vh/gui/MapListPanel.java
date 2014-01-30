package com.vh.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.vh.translation.TranslationEntry;
import com.vh.translation.TranslationMap;
import com.vh.translation.TranslationUtil;
import com.vh.util.LoggerConfiguration;

@SuppressWarnings("serial")
public class MapListPanel extends JPanel {

	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private final MapContentPanel mapContentPanel;
	private final JTree tree;
	private final Map<File, TranslationMap> maps = Collections
			.synchronizedMap(new HashMap<File, TranslationMap>());
	private final File[] files;

	public MapListPanel(MapContentPanel mapContentPanel) {
		this.mapContentPanel = mapContentPanel;

		files = retrieveFiles();

		setLayout(new GridLayout(1, 1));
		setBorder(new EtchedBorder());
		tree = buildTreeComponent(files);
		add(new JScrollPane(tree));

		runFilesLoadingInBackground();
	}

	private File[] retrieveFiles() {
		File rootFolder = new File("VH/branches/working");
		File[] files = rootFolder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return !file.isHidden() && file.isFile()
						&& file.getName().startsWith("Map");
			}
		});
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareToIgnoreCase(f2.getName());
			}
		});
		return files;
	}

	private JTree buildTreeComponent(File[] files) {
		final JTree tree = new JTree(files);
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
					File file = (File) value;
					value = file.getName();
					TranslationMap map;
					map = maps.get(file);
					int remaining = -1;
					if (map == null) {
						value += " (loading)";
					} else {
						int total = 0;
						remaining = 0;
						Iterator<TranslationEntry> iterator = map.iterator();
						while (iterator.hasNext()) {
							TranslationEntry entry = iterator.next();
							if (entry.isUnused()) {
								// don't count it
							} else {
								total++;
								remaining += entry.isActuallyTranslated() ? 0
										: 1;
							}
						}

						if (remaining == 0) {
							value += "  (cleared)";
						} else {
							int percent = 100 * remaining / total;
							value += " (" + remaining + " = " + percent + "%)";
						}
					}
					DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) defaultRenderer
							.getTreeCellRendererComponent(tree, value,
									selected, expanded, leaf, row, hasFocus);
					if (remaining == 0) {
						renderer.setEnabled(false);
						// renderer.setForeground(SystemColor.inactiveCaption);
					} else {
						// keep it activated
					}
					return renderer;
				} else {
					return defaultRenderer.getTreeCellRendererComponent(tree,
							value, selected, expanded, leaf, row, hasFocus);
				}
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
				if (event.getButton() == MouseEvent.BUTTON1
						&& event.getClickCount() == 2) {
					DynamicUtilTreeNode node = (DynamicUtilTreeNode) tree
							.getSelectionPath().getLastPathComponent();
					File file = (File) node.getUserObject();
					try {
						displayContentOf(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					// nothing to do
				}
			}

		});
		return tree;
	}

	private void runFilesLoadingInBackground() {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		for (final File file : files) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					try {
						loadFileIfNecessary(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
		executor.shutdown();
		forceExecutorShutdownIfAppClosed(executor);
	}

	private void forceExecutorShutdownIfAppClosed(final ExecutorService executor) {
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorRemoved(AncestorEvent arg0) {
				// do nothing
			}

			@Override
			public void ancestorMoved(AncestorEvent arg0) {
				// do nothing
			}

			@Override
			public void ancestorAdded(AncestorEvent arg0) {
				JFrame frame = (JFrame) SwingUtilities
						.getWindowAncestor(MapListPanel.this);
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
						executor.shutdownNow();
					}

					@Override
					public void windowClosed(WindowEvent arg0) {
						// do nothing
					}

					@Override
					public void windowActivated(WindowEvent arg0) {
						// do nothing
					}
				});
			}
		});
	}

	private void loadFileIfNecessary(final File file) throws IOException {
		synchronized (maps) {
			if (maps.containsKey(file)) {
				return;
			} else {
				logger.info("Loading " + file.getName() + "...");
				maps.put(file, TranslationUtil.readMap(file));
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						logger.info("Refreshing...");
						TreeModel model = tree.getModel();
						int index = Arrays.binarySearch(files, file);
						DynamicUtilTreeNode node = (DynamicUtilTreeNode) model
								.getChild(model.getRoot(), index);
						model.valueForPathChanged(new TreePath(node.getPath()),
								node.getUserObject());
					}
				});
			}
		}
	}

	private void displayContentOf(File file) throws IOException {
		loadFileIfNecessary(file);
		mapContentPanel.setMap(maps.get(file));
	}
}
