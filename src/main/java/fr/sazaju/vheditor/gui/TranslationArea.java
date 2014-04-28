package fr.sazaju.vheditor.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.translation.TranslationEntry;

@SuppressWarnings("serial")
public class TranslationArea extends JTextArea {

	private final TranslationEntry entry;
	private final TreeSet<Integer> limits;
	private static final List<Color> limitColors = Arrays.asList(Color.RED,
			Color.LIGHT_GRAY);

	public TranslationArea(TranslationEntry entry) {
		super(entry.getTranslatedVersion());
		this.entry = entry;
		limits = new TreeSet<Integer>(new Comparator<Integer>() {

			@Override
			public int compare(Integer i1, Integer i2) {
				return i2.compareTo(i1);
			}
		}) {
			@Override
			public boolean add(Integer e) {
				if (e == null) {
					return true;
				} else {
					return super.add(e);
				}
			}
		};
		limits.add(entry.getCharLimit(false));
		limits.add(entry.getCharLimit(true));
		setBorder(new EtchedBorder());
		setFont(new Font("monospaced", Font.PLAIN, getFont().getSize()));
	}

	public void save() {
		entry.setTranslatedVersion(getText());
	}

	public void reset() {
		setText(entry.getTranslatedVersion());
	}

	public boolean isModified() {
		return !getText().equals(entry.getTranslatedVersion());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int charWidth = g.getFontMetrics().charWidth('m');
		Iterator<Integer> limitIterator = limits.iterator();
		Iterator<Color> colorIterator = limitColors.iterator();
		while (limitIterator.hasNext()) {
			Integer limit = charWidth * limitIterator.next();
			g.setColor(colorIterator.next());
			g.drawLine(limit, 0, limit, getHeight());
		}
	}
}
