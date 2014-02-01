package com.vh.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import com.vh.translation.TranslationEntry;
import com.vh.util.Listener;

@SuppressWarnings("serial")
public class MapEntryPanel extends JPanel {

	private final JTextArea originalArea;
	private final JTextArea translationArea;
	private final JLabel limitLabel;
	private TranslationEntry entry;

	public MapEntryPanel() {
		setBorder(new EtchedBorder());

		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.gridx = 0;
		constraints.gridy = 0;

		add(new JLabel("Japanese"), constraints);

		constraints.gridy = 2;
		add(new JLabel("English"), constraints);

		constraints.gridy = 4;
		limitLabel = new JLabel();
		add(limitLabel, constraints);

		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		originalArea = createEditionArea();
		originalArea.setEditable(false);
		add(originalArea, constraints);

		constraints.gridy = 3;
		translationArea = createEditionArea();
		add(translationArea, constraints);

		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.VERTICAL;
		constraints.weighty = 1;
		constraints.gridx = 1;
		constraints.gridheight = 5;
		add(buildBrowsingButtons(), constraints);
	}

	private JPanel buildBrowsingButtons() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 1, 0, 5));
		panel.add(new JButton(new AbstractAction("First") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Listener listener : listeners) {
					if (listener instanceof FirstEntryListener) {
						listener.eventGenerated();
					} else {
						continue;
					}
				}
			}
		}));
		panel.add(new JButton(new AbstractAction("Prev.") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Listener listener : listeners) {
					if (listener instanceof PreviousEntryListener) {
						listener.eventGenerated();
					} else {
						continue;
					}
				}
			}
		}));
		panel.add(new JButton(new AbstractAction("Next") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Listener listener : listeners) {
					if (listener instanceof NextEntryListener) {
						listener.eventGenerated();
					} else {
						continue;
					}
				}
			}
		}));
		panel.add(new JButton(new AbstractAction("Last") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Listener listener : listeners) {
					if (listener instanceof LastEntryListener) {
						listener.eventGenerated();
					} else {
						continue;
					}
				}
			}
		}));
		panel.add(new JButton(new AbstractAction("Untr.") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Listener listener : listeners) {
					if (listener instanceof UntranslatedEntryListener) {
						listener.eventGenerated();
					} else {
						continue;
					}
				}
			}
		}));
		return panel;
	}

	private JTextArea createEditionArea() {
		JTextArea editionArea = new JTextArea();
		editionArea.setBorder(new EtchedBorder());
		return editionArea;
	}

	public void setEntry(TranslationEntry entry) {
		Integer charLimitFace = entry.getCharLimit(true);
		Integer charLimitNoFace = entry.getCharLimit(false);
		String context = entry.getContext();
		boolean isUnused = entry.isUnused();

		String limit;
		if (charLimitFace == null && charLimitNoFace == null) {
			limit = "";
		} else if (charLimitFace == null && charLimitNoFace != null) {
			limit = charLimitNoFace.toString();
		} else if (charLimitFace != null && charLimitNoFace == null) {
			limit = charLimitFace.toString();
		} else if (charLimitFace != null && charLimitNoFace != null) {
			/*
			 * TODO replace obvious condition by face displayed or not (3 cases:
			 * yes, no, unknown).
			 */
			limit = charLimitFace + "-" + charLimitNoFace;
		} else {
			throw new IllegalStateException("This case should not happen.");
		}
		limitLabel.setText("Limits: "
				+ (limit.isEmpty() ? "none" : limit + " chars"));
		originalArea.setText(entry.getOriginalVersion());
		translationArea.setText(entry.getTranslatedVersion());
		this.entry = entry;
	}

	private final Collection<Listener> listeners = new LinkedList<>();

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	public static interface NextEntryListener extends Listener {
	}

	public static interface PreviousEntryListener extends Listener {
	}

	public static interface FirstEntryListener extends Listener {
	}

	public static interface LastEntryListener extends Listener {
	}

	public static interface UntranslatedEntryListener extends Listener {
	}
}
