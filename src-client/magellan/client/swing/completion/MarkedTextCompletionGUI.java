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

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;

import magellan.client.completion.AutoCompletion;
import magellan.library.completion.Completion;
import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class MarkedTextCompletionGUI extends AbstractCompletionGUI implements KeyListener {
  protected boolean addLinebreak = false;
  protected Completion lastCompletion = null;
  protected JTextComponent lastEditor = null;
  protected boolean markedText = false;
  protected boolean caretUpdate = false;
  protected int selectedArea[];
  protected AutoCompletion ac;

  /**
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(java.awt.event.KeyEvent p1) {
    //
  }

  /**
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  public void keyPressed(java.awt.event.KeyEvent e) {
    if (lastEditor == null) { // forget to remove listener?

      if (e.getSource() instanceof Component) {
        ((Component) e.getSource()).removeKeyListener(this);
      }

      return;
    }

    if (!markedText)
      return;

    int code = e.getKeyCode();

    // no "real" key
    if (e.isActionKey())
      return;

    if ((code == KeyEvent.VK_CONTROL) || (code == KeyEvent.VK_SHIFT) || (code == KeyEvent.VK_ALT)
        || (code == KeyEvent.VK_TAB))
      return;

    for (int i = 0; i < AutoCompletion.CK_START; i++) {
      if (ac.isCompleterKey(e, i))
        return;
    }

    // delete our selection
    markedText = false;
  }

  /**
   */
  public void keyTyped(java.awt.event.KeyEvent p1) {
    //
  }

  /**
   * Initialize this GUI for use with the given AutoCompletion. This method is called by
   * AutoCompletion when the GUI is added to it.
   */
  public void init(AutoCompletion aac) {
    ac = aac;
    selectedArea = new int[2];
    selectedArea[0] = -1;
  }

  /**
   * Should return true if this GUI is currently offering a completion to the user.
   */
  public boolean isOfferingCompletion() {
    return markedText;
  }

  /**
   * Called the advice this GUI to offer the given completions in the given Editor to the user.
   * 
   * @see magellan.client.swing.completion.CompletionGUI#offerCompletion(javax.swing.text.JTextComponent,
   *      java.util.Collection, java.lang.String)
   */
  public void offerCompletion(JTextComponent editor, Collection<Completion> completions, String stub) {
    lastEditor = editor;
    markedText = false;
    editor.addKeyListener(this);

    Completion cmp = completions.iterator().next();

    String cpltStr = null;

    if (startsWith(cmp.getValue(), stub)) {
      cpltStr = cmp.getValue() + cmp.getPostfix();
    } else if (startsWith(cmp.getName(), stub)) {
      cpltStr = cmp.getName() + cmp.getPostfix();
    } else {
      cpltStr = cmp.getValue() + cmp.getPostfix();
    }

    // check for line break and cut it
    if (cpltStr.indexOf('\n') > -1) {
      cpltStr = cpltStr.substring(0, cpltStr.length() - 1);
    }

    if (stub.length() < cpltStr.length()) {
      try {
        int offset = editor.getCaretPosition();
        selectedArea[0] = offset;
        cpltStr = cpltStr.substring(stub.length(), cpltStr.length());
        markedText = true;
        caretUpdate = true;
        editor.getDocument().insertString(offset, cpltStr, new SimpleAttributeSet());
        caretUpdate = true;
        editor.select(offset, offset + cpltStr.length());
        selectedArea[1] = editor.getSelectionEnd();
        lastCompletion = cmp;
      } catch (Exception exc) {
        // skip
      }
    }
  }

  /**
   * Checks if s1 is starting with s2, case-insensitive
   */
  protected boolean startsWith(String s1, String s2) {
    s1 = s1.toLowerCase();
    s2 = s2.toLowerCase();

    return s1.startsWith(s2);
  }

  /**
   * @see magellan.client.swing.completion.CompletionGUI#cycleCompletion(javax.swing.text.JTextComponent,
   *      java.util.Collection, java.lang.String, int)
   */
  public void cycleCompletion(JTextComponent editor, Collection<Completion> completions,
      String stub, int index) {
    if (!markedText || (editor.getSelectionStart() < 0))
      return;

    Iterator<Completion> it = completions.iterator();
    Completion cmp = it.next();

    for (int i = 1; i <= index; i++) {
      if (!it.hasNext()) {
        it = completions.iterator();
      }

      cmp = it.next();
    }

    String cpltStr = null;

    if (cmp.getValue().startsWith(stub)) {
      cpltStr = cmp.getValue() + cmp.getPostfix();
    } else if (cmp.getName().startsWith(stub)) {
      cpltStr = cmp.getName() + cmp.getPostfix();
    } else {
      cpltStr = cmp.getValue() + cmp.getPostfix();
    }

    // check for line break and cut it
    if (cpltStr.indexOf('\n') > -1) {
      cpltStr = cpltStr.substring(0, cpltStr.length() - 1);
    }

    if (stub.length() < cpltStr.length()) {
      cpltStr = cpltStr.substring(stub.length(), cpltStr.length());

      int pos = editor.getSelectionStart();
      markedText = true;
      caretUpdate = true;
      selectedArea[0] = editor.getSelectionStart();
      lastEditor.replaceSelection(cpltStr);
      caretUpdate = true;
      editor.select(pos, pos + cpltStr.length());
      selectedArea[1] = editor.getSelectionEnd();
      lastCompletion = cmp;
      lastEditor = editor; // shouldn't be necessary
    }
  }

  /**
   * Called when this GUI should stop offering completions.
   */
  public void stopOffer() {
    if (lastEditor != null) {
      lastEditor.removeKeyListener(this);
      checkMarkedText();

      if (markedText) {
        lastEditor.replaceSelection(null);
      }
    }

    markedText = false;
  }

  /**
   * checks if the selected text is still ours(same as in selectedArea). May clear markedText flag.
   */
  protected void checkMarkedText() {
    //
  }

  /**
   * If this GUI needs some special keys the Key-Codes con be obtained by this method.
   */
  public int[] getSpecialKeys() {
    return null;
  }

  /**
   * When AutoCompletion recognizes a special key of getSpecialKeys(), this method is called with
   * the key found.
   */
  public void specialKeyPressed(int key) {
    // no keys
  }

  /**
   * If the editor my lose the focus because of a GUI action(usually after specialKeyPressed()),
   * this method should return true to avoid AutoCompletion calling stopOffer().
   */
  public boolean editorMayLoseFocus() {
    return false;
  }

  /**
   * If the editor my update the caret because of a GUI action(usually after specialKeyPressed()),
   * this method should return true to avoid AutoCompletion calling stopOffer().
   */
  public boolean editorMayUpdateCaret() {
    if (caretUpdate) {
      caretUpdate = false;

      return true;
    }

    return markedText;
  }

  /**
   * Returns the currently selected Completion object.
   */
  public Completion getSelectedCompletion() {
    return lastCompletion;
  }

  /**
   */
  @Override
  public String getTitle() {
    return Resources.get("completion.markedtextcompletiongui.gui.title");
  }
}
