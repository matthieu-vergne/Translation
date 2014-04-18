package fr.sazaju.vheditor.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.util.Listener;

@SuppressWarnings("serial")
public class MapToolsPanel extends JPanel {

	public MapToolsPanel() {
		JButton previous = new JButton(new AbstractAction("<") {

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
		});
		previous.setToolTipText("Go to previous entry");
		
		JButton next = new JButton(new AbstractAction(">") {

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
		});
		next.setToolTipText("Go to next entry");
		
		JButton first = new JButton(new AbstractAction("|<") {

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
		});
		first.setToolTipText("Go to first entry");
		
		JButton last = new JButton(new AbstractAction(">|") {

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
		});
		last.setToolTipText("Go to last entry");
		
		JButton untranslated = new JButton(new AbstractAction("Jap only") {

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
		});
		untranslated.setToolTipText("Go to next untranslated entry");
		
		setBorder(new EtchedBorder());
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		add(previous, constraints);
		constraints.gridx = 1;
		constraints.gridy = 0;
		add(next, constraints);
		constraints.gridx = 0;
		constraints.gridy = 1;
		add(first, constraints);
		constraints.gridx = 1;
		constraints.gridy = 1;
		add(last, constraints);
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		add(untranslated, constraints);
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
