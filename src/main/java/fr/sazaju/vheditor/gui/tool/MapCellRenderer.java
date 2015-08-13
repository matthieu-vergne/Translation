package fr.sazaju.vheditor.gui.tool;

import java.awt.Component;
import java.awt.Font;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import fr.sazaju.vheditor.util.MapInformer;
import fr.sazaju.vheditor.util.MapInformer.NoDataException;
import fr.sazaju.vheditor.util.MapNamer;
import fr.vergne.logging.LoggerConfiguration;

public class MapCellRenderer<MapID> implements TreeCellRenderer {

	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private final TreeCellRenderer basicRenderer;
	private final MapInformer<MapID> informer;

	public MapCellRenderer(TreeCellRenderer basicRenderer,
			MapInformer<MapID> informer) {
		this.basicRenderer = basicRenderer;
		this.informer = informer;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object cell,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (cell instanceof MapTreeNode) {
			@SuppressWarnings("unchecked")
			MapID id = ((MapTreeNode<MapID>) cell).getMapID();

			String description;
			try {
				int remaining = informer.getEntriesRemaining(id);
				int total = informer.getEntriesCount(id);
				if (remaining > 0) {
					int percent = 100 - 100 * remaining / total;
					description = percent + "%, " + remaining + " remaining";
				} else {
					description = "cleared";
				}
			} catch (NoDataException e) {
				description = "loading";
			}

			String label = mapNamer.getNameFor(id) + " (" + description + ")";
			DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) basicRenderer
					.getTreeCellRendererComponent(tree, label, selected,
							expanded, leaf, row, hasFocus);
			try {
				if (informer.getEntriesRemaining(id) == 0) {
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

	private MapNamer<MapID> mapNamer = new MapNamer<MapID>() {

		@Override
		public String getNameFor(MapID id) {
			return id.toString();
		}
	};

	public void setMapNamer(MapNamer<MapID> mapNamer) {
		this.mapNamer = mapNamer;
	}

	public MapNamer<MapID> getMapNamer() {
		return mapNamer;
	}
}
