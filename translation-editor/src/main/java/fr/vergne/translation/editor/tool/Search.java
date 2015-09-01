package fr.vergne.translation.editor.tool;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
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

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationProject;

@SuppressWarnings("serial")
public class Search<MapID> extends JPanel implements Tool<MapID> {

	private ToolProvider<MapID> provider;
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
					final String blanks = "[\\s\u3000]++";
					final String searched = input.getText().replaceAll(blanks,
							" ");
					final TranslationProject<?, MapID, ?> project = provider
							.getProject();
					List<MapID> ids = new LinkedList<>();
					for (MapID mapID : project) {
						ids.add(mapID);
					}
					Collections.sort(ids, new Comparator<MapID>() {

						@Override
						public int compare(MapID id1, MapID id2) {
							return id1.toString().compareToIgnoreCase(
									id2.toString());
						}
					});
					final Iterator<MapID> projectIterator = ids.iterator();
					SwingUtilities.invokeLater(new Runnable() {

						private Iterator<? extends TranslationEntry<?>> mapIterator = Collections
								.<TranslationEntry<?>> emptyList().iterator();
						private MapID id;
						private int index;

						@Override
						public void run() {
							if (!searching) {
								// stopped
							} else if (!projectIterator.hasNext()) {
								// finished
								searchButton.doClick();
							} else if (!mapIterator.hasNext()) {
								id = projectIterator.next();
								TranslationMap<?> map = project.getMap(id);
								mapIterator = map.iterator();
								index = 0;
								SwingUtilities.invokeLater(this);
							} else {
								TranslationEntry<?> entry = mapIterator.next();
								String original = entry.getOriginalContent()
										.replaceAll(blanks, " ");
								String translation = entry
										.getCurrentTranslation().replaceAll(
												blanks, " ");
								if (original.contains(searched)
										|| translation.contains(searched)) {
									addResult(new Result<MapID>(id, index));
								} else {
									// not this entry
								}
								index++;
								SwingUtilities.invokeLater(this);
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
					@SuppressWarnings("unchecked")
					Result<MapID> target = (Result<MapID>) node.getUserObject();
					provider.loadMapEntry(target.getMapID(),
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
					@SuppressWarnings("unchecked")
					Result<MapID> result = (Result<MapID>) value;
					MapID id = result.getMapID();
					int index = result.getEntryIndex();
					value = id + "(" + index + ")";
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
	public void setToolProvider(ToolProvider<MapID> provider) {
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

	private void addResult(final Result<MapID> result) {
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

	private static class Result<MapID> {
		private final MapID mapId;
		private final int entryIndex;

		public Result(MapID mapId, int entryIndex) {
			this.mapId = mapId;
			this.entryIndex = entryIndex;
		}

		public MapID getMapID() {
			return mapId;
		}

		public int getEntryIndex() {
			return entryIndex;
		}
	}
}
