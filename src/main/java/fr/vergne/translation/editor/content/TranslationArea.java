package fr.vergne.translation.editor.content;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMetadata.Field;

@SuppressWarnings("serial")
public class TranslationArea extends JTextArea {

	private static final String ACTION_UNDO = "undo";
	private static final String ACTION_REDO = "redo";
	private final TranslationEntry<?> entry;
	private final TreeSet<Integer> limits;
	private static final Color colorMin = Color.LIGHT_GRAY;
	private static final Color colorMax = Color.RED;

	public TranslationArea(TranslationEntry<?> entry,
			Collection<Integer> characterLimits) {
		super(entry.getCurrentTranslation());
		this.entry = entry;
		this.limits = new TreeSet<Integer>(characterLimits);
		setBorder(new EtchedBorder());
		setFont(new Font("monospaced", Font.PLAIN, getFont().getSize()));

		final UndoManager manager = new UndoManager();
		getDocument().addUndoableEditListener(new UndoableEditListener() {

			@Override
			public void undoableEditHappened(UndoableEditEvent event) {
				manager.addEdit(new DocumentEdit(event.getEdit()));
			}
		});
		getActionMap().put(ACTION_UNDO, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (manager.canUndo()) {
					manager.undo();
				} else {
					// don't undo
				}
			}
		});
		getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit
						.getDefaultToolkit().getMenuShortcutKeyMask()),
				ACTION_UNDO);
		getActionMap().put(ACTION_REDO, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (manager.canRedo()) {
					manager.redo();
				} else {
					// don't undo
				}
			}
		});
		getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit
						.getDefaultToolkit().getMenuShortcutKeyMask()),
				ACTION_REDO);
	}

	public static Collection<Integer> retrieveLimits(TranslationEntry<?> entry,
			Collection<Field<Integer>> limitFields) {
		Collection<Integer> limits = new HashSet<Integer>();
		for (Field<Integer> field : limitFields) {
			limits.add(entry.getMetadata().get(field));
		}
		limits.remove(null);
		return limits;
	}

	public TranslationArea(TranslationEntry<?> entry) {
		this(entry, Collections.<Integer> emptyList());
	}

	public void save() {
		entry.setCurrentTranslation(getText());
	}

	public void reset() {
		setText(entry.getCurrentTranslation());
	}

	public boolean isModified() {
		return !getText().equals(entry.getCurrentTranslation());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int charWidth = g.getFontMetrics().charWidth('m');
		if (limits.isEmpty()) {
			// no limit to draw
		} else if (limits.size() == 1) {
			Integer charLimit = limits.first();
			Integer pixelLimit = charWidth * charLimit;
			g.setColor(colorMax);
			g.drawLine(pixelLimit, 0, pixelLimit, getHeight());
		} else {
			Integer lowLimit = limits.first();
			Integer highLimit = limits.last();
			for (Integer charLimit : limits) {
				Integer pixelLimit = charWidth * charLimit;
				g.setColor(weightedColor(lowLimit, charLimit, highLimit));
				g.drawLine(pixelLimit, 0, pixelLimit, getHeight());
			}
		}
	}

	private Color weightedColor(int min, int current, int max) {
		int rMin = colorMin.getRed();
		int gMin = colorMin.getGreen();
		int bMin = colorMin.getBlue();

		int rMax = colorMax.getRed();
		int gMax = colorMax.getGreen();
		int bMax = colorMax.getBlue();

		double factor = (double) (current - min) / (max - min);

		int r = average(rMin, rMax, factor);
		int g = average(gMin, gMax, factor);
		int b = average(bMin, bMax, factor);

		return new Color(r, g, b);
	}

	private int average(int rMin, int rMax, double factor) {
		return (int) ((1 - factor) * rMin + factor * rMax);
	}

	private static class DocumentEdit extends CompoundEdit {

		private boolean isUndone = false;

		public DocumentEdit(UndoableEdit edit) {
			addEdit(edit);
		}

		@Override
		public boolean addEdit(UndoableEdit edit) {
			DefaultDocumentEvent docEvent;
			if (edit instanceof DocumentEdit) {
				docEvent = (DefaultDocumentEvent) ((DocumentEdit) edit)
						.lastEdit();
			} else if (edit instanceof DefaultDocumentEvent) {
				docEvent = (DefaultDocumentEvent) edit;
			} else {
				throw new IllegalArgumentException("Not managed edit: "
						+ edit.getClass());
			}
			DefaultDocumentEvent previousEvent = (DefaultDocumentEvent) lastEdit();
			if (previousEvent == null) {
				// start of the edit
				return super.addEdit(docEvent);
			} else if (docEvent.getOffset() == previousEvent.getOffset()
					+ previousEvent.getLength()) {
				// continuation of the edit
				return super.addEdit(docEvent);
			} else {
				end();
				return false;
			}
		}

		@Override
		public void undo() throws CannotUndoException {
			end();
			super.undo();
			isUndone = true;
		}

		@Override
		public boolean canUndo() {
			return !isUndone;
		}

		@Override
		public boolean canRedo() {
			return isUndone;
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			isUndone = false;
		}
	}

	public TranslationEntry<?> getEntry() {
		return entry;
	}
}
