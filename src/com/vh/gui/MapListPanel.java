package com.vh.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
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
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
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

// TODO make the current folder persistent in an INI file
// TODO save modifications (manage CTRL+S shortcut)
@SuppressWarnings("serial")
public class MapListPanel extends JPanel {

	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private final MapContentPanel mapContentPanel;
	private final JTree tree;
	private final Map<File, TranslationMap> loadedMaps = Collections
			.synchronizedMap(new HashMap<File, TranslationMap>());
	private File[] currentFiles;

	public MapListPanel(MapContentPanel mapContentPanel) {
		this.mapContentPanel = mapContentPanel;

		setBorder(new EtchedBorder());

		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		add(buildFileChooserPanel(), constraints);

		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 1;
		tree = buildTreeComponent();
		add(new JScrollPane(tree), constraints);

		refreshTree(new File("."));
	}

	private void refreshTree(File folder) {
		currentFiles = retrieveFiles(folder);
		tree.setModel(new JTree(currentFiles).getModel());
		runFilesLoadingInBackground(currentFiles);
	}

	private JPanel buildFileChooserPanel() {
		final JTextField folderPathField = new JTextField();

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
					File folder = fileChooser.getSelectedFile();
					String fullPath = folder.getPath();
					try {
						String localPath = new File(".").getCanonicalPath();
						fullPath = fullPath.replaceAll(
								"^" + Pattern.quote(localPath) + "/?", "");
					} catch (IOException e) {
						e.printStackTrace();
					}
					folderPathField.setText(fullPath);
					refreshTree(folder);
				} else {

				}
			}
		});

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
				return !file.isHidden() && file.isFile()
						&& file.getName().startsWith("Map");
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

	private JTree buildTreeComponent() {
		final JTree tree = new JTree(new Object[0]);
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
					map = loadedMaps.get(file);
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

	private void runFilesLoadingInBackground(File[] currentFiles) {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		for (final File file : currentFiles) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					try {
						try {
							/*
							 * Brake to avoid an overloading effect for fast
							 * computers. Files can be loaded on demand anyway,
							 * so it is not blocking, only the display of the
							 * translations advancement is delayed.
							 */
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
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
			}
		});
	}

	private void loadFileIfNecessary(final File file) throws IOException {
		synchronized (loadedMaps) {
			if (loadedMaps.containsKey(file)) {
				return;
			} else {
				logger.info("Loading " + file.getName() + "...");
				loadedMaps.put(file, TranslationUtil.readMap(file));
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						logger.info("Refreshing...");
						TreeModel model = tree.getModel();
						int index = Arrays.binarySearch(currentFiles, file);
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
		mapContentPanel.setMap(loadedMaps.get(file));
	}
}
