package fr.sazaju.vheditor.util;

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

public class CollectionManager<Element> {

	private final Map<ComposedKey<Object>, Collection<Element>> collections = new HashMap<ComposedKey<Object>, Collection<Element>>();
	private final HashSet<Element> allElements = new HashSet<Element>();

	@SuppressWarnings("unchecked")
	public void addCollection(Filter<Element> filter,
			final Comparator<Element> comparator) {
		ComposedKey<Object> key = new ComposedKey<Object>(filter, comparator);
		if (collections.containsKey(key)) {
			throw new IllegalArgumentException("Already existing collection.");
		} else {
			Collection<Element> collection = new TreeSet<Element>();
			collection.addAll(FilterUtil.filter(allElements, filter));
			collections.put(key, collection);
		}
	}

	public Collection<Element> getCollection(Filter<Element> filter,
			Comparator<Element> comparator) {
		ComposedKey<Object> key = new ComposedKey<Object>(filter, comparator);
		return collections.get(key);
	}

	public void removeCollection(Filter<Element> filter,
			Comparator<Element> comparator) {
		ComposedKey<Object> key = new ComposedKey<Object>(filter, comparator);
		collections.remove(key);
	}

	public void addElement(Element element) {
		allElements.add(element);
		for (Entry<ComposedKey<Object>, Collection<Element>> entry : collections
				.entrySet()) {
			Filter<Element> filter = entry.getKey().get(0);
			Collection<Element> collection = entry.getValue();

			if (filter.isSupported(element)) {
				collection.add(element);
			} else {
				// do not add
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void addAllElements(Collection<Element> elements) {
		allElements.addAll(elements);
		for (Entry<ComposedKey<Object>, Collection<Element>> entry : collections
				.entrySet()) {
			Filter<Element> filter = entry.getKey().get(0);
			Collection<Element> collection = entry.getValue();
			collection.addAll(FilterUtil.filter(elements, filter));
		}
	}

	public void removeElement(Element element) {
		allElements.remove(element);
		for (Collection<Element> collection : collections.values()) {
			collection.remove(element);
		}
	}

	public void removeAllElements(Collection<Element> elements) {
		allElements.removeAll(elements);
		for (Collection<Element> collection : collections.values()) {
			collection.removeAll(elements);
		}
	}

	public void clear() {
		allElements.clear();
		for (Collection<Element> collection : collections.values()) {
			collection.clear();
		}
	}

	public void recheckElement(Element element) {
		if (!allElements.contains(element)) {
			throw new IllegalArgumentException("Unknown element: " + element);
		} else {
			for (Entry<ComposedKey<Object>, Collection<Element>> entry : collections
					.entrySet()) {
				Filter<Element> filter = entry.getKey().get(0);
				Collection<Element> collection = entry.getValue();

				if (filter.isSupported(element)) {
					collection.add(element);
				} else {
					collection.remove(element);
				}
			}
		}
	}

	public boolean containsElement(Element element) {
		return allElements.contains(element);
	}

	public Collection<Element> getAllElements() {
		return allElements;
	}
}
