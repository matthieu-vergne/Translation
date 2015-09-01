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
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationProject;
import fr.vergne.translation.editor.MapListPanel.MapSelectedListener;
import fr.vergne.translation.editor.ProjectLoaderPanel.ProjectLoadedListener;
import fr.vergne.translation.editor.content.EntryComponentFactory;
import fr.vergne.translation.editor.content.FilterAction;
import fr.vergne.translation.editor.content.MapComponentFactory;
import fr.vergne.translation.editor.content.SimpleEntryComponent;
import fr.vergne.translation.editor.content.SimpleMapComponent;
import fr.vergne.translation.editor.tool.FileBasedProperties;
import fr.vergne.translation.editor.tool.Search;
import fr.vergne.translation.editor.tool.ToolProvider;
import fr.vergne.translation.impl.EmptyProject;
import fr.vergne.translation.impl.TranslationUtil;
import fr.vergne.translation.util.EntryFilter;
import fr.vergne.translation.util.Feature;
import fr.vergne.translation.util.MapNamer;
import fr.vergne.translation.util.ProjectLoader;
import fr.vergne.translation.util.impl.DefaultMapNamer;

@SuppressWarnings("serial")
public class Editor<MapID, TEntry extends TranslationEntry<?>, TMap extends TranslationMap<TEntry>, TProject extends TranslationProject<TEntry, MapID, TMap>>
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
	private static final String CONFIG_THEME = "theme";
	private static final String CONFIG_MAP_NAMER = "mapNamer";
	private static final String CONFIG_MAP_DIR = "mapDir";
	private static final String CONFIG_CLEARED_DISPLAYED = "clearedDisplayed";
	// TODO make config non-static
	public static final FileBasedProperties config = new FileBasedProperties(
			"vh-editor.ini", true);
	private final ToolProvider<MapID> toolProvider;
	private final ProjectLoaderPanel<TProject> projectPanel;
	private final MapListPanel<TEntry, TMap, MapID, TProject> listPanel;
	private final MapContentPanel<MapID> mapPanel;
	private TranslationProject<TEntry, MapID, TMap> currentProject = new EmptyProject<>();
	private final DefaultMapNamer<MapID> defaultMapNamer = new DefaultMapNamer<>();

	public Editor(ProjectLoader<TProject> projectLoader,
			MapComponentFactory<?> mapComponentFactory) {
		// TODO rename as ConfigurationProvider
		// TODO use it instead of the static config
		toolProvider = new ToolProvider<MapID>() {

			@Override
			public TranslationProject<?, MapID, ?> getProject() {
				return listPanel.getProject();
			}

			@Override
			public MapNamer<MapID> getMapNamer() {
				return retrieveCurrentMapNamer();
			}

			@Override
			public void loadMap(MapID id) {
				loadMapEntry(id, 0);
			}

			@Override
			public void loadMapEntry(MapID id, int entryIndex) {
				try {
					mapPanel.setMap(id, entryIndex);
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

		listPanel = new MapListPanel<>(toolProvider);
		listPanel.addMapSelectedListener(new MapSelectedListener<MapID>() {

			@Override
			public void mapSelected(MapID id) {
				toolProvider.loadMap(id);
			}
		});

		final JPanel filters = new JPanel();

		projectPanel = new ProjectLoaderPanel<>(projectLoader);
		final JMenu projectMenu = new JMenu("Project");
		final JMenu listMenu = new JMenu("List");
		projectPanel
				.addProjectLoadedListener(new ProjectLoadedListener<TProject>() {

					@Override
					public void projectLoaded(File directory, TProject project) {
						Editor.config.setProperty(CONFIG_MAP_DIR,
								directory.toString());
						currentProject = project;
						logger.info("Project loaded: " + project);

						updateFilters(project, filters, mapPanel);
						projectMenu.removeAll();
						listMenu.removeAll();

						for (final Feature feature : project.getFeatures()) {
							logger.fine("Adding project feature: "
									+ feature.getName());
							JMenuItem item = new JMenuItem(new AbstractAction(
									feature.getName()) {

								@Override
								public void actionPerformed(ActionEvent arg0) {
									feature.run();
								}
							});
							item.setToolTipText(formatTooltip(feature
									.getDescription()));
							projectMenu.add(item);
						}

						logger.fine("Adding list clear display");
						final JCheckBoxMenuItem displayCleared = new JCheckBoxMenuItem();
						displayCleared.setAction(new AbstractAction("Cleared") {

							@Override
							public void actionPerformed(ActionEvent arg0) {
								boolean isClearedDisplay = displayCleared
										.isSelected();
								Editor.config.setProperty(
										CONFIG_CLEARED_DISPLAYED, ""
												+ isClearedDisplay);
								listPanel.setClearedDisplayed(isClearedDisplay);
							}
						});
						boolean isClearedDisplayed = Boolean
								.parseBoolean(Editor.config.getProperty(
										CONFIG_CLEARED_DISPLAYED, "true"));
						displayCleared.setSelected(isClearedDisplayed);
						displayCleared.setToolTipText("Display cleared maps.");
						listPanel.setClearedDisplayed(isClearedDisplayed);
						logger.fine("Clear display status: "+isClearedDisplayed);
						listMenu.add(displayCleared);

						listMenu.addSeparator();
						MapNamer<MapID> currentNamer = retrieveCurrentMapNamer();
						ButtonGroup group = new ButtonGroup();
						for (final MapNamer<MapID> namer : project
								.getMapNamers()) {
							logger.fine("Adding list namer: " + namer.getName());
							final JRadioButtonMenuItem nameItem = new JRadioButtonMenuItem(
									new AbstractAction(namer.getName()) {

										@Override
										public void actionPerformed(
												ActionEvent e) {
											Editor.config.setProperty(
													CONFIG_MAP_NAMER,
													namer.getName());
											listPanel.requestUpdate();
										}
									});
							if (namer.equals(currentNamer)) {
								nameItem.setSelected(true);
								logger.fine("Set as current namer.");
							} else {
								// let it unselected
							}
							nameItem.setToolTipText(formatTooltip(namer
									.getDescription()));
							group.add(nameItem);
							listMenu.add(nameItem);
						}

						listPanel.setProject(project);
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

		JPanel leftPanel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints2 = new GridBagConstraints();
		constraints2.gridx = 0;
		constraints2.gridy = 0;
		constraints2.fill = GridBagConstraints.HORIZONTAL;
		constraints2.weightx = 1;
		leftPanel.add(projectPanel, constraints2);
		constraints2.gridy++;
		constraints2.fill = GridBagConstraints.BOTH;
		constraints2.weighty = 1;
		leftPanel.add(listPanel, constraints2);

		final JSplitPane rootSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rootSplit.setLeftComponent(leftPanel);
		rootSplit.setRightComponent(translationPanel);
		rootSplit.setResizeWeight(1.0 / 3);

		setLayout(new GridLayout(1, 1));
		add(rootSplit);

		JMenu themeMenu = new JMenu("Theme");
		ButtonGroup group = new ButtonGroup();
		String currentTheme = config.getProperty(CONFIG_THEME,
				UIManager.getSystemLookAndFeelClassName());
		for (final LookAndFeelInfo theme : UIManager.getInstalledLookAndFeels()) {
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
					new AbstractAction(theme.getName()) {

						@Override
						public void actionPerformed(ActionEvent event) {
							try {
								UIManager.setLookAndFeel(theme.getClassName());
								SwingUtilities
										.updateComponentTreeUI(Editor.this);
								Editor.this.setSize(Editor.this.getSize());

								String name = theme.getClassName();
								config.setProperty(CONFIG_THEME, name);
								logger.info("Apply theme: " + name);
							} catch (ClassNotFoundException
									| InstantiationException
									| IllegalAccessException
									| UnsupportedLookAndFeelException ex) {
								logger.log(Level.SEVERE, null, ex);
							}
						}
					});

			if (theme.getClassName().equals(currentTheme)) {
				item.setSelected(true);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						item.getAction().actionPerformed(null);
					}
				});
			} else {
				// keep it unselected
			}
			group.add(item);
			themeMenu.add(item);
		}

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(projectMenu);
		menuBar.add(listMenu);
		menuBar.add(themeMenu);
		setJMenuBar(menuBar);

		setTitle("Translation Editor");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

		String projectPath = Editor.config.getProperty(CONFIG_MAP_DIR, null);
		if (projectPath == null) {
			// nothing to load
		} else {
			projectPanel.setProjectPath(new File(projectPath));
		}

		pack();

		finalizeConfig(rootSplit);
	}

	private String formatTooltip(String description) {
		return "<html><p width=\"300px\">" + description + "</p></html>";
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

	protected void updateFilters(TProject project, JPanel filterComponent,
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
		Collection<? extends EntryFilter<?>> filters = project
				.getEntryFilters();
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
				nextButton.setToolTipText(formatTooltip(filter.getDescription()));
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

	private MapNamer<MapID> retrieveCurrentMapNamer() {
		Collection<MapNamer<MapID>> namers = currentProject.getMapNamers();
		if (namers.isEmpty()) {
			return defaultMapNamer;
		} else {
			MapNamer<MapID> defaultNamer = namers.iterator().next();
			String id = Editor.config.getProperty(CONFIG_MAP_NAMER,
					defaultNamer.getName());
			for (MapNamer<MapID> namer : namers) {
				if (namer.getName().equals(id)) {
					return namer;
				} else {
					// not found yet
				}
			}
			return defaultNamer;
		}
	}

}
