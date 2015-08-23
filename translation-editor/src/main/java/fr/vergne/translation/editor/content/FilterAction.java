package fr.vergne.translation.editor.content;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.editor.MapContentPanel;
import fr.vergne.translation.util.EntryFilter;

@SuppressWarnings("serial")
public class FilterAction<Entry extends TranslationEntry<?>> extends
		AbstractAction {

	private EntryFilter<Entry> filter;
	private MapContentPanel<?> mapPanel;

	public FilterAction(EntryFilter<Entry> filter, MapContentPanel<?> mapPanel) {
		super(filter.getName());
		this.filter = filter;
		this.mapPanel = mapPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Collection<Integer> untranslatedEntries = new LinkedList<Integer>();
		Iterator<? extends TranslationEntry<?>> iterator = mapPanel.getMap()
				.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			@SuppressWarnings("unchecked")
			Entry entry = (Entry) iterator.next();
			if (filter.isRelevant(entry)) {
				untranslatedEntries.add(index);
			} else {
				// already translated
			}
			index++;
		}

		if (untranslatedEntries.isEmpty()) {
			JOptionPane.showMessageDialog(mapPanel, "No entry found for: "
					+ filter.getName());
		} else {
			int currentEntry = mapPanel.getCurrentEntryIndex();
			TreeSet<Integer> orderedEntries = new TreeSet<Integer>(
					untranslatedEntries);
			Integer next = orderedEntries.ceiling(currentEntry + 1);
			if (next == null) {
				JOptionPane
						.showMessageDialog(mapPanel,
								"End of the entries reached. Search from the beginning.");
				mapPanel.goToEntry(orderedEntries.first());
			} else {
				mapPanel.goToEntry(next);
			}
		}
	}

	@Override
	public String toString() {
		return filter.getName();
	}

	public EntryFilter<Entry> getFilter() {
		return filter;
	}
}
