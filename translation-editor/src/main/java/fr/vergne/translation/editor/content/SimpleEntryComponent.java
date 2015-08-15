package fr.vergne.translation.editor.content;

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.commons.lang3.StringUtils;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMetadata;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.editor.content.EntryComponentFactory.EntryComponent;

@SuppressWarnings("serial")
public class SimpleEntryComponent<Entry extends TranslationEntry<?>> extends
		JPanel implements EntryComponent {

	private final TranslationArea translationArea;

	public SimpleEntryComponent(Entry entry, Collection<Integer> limits) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		TranslationMetadata metadata = entry.getMetadata();
		List<String> lines = new LinkedList<>();
		for (Field<?> field : metadata) {
			Object value = metadata.get(field);
			lines.add(field + ": " + value);
		}
		String tooltip = "<html>" + StringUtils.join(lines, "<br>") + "</html>";

		JTextArea original = new JTextArea(entry.getOriginalContent());
		original.setEditable(false);
		original.setToolTipText(tooltip);
		add(original);

		translationArea = new TranslationArea(entry, limits);
		translationArea.setToolTipText(tooltip);
		add(translationArea);

	}

	public SimpleEntryComponent(Entry entry) {
		this(entry, Collections.<Integer> emptyList());
	}

	@Override
	public Component getTranslationComponent() {
		return translationArea;
	}
}
