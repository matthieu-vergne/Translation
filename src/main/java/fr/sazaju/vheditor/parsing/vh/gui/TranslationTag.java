package fr.sazaju.vheditor.parsing.vh.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.parsing.vh.map.VHEntry;
import fr.sazaju.vheditor.translation.TranslationEntry;

@SuppressWarnings("serial")
public class TranslationTag<Entry extends TranslationEntry<?>> extends JPanel {

	private final Entry entry;
	private boolean isMarked;

	public TranslationTag(final Entry entry) {
		this.entry = entry;
		this.isMarked = entry.getMetadata().get(VHEntry.MARKED_AS_UNTRANSLATED);

		final JLabel tag = new JLabel() {
			@Override
			public String getText() {
				return entry.getMetadata().get(VHEntry.MARKED_AS_UNTRANSLATED) ? "# UNTRANSLATED"
						: "<html><s># UNTRANSLATED</s></html>";
			}
		};

		JButton toggleButton = new JButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				entry.getMetadata().set(
						VHEntry.MARKED_AS_UNTRANSLATED,
						!entry.getMetadata()
								.get(VHEntry.MARKED_AS_UNTRANSLATED));
				/*
				 * Set a different text than the current value to force the
				 * generation of the update event.
				 */
				tag.setText(""
						+ entry.getMetadata().get(
								VHEntry.MARKED_AS_UNTRANSLATED));
			}
		}) {
			@Override
			public String getText() {
				return entry.getMetadata().get(VHEntry.MARKED_AS_UNTRANSLATED) ? "-"
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
		return entry.getMetadata().get(VHEntry.MARKED_AS_UNTRANSLATED) != isMarked;
	}

	public void save() {
		isMarked = entry.getMetadata().get(VHEntry.MARKED_AS_UNTRANSLATED);
	}
}
