package fr.sazaju.vheditor.gui.tool;

import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import fr.sazaju.vheditor.util.MapInformer;
import fr.sazaju.vheditor.util.MapInformer.NoDataException;
import fr.sazaju.vheditor.util.MapNamer;
import fr.vergne.logging.LoggerConfiguration;

public class MapCellRenderer implements TreeCellRenderer {

	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private final TreeCellRenderer basicRenderer;
	// TODO generalize to any type, not only File
	private final MapInformer<File> informer;

	// TODO generalize to any type, not only File
	public MapCellRenderer(TreeCellRenderer basicRenderer,
			MapInformer<File> informer) {
		this.basicRenderer = basicRenderer;
		this.informer = informer;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object cell,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (cell instanceof MapTreeNode) {
			@SuppressWarnings("unchecked")
			File file = ((MapTreeNode<File>) cell).getMapID();

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

			String value = mapNamer.getNameFor(file) + " (" + description + ")";
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
			return basicRenderer.getTreeCellRendererComponent(tree, cell,
					selected, expanded, leaf, row, hasFocus);
		}
	}

	// TODO generalize to any type, not only File
	private MapNamer<File> mapNamer = new MapNamer<File>() {

		@Override
		public String getNameFor(File file) {
			return file.getName();
		}
	};

	// TODO generalize to any type, not only File
	public void setMapNamer(MapNamer<File> mapNamer) {
		this.mapNamer = mapNamer;
	}

	// TODO generalize to any type, not only File
	public MapNamer<File> getMapNamer() {
		return mapNamer;
	}
}
