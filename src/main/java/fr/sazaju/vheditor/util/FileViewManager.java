package fr.sazaju.vheditor.util;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import fr.vergne.collection.filter.Filter;
import fr.vergne.collection.filter.FilterUtil;
import fr.vergne.collection.util.ComposedKey;

public class FileViewManager {

	private final Map<ComposedKey<Object>, Collection<File>> collections = new HashMap<ComposedKey<Object>, Collection<File>>();
	private final HashSet<File> allFiles = new HashSet<File>();

	@SuppressWarnings("unchecked")
	public void addCollection(Filter<File> filter,
			final Comparator<File> comparator) {
		ComposedKey<Object> key = new ComposedKey<Object>(filter, comparator);
		if (collections.containsKey(key)) {
			throw new IllegalArgumentException("Already existing collection.");
		} else {
			Collection<File> collection = new TreeSet<File>(
					new Comparator<File>() {

						@Override
						public int compare(File f1, File f2) {
							int comparison = comparator.compare(f1, f2);
							if (comparison == 0) {
								// ensure similar files are not overridden
								String path1 = f1.getAbsolutePath();
								String path2 = f2.getAbsolutePath();
								return path1.compareTo(path2);
								// String name1 = f1.getName();
								// String name2 = f2.getName();
								// return name1.compareTo(name2);
							} else {
								return comparison;
							}
						}
					});
			collection.addAll(FilterUtil.filter(allFiles, filter));
			collections.put(key, collection);
		}
	}

	public Collection<File> getCollection(Filter<File> filter,
			Comparator<File> comparator) {
		ComposedKey<Object> key = new ComposedKey<Object>(filter, comparator);
		return collections.get(key);
	}

	public void removeCollection(Filter<File> filter,
			Comparator<File> comparator) {
		ComposedKey<Object> key = new ComposedKey<Object>(filter, comparator);
		collections.remove(key);
	}

	public void addFile(File file) {
		allFiles.add(file);
		for (Entry<ComposedKey<Object>, Collection<File>> entry : collections
				.entrySet()) {
			Filter<File> filter = entry.getKey().get(0);
			Collection<File> collection = entry.getValue();

			if (filter.isSupported(file)) {
				collection.add(file);
			} else {
				// do not add
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void addAllFiles(Collection<File> files) {
		allFiles.addAll(files);
		for (Entry<ComposedKey<Object>, Collection<File>> entry : collections
				.entrySet()) {
			Filter<File> filter = entry.getKey().get(0);
			Collection<File> collection = entry.getValue();
			collection.addAll(FilterUtil.filter(files, filter));
		}
	}

	public void removeFile(File file) {
		allFiles.remove(file);
		for (Collection<File> collection : collections.values()) {
			collection.remove(file);
		}
	}

	public void removeAllFiles(Collection<File> files) {
		allFiles.removeAll(files);
		for (Collection<File> collection : collections.values()) {
			collection.removeAll(files);
		}
	}

	public void clearFiles() {
		allFiles.clear();
		for (Collection<File> collection : collections.values()) {
			collection.clear();
		}
	}

	public void recheckFile(File file) {
		if (!allFiles.contains(file)) {
			throw new IllegalArgumentException("Unknown file: " + file);
		} else {
			for (Entry<ComposedKey<Object>, Collection<File>> entry : collections
					.entrySet()) {
				Filter<File> filter = entry.getKey().get(0);
				Collection<File> collection = entry.getValue();

				if (filter.isSupported(file)) {
					collection.add(file);
				} else {
					collection.remove(file);
				}
			}
		}
	}

	public boolean containsFile(File file) {
		return allFiles.contains(file);
	}

	public Collection<File> getAllFiles() {
		return allFiles;
	}
}
