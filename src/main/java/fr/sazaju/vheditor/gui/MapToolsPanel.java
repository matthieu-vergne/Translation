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

@SuppressWarnings("serial")
public class MapToolsPanel extends JPanel {

	public MapToolsPanel() {
		JButton previous = new JButton(new AbstractAction("<") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (MapToolsListener listener : listeners) {
					if (listener instanceof PreviousEntryListener) {
						listener.buttonPushed();
					} else {
						continue;
					}
				}
			}
		});
		previous.setToolTipText("Go to previous entry (ALT+LEFT).");

		JButton next = new JButton(new AbstractAction(">") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (MapToolsListener listener : listeners) {
					if (listener instanceof NextEntryListener) {
						listener.buttonPushed();
					} else {
						continue;
					}
				}
			}
		});
		next.setToolTipText("Go to next entry (ALT+RIGHT).");

		JButton first = new JButton(new AbstractAction("|<") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (MapToolsListener listener : listeners) {
					if (listener instanceof FirstEntryListener) {
						listener.buttonPushed();
					} else {
						continue;
					}
				}
			}
		});
		first.setToolTipText("Go to first entry (ALT+HOME).");

		JButton last = new JButton(new AbstractAction(">|") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (MapToolsListener listener : listeners) {
					if (listener instanceof LastEntryListener) {
						listener.buttonPushed();
					} else {
						continue;
					}
				}
			}
		});
		last.setToolTipText("Go to last entry (ALT+END).");

		JButton untranslated = new JButton(new AbstractAction("Jap only") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (MapToolsListener listener : listeners) {
					if (listener instanceof UntranslatedEntryListener) {
						listener.buttonPushed();
					} else {
						continue;
					}
				}
			}
		});
		untranslated
				.setToolTipText("Go to next untranslated entry (ALT+ENTER).");

		JButton save = new JButton(new AbstractAction("Save") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (MapToolsListener listener : listeners) {
					if (listener instanceof SaveMapListener) {
						listener.buttonPushed();
					} else {
						continue;
					}
				}
			}
		});
		save.setToolTipText("Write the modifications to the map file (CTRL+S).");

		JButton reset = new JButton(new AbstractAction("Reset") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (MapToolsListener listener : listeners) {
					if (listener instanceof ResetMapListener) {
						listener.buttonPushed();
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

	private final Collection<MapToolsListener> listeners = new LinkedList<MapToolsListener>();

	public void addListener(MapToolsListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MapToolsListener listener) {
		listeners.remove(listener);
	}

	public static interface MapToolsListener {
		public void buttonPushed();
	}

	public static interface NextEntryListener extends MapToolsListener {
	}

	public static interface PreviousEntryListener extends MapToolsListener {
	}

	public static interface FirstEntryListener extends MapToolsListener {
	}

	public static interface LastEntryListener extends MapToolsListener {
	}

	public static interface UntranslatedEntryListener extends MapToolsListener {
	}

	public static interface SaveMapListener extends MapToolsListener {
	}

	public static interface ResetMapListener extends MapToolsListener {
	}
}
