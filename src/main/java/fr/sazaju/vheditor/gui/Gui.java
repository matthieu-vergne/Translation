package fr.sazaju.vheditor.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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
import java.util.Collection;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.gui.tool.Search;
import fr.sazaju.vheditor.gui.tool.ToolProvider;

@SuppressWarnings("serial")
public class Gui extends JFrame {

	private static final String ACTION_NEXT_JAP = "nextJap";
	private static final String ACTION_NEXT_UNTRANSLATED = "nextUntranslated";
	private static final String ACTION_LAST_ENTRY = "lastEntry";
	private static final String ACTION_FIRST_ENTRY = "firstEntry";
	private static final String ACTION_NEXT_ENTRY = "nextEntry";
	private static final String ACTION_PREVIOUS_ENTRY = "previousEntry";
	private static final String ACTION_SAVE = "save";
	private static final String ACTION_RESET = "reset";
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
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

		final MapListPanel listPanel = new MapListPanel();
		final MapContentPanel mapPanel = new MapContentPanel();
		configureListeners(listPanel, mapPanel);
		ToolPanel toolPanel = new ToolPanel();

		final ToolProvider provider = new ToolProvider() {

			@Override
			public Collection<File> getMapFiles() {
				return listPanel.getFiles();
			}

			@Override
			public void loadMap(File mapFile) {
				mapPanel.setMap(mapFile);
			}

			@Override
			public void loadMapEntry(File mapFile, int entryIndex) {
				mapPanel.setMap(mapFile, entryIndex);
			}
		};
		configureTools(toolPanel, provider);

		JPanel translationPanel = new JPanel();
		translationPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 0;
		translationPanel.add(configureButtons(mapPanel), constraints);
		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 0;
		constraints.weighty = 1;
		translationPanel.add(toolPanel, constraints);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 2;
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

		finalizeConfig(rootSplit);
	}

	private void configureTools(final ToolPanel toolPanel,
			final ToolProvider provider) {
		Search tool = new Search();
		tool.setToolProvider(provider);
		toolPanel.addTool(tool);
	}

	private void finalizeConfig(final JSplitPane rootSplit) {
		setLocation(Integer.parseInt(config.getProperty(CONFIG_X, "0")),
				Integer.parseInt(config.getProperty(CONFIG_Y, "0")));
		setSize(new Dimension(Integer.parseInt(config.getProperty(CONFIG_WIDTH,
				"700")), Integer.parseInt(config.getProperty(CONFIG_HEIGHT,
				"500"))));
		rootSplit.setDividerLocation(Integer.parseInt(config.getProperty(
				CONFIG_SPLIT, "" + (getWidth() * 4 / 10))));
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
	}

	private void configureListeners(final MapListPanel listPanel,
			final MapContentPanel mapPanel) {
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
	}

	private JPanel configureButtons(final MapContentPanel mapPanel) {
		ActionMap actions = getRootPane().getActionMap();
		InputMap inputs = getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		actions.put(ACTION_PREVIOUS_ENTRY, new AbstractAction("<") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(mapPanel.getCurrentEntryIndex() - 1);
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
				InputEvent.ALT_DOWN_MASK), ACTION_PREVIOUS_ENTRY);
		JButton previous = new JButton(actions.get(ACTION_PREVIOUS_ENTRY));
		previous.setToolTipText("Go to previous entry (ALT+LEFT).");

		actions.put(ACTION_NEXT_ENTRY, new AbstractAction(">") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(mapPanel.getCurrentEntryIndex() + 1);
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
				InputEvent.ALT_DOWN_MASK), ACTION_NEXT_ENTRY);
		JButton next = new JButton(actions.get(ACTION_NEXT_ENTRY));
		next.setToolTipText("Go to next entry (ALT+RIGHT).");

		actions.put(ACTION_FIRST_ENTRY, new AbstractAction("|<") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(0);
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
				InputEvent.ALT_DOWN_MASK), ACTION_FIRST_ENTRY);
		JButton first = new JButton(actions.get(ACTION_FIRST_ENTRY));
		first.setToolTipText("Go to first entry (ALT+HOME).");

		actions.put(ACTION_LAST_ENTRY, new AbstractAction(">|") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(mapPanel.getMap().sizeUsed() - 1);
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,
				InputEvent.ALT_DOWN_MASK), ACTION_LAST_ENTRY);
		JButton last = new JButton(actions.get(ACTION_LAST_ENTRY));
		last.setToolTipText("Go to last entry (ALT+END).");

		actions.put(ACTION_NEXT_JAP, new AbstractAction("Jap only") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToNextUntranslatedEntry(false);
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
				InputEvent.ALT_DOWN_MASK), ACTION_NEXT_JAP);
		JButton japOnly = new JButton(actions.get(ACTION_NEXT_JAP));
		japOnly.setToolTipText("Go to next untranslated entry (ALT+ENTER).");

		actions.put(ACTION_NEXT_UNTRANSLATED, new AbstractAction("#UNTRANSLATED") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToNextUntranslatedEntry(true);
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
				InputEvent.ALT_DOWN_MASK + InputEvent.CTRL_DOWN_MASK), ACTION_NEXT_UNTRANSLATED);
		JButton untranslated = new JButton(actions.get(ACTION_NEXT_UNTRANSLATED));
		untranslated
				.setToolTipText("Go to next #UNTRANSLATED entry (CTRL+ALT+ENTER).");

		actions.put(ACTION_SAVE, new AbstractAction("Save") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.save();
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_DOWN_MASK), ACTION_SAVE);
		JButton save = new JButton(actions.get(ACTION_SAVE));
		save.setToolTipText("Write the modifications to the map file (CTRL+S).");

		actions.put(ACTION_RESET, new AbstractAction("Reset") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.reset();
			}
		});
		// no key binding to avoid wrong manipulation
		JButton reset = new JButton(actions.get(ACTION_RESET));
		reset.setToolTipText("Cancel all the modifications.");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new EtchedBorder());
		buttonPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		Insets closeInsets = new Insets(5, 5, 5, 5);
		Insets farInsets = new Insets(20, 5, 5, 5);
		constraints.insets = closeInsets;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;

		buttonPanel.add(previous, constraints);
		constraints.gridx++;
		buttonPanel.add(next, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		buttonPanel.add(first, constraints);
		constraints.gridx++;
		buttonPanel.add(last, constraints);

		constraints.gridwidth = 2;
		
		constraints.gridx = 0;
		constraints.gridy++;
		buttonPanel.add(japOnly, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		buttonPanel.add(untranslated, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.insets = farInsets;
		buttonPanel.add(save, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.insets = closeInsets;
		buttonPanel.add(reset, constraints);

		return buttonPanel;
	}

	private boolean isMapSafe(final MapContentPanel mapContentPanel) {
		boolean mapSafe = !mapContentPanel.isModified();
		if (!mapSafe) {
			int answer = JOptionPane.showOptionDialog(Gui.this,
					"The map has been modified. Would you like to save it?",
					"Save the Current Map?", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null, new String[] { "Yes",
							"No", "Cancel" }, "Cancel");
			if (answer == JOptionPane.YES_OPTION) {
				mapContentPanel.save();
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

	public static void main(String[] args) {
		new Runnable() {
			public void run() {
				new Gui().setVisible(true);
			}
		}.run();
	}
}
