package fr.sazaju.vheditor.parsing.vh;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import fr.sazaju.vheditor.gui.Gui;
import fr.sazaju.vheditor.gui.parsing.MapLabelPage;
import fr.sazaju.vheditor.gui.parsing.MapRow;
import fr.sazaju.vheditor.gui.parsing.MapTable;
import fr.sazaju.vheditor.parsing.vh.map.VHMap;
import fr.sazaju.vheditor.translation.impl.MapFilesProject;
import fr.sazaju.vheditor.util.MultiReader;
import fr.vergne.logging.LoggerConfiguration;
import fr.vergne.parsing.layer.exception.ParsingException;

public class VHProject extends MapFilesProject<VHMap> {

	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private static final String CONFIG_LABEL_SOURCE = "labelSource";
	private static final String CONFIG_LABEL_PREFIX = "label.";
	private static final String CONFIG_LABEL_LAST_UPDATE = "lastLabelUpdate";
	private final Map<String, String> mapLabels = Collections
			.synchronizedMap(new HashMap<String, String>());

	// FIXME consider map labels
	@SuppressWarnings("serial")
	public VHProject(File directory) {
		super(retrieveFiles(directory), new MultiReader<File, VHMap>() {

			@Override
			public VHMap read(File file) {
				try {
					return new VHMap(file);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});

		final JButton labelSource = new JButton();
		labelSource.setAction(new AbstractAction("Source") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String source = Gui.config.getProperty(CONFIG_LABEL_SOURCE);
				Object answer = JOptionPane
						.showInputDialog(
								null,
								"Please provide the location of the page describing the labels (URL or local file):",
								"Label Source", JOptionPane.QUESTION_MESSAGE,
								null, null, source);
				if (answer == null || source.equals(answer)) {
					// no change requested
				} else if (((String) answer).isEmpty()) {
					displayError("An empty location is of no use, so the change is cancelled.");
				} else {
					logger.info("Label source set: " + answer);
					Gui.config.setProperty(CONFIG_LABEL_SOURCE,
							answer.toString());
					Gui.config.setProperty(CONFIG_LABEL_LAST_UPDATE, "" + 0);
				}
			}
		});
		labelSource
				.setToolTipText("Configure the source where to load the maps' labels from.");
		addExtraFeature(labelSource);

		final JButton updateLabels = new JButton();
		updateLabels.setAction(new AbstractAction("Update") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					loadLabels(true);
					mapLabels.clear();
					// TODO reactivate
					// ((ListModel<File>) tree.getModel()).requestUpdate();
				} catch (Exception e) {
					displayError(e.getMessage());
				}
			}
		});
		updateLabels
				.setToolTipText("Request the update of the labels from the label source.");
		addExtraFeature(updateLabels);
	}

	@SuppressWarnings("serial")
	private static List<File> retrieveFiles(File directory) {
		File[] f = directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isFile() && file.length() > 0;
			}

		});
		LinkedList<File> files = new LinkedList<>();
		for (File file : f) {
			files.add(new File(file.getPath()) {
				@Override
				public String toString() {
					return this.getName();
				}
			});
		}
		return files;
	}

	private void loadLabels(boolean force) {
		if (Gui.config.containsKey(CONFIG_LABEL_SOURCE)) {
			// source already configured
		} else {
			Gui.config.setProperty(CONFIG_LABEL_SOURCE,
					"https://www.assembla.com/spaces/VH/wiki/Map_List");
		}
		long lastUpdate = Long.parseLong(Gui.config.getProperty(
				CONFIG_LABEL_LAST_UPDATE, "0"));
		if (!force && System.currentTimeMillis() < lastUpdate + 86400000) {
			// not old enough
		} else {
			String source = Gui.config.getProperty(CONFIG_LABEL_SOURCE);
			URL url;
			try {
				url = new URL(source);
			} catch (MalformedURLException e) {
				try {
					url = new File(source).toURI().toURL();
				} catch (MalformedURLException e1) {
					throw new RuntimeException("Malformed URL: " + source, e);
				}
			}
			try {
				loadLabelsFrom(url);
			} finally {
				Gui.config.setProperty(CONFIG_LABEL_LAST_UPDATE,
						"" + System.currentTimeMillis());
			}
		}
	}

	private void loadLabelsFrom(URL pageUrl) {
		logger.info("Loading page from " + pageUrl + "...");
		String pageContent;
		try {
			URLConnection connection = pageUrl.openConnection();
			Pattern pattern = Pattern
					.compile("text/html;\\s+charset=([^\\s]+)\\s*");
			Matcher matcher = pattern.matcher(connection.getContentType());
			String charset = matcher.matches() ? matcher.group(1) : "UTF-8";
			Reader reader = new InputStreamReader(connection.getInputStream(),
					charset);
			StringBuilder buffer = new StringBuilder();
			int ch;
			while ((ch = reader.read()) >= 0) {
				buffer.append((char) ch);
			}
			pageContent = buffer.toString();
			reader.close();
		} catch (Exception e) {
			throw new RuntimeException("Impossible to read the source "
					+ pageUrl, e);
		}

		logger.info("Parsing content...");
		MapLabelPage mapLabelPage = new MapLabelPage();
		try {
			mapLabelPage.setContent(pageContent);
		} catch (ParsingException e) {
			throw new RuntimeException("Impossible to find map labels in "
					+ pageUrl, e);
		}
		logger.info("Content parsed.");

		logger.info("Saving labels...");
		MapTable table = mapLabelPage.getTable();
		int total = table.size();
		for (MapRow row : table) {
			String name = "Map" + row.getId() + ".txt";
			String label = row.getEnglishLabel();
			Gui.config.setProperty(CONFIG_LABEL_PREFIX + name, label);
		}
		logger.info("Labels saved: " + total);
	}

	protected String retrieveMapLabel(File id) {
		String idName = id.toString();
		logger.finest("Retrieving label for " + idName + "...");
		if (mapLabels.containsKey(idName)) {
			// already loaded
		} else {
			try {
				loadLabels(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mapLabels.put(idName,
					Gui.config.getProperty(CONFIG_LABEL_PREFIX + idName, null));
		}
		String label = mapLabels.get(idName);
		logger.finest("Label retrieved: " + idName + " = " + label);
		return label;
	}

	private void displayError(String message) {
		JOptionPane.showOptionDialog(null, message, "Loading Failed",
				JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null,
				new Object[] { "OK" }, "OK");
	}
}
