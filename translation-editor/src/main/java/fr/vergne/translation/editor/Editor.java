package fr.vergne.translation.editor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationProject;
import fr.vergne.translation.editor.MapListPanel.MapSelectedListener;
import fr.vergne.translation.editor.content.EntryComponentFactory;
import fr.vergne.translation.editor.content.FilterAction;
import fr.vergne.translation.editor.content.MapComponentFactory;
import fr.vergne.translation.editor.content.SimpleEntryComponent;
import fr.vergne.translation.editor.content.SimpleMapComponent;
import fr.vergne.translation.editor.tool.FileBasedProperties;
import fr.vergne.translation.editor.tool.Search;
import fr.vergne.translation.editor.tool.ToolProvider;
import fr.vergne.translation.impl.TranslationUtil;
import fr.vergne.translation.util.EntryFilter;
import fr.vergne.translation.util.ProjectLoader;

@SuppressWarnings("serial")
public class Editor<MapID, TEntry extends TranslationEntry<?>, TMap extends TranslationMap<TEntry>, TProject extends TranslationProject<MapID, TMap>>
		extends JFrame {

	private static final Logger logger = Logger.getLogger(Editor.class
			.getName());
	private static final String ACTION_LAST_ENTRY = "lastEntry";
	private static final String ACTION_FIRST_ENTRY = "firstEntry";
	private static final String ACTION_NEXT_ENTRY = "nextEntry";
	private static final String ACTION_PREVIOUS_ENTRY = "previousEntry";
	private static final String ACTION_SAVE = "save";
	private static final String ACTION_RESET = "reset";
	private static final String CONFIG_Y = "y";
	private static final String CONFIG_X = "x";
	private static final String CONFIG_FILTER = "filter";
	private static final String CONFIG_WIDTH = "width";
	private static final String CONFIG_HEIGHT = "height";
	private static final String CONFIG_SPLIT = "split";
	// TODO make config non-static
	public static final FileBasedProperties config = new FileBasedProperties(
			"vh-editor.ini", true);
	private final ToolProvider<MapID> toolProvider;
	private final MapListPanel<TEntry, TMap, MapID, TProject> listPanel;
	private final MapContentPanel<MapID> mapPanel;
	private final JPanel filters;

	public Editor(ProjectLoader<TProject> projectLoader,
			MapComponentFactory<?> mapComponentFactory) {
		toolProvider = new ToolProvider<MapID>() {

			@Override
			public TranslationProject<MapID, ?> getProject() {
				return listPanel.getProject();
			}

			@Override
			public void loadMap(MapID id) {
				loadMapEntry(id, 0);
			}

			@Override
			public void loadMapEntry(MapID id, int entryIndex) {
				try {
					TranslationMap<?> map = listPanel.getProject().getMap(id);
					mapPanel.setMap(id, entryIndex);
					updateFilters(map, filters, mapPanel);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(Editor.this, e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		mapPanel = new MapContentPanel<MapID>(toolProvider, mapComponentFactory);
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
				mapPanel.alignStoredAndCurrentValues();
				dispose();
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

		listPanel = new MapListPanel<>(projectLoader);
		listPanel.addListener(new MapSelectedListener<MapID>() {

			@Override
			public void mapSelected(MapID id) {
				toolProvider.loadMap(id);
			}
		});

		mapPanel.addUpdateListener(new MapContentPanel.MapUpdateListener<MapID>() {

			@Override
			public void mapModified(final MapID id,
					final boolean isDifferentFromStore) {
				listPanel.setModifiedStatus(id, isDifferentFromStore);
				if (isDifferentFromStore) {
					// do not care about new progress
				} else {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							listPanel.updateMapSummary(id, true);
						}
					});
				}
			}
		});
		ToolPanel toolPanel = new ToolPanel();

		filters = new JPanel();

		configureTools(toolPanel, toolProvider);

		JPanel translationPanel = new JPanel();
		translationPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 0;
		translationPanel.add(configureButtons(mapPanel, filters), constraints);
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

		setTitle("Translation Editor");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

		pack();

		finalizeConfig(rootSplit);
	}

	public Editor(ProjectLoader<TProject> projectLoader,
			final EntryComponentFactory<?> entryFactory) {
		this(projectLoader, new MapComponentFactory<SimpleMapComponent>() {

			@Override
			public SimpleMapComponent createMapComponent(TranslationMap<?> map) {
				return new SimpleMapComponent(map, entryFactory);
			}

		});
	}

	public Editor(ProjectLoader<TProject> projectLoader) {
		this(projectLoader,
				new EntryComponentFactory<SimpleEntryComponent<?>>() {

					@Override
					public SimpleEntryComponent<?> createEntryComponent(
							TranslationEntry<?> entry) {
						return new SimpleEntryComponent<>(entry);
					}
				});
	}

	protected void updateFilters(TranslationMap<?> map, JPanel filterComponent,
			final MapContentPanel<MapID> mapPanel) {
		filterComponent.removeAll();
		filterComponent.setLayout(new GridBagLayout());

		EntryFilter<TranslationEntry<?>> noTranslationEntry = new EntryFilter<TranslationEntry<?>>() {

			@Override
			public String getName() {
				return "No translation";
			}

			@Override
			public String getDescription() {
				return "Search for entries which have some original content but no translation.";
			}

			@Override
			public boolean isRelevant(TranslationEntry<?> entry) {
				return !TranslationUtil.isActuallyTranslated(entry);
			}
		};

		final JComboBox<FilterAction<?>> comboBox = new JComboBox<>();
		List<String> filterNames = new LinkedList<>();
		Collection<? extends EntryFilter<?>> filters = map.getEntryFilters();
		for (EntryFilter<?> filter : filters) {
			comboBox.addItem(new FilterAction<>(filter, mapPanel));
			filterNames.add(filter.getName());
		}
		comboBox.addItem(new FilterAction<>(noTranslationEntry, mapPanel));

		final JButton nextButton = new JButton(new AbstractAction("Next") {

			@Override
			public void actionPerformed(ActionEvent e) {
				FilterAction<?> action = (FilterAction<?>) comboBox
						.getSelectedItem();
				action.actionPerformed(e);
			}

		});

		comboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				FilterAction<?> filterAction = (FilterAction<?>) comboBox
						.getSelectedItem();
				EntryFilter<?> filter = filterAction.getFilter();
				nextButton.setToolTipText(filter.getDescription());
				logger.info("Select filter: " + filterAction);
				config.setProperty(CONFIG_FILTER, "" + filterAction);
			}
		});

		String preferredFilter = config.getProperty(CONFIG_FILTER, null);
		if (preferredFilter == null) {
			comboBox.setSelectedIndex(0);
		} else {
			for (int i = 0; i < comboBox.getItemCount(); i++) {
				FilterAction<?> item = comboBox.getItemAt(i);
				if (item.toString().equals(preferredFilter)) {
					comboBox.setSelectedItem(item);
				} else {
					// search for another one
				}
			}
		}

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		filterComponent.add(comboBox, constraints);

		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 0;
		constraints.gridx = 1;
		constraints.insets = new Insets(0, 10, 0, 0);
		filterComponent.add(nextButton, constraints);
	}

	private void configureTools(final ToolPanel toolPanel,
			final ToolProvider<MapID> provider) {
		Search<MapID> tool = new Search<>();
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

	private JPanel configureButtons(final MapContentPanel<MapID> mapPanel,
			JPanel filters) {
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
				mapPanel.goToEntry(mapPanel.getMap().size() - 1);
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,
				InputEvent.ALT_DOWN_MASK), ACTION_LAST_ENTRY);
		JButton last = new JButton(actions.get(ACTION_LAST_ENTRY));
		last.setToolTipText("Go to last entry (ALT+END).");

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
		buttonPanel.add(filters, constraints);

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
}
