package fr.sazaju.vheditor.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class Gui extends JFrame {

	private static final String ACTION_NEXT_JAP = "nextJap";
	private static final String ACTION_LAST_ENTRY = "lastEntry";
	private static final String ACTION_FIRST_ENTRY = "firstEntry";
	private static final String ACTION_NEXT_ENTRY = "nextEntry";
	private static final String ACTION_PREVIOUS_ENTRY = "previousEntry";
	private static final String ACTION_SAVE = "save";
	private static final String CONFIG_Y = "y";
	private static final String CONFIG_X = "x";
	private static final String CONFIG_WIDTH = "width";
	private static final String CONFIG_HEIGHT = "height";
	private static final String CONFIG_SPLIT = "split";
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
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setPreferredSize(new Dimension(Integer.parseInt(config.getProperty(
				CONFIG_WIDTH, "700")), Integer.parseInt(config.getProperty(
				CONFIG_HEIGHT, "500"))));
		setLocation(Integer.parseInt(config.getProperty(CONFIG_X, "0")),
				Integer.parseInt(config.getProperty(CONFIG_Y, "0")));
		addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent arg0) {
				// nothing to do
			}

			@Override
			public void componentResized(ComponentEvent arg0) {
				config.setProperty(CONFIG_WIDTH, "" + getWidth());
				config.setProperty(CONFIG_HEIGHT, "" + getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
				config.setProperty(CONFIG_X, "" + getX());
				config.setProperty(CONFIG_Y, "" + getY());
			}

			@Override
			public void componentHidden(ComponentEvent arg0) {
				// nothing to do
			}
		});

		MapListPanel listPanel = new MapListPanel();
		MapContentPanel mapPanel = new MapContentPanel();
		MapToolsPanel toolsPanel = new MapToolsPanel();
		configureMapListeners(listPanel, mapPanel, toolsPanel);

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
		translationPanel.add(mapPanel, constraints);

		final JSplitPane rootSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rootSplit.setLeftComponent(listPanel);
		rootSplit.setRightComponent(translationPanel);
		rootSplit.setResizeWeight(1.0 / 3);

		setLayout(new GridLayout(1, 1));
		add(rootSplit);

		pack();
		int min = rootSplit.getMinimumDividerLocation();
		min = min == -1 ? 0 : min;
		int max = rootSplit.getMaximumDividerLocation();
		max = max == -1 ? rootSplit.getWidth() : max;
		rootSplit.setDividerLocation(Integer.parseInt(config.getProperty(
				CONFIG_SPLIT, "" + (min + (max - min) * 4 / 10))));
		rootSplit.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getPropertyName().equals(
						JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
					config.setProperty(CONFIG_SPLIT, "" + event.getNewValue());
				} else {
					// do not care about other properties
				}
			}
		});
	}

	private void configureMapListeners(final MapListPanel listPanel,
			final MapContentPanel mapPanel, final MapToolsPanel toolsPanel) {
		ActionMap actions = getRootPane().getActionMap();
		actions.put(ACTION_SAVE, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.applyModifications();
			}
		});
		actions.put(ACTION_PREVIOUS_ENTRY, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(mapPanel.getDisplayedEntryIndex() - 1);
			}
		});
		actions.put(ACTION_NEXT_ENTRY, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(mapPanel.getDisplayedEntryIndex() + 1);
			}
		});
		actions.put(ACTION_FIRST_ENTRY, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(0);
			}
		});
		actions.put(ACTION_LAST_ENTRY, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(mapPanel.getMap().sizeUsed() - 1);
			}
		});
		actions.put(ACTION_NEXT_JAP, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				goToNextUntranslatedEntry(mapPanel);
			}
		});

		InputMap inputs = getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_DOWN_MASK), ACTION_SAVE);
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
				InputEvent.ALT_DOWN_MASK), ACTION_PREVIOUS_ENTRY);
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
				InputEvent.ALT_DOWN_MASK), ACTION_NEXT_ENTRY);
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
				InputEvent.ALT_DOWN_MASK), ACTION_FIRST_ENTRY);
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,
				InputEvent.ALT_DOWN_MASK), ACTION_LAST_ENTRY);
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
				InputEvent.ALT_DOWN_MASK), ACTION_NEXT_JAP);

		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent arg0) {
				// nothing to do
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// nothing to do
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// nothing to do
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// nothing to do
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				if (isMapSafe(mapPanel)) {
					dispose();
				} else {
					// map unsafe
				}
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// nothing to do
			}

			@Override
			public void windowActivated(WindowEvent arg0) {
				// nothing to do
			}
		});

		listPanel.addListener(new MapListPanel.FileSelectedListener() {

			@Override
			public void fileSelected(File file) {
				if (isMapSafe(mapPanel)) {
					mapPanel.setMap(file);
				} else {
					// map unsafe
				}
			}
		});

		toolsPanel.addListener(new MapToolsPanel.NextEntryListener() {

			@Override
			public void buttonPushed() {
				mapPanel.goToEntry(mapPanel.getDisplayedEntryIndex() + 1);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.PreviousEntryListener() {

			@Override
			public void buttonPushed() {
				mapPanel.goToEntry(mapPanel.getDisplayedEntryIndex() - 1);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.FirstEntryListener() {

			@Override
			public void buttonPushed() {
				mapPanel.goToEntry(0);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.LastEntryListener() {

			@Override
			public void buttonPushed() {
				mapPanel.goToEntry(mapPanel.getMap().sizeUsed() - 1);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.UntranslatedEntryListener() {

			@Override
			public void buttonPushed() {
				goToNextUntranslatedEntry(mapPanel);
			}

		});
		toolsPanel.addListener(new MapToolsPanel.SaveMapListener() {

			@Override
			public void buttonPushed() {
				mapPanel.applyModifications();
				listPanel.updateMapDescriptor(mapPanel.getMap().getBaseFile(),
						true);
			}
		});
		toolsPanel.addListener(new MapToolsPanel.ResetMapListener() {

			@Override
			public void buttonPushed() {
				mapPanel.cancelModifications();
			}
		});
	}

	private boolean isMapSafe(final MapContentPanel mapContentPanel) {
		boolean mapSafe = !mapContentPanel.isModified();
		if (!mapSafe) {
			int answer = JOptionPane.showConfirmDialog(Gui.this,
					"The map has been modified. Would you like to save it?");
			if (answer == JOptionPane.YES_OPTION) {
				mapContentPanel.applyModifications();
				mapSafe = true;
			} else if (answer == JOptionPane.NO_OPTION) {
				mapSafe = true;
			} else if (answer == JOptionPane.CANCEL_OPTION) {
				// cancel the request
			} else {
				throw new IllegalStateException("Unmanaged answer: " + answer);
			}
		} else {
			// already safe
		}
		return mapSafe;
	}

	private void goToNextUntranslatedEntry(final MapContentPanel mapPanel) {
		TreeSet<Integer> untranslatedEntries = new TreeSet<Integer>(
				mapPanel.getUntranslatedEntryIndexes());
		if (untranslatedEntries.isEmpty()) {
			JOptionPane.showMessageDialog(Gui.this,
					"All the entries are already translated.");
		} else {
			int currentEntry = mapPanel.getDisplayedEntryIndex();
			Integer next = untranslatedEntries.ceiling(currentEntry + 1);
			if (next == null) {
				JOptionPane
						.showMessageDialog(Gui.this,
								"End of the entries reached. Search from the beginning.");
				mapPanel.goToEntry(untranslatedEntries.first());
			} else {
				mapPanel.goToEntry(next);
			}
		}
	}

	public static void main(String[] args) {
		new Runnable() {
			public void run() {
				new Gui().setVisible(true);
			}
		}.run();
	}
}
