package fr.sazaju.vheditor.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.parsing.BackedTranslationMap;
import fr.sazaju.vheditor.translation.parsing.BackedTranslationMap.EmptyMapException;
import fr.vergne.logging.LoggerConfiguration;

@SuppressWarnings("serial")
public class MapListPanel extends JPanel {

	private static final String CONFIG_CLEARED_DISPLAYED = "clearedDisplayed";
	private static final String CONFIG_MAP_DIR = "mapDir";
	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private final JTextField folderPathField = new JTextField();
	private final JTree tree;
	private final Map<File, MapDescriptor> mapDescriptors = Collections
			.synchronizedMap(new HashMap<File, MapDescriptor>());
	private int displayedDescriptors = 0;
	private final TreeSet<File> currentFiles = new TreeSet<File>(
			new Comparator<File>() {

				@Override
				public int compare(File f1, File f2) {
					return f1.getName().compareToIgnoreCase(f2.getName());
				}
			});

	public MapListPanel() {
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
		tree = buildTreeComponent();
		add(new JScrollPane(tree), constraints);

		constraints.gridy++;
		constraints.fill = GridBagConstraints.NONE;
		constraints.weighty = 0;
		JPanel options = buildQuickOptions();
		add(options, constraints);

		updateFiles(new File(Gui.config.getProperty(CONFIG_MAP_DIR, "")));
	}

	private JPanel buildQuickOptions() {
		JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout());
		final JCheckBox displayCleared = new JCheckBox();
		displayCleared.setAction(new AbstractAction("Cleared") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean selected = displayCleared.isSelected();
				Gui.config.setProperty(CONFIG_CLEARED_DISPLAYED, "" + selected);
				((ListModel) tree.getModel()).setClearedDisplayed(selected);
				refreshTree(true);
			}
		});
		displayCleared.setSelected(((ListModel) tree.getModel())
				.isClearedDisplayed());
		displayCleared.setToolTipText("Display cleared maps.");
		buttons.add(displayCleared);
		return buttons;
	}

	private void updateFiles(File folder) {
		synchronized (mapDescriptors) {
			List<File> newFiles = Arrays.asList(retrieveFiles(folder));
			if (currentFiles.containsAll(newFiles)
					&& newFiles.containsAll(currentFiles)) {
				// same files, don't change
			} else {
				Collection<File> removed = new LinkedList<File>(currentFiles);
				removed.removeAll(newFiles);
				for (File file : removed) {
					mapDescriptors.remove(file);
				}
				currentFiles.clear();
				currentFiles.addAll(newFiles);
				displayedDescriptors = 0;

				Gui.config.setProperty(CONFIG_MAP_DIR, folder.toString());
				folderPathField.setText(folder.toString());
				((ListModel) tree.getModel()).setFiles(currentFiles);
				runFilesLoadingInBackground(currentFiles);
			}
		}
	}

	private void refreshTree(boolean force) {
		synchronized (mapDescriptors) {
			if (force || mapDescriptors.size() > displayedDescriptors) {
				logger.info("Refreshing...");
				ListModel model = (ListModel) tree.getModel();
				TreePath[] selection = tree.getSelectionPaths();
				if (!model.isClearedDisplayed() && selection != null
						&& selection.length > 0) {
					LinkedList<TreePath> paths = new LinkedList<TreePath>(
							Arrays.asList(selection));
					Iterator<TreePath> iterator = paths.iterator();
					while (iterator.hasNext()) {
						TreePath path = iterator.next();
						File file = (File) ((DefaultMutableTreeNode) path
								.getLastPathComponent()).getUserObject();
						MapDescriptor descriptor = mapDescriptors.get(file);
						if (descriptor != null && descriptor.remaining == 0) {
							iterator.remove();
						} else {
							// still present
						}
					}
					selection = paths.toArray(new TreePath[paths.size()]);
					tree.clearSelection();
					tree.setSelectionPaths(selection);
				} else {
					// no selection to update
				}

				for (TreeModelListener listener : ((ListModel) tree.getModel())
						.getTreeModelListeners()) {
					listener.treeStructureChanged(new TreeModelEvent(tree,
							new Object[] { tree.getModel().getRoot() }));
				}
				tree.setSelectionPaths(selection);
				displayedDescriptors = mapDescriptors.size();
			} else {
				// already refreshed
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

	private JTree buildTreeComponent() {
		final JTree tree = new JTree(new ListModel(mapDescriptors,
				Boolean.parseBoolean(Gui.config.getProperty(
						CONFIG_CLEARED_DISPLAYED, "true"))));
		tree.setRootVisible(false);
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
					MapDescriptor descriptor = mapDescriptors.get(file);
					String description;
					if (descriptor == null) {
						description = "loading";
					} else if (descriptor.remaining == 0) {
						description = "cleared";
					} else {
						int percent = 100 - 100 * descriptor.remaining
								/ descriptor.total;
						description = percent + "%, " + descriptor.remaining
								+ " remaining";
					}
					value = value + " (" + description + ")";
					DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) defaultRenderer
							.getTreeCellRendererComponent(tree, value,
									selected, expanded, leaf, row, hasFocus);
					renderer.setFont(renderer.getFont().deriveFont(
							descriptor == null ? Font.ITALIC : Font.PLAIN));
					if (descriptor != null && descriptor.remaining == 0) {
						renderer.setEnabled(false);
					} else {
						// keep it as is
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
				synchronized (mapDescriptors) {
					if (event.getButton() == MouseEvent.BUTTON1
							&& event.getClickCount() == 2) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
								.getSelectionPath().getLastPathComponent();
						File file = (File) node.getUserObject();
						updateMapDescriptor(file, false);
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
		return tree;
	}

	private void runFilesLoadingInBackground(TreeSet<File> files) {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		for (final File file : files) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					if (currentFiles.contains(file)) {
						try {
							/*
							 * Brake to avoid an overloading effect for fast
							 * computers. Files can be loaded on demand anyway,
							 * so it is not blocking, only the display of the
							 * translations advancement is delayed.
							 */
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
						updateMapDescriptor(file, false);
					} else {
						// different folder selected meanwhile
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
							// do nothing
						}

						@Override
						public void windowClosed(WindowEvent arg0) {
							executor.shutdownNow();
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

	public void updateMapDescriptor(final File file, boolean force) {
		synchronized (mapDescriptors) {
			if (!force && mapDescriptors.get(file) != null) {
				// nothing to load
			} else {
				logger.info("Loading " + file.getName() + "...");
				MapDescriptor descriptor = new MapDescriptor();
				try {
					BackedTranslationMap map = new BackedTranslationMap(file);
					descriptor.total = map.sizeUsed();
					descriptor.remaining = 0;
					Iterator<? extends TranslationEntry> iterator = map
							.iteratorUsed();
					while (iterator.hasNext()) {
						TranslationEntry entry = iterator.next();
						descriptor.remaining += entry.isActuallyTranslated() ? 0
								: 1;
					}
				} catch (EmptyMapException e) {
					descriptor.total = 0;
					descriptor.remaining = 0;
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				mapDescriptors.put(file, descriptor);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						refreshTree(false);
					}
				});
			}
		}
	}

	public static class MapDescriptor {
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

}
