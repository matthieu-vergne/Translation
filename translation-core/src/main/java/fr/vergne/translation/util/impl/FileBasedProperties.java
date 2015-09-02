package fr.vergne.translation.util.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SuppressWarnings("serial")
public class FileBasedProperties extends Properties {

	private final File file;
	private final boolean saveUponModification;

	public FileBasedProperties(File file, boolean saveUponModification) {
		this.file = file;
		this.saveUponModification = saveUponModification;
		try {
			InputStream stream = new FileInputStream(file);
			load(stream);
			stream.close();
		} catch (FileNotFoundException e) {
			// the file just does not exist yet
		} catch (IOException e) {
			throw new RuntimeException("Impossible to load the property file "
					+ file, e);
		}
	}

	public FileBasedProperties(String filePath, boolean saveUponModification) {
		this(new File(filePath), saveUponModification);
	}

	@Override
	public synchronized Object setProperty(String key, String value) {
		Object property = super.setProperty(key, value);
		if (saveUponModification) {
			save();
		} else {
			// do not save automatically
		}
		return property;
	}

	public void save() {
		try {
			FileOutputStream stream = new FileOutputStream(file);
			store(stream, null);
			stream.close();
		} catch (IOException e) {
			throw new RuntimeException("Impossible to save the property file "
					+ file, e);
		}
	}
}
