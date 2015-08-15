package fr.sazaju.vheditor.gui.content;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import fr.sazaju.vheditor.gui.MapContentPanel;
import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.util.EntryFilter;

@SuppressWarnings("serial")
public class FilterButton extends JButton {

	public <Entry extends TranslationEntry<?>> FilterButton(
			final EntryFilter<Entry> filter, final MapContentPanel mapPanel) {
		super(new AbstractAction(filter.getName()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				Collection<Integer> untranslatedEntries = new LinkedList<Integer>();
				Iterator<? extends TranslationEntry<?>> iterator = mapPanel
						.getMap().iterator();
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
					JOptionPane.showMessageDialog(mapPanel,
							"No entry found for: " + filter.getName());
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
		});
		setToolTipText(filter.getDescription());
	}

}
