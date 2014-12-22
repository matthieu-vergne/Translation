package fr.sazaju.vheditor.gui.tool;

import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import fr.sazaju.vheditor.util.MapInformer;
import fr.sazaju.vheditor.util.MapInformer.NoDataException;
import fr.sazaju.vheditor.util.MapNamer;
import fr.vergne.logging.LoggerConfiguration;

public class MapCellRenderer implements TreeCellRenderer {

	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private final TreeCellRenderer basicRenderer;
	private final MapInformer informer;

	public MapCellRenderer(TreeCellRenderer basicRenderer, MapInformer informer) {
		this.basicRenderer = basicRenderer;
		this.informer = informer;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object cell,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
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
			value = mapNamer.getNameFor(file);

			String description;
			try {
				int remaining = informer.getEntriesRemaining(file);
				int total = informer.getEntriesCount(file);
				if (remaining > 0) {
					int percent = 100 - 100 * remaining / total;
					description = percent + "%, " + remaining + " remaining";
				} else {
					description = "cleared";
				}
			} catch (NoDataException e) {
				description = "loading";
			}
			value = value + " (" + description + ")";

			DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) basicRenderer
					.getTreeCellRendererComponent(tree, value, selected,
							expanded, leaf, row, hasFocus);
			try {
				if (informer.getEntriesRemaining(file) == 0) {
					renderer.setEnabled(false);
				} else {
					// keep it as is
				}
				// TODO Remove?
				renderer.setFont(renderer.getFont().deriveFont(Font.PLAIN));
			} catch (NoDataException e) {
				renderer.setFont(renderer.getFont().deriveFont(Font.ITALIC));
			}

			return renderer;
		} else {
			return basicRenderer.getTreeCellRendererComponent(tree, value,
					selected, expanded, leaf, row, hasFocus);
		}
	}

	private MapNamer mapNamer = new MapNamer() {

		@Override
		public String getNameFor(File file) {
			return file.getName();
		}
	};

	public void setMapNamer(MapNamer mapNamer) {
		this.mapNamer = mapNamer;
	}

	public MapNamer getMapNamer() {
		return mapNamer;
	}
}
