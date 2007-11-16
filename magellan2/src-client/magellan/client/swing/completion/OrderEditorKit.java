/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.client.swing.completion;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TextAction;
import javax.swing.text.Utilities;

/**
 * This object is designed to change the default behaviour of JTextPane. I (Ilja Pavkovic)
 * personally dislike the way &lt;CTRL&gt;-&lt;Right&gt; and &lt;CTRL&gt;-&lt;Left;gt; is handled.
 * Therefore I changed NextWordAction and PreviousWordAction.
 */
public class OrderEditorKit extends StyledEditorKit {
	/** DOCUMENT-ME */
	public static final String copyLineActionKeyStroke = "ctrl shift C";

	/** DOCUMENT-ME */
	public static final String copyLineAction = "copy-line-to-clipboard";
	private static final Action defaultActions[] = {
													   new CopyLineAction(),
													   new PreviousWordAction(previousWordAction,
																			  false), // CTRL-Left
	new NextWordAction(nextWordAction, false), // CTRL-Right
	new PreviousWordAction(selectionPreviousWordAction, true), // CTRL-Left selected
	new NextWordAction(selectionNextWordAction, true) // CTRL-Right selected
												   };

	/**
	 * Creates a new OrderEditorKit object.
	 */
	public OrderEditorKit() {
		super();
	}

	/**
	 * Fetches the order list for the editor.  This is the list of orders supported by the
	 * superclass augmented by the collection of orders defined locally for style operations.
	 *
	 * @return the order list
	 */
	public Action[] getActions() {
		return TextAction.augmentList(super.getActions(), OrderEditorKit.defaultActions);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * @author $Author: $
	 * @version $Revision: 171 $
	 */
	public static class CopyLineAction extends TextAction {
		/**
		 * Creates a new CopyLineAction object.
		 */
		public CopyLineAction() {
			super(copyLineAction);
		}

		/**
		 * The operation to perform when this action is triggered.
		 *
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			JTextComponent target = getTextComponent(e);
			int caret = target.getCaretPosition();
			String text = target.getText();

			int from = text.substring(0, caret).lastIndexOf("\n") + 1;

			int to = text.indexOf("\n", caret);

			if(to == -1) {
				to = text.length();
			}

			String toCopy = text.substring(from, to);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(toCopy),
																		 null);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * @author $Author: $
	 * @version $Revision: 171 $
	 */
	public static class NextWordAction extends TextAction {
		private boolean select;

		/**
		 * Create this action with the appropriate identifier.
		 *
		 * @param nm the name of the action, Action.NAME.
		 * @param select whether to extend the selection when changing the caret position.
		 */
		NextWordAction(String nm, boolean select) {
			super(nm);
			this.select = select;
		}

		/**
		 * The operation to perform when this action is triggered.
		 *
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			JTextComponent target = getTextComponent(e);

			if(target != null) {
				int oldPos = target.getCaretPosition();
				int rowEnd = -1;
				int newPos = -1;

				try {
					rowEnd = Utilities.getRowEnd(target, oldPos);
					newPos = Utilities.getNextWord(target, oldPos);
				} catch(BadLocationException bl) {
					// right now we don't beep (as default) but (possibly) move the pointer away
					//           target.getToolkit().beep();
				}

				int offs = newPos;

				if((newPos == -1) || (newPos > rowEnd)) {
					// newPos cannot be determined or it is beyond the rowEnd,
					// so we choose the rowEnd.this will possibly changed
					offs = rowEnd;
				}

				if(offs == oldPos) {
					// now, the position obviously did not change, so we manually move to next row
					offs++;
				}

				if(oldPos == target.getText().length()) {
					target.getToolkit().beep();
				} else {
					if(select) {
						target.moveCaretPosition(offs);
					} else {
						target.setCaretPosition(offs);
					}
				}
			}
		}
	}

	/*
	 * Position the caret to the previousning of the word.
	 * @see DefaultEditorKit#previousWordAction
	 * @see DefaultEditorKit#selectPreviousWordAction
	 * @see DefaultEditorKit#getActions
	 */
	static class PreviousWordAction extends TextAction {
		private boolean select;

		/**
		 * Create this action with the appropriate identifier.
		 *
		 * @param nm the name of the action, Action.NAME.
		 * @param select whether to extend the selection when changing the caret position.
		 */
		PreviousWordAction(String nm, boolean select) {
			super(nm);
			this.select = select;
		}

		/**
		 * The operation to perform when this action is triggered.
		 *
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			JTextComponent target = getTextComponent(e);

			if(target != null) {
				int oldPos = target.getCaretPosition();
				int rowStart = -1;
				int newPos = -1;

				try {
					rowStart = Utilities.getParagraphElement(target, oldPos).getStartOffset();
					newPos = Utilities.getPreviousWord(target, oldPos);
				} catch(BadLocationException bl) {
				}

				int offs = newPos;

				if((newPos == -1) || (newPos < rowStart)) {
					// newPos cannot be determined or it is in front of rowStart,
					// so we choose the rowStart. this will possibly changed
					offs = rowStart;
				}

				if(offs == oldPos) {
					// the cursor did not move, so we force to go up one line
					offs--;
				}

				if(offs < 0) {
					target.getToolkit().beep();
				} else {
					if(select) {
						target.moveCaretPosition(offs);
					} else {
						target.setCaretPosition(offs);
					}
				}
			}
		}
	}
}
