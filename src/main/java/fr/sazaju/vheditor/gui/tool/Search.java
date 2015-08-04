package fr.sazaju.vheditor.gui.tool;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.FileUtils;

import fr.sazaju.vheditor.parsing.vh.map.BackedTranslationMap;
import fr.sazaju.vheditor.parsing.vh.map.MapEntry;

@SuppressWarnings("serial")
public class Search extends JPanel implements Tool {

	private ToolProvider provider;
	private final JTree results;
	private boolean searching = false;

	public Search() {
		setMinimumSize(new Dimension(200, 0));

		final JTextArea input = new JTextArea();
		input.setToolTipText("<html>"
				+ "Write here the text you are looking for<br>"
				+ "and then click on the search button.<br>"
				+ "Blank characters, like newlines and spaces,<br>"
				+ "are not considered, so you do not need<br>"
				+ "to respect them." + "</html>");
		results = buildResultList();
		results.setToolTipText("<html>"
				+ "The results of the search are displayed here.<br>"
				+ "Double click on the one you want to open it." + "</html>");
		JToggleButton searchButton = buildSearchButton(input);
		searchButton.setToolTipText("<html>"
				+ "Toggle this button to launch the search.<br>"
				+ "You can stop a search by toggling back<br>"
				+ "this button before the search ends." + "</html>");

		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		{
			JScrollPane scrollPane = new JScrollPane(input);
			int lineHeight = input.getFontMetrics(input.getFont()).getHeight();
			Dimension dimensions = new Dimension(0, 4 * lineHeight);
			scrollPane.setMinimumSize(dimensions);
			add(scrollPane, constraints);
		}

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 0;
		add(searchButton, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		add(new JScrollPane(results), constraints);
	}

	private JToggleButton buildSearchButton(final JTextArea input) {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				JFrame frame = null;
				while (frame == null) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					frame = (JFrame) SwingUtilities
							.getWindowAncestor(Search.this);
				}
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
		});
		final JToggleButton searchButton = new JToggleButton();
		searchButton.setAction(new AbstractAction("Search") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				searching = searchButton.isSelected();
				if (!searching) {
					input.setEnabled(true);
					/*
					 * Expect the remaining threads to stop before another
					 * search is run.
					 */
				} else if (input.getText().trim().isEmpty()) {
					// nothing to search
					searchButton.doClick();
				} else {
					input.setEnabled(false);
					clearResults();
					final String searched = input.getText().replaceAll(
							"[\\s\u3000]++", " ");
					for (final File file : provider.getMapFiles()) {
						executor.submit(new Runnable() {

							@Override
							public void run() {
								if (!searching) {
									// search stopped
								} else {
									searchInMap(file, searched);
								}
							}

						});
					}
					executor.submit(new Runnable() {

						@Override
						public void run() {
							if (searchButton.isSelected()) {
								searchButton.doClick();
							} else {
								// already unselected
							}
						}

					});
				}
			}
		});
		return searchButton;
	}

	private JTree buildResultList() {
		final JTree results = new JTree(new Object[0]);
		results.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		results.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// nothing to do
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// nothing to do
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// nothing to do
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// nothing to do
			}

			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) results
							.getSelectionPath().getLastPathComponent();
					Result target = (Result) node.getUserObject();
					provider.loadMapEntry(target.getMapFile(),
							target.getEntryIndex());
				} else {
					// single click
				}
			}
		});
		final TreeCellRenderer cellRenderer = results.getCellRenderer();
		results.setCellRenderer(new TreeCellRenderer() {

			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				value = ((DefaultMutableTreeNode) value).getUserObject();
				if (value instanceof Result) {
					Result result = (Result) value;
					File file = result.getMapFile();
					int index = result.getEntryIndex();
					value = file.getName() + "(" + index + ")";
				} else {
					// not managed
				}
				return cellRenderer.getTreeCellRendererComponent(tree, value,
						selected, expanded, leaf, row, hasFocus);
			}
		});
		return results;
	}

	@Override
	public void setToolProvider(ToolProvider provider) {
		this.provider = provider;
	}

	@Override
	public String getTitle() {
		return "Search";
	}

	@Override
	public JPanel instantiatePanel() {
		return this;
	}

	private void addResult(final Result result) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				DefaultTreeModel model = (DefaultTreeModel) results.getModel();
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) model
						.getRoot();
				root.add(new DefaultMutableTreeNode(result));
				results.setModel(new DefaultTreeModel(root));
			}
		});
	}

	private void clearResults() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				DefaultTreeModel model = (DefaultTreeModel) results.getModel();
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) model
						.getRoot();
				root.removeAllChildren();
				results.setModel(new DefaultTreeModel(root));
			}
		});
	}

	private void searchInMap(final File file, final String searched) {
		try {
			String blanks = "[\\s\u3000]++";
			String mapContent = FileUtils.readFileToString(file, "UTF-8")
					.replaceAll(blanks, " ");
			if (mapContent.contains(searched)) {
				BackedTranslationMap map = new BackedTranslationMap(file);
				Iterator<MapEntry> iterator = map.iterator();
				int index = -1;
				while (searching && iterator.hasNext()) {
					MapEntry entry = iterator.next();
					index++;
					String original = entry.getOriginalContent().replaceAll(
							blanks, " ");
					String translation = entry.getCurrentTranslation()
							.replaceAll(blanks, " ");
					if (original.contains(searched)
							|| translation.contains(searched)) {
						// overload minimization
						Thread.sleep(100);
						addResult(new Result(file, index));
					} else {
						// not this entry
					}
				}
			} else {
				// not in this map
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// do not care about sleep interruption
		}
	}

	private static class Result {
		private final File mapFile;
		private final int entryIndex;

		public Result(File mapFile, int entryIndex) {
			this.mapFile = mapFile;
			this.entryIndex = entryIndex;
		}

		public File getMapFile() {
			return mapFile;
		}

		public int getEntryIndex() {
			return entryIndex;
		}
	}
}
