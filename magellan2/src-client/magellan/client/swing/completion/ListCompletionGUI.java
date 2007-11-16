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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import magellan.client.completion.AutoCompletion;
import magellan.library.completion.Completion;
import magellan.library.utils.JVMUtilities;
import magellan.library.utils.Resources;


/**
 * DOCUMENT-ME
 *
 * @author Andreas
 * @version 1.0
 */
public class ListCompletionGUI extends AbstractCompletionGUI {
	protected ListPane listPane;
	protected AutoCompletion ac;
	protected int specialKeys[];

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void init(AutoCompletion ac) {
		this.ac = ac;
		listPane = new ListPane();
		specialKeys = new int[2];
		specialKeys[0] = KeyEvent.VK_UP;
		specialKeys[1] = KeyEvent.VK_DOWN;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 */
	public void offerCompletion(JTextComponent editor, Collection completions, String stub) {
		listPane.choiceList.setListData(completions.toArray());
		listPane.choiceList.setSelectedIndex(0);
		listPane.choiceList.setVisibleRowCount(0);

		// align list pane
		try {
			Rectangle caretBounds = editor.modelToView(editor.getCaretPosition());
			Point p = new Point(editor.getLocationOnScreen());
			p.translate(caretBounds.x, caretBounds.y + caretBounds.height);

			if((p.getY() + listPane.getHeight()) > Toolkit.getDefaultToolkit().getScreenSize()
															  .getHeight()) {
				p.translate(0, (int) (-listPane.getHeight() - caretBounds.getHeight()));
			}

			int outOfSight = (int) ((p.getX() + listPane.getWidth()) -
							 Toolkit.getDefaultToolkit().getScreenSize().getWidth());

			if(outOfSight > 0) {
				p.translate(-outOfSight, 0);
			}

			listPane.setLocation(p);
		} catch(BadLocationException ble) {
		}

		if(!listPane.isVisible()) {
			listPane.setVisible(true);
		}
	}

	/** sets the currently selected index in the list
	 *  DOCUMENT-ME
	 *  
	 * @see magellan.client.swing.completion.CompletionGUI#cycleCompletion(javax.swing.text.JTextComponent, java.util.Collection, java.lang.String, int)
	 */
	public void cycleCompletion(JTextComponent editor, Collection completions, String stub,
								int index) {
		listPane.setSelectedIndex(index);
	}

	/**
	 * DOCUMENT-ME
	 */
	public void stopOffer() {
		listPane.setVisible(false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * @return TODO: the currently selected item from the completion list
	 */
	public Completion getSelectedCompletion() {
		return (Completion) listPane.choiceList.getSelectedValue();
	}

	/**
	 * Inserts a completion triggered by the Choice list
	 */ 
	protected void insertCompletion() {
		ac.insertCompletion((Completion) listPane.choiceList.getSelectedValue());
	}

	// pavkovic 2003.01.28: this is a Map of the default Translations mapped to this class
	// it is called by reflection (we could force the implementation of an interface,
	// this way it is more flexible.)
	// Pls use this mechanism, so the translation files can be created automagically
	// by inspecting all classes.
	private static Map<String,String> defaultTranslations;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static synchronized Map<String,String> getDefaultTranslations() {
		if(defaultTranslations == null) {
			defaultTranslations = new Hashtable<String, String>();
			defaultTranslations.put("gui.title", "List");
		}

		return defaultTranslations;
	}

	/**
	 * Extends JList with a KeyListener which handles the key and mouse
	 * events for the completion list, i.e. VK_TAB, VK_ESC and VK 
	 * 
	 */
	class CompletionList extends JList implements KeyListener {
		/**
		 * Creates a new CompletionList object.
		 */
		public CompletionList() {
			this.addKeyListener(this);
			this.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						CompletionList.this.mouseClicked(e);
					}
				});
		}

		/**
		 * DOCUMENT-ME
		 * 
		 * @deprecated Deprecated. As of 1.4, replaced by Component.setFocusTraversalKeys(int, Set) and Container.setFocusCycleRoot(boolean).
		 *
		 * 
		 */
		public boolean isManagingFocus() {
			return true;
		}

		/**
		 * We are not interested in KeyPressed events.
		 *
		 * @param e 
		 */
		public void keyPressed(KeyEvent e) {
			if((e.getKeyCode() == KeyEvent.VK_TAB) || (e.getKeyCode() == KeyEvent.VK_ENTER)) {
				insertCompletion();
				this.getTopLevelAncestor().setVisible(false);
				e.consume();
			}

			if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				this.getTopLevelAncestor().setVisible(false);
				e.consume();
			}
		}

		/**
		 * We are not interested in KeyReleased events.
		 *
		 * @param e 
		 */
		public void keyReleased(KeyEvent e) {
		}

		/**
		 * Manage key Events for the list.
		 *
		 * @param e The event that just happened.
		 */
		public void keyTyped(KeyEvent e) {
		}

		private void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) {
				insertCompletion();
				this.getTopLevelAncestor().setVisible(false);
				e.consume();
			}
		}
	}

	/**
	 * A floating window that holds a list of choices. Implements FocusListener
	 * in order to be distroyed if the "owning" window loses the focus.
	 *
	 */
	class ListPane extends JWindow { // implements FocusListener{
		/** 
		 * The list components that displays the selection. 
		 * 
		 */
		public CompletionList choiceList = null;

		/**
		 * Creates a new ListPane object.
		 */
		public ListPane() {
			super(new JFrame() {
					public boolean isShowing() {
						return true;
					}
				});

			// call setFocusableWindowState (true) on java 1.4 while staying compatible with Java 1.3
			JVMUtilities.setFocusableWindowState(this, true);

			JScrollPane scrollPane = new JScrollPane();

			scrollPane.setCursor(Cursor.getDefaultCursor());
			choiceList = new CompletionList();
			choiceList.setFont(new Font("Monospaced", Font.PLAIN, 10));
			choiceList.setBackground(new Color(255, 255, 204));
			choiceList.setMinimumSize(new Dimension(100, 50));
			choiceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			scrollPane.setViewportView(choiceList);
			scrollPane.setBounds(new Rectangle(0, 100, 150, 75));
			scrollPane.setMaximumSize(new Dimension(150, 75));

			this.getContentPane().add(scrollPane);

			this.pack();

			this.addFocusListener(new FocusAdapter() {
					public void focusGained(FocusEvent e) {
						choiceList.requestFocus();
					}
				});
		}

		/**
		 * Set the selected index to <tt>index</tt>.
		 *
		 * @param index The index we want to select.
		 */
		public void setSelectedIndex(int index) {
			choiceList.setSelectedIndex(index);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int[] getSpecialKeys() {
		return specialKeys;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void specialKeyPressed(int key) {
		if(listPane.isVisible()) {
			listPane.requestFocus();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isOfferingCompletion() {
		return listPane.isVisible();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean editorMayLoseFocus() {
		return true;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean editorMayUpdateCaret() {
		return false;
	}
  

  /**
   */
  @Override
  public String getTitle() {
    return Resources.get("completion.listcompletiongui.gui.title");
  }
}
