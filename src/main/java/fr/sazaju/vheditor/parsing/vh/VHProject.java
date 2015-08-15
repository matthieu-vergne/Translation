package fr.sazaju.vheditor.parsing.vh;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import fr.sazaju.vheditor.parsing.vh.map.VHMap;
import fr.vergne.logging.LoggerConfiguration;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.translation.editor.Editor;
import fr.vergne.translation.editor.parsing.MapLabelPage;
import fr.vergne.translation.editor.parsing.MapRow;
import fr.vergne.translation.editor.parsing.MapTable;
import fr.vergne.translation.editor.tool.FileBasedProperties;
import fr.vergne.translation.impl.MapFilesProject;
import fr.vergne.translation.util.MultiReader;
import fr.vergne.translation.util.impl.SimpleFeature;

public class VHProject extends MapFilesProject<VHMap> {

	public static final Logger logger = LoggerConfiguration.getSimpleLogger();
	private static final String CONFIG_LABEL_SOURCE = "labelSource";
	private static final String CACHE_LABEL_PREFIX = "label.";
	private static final String CONFIG_LABEL_LAST_UPDATE = "lastLabelUpdate";
	private final FileBasedProperties cache = new FileBasedProperties(
			"vh-cache", false);

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

		if (Editor.config.containsKey(CONFIG_LABEL_SOURCE)) {
			// source already configured
		} else {
			Editor.config.setProperty(CONFIG_LABEL_SOURCE,
					"https://www.assembla.com/spaces/VH/wiki/Map_List");
		}
		loadLabels(false);

		addFeature(new SimpleFeature("Source",
				"Configure the source where to load the maps' labels from.") {

			@Override
			public void run() {
				String source = Editor.config.getProperty(CONFIG_LABEL_SOURCE);
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
					Editor.config.setProperty(CONFIG_LABEL_SOURCE,
							answer.toString());
					Editor.config.setProperty(CONFIG_LABEL_LAST_UPDATE, "" + 0);
				}
			}
		});

		addFeature(new SimpleFeature("Update",
				"Request the update of the labels from the label source.") {

			@Override
			public void run() {
				try {
					cache.clear();
					loadLabels(true);
					// TODO update list panel
				} catch (Exception e) {
					e.printStackTrace();
					String message = e.getMessage() != null ? e.getMessage()
							: "An error occurred ("
									+ e.getClass().getSimpleName()
									+ "). Please read the logs.";
					displayError(message);
				}
			}
		});
	}

	@SuppressWarnings("serial")
	private static List<File> retrieveFiles(File directory) {
		File[] f = directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isFile() && file.length() > 0;
			}

		});

		if (f == null) {
			throw new RuntimeException("Impossible to retrieve the files from "
					+ directory);
		} else {
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
	}

	private void loadLabels(boolean force) {
		long lastUpdate = Long.parseLong(Editor.config.getProperty(
				CONFIG_LABEL_LAST_UPDATE, "0"));
		if (!force && System.currentTimeMillis() < lastUpdate + 86400000) {
			// not old enough
		} else {
			String source = Editor.config.getProperty(CONFIG_LABEL_SOURCE);
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
				Editor.config.setProperty(CONFIG_LABEL_LAST_UPDATE,
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
		int total = 0;
		for (MapRow row : table) {
			String name = "Map" + row.getId() + ".txt";
			String label = row.getEnglishLabel();
			String key = CACHE_LABEL_PREFIX + name;
			if (!label.matches("[^a-zA-Z]*[a-zA-Z]+.*")) {
				logger.finest("- " + name + " = " + label + " (ignored)");
				cache.remove(key);
			} else {
				logger.finest("- " + name + " = " + label);
				cache.setProperty(key, label);
				total++;
			}
		}
		cache.save();
		logger.info("Labels saved: " + total);
	}

	@Override
	public String getMapName(File file) {
		return cache.getProperty(CACHE_LABEL_PREFIX + file.getName());
	}

	@Override
	public void setMapName(File file, String name) {
		throw new UnsupportedOperationException(
				"Map names are retrieved another way, this method cannot be used anymore.");
	}

	private void displayError(String message) {
		JOptionPane.showOptionDialog(null, message, "Loading Failed",
				JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null,
				new Object[] { "OK" }, "OK");
	}
}
