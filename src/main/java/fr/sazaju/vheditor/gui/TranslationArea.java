package fr.sazaju.vheditor.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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

import fr.sazaju.vheditor.translation.TranslationComment.Field;
import fr.sazaju.vheditor.translation.TranslationEntry;

@SuppressWarnings("serial")
public class TranslationArea extends JTextArea {

	private static final String ACTION_UNDO = "undo";
	private static final String ACTION_REDO = "redo";
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
		limits.add(entry.getComment().get(Field.CHAR_LIMIT_NO_FACE));
		limits.add(entry.getComment().get(Field.CHAR_LIMIT_FACE));
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

	public TranslationEntry getEntry() {
		return entry;
	}
}
