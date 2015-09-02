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
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
import fr.vergne.translation.editor.tool.Search;
import fr.vergne.translation.editor.tool.ToolProvider;
import fr.vergne.translation.impl.EmptyProject;
import fr.vergne.translation.util.EntryFilter;
import fr.vergne.translation.util.Feature;
import fr.vergne.translation.util.MapNamer;
import fr.vergne.translation.util.ProjectLoader;
import fr.vergne.translation.util.Setting;
import fr.vergne.translation.util.Setting.SettingKey;
import fr.vergne.translation.util.impl.DefaultMapNamer;

@SuppressWarnings("serial")
public class Editor<MapID, TEntry extends TranslationEntry<?>, TMap extends TranslationMap<TEntry>, TProject extends TranslationProject<TEntry, MapID, TMap>>
		extends JFrame {

	private static final Logger logger = Logger.getLogger(Editor.class
			.getName());

	private final Setting<?> settings;
	private final SettingKey<Integer> frameXSetting;
	private final SettingKey<Integer> frameYSetting;
	private final SettingKey<Integer> frameWidthSetting;
	private final SettingKey<Integer> frameHeightSetting;
	private final SettingKey<Integer> frameSplitSetting;
	private final SettingKey<String> themeSetting;
	private final SettingKey<String> mapDirSetting;
	private final SettingKey<String> mapNamerSetting;
	private final SettingKey<String> remainingFilterSetting;
	private final SettingKey<String> nextFilterSetting;
	private final SettingKey<Boolean> clearedDisplayedSetting;
	// TODO reduce the following members to method variables
	private final MapContentPanel<MapID> mapPanel;
	private TranslationProject<TEntry, MapID, TMap> currentProject = new EmptyProject<>();
	private final DefaultMapNamer<MapID> defaultMapNamer = new DefaultMapNamer<>();

	public Editor(ProjectLoader<TProject> projectLoader,
			MapComponentFactory<?> mapComponentFactory,
			final Setting<? super String> settings) {
		this.settings = settings;
		frameXSetting = settings.registerKey("x", 0);
		frameYSetting = settings.registerKey("y", 0);
		frameWidthSetting = settings.registerKey("width", 700);
		frameHeightSetting = settings.registerKey("height", 500);
		frameSplitSetting = settings.registerKey("split",
				settings.get(frameWidthSetting) * 4 / 10);
		themeSetting = settings.registerKey("theme",
				UIManager.getSystemLookAndFeelClassName());
		mapDirSetting = settings.registerKey("mapDir", (String) null);
		mapNamerSetting = settings.registerKey("mapNamer", (String) null);
		remainingFilterSetting = settings.registerKey("remainingFilter",
				(String) null);
		nextFilterSetting = settings.registerKey("filter", (String) null);
		clearedDisplayedSetting = settings
				.registerKey("clearedDisplayed", true);

		final ToolProvider<MapID> toolProvider = new ToolProvider<MapID>() {

			@Override
			public TranslationProject<?, MapID, ?> getProject() {
				return currentProject;
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

		final ProjectLoaderPanel<TProject> projectPanel = new ProjectLoaderPanel<>(
				projectLoader);
		final MapListPanel<TEntry, TMap, MapID, TProject> listPanel = new MapListPanel<>(
				toolProvider);
		mapPanel = new MapContentPanel<MapID>(toolProvider, mapComponentFactory);
		final ToolPanel toolPanel = new ToolPanel();

		final JPanel filters = new JPanel();

		JMenuBar menuBar = new JMenuBar();
		final JMenu projectMenu = new JMenu("Project");
		menuBar.add(projectMenu);
		final JMenu listMenu = new JMenu("List");
		menuBar.add(listMenu);
		JMenu themeMenu = new JMenu("Theme");
		menuBar.add(themeMenu);

		projectPanel
				.addProjectLoadedListener(new ProjectLoadedListener<TProject>() {

					@Override
					public void projectLoaded(File directory, TProject project) {
						settings.set(mapDirSetting, directory.toString());
						currentProject = project;
						logger.info("Project loaded: " + project);

						updateEntryFilters(project, filters, mapPanel);
						updateProjectMenu(projectMenu, listPanel, project);
						updateListMenu(listMenu, listPanel, project);

						RemainingFilterConfig<TEntry> config = retrieveRemainingFilterConfig(project);
						listPanel.setRemainingFilter(config.filter);
						listPanel.setProject(project);
					}

				});
		listPanel.addMapSelectedListener(new MapSelectedListener<MapID>() {

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

		ButtonGroup group = new ButtonGroup();
		String currentTheme = settings.get(themeSetting);
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
								settings.set(themeSetting, name);
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
		configureTools(toolPanel, toolProvider);
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
		setJMenuBar(menuBar);
		setTitle("Translation Editor");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

		String projectPath = settings.get(mapDirSetting);
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
			final EntryComponentFactory<?> entryFactory,
			Setting<? super String> settings) {
		this(projectLoader, new MapComponentFactory<SimpleMapComponent>() {

			@Override
			public SimpleMapComponent createMapComponent(TranslationMap<?> map) {
				return new SimpleMapComponent(map, entryFactory);
			}

		}, settings);
	}

	public Editor(ProjectLoader<TProject> projectLoader,
			Setting<? super String> settings) {
		this(projectLoader,
				new EntryComponentFactory<SimpleEntryComponent<?>>() {

					@Override
					public SimpleEntryComponent<?> createEntryComponent(
							TranslationEntry<?> entry) {
						return new SimpleEntryComponent<>(entry);
					}
				}, settings);
	}

	protected void updateEntryFilters(TProject project, JPanel filterComponent,
			final MapContentPanel<MapID> mapPanel) {
		filterComponent.removeAll();
		filterComponent.setLayout(new GridBagLayout());

		final JComboBox<FilterAction<?>> comboBox = new JComboBox<>();
		List<String> filterNames = new LinkedList<>();
		Collection<? extends EntryFilter<?>> filters = project
				.getEntryFilters();
		for (EntryFilter<?> filter : filters) {
			comboBox.addItem(new FilterAction<>(filter, mapPanel));
			filterNames.add(filter.getName());
		}

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
				settings.set(nextFilterSetting, filterAction.getFilter()
						.getName());
			}
		});

		String preferredFilter = settings.get(nextFilterSetting);
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
		setLocation(settings.get(frameXSetting), settings.get(frameYSetting));
		setSize(new Dimension(settings.get(frameWidthSetting),
				settings.get(frameHeightSetting)));
		rootSplit.setDividerLocation(settings.get(frameSplitSetting));
		rootSplit.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getPropertyName().equals(
						JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
					settings.set(frameSplitSetting, (int) event.getNewValue());
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
				settings.set(frameWidthSetting, getWidth());
				settings.set(frameHeightSetting, getHeight());
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
				settings.set(frameXSetting, getX());
				settings.set(frameYSetting, getY());
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

		final String previousEntryAction = "previousEntry";
		actions.put(previousEntryAction, new AbstractAction("<") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(mapPanel.getCurrentEntryIndex() - 1);
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
				InputEvent.ALT_DOWN_MASK), previousEntryAction);
		JButton previous = new JButton(actions.get(previousEntryAction));
		previous.setToolTipText("Go to previous entry (ALT+LEFT).");

		final String nextEntryAction = "nextEntry";
		actions.put(nextEntryAction, new AbstractAction(">") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(mapPanel.getCurrentEntryIndex() + 1);
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
				InputEvent.ALT_DOWN_MASK), nextEntryAction);
		JButton next = new JButton(actions.get(nextEntryAction));
		next.setToolTipText("Go to next entry (ALT+RIGHT).");

		final String firstEntryAction = "firstEntry";
		actions.put(firstEntryAction, new AbstractAction("|<") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(0);
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
				InputEvent.ALT_DOWN_MASK), firstEntryAction);
		JButton first = new JButton(actions.get(firstEntryAction));
		first.setToolTipText("Go to first entry (ALT+HOME).");

		final String lastEntryAction = "lastEntry";
		actions.put(lastEntryAction, new AbstractAction(">|") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.goToEntry(mapPanel.getMap().size() - 1);
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,
				InputEvent.ALT_DOWN_MASK), lastEntryAction);
		JButton last = new JButton(actions.get(lastEntryAction));
		last.setToolTipText("Go to last entry (ALT+END).");

		final String saveAction = "save";
		actions.put(saveAction, new AbstractAction("Save") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.save();
			}
		});
		inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_DOWN_MASK), saveAction);
		JButton save = new JButton(actions.get(saveAction));
		save.setToolTipText("Write the modifications to the map file (CTRL+S).");

		final String resetAction = "reset";
		actions.put(resetAction, new AbstractAction("Reset") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.reset();
			}
		});
		// no key binding to avoid wrong manipulation
		JButton reset = new JButton(actions.get(resetAction));
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
			String id = settings.get(mapNamerSetting);
			id = id == null ? defaultNamer.getName() : id;
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

	private void updateProjectMenu(final JMenu projectMenu,
			final MapListPanel<TEntry, TMap, MapID, TProject> listPanel,
			final TProject project) {
		projectMenu.removeAll();

		for (final Feature feature : project.getFeatures()) {
			logger.fine("Adding project feature: " + feature.getName());
			JMenuItem item = new JMenuItem(
					new AbstractAction(feature.getName()) {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							feature.run();
						}
					});
			item.setToolTipText(formatTooltip(feature.getDescription()));
			projectMenu.add(item);
		}
	}

	private void updateListMenu(final JMenu listMenu,
			final MapListPanel<TEntry, TMap, MapID, TProject> listPanel,
			final TProject project) {
		listMenu.removeAll();

		logger.fine("Adding list clear display");
		final JCheckBoxMenuItem displayCleared = new JCheckBoxMenuItem();
		displayCleared.setAction(new AbstractAction("Cleared") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean isClearedDisplay = displayCleared.isSelected();
				settings.set(clearedDisplayedSetting, isClearedDisplay);
				listPanel.setClearedDisplayed(isClearedDisplay);
			}
		});
		boolean isClearedDisplayed = settings.get(clearedDisplayedSetting);
		displayCleared.setSelected(isClearedDisplayed);
		displayCleared.setToolTipText("Display cleared maps.");
		listPanel.setClearedDisplayed(isClearedDisplayed);
		logger.fine("Clear display status: " + isClearedDisplayed);
		listMenu.add(displayCleared);

		JMenuItem statItem = new JMenuItem(new AbstractAction(
				"Remaining Filter") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final RemainingFilterConfig<TEntry> initialConfig = retrieveRemainingFilterConfig(project);
				final RemainingFilterConfig<TEntry> config = new RemainingFilterConfig<>(
						initialConfig);

				JPanel remainingEntriesPanel = new JPanel();
				remainingEntriesPanel.setLayout(new GridLayout(0, 1));
				ButtonGroup group = new ButtonGroup();
				for (final EntryFilter<TEntry> filter : project
						.getEntryFilters()) {
					final JRadioButton selection = new JRadioButton(
							new AbstractAction(filter.getName()) {

								@Override
								public void actionPerformed(ActionEvent e) {
									config.filter = filter;
								}
							});

					selection.setSelected(initialConfig.filter.equals(filter));

					group.add(selection);
					remainingEntriesPanel.add(selection);
				}

				JPanel message = new JPanel();
				message.setLayout(new BoxLayout(message, BoxLayout.PAGE_AXIS));
				JLabel comp = new JLabel(
						"Select the filter to use for the remaining entries:");
				comp.setAlignmentX(JLabel.LEFT_ALIGNMENT);
				message.add(comp);
				remainingEntriesPanel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
				message.add(remainingEntriesPanel);

				int answer = JOptionPane.showConfirmDialog(Editor.this,
						message, "Remaining Stats",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null);

				if (answer == JOptionPane.OK_OPTION
						&& !config.equals(initialConfig)) {
					settings.set(remainingFilterSetting,
							config.filter.getName());
					listPanel.setRemainingFilter(config.filter);
				} else {
					// keep current config
				}
			}

		});
		statItem.setToolTipText(formatTooltip("Choose what to consider as a remaining entry."));
		listMenu.add(statItem);
		statItem.setEnabled(!project.getEntryFilters().isEmpty());

		listMenu.addSeparator();
		MapNamer<MapID> currentNamer = retrieveCurrentMapNamer();
		ButtonGroup group = new ButtonGroup();
		for (final MapNamer<MapID> namer : project.getMapNamers()) {
			logger.fine("Adding list namer: " + namer.getName());
			final JRadioButtonMenuItem nameItem = new JRadioButtonMenuItem(
					new AbstractAction(namer.getName()) {

						@Override
						public void actionPerformed(ActionEvent e) {
							settings.set(mapNamerSetting, namer.getName());
							listPanel.requestUpdate();
						}
					});
			if (namer.equals(currentNamer)) {
				nameItem.setSelected(true);
				logger.fine("Set as current namer.");
			} else {
				// let it unselected
			}
			nameItem.setToolTipText(formatTooltip(namer.getDescription()));
			group.add(nameItem);
			listMenu.add(nameItem);
		}
	}

	private static class RemainingFilterConfig<TEntry extends TranslationEntry<?>> {
		private EntryFilter<TEntry> filter;

		public RemainingFilterConfig() {
		}

		public RemainingFilterConfig(RemainingFilterConfig<TEntry> config) {
			filter = config.filter;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (obj instanceof RemainingFilterConfig) {
				RemainingFilterConfig<?> c = (RemainingFilterConfig<?>) obj;
				return filter.equals(c.filter);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return filter.hashCode();
		}
	}

	private RemainingFilterConfig<TEntry> retrieveRemainingFilterConfig(
			final TProject project) {
		final RemainingFilterConfig<TEntry> initialConfig = new RemainingFilterConfig<>();
		String filterName = settings.get(remainingFilterSetting);
		if (filterName == null) {
			if (project.getEntryFilters().isEmpty()) {
				throw new RuntimeException(
						"This dialog should not be shown if no filters are provided.");
			} else {
				filterName = project.getEntryFilters().iterator().next()
						.getName();
			}
		} else {
			// use the one configured
		}

		for (EntryFilter<TEntry> filter : project.getEntryFilters()) {
			if (filter.getName().equals(filterName)) {
				initialConfig.filter = filter;
			} else {
				// continue searching
			}
		}

		return initialConfig;
	}
}
