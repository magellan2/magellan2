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
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import magellan.client.completion.AutoCompletion;
import magellan.library.completion.Completion;
import magellan.library.utils.Resources;

/**
 * completion GUI that shows a list of entries in a floating window.
 *
 * @author Andreas
 * @version 1.0
 */
public class ListCompletionGUI extends AbstractCompletionGUI {
  protected ListPane listPane;
  protected AutoCompletion autoCompletion;
  protected int specialKeys[];
  private Point position;

  /**
   * @see magellan.client.swing.completion.CompletionGUI#init(magellan.client.completion.AutoCompletion)
   */
  public void init(AutoCompletion ac) {
    autoCompletion = ac;
    specialKeys = new int[2];
    specialKeys[0] = KeyEvent.VK_UP;
    specialKeys[1] = KeyEvent.VK_DOWN;
  }

  /**
   * @see magellan.client.swing.completion.CompletionGUI#offerCompletion(javax.swing.text.JTextComponent,
   *      java.util.Collection, java.lang.String)
   */
  public void
      offerCompletion(JTextComponent editor, Collection<Completion> completions, String stub) {
    if (listPane == null) {
      listPane = new ListPane(SwingUtilities.windowForComponent(editor));
    }

    listPane.choiceList.setListData(completions.toArray());
    listPane.choiceList.setSelectedIndex(0);
    listPane.choiceList.setVisibleRowCount(0);
    listPane.setSelectedIndex(0);

    // align list pane
    try {
      // this might cause an NPE under certain circumstances, see Mantis bug #297
      Rectangle caretBounds = editor.modelToView(editor.getCaretPosition());
      Point p = new Point(editor.getLocationOnScreen());
      p.translate(caretBounds.x, caretBounds.y + caretBounds.height);

      if ((p.getY() + listPane.getHeight()) > Toolkit.getDefaultToolkit().getScreenSize()
          .getHeight()) {
        p.translate(0, (int) (-listPane.getHeight() - caretBounds.getHeight()));
      }

      int outOfSight =
          (int) ((p.getX() + listPane.getWidth()) - Toolkit.getDefaultToolkit().getScreenSize()
              .getWidth());

      if (outOfSight > 0) {
        p.translate(-outOfSight, 0);
      }
      updatePosition(p);
      if (!listPane.isVisible()) {
        listPane.setLocation(position);
        listPane.setVisible(true);
        listPane.toFront();
      }
    } catch (BadLocationException ble) {
      ble.printStackTrace();
      hidePane();
    }

  }

  private void updatePosition(Point p) {
    if (position == null || position.y != p.y) {
      // we were invisible or new line: draw at new location
      listPane.setLocation(p);
      position = p;
    } else {
      // same line: keep position
    }
  }

  /**
   * sets the currently selected index in the list
   *
   * @see magellan.client.swing.completion.CompletionGUI#cycleCompletion(javax.swing.text.JTextComponent,
   *      java.util.Collection, java.lang.String, int)
   */
  public void cycleCompletion(JTextComponent editor, Collection<Completion> completions,
      String stub, int index) {
    listPane.setSelectedIndex(index);
  }

  /**
   * @see magellan.client.swing.completion.CompletionGUI#stopOffer()
   */
  public void stopOffer() {
    hidePane();
    position = null;
  }

  private void hidePane() {
    if (listPane != null) {
      listPane.setVisible(false);
      listPane.dispose();
      listPane = null;
    }
  }

  /**
   * @return the currently selected item from the completion list
   * @see magellan.client.swing.completion.CompletionGUI#getSelectedCompletion()
   */
  public Completion getSelectedCompletion() {
    return listPane == null ? null : (Completion) listPane.choiceList.getSelectedValue();
  }

  /**
   * Inserts a completion triggered by the Choice list
   */
  protected void insertCompletion() {
    autoCompletion.insertCompletion((Completion) listPane.choiceList.getSelectedValue());
  }

  /**
   * Extends JList with a KeyListener which handles the key and mouse events for the completion
   * list, i.e. VK_TAB, VK_ESC and VK
   */
  class CompletionList extends JList implements KeyListener {
    /**
     * Creates a new CompletionList object.
     */
    public CompletionList() {
      addKeyListener(this);
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          CompletionList.this.mouseClicked(e);
        }
      });
    }

    /**
     * DOCUMENT-ME
     *
     * @deprecated Deprecated. As of 1.4, replaced by Component.setFocusTraversalKeys(int, Set) and
     *             Container.setFocusCycleRoot(boolean).
     */
    @Deprecated
    @Override
    public boolean isManagingFocus() {
      return true;
    }

    /**
     * Manage key Events for the list.
     *
     * @param e
     */
    public void keyPressed(KeyEvent e) {
      if ((e.getKeyCode() == KeyEvent.VK_TAB) || (e.getKeyCode() == KeyEvent.VK_ENTER)) {
        insertCompletion();
        getTopLevelAncestor().setVisible(false);
        e.consume();
      }

      if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        getTopLevelAncestor().setVisible(false);
        e.consume();
      }
    }

    /**
     * We are not interested in KeyReleased events.
     *
     * @param e
     */
    public void keyReleased(KeyEvent e) {
      // ignored
    }

    /**
     * We are not interested in KeyReleased events.
     *
     * @param e The event that just happened.
     */
    public void keyTyped(KeyEvent e) {
      // ignored
    }

    private void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
        insertCompletion();
        getTopLevelAncestor().setVisible(false);
        e.consume();
      }
    }
  }

  /**
   * A floating window that holds a list of choices.
   */
  class ListPane extends JWindow {
    /**
     * The list components that displays the selection.
     */
    public CompletionList choiceList = null;

    /**
     * Creates a new ListPane object.
     *
     * @param parent
     */
    public ListPane(Window parent) {
      super(parent);

      setFocusableWindowState(false);

      JScrollPane scrollPane = new JScrollPane();

      scrollPane.setCursor(Cursor.getDefaultCursor());
      choiceList = new CompletionList();
      int size = choiceList.getFont().getSize() * 9 / 10;
      choiceList.setFont(new Font("Monospaced", Font.PLAIN, size));
      choiceList.setBackground(new Color(255, 255, 204));
      choiceList.setMinimumSize(new Dimension(size * 6, size * 3));
      choiceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      scrollPane.setViewportView(choiceList);
      scrollPane.setBounds(new Rectangle(0, 100, 150, 75));
      scrollPane.setMaximumSize(new Dimension(size * 10, size * 5));

      getContentPane().add(scrollPane);

      pack();

      addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
          choiceList.requestFocusInWindow();
        }
      });
    }

    /**
     * Set the selected index to <kbd>index</kbd>.
     *
     * @param index The index we want to select.
     */
    public void setSelectedIndex(int index) {
      choiceList.setSelectedIndex(index);
      choiceList.ensureIndexIsVisible(index);
    }
  }

  /**
   * @see magellan.client.swing.completion.CompletionGUI#getSpecialKeys()
   */
  public int[] getSpecialKeys() {
    return specialKeys;
  }

  /**
   * @see magellan.client.swing.completion.CompletionGUI#specialKeyPressed(int)
   */
  public void specialKeyPressed(int key) {
    if (listPane != null && listPane.isVisible()) {
      listPane.requestFocusInWindow();
    }
  }

  /**
   * @see magellan.client.swing.completion.CompletionGUI#isOfferingCompletion()
   */
  public boolean isOfferingCompletion() {
    return listPane != null && listPane.isVisible();
  }

  /**
   * Since Java 1.4 this cannot happen. Returns false.
   */
  public boolean editorMayLoseFocus() {
    return false;
  }

  /**
   * Returns false.
   *
   * @see magellan.client.swing.completion.CompletionGUI#editorMayUpdateCaret()
   */
  public boolean editorMayUpdateCaret() {
    return false;
  }

  /**
   * @see magellan.client.swing.completion.AbstractCompletionGUI#getTitle()
   */
  @Override
  public String getTitle() {
    return Resources.get("completion.listcompletiongui.gui.title");
  }
}
