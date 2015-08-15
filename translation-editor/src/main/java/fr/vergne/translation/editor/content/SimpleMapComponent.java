package fr.vergne.translation.editor.content;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.editor.content.EntryComponentFactory.EntryComponent;
import fr.vergne.translation.editor.content.MapComponentFactory.MapComponent;

@SuppressWarnings("serial")
public class SimpleMapComponent extends JPanel implements MapComponent {

	public SimpleMapComponent(TranslationMap<?> map,
			EntryComponentFactory<?> entryFactory) {
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = GridBagConstraints.RELATIVE;
		constraints.insets = new Insets(0, 0, 10, 0);
		for (TranslationEntry<?> entry : map) {
			add(entryFactory.createEntryComponent(entry), constraints);
		}
	}

	@Override
	public EntryComponent getEntryComponent(int index) {
		return (EntryComponent) getComponent(index);
	}
}
