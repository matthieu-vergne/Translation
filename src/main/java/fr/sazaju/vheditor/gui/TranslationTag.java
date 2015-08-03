package fr.sazaju.vheditor.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.translation.TranslationComment.Field;
import fr.sazaju.vheditor.translation.TranslationEntry;

@SuppressWarnings("serial")
public class TranslationTag extends JPanel {

	private final TranslationEntry entry;
	private boolean isMarked;

	public TranslationTag(final TranslationEntry entry) {
		this.entry = entry;
		this.isMarked = !entry.getComment().get(Field.MARKED_AS_TRANSLATED);

		final JLabel tag = new JLabel() {
			@Override
			public String getText() {
				return !entry.getComment().get(Field.MARKED_AS_TRANSLATED) ? "# UNTRANSLATED"
						: "<html><s># UNTRANSLATED</s></html>";
			}
		};

		JButton toggleButton = new JButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				entry.getComment().set(Field.MARKED_AS_TRANSLATED,
						!entry.getComment().get(Field.MARKED_AS_TRANSLATED));
				/*
				 * Set a different text than the current value to force the
				 * generation of the update event.
				 */
				tag.setText(""
						+ !entry.getComment().get(Field.MARKED_AS_TRANSLATED));
			}
		}) {
			@Override
			public String getText() {
				return !entry.getComment().get(Field.MARKED_AS_TRANSLATED) ? "-"
						: "+";
			}
		};

		setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		add(tag);
		add(new JLabel("   "));
		add(toggleButton);
		/*
		 * In order to avoid the button making the line bigger, its size should
		 * be reduced. If we directly set a reduced size, its content is
		 * generally replaced by "..." because the insets don't allow such a
		 * reduced size. To fix that, we first minimize the insets by setting an
		 * EmptyBorder, then set a real border to have a normal rendering.
		 */
		toggleButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		toggleButton.setBorder(new EtchedBorder());
		// now we can reduce the size of the button
		int length = tag.getFontMetrics(tag.getFont()).getHeight();
		toggleButton.setPreferredSize(new Dimension(length, length));
		
		setOpaque(false);
	}

	public boolean isModified() {
		return !entry.getComment().get(Field.MARKED_AS_TRANSLATED) != isMarked;
	}

	public void save() {
		isMarked = !entry.getComment().get(Field.MARKED_AS_TRANSLATED);
	}
}
