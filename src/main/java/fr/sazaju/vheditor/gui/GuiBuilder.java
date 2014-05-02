package fr.sazaju.vheditor.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.parsing.AdviceLine;
import fr.sazaju.vheditor.translation.parsing.BackedTranslationMap;
import fr.sazaju.vheditor.translation.parsing.ContentBlock;
import fr.sazaju.vheditor.translation.parsing.ContextLine;
import fr.sazaju.vheditor.translation.parsing.EndLine;
import fr.sazaju.vheditor.translation.parsing.EntryLoop;
import fr.sazaju.vheditor.translation.parsing.MapEntry;
import fr.sazaju.vheditor.translation.parsing.MapHeader;
import fr.sazaju.vheditor.translation.parsing.StartLine;
import fr.sazaju.vheditor.translation.parsing.TranslationLine;
import fr.sazaju.vheditor.translation.parsing.UntranslatedLine;
import fr.sazaju.vheditor.translation.parsing.UnusedTransLine;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.impl.Loop;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Newline;
import fr.vergne.parsing.layer.impl.base.Option;

public class GuiBuilder {

	private static final Color UNUSED_COLOR = Color.MAGENTA;

	public static Component instantiateMapGui(Layer layer) {
		if (layer instanceof MapHeader || layer instanceof UnusedTransLine) {
			return new JLabel(layer.getContent());
		} else if (layer instanceof MapEntry) {
			// use a specific panel to simplify the map browsing
			JPanel panel = new EntryPanel();
			panel.setOpaque(false);
			panel.setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.anchor = GridBagConstraints.LINE_START;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 1;
			MapEntry mapEntry = (MapEntry) layer;
			panel.add(instantiateEntryGui(mapEntry.get(0), mapEntry),
					constraints);
			panel.add(instantiateEntryGui(mapEntry.get(1), mapEntry),
					constraints);
			panel.add(instantiateEntryGui(mapEntry.get(2), mapEntry),
					constraints);
			panel.add(instantiateEntryGui(mapEntry.get(3), mapEntry),
					constraints);
			JTextArea original = new JTextArea(mapEntry.getOriginalVersion());
			original.setEditable(false);
			panel.add(original, constraints);
			panel.add(instantiateEntryGui(mapEntry.get(5), mapEntry),
					constraints);
			panel.add(new TranslationArea(mapEntry), constraints);
			panel.add(instantiateEntryGui(mapEntry.get(7), mapEntry),
					constraints);
			return panel;
		} else if (layer instanceof EntryLoop) {
			JPanel panel = new JPanel();
			panel.setOpaque(false);
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			EntryLoop loop = (EntryLoop) layer;
			for (MapEntry mapEntry : loop) {
				panel.add(instantiateMapGui(mapEntry));
			}
			return panel;
		} else if (layer instanceof BackedTranslationMap) {
			JPanel panel = new JPanel();
			panel.setOpaque(false);
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			BackedTranslationMap map = (BackedTranslationMap) layer;
			panel.add(instantiateMapGui(map.get(0)));
			panel.add(instantiateMapGui(map.get(1)));

			Option<Suite> option = map.get(2);
			if (option.isPresent()) {
				Component unusedLine = instantiateMapGui(option.getOption()
						.get(0));
				unusedLine.setBackground(UNUSED_COLOR);
				panel.add(unusedLine);
				JPanel unusedPanel = (JPanel) instantiateMapGui(option
						.getOption().get(1));
				unusedPanel.setBackground(UNUSED_COLOR);
				unusedPanel.setOpaque(true);
				panel.add(unusedPanel);
			} else {
				// no unused part
			}
			return panel;
		} else {
			throw new IllegalArgumentException("Unmanaged Layer: "
					+ layer.getClass());
		}
	}

	public static Component instantiateEntryGui(Layer layer,
			TranslationEntry entry) {
		if (layer instanceof StartLine || layer instanceof ContextLine
				|| layer instanceof AdviceLine
				|| layer instanceof TranslationLine) {
			return new JLabel(layer.getContent());
		} else if (layer instanceof EndLine) {
			JPanel panel = new JPanel();
			panel.setOpaque(false);
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			panel.setOpaque(false);

			EndLine end = (EndLine) layer;
			panel.add(new JLabel(layer.getContent()));

			Loop<Newline> newlines = end.get(1);
			for (int i = 1; i < newlines.size(); i++) {
				panel.add(new JLabel(" "));
			}
			return panel;
		} else if (layer instanceof ContentBlock) {
			String clazz = layer.getClass().getSimpleName();
			throw new IllegalArgumentException(
					clazz
							+ " elements have different implementations. Use this method on a complete entry rather than on the specific "
							+ clazz + ".");
		} else if (layer instanceof Option) {
			Option<?> option = (Option<?>) layer;
			Layer sublayer = option.getOption();
			if (sublayer instanceof UntranslatedLine) {
				return new TranslationTag(entry);
			} else if (sublayer instanceof AdviceLine) {
				return option.isPresent() ? instantiateEntryGui(sublayer, entry)
						: new JLabel();
			} else {
				throw new IllegalArgumentException("Unmanaged Option: "
						+ sublayer.getClass());
			}
		} else {
			throw new IllegalArgumentException("Unmanaged Layer: "
					+ layer.getClass());
		}
	}

	@SuppressWarnings("serial")
	public static class EntryPanel extends JPanel {

		public TranslationArea getTranslationArea() {
			return (TranslationArea) getComponent(6);
		}
	}
}
