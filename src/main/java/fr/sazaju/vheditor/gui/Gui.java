package fr.sazaju.vheditor.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class Gui extends JFrame {

	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	public static final File configFile = new File("vh-editor.ini");
	public static final Properties config = new Properties() {
		public synchronized Object setProperty(String key, String value) {
			Object result = super.setProperty(key, value);
			try {
				configFile.createNewFile();
				store(new FileOutputStream(configFile), null);
			} catch (IOException e) {
				throw new RuntimeException(
						"Impossible to create the config file " + configFile, e);
			}
			return result;
		};
	};

	public Gui() {
		try {
			InputStream is = new FileInputStream(configFile);
			config.load(is);
		} catch (FileNotFoundException e) {
			// the file just does not exist yet
		} catch (IOException e) {
			throw new RuntimeException("Impossible to load the config file "
					+ configFile, e);
		}

		setTitle("VH Translation Tool");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(Integer.parseInt(config.getProperty(
				WIDTH, "700")), Integer.parseInt(config.getProperty(HEIGHT,
				"500"))));
		addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent arg0) {
				// nothing to do
			}

			@Override
			public void componentResized(ComponentEvent arg0) {
				config.setProperty(WIDTH, "" + getWidth());
				config.setProperty(HEIGHT, "" + getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
				// nothing to do
			}

			@Override
			public void componentHidden(ComponentEvent arg0) {
				// nothing to do
			}
		});

		MapListPanel mapListPanel = new MapListPanel();
		MapContentPanel mapContentPanel = new MapContentPanel();
		MapToolsPanel toolsPanel = new MapToolsPanel();
		configureListeners(mapListPanel, mapContentPanel, toolsPanel);

		JPanel translationPanel = new JPanel();
		translationPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 0;
		translationPanel.add(toolsPanel, constraints);
		constraints.gridx = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		translationPanel.add(mapContentPanel, constraints);

		final JSplitPane rootSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rootSplit.setLeftComponent(mapListPanel);
		rootSplit.setRightComponent(translationPanel);
		rootSplit.setResizeWeight(1.0 / 3);

		setLayout(new GridLayout(1, 1));
		add(rootSplit);

		pack();
		rootSplit.setDividerLocation(rootSplit.getResizeWeight());
	}

	private void configureListeners(final MapListPanel mapListPanel,
			final MapContentPanel mapContentPanel,
			final MapToolsPanel toolsPanel) {
		mapListPanel.addListener(new MapListPanel.FileSelectedListener() {

			@Override
			public void fileSelected(File file) {
				mapListPanel.retrieveMapDescriptor(file, false);
				mapContentPanel.setMap(file);
			}
		});

		toolsPanel.addListener(new MapToolsPanel.NextEntryListener() {

			@Override
			public void buttonPushed() {
				mapContentPanel.goToEntry(mapContentPanel
						.getDisplayedEntryIndex() + 1);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.PreviousEntryListener() {

			@Override
			public void buttonPushed() {
				mapContentPanel.goToEntry(mapContentPanel
						.getDisplayedEntryIndex() - 1);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.FirstEntryListener() {

			@Override
			public void buttonPushed() {
				mapContentPanel.goToEntry(0);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.LastEntryListener() {

			@Override
			public void buttonPushed() {
				mapContentPanel
						.goToEntry(mapContentPanel.getMap().sizeUsed() - 1);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.UntranslatedEntryListener() {

			@Override
			public void buttonPushed() {
				TreeSet<Integer> untranslatedEntries = new TreeSet<Integer>(
						mapContentPanel.getUntranslatedEntryIndexes());
				if (untranslatedEntries.isEmpty()) {
					JOptionPane.showMessageDialog(Gui.this,
							"All the entries are already translated.");
				} else {
					int currentEntry = mapContentPanel.getDisplayedEntryIndex();
					Integer next = untranslatedEntries
							.ceiling(currentEntry + 1);
					if (next == null) {
						JOptionPane
								.showMessageDialog(Gui.this,
										"End of the entries reached. Search from the beginning.");
						mapContentPanel.goToEntry(untranslatedEntries.first());
					} else {
						mapContentPanel.goToEntry(next);
					}
				}
			}
		});
		toolsPanel.addListener(new MapToolsPanel.SaveMapListener() {

			@Override
			public void buttonPushed() {
				mapContentPanel.applyModifications();
			}
		});
		toolsPanel.addListener(new MapToolsPanel.ResetMapListener() {

			@Override
			public void buttonPushed() {
				mapContentPanel.cancelModifications();
			}
		});
	}

	public static void main(String[] args) {
		new Runnable() {
			public void run() {
				new Gui().setVisible(true);
			}
		}.run();
	}
}
