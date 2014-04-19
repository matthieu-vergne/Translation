package fr.sazaju.vheditor.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

// TODO manage persistent config file (VH folder, ...)
@SuppressWarnings("serial")
public class Gui extends JFrame {

	private final MapListPanel mapListPanel;
	private final MapContentPanel mapContentPanel;
	private final MapToolsPanel toolsPanel;
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
		setMinimumSize(new Dimension(800, 500));

		toolsPanel = new MapToolsPanel();
		mapContentPanel = new MapContentPanel(toolsPanel);
		mapListPanel = new MapListPanel(mapContentPanel);

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

	public static void main(String[] args) {
		new Runnable() {
			public void run() {
				new Gui().setVisible(true);
			}
		}.run();
	}
}
