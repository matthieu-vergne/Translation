package fr.sazaju.vheditor.parsing.vh.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import fr.sazaju.vheditor.gui.content.EntryComponentFactory.EntryComponent;
import fr.sazaju.vheditor.gui.content.MapComponentFactory.MapComponent;
import fr.sazaju.vheditor.gui.content.TranslationArea;
import fr.sazaju.vheditor.parsing.vh.map.AdviceLine;
import fr.sazaju.vheditor.parsing.vh.map.ContentBlock;
import fr.sazaju.vheditor.parsing.vh.map.ContextLine;
import fr.sazaju.vheditor.parsing.vh.map.EndLine;
import fr.sazaju.vheditor.parsing.vh.map.EntryLoop;
import fr.sazaju.vheditor.parsing.vh.map.MapHeader;
import fr.sazaju.vheditor.parsing.vh.map.StartLine;
import fr.sazaju.vheditor.parsing.vh.map.TranslationLine;
import fr.sazaju.vheditor.parsing.vh.map.UntranslatedLine;
import fr.sazaju.vheditor.parsing.vh.map.UnusedTransLine;
import fr.sazaju.vheditor.parsing.vh.map.VHEntry;
import fr.sazaju.vheditor.parsing.vh.map.VHMap;
import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class VHGuiBuilder {

	private static final Color UNUSED_COLOR = Color.MAGENTA;

	public static Component instantiateMapGui(VHMap map) {
		MapPanel panel = new MapPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = GridBagConstraints.RELATIVE;

		MapHeader header = map.get(0);
		JLabel headerLine = new JLabel(header.getContent());
		panel.add(headerLine, constraints);

		for (VHEntry mapEntry : (EntryLoop) map.get(1)) {
			panel.add(instantiateEntryGui(mapEntry), constraints);
		}

		Option<Suite> option = map.get(2);
		if (!option.isPresent()) {
			// no unused entries
		} else {
			UnusedTransLine unusedHeader = option.getOption().get(0);
			JLabel unusedLabel = new JLabel(unusedHeader.getContent());
			unusedLabel.setOpaque(true);
			unusedLabel.setBackground(UNUSED_COLOR);
			panel.add(unusedLabel, constraints);

			JPanel unusedPanel = new JPanel();
			unusedPanel.setOpaque(true);
			unusedPanel.setBackground(UNUSED_COLOR);
			unusedPanel.setLayout(new BoxLayout(unusedPanel,
					BoxLayout.PAGE_AXIS));
			unusedPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
			for (VHEntry mapEntry : (EntryLoop) option.getOption().get(1)) {
				unusedPanel.add(instantiateEntryGui(mapEntry));
			}
			panel.add(unusedPanel, constraints);
		}
		return panel;
	}

	private static Component instantiateEntryGui(VHEntry mapEntry) {
		EntryPanel panel = new EntryPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		panel.add(instantiateEntryGui(mapEntry.get(0), mapEntry), constraints);
		panel.add(instantiateEntryGui(mapEntry.get(1), mapEntry), constraints);
		panel.add(instantiateEntryGui(mapEntry.get(2), mapEntry), constraints);
		panel.add(instantiateEntryGui(mapEntry.get(3), mapEntry), constraints);
		JTextArea original = new JTextArea(mapEntry.getOriginalContent());
		original.setEditable(false);
		panel.add(original, constraints);
		panel.add(instantiateEntryGui(mapEntry.get(5), mapEntry), constraints);
		Collection<Integer> limits = TranslationArea.retrieveLimits(mapEntry,
				Arrays.asList(VHEntry.CHAR_LIMIT_NO_FACE,
						VHEntry.CHAR_LIMIT_FACE));
		panel.add(new TranslationArea(mapEntry, limits), constraints);
		panel.add(instantiateEntryGui(mapEntry.get(7), mapEntry), constraints);
		return panel;
	}

	public static Component instantiateEntryGui(Layer layer,
			TranslationEntry<?> entry) {
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
				return new TranslationTag<TranslationEntry<?>>(entry);
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
	public static class EntryPanel extends JPanel implements EntryComponent {

		@Override
		public TranslationArea getTranslationComponent() {
			return (TranslationArea) getComponent(6);
		}

		public TranslationEntry<?> getEntry() {
			return getTranslationComponent().getEntry();
		}

		@SuppressWarnings("unchecked")
		public TranslationTag<TranslationEntry<?>> getTranslationTag() {
			return (TranslationTag<TranslationEntry<?>>) getComponent(1);
		}
	}

	@SuppressWarnings("serial")
	public static class MapPanel extends JPanel implements MapComponent {

		@Override
		public EntryComponent getEntryComponent(int index) {
			return (EntryComponent) getComponent(index + 1);
		}
	}
}
