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
		previous.setToolTipText("Go to previous entry.");

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
		next.setToolTipText("Go to next entry.");

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
		first.setToolTipText("Go to first entry.");

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
		last.setToolTipText("Go to last entry.");

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
		untranslated.setToolTipText("Go to next untranslated entry.");

		JButton save = new JButton(new AbstractAction("Save") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Listener listener : listeners) {
					if (listener instanceof SaveMapListener) {
						listener.eventGenerated();
					} else {
						continue;
					}
				}
			}
		});
		save.setToolTipText("Write the modifications to the map file.");

		JButton reset = new JButton(new AbstractAction("Reset") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Listener listener : listeners) {
					if (listener instanceof ResetMapListener) {
						listener.eventGenerated();
					} else {
						continue;
					}
				}
			}
		});
		reset.setToolTipText("Cancel all the modifications.");

		setBorder(new EtchedBorder());
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		Insets closeInsets = new Insets(5, 5, 5, 5);
		Insets farInsets = new Insets(20, 5, 5, 5);
		constraints.insets = closeInsets;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;

		add(previous, constraints);
		constraints.gridx++;
		add(next, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		add(first, constraints);
		constraints.gridx++;
		add(last, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		add(untranslated, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		constraints.insets = farInsets;
		add(save, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		constraints.insets = closeInsets;
		add(reset, constraints);
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

	public static interface SaveMapListener extends Listener {
	}

	public static interface ResetMapListener extends Listener {
	}
}
