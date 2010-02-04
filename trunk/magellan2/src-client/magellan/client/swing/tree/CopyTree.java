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

package magellan.client.swing.tree;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * An extended JTree that supports CopyToClipboard by STRG+C and STRG+INSERT
 * 
 * @author Ulrich Küster
 */
public class CopyTree extends JTree implements KeyListener {

  /**
   * Creates a new CopyTree object.
   */
  public CopyTree(TreeModel model) {
    super(model);
    initTree();
  }

  /**
   * Creates a new object.
   */
  public CopyTree() {
    super();
    initTree();
  }

  /**
   * Used for initialization issues
   */
  private void initTree() {
    // delete F2-key-binding to startEditing to allow bookmarking to be activ
    // @see com.eressea.demo.desktop.BookmarkManager
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "none");

    // shortcuts = new Vector();
    // 0-1 copyshortcut
    // shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.CTRL_MASK));
    // shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
    // register for shortcut
    addKeyListener(this);
  }

  private void shortCut_Copy() {
    if (!hasFocus())
      return;

    String text = "";
    TreePath selectedPaths[] = getSelectionPaths();

    if (selectedPaths != null) {
      for (TreePath selectedPath : selectedPaths) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();

        if (node != null) {
          Object obj = node.getUserObject();

          if (obj instanceof SupportsClipboard) {
            SupportsClipboard s = (SupportsClipboard) obj;

            if (text.length() == 0) {
              text = s.getClipboardValue();
            } else {
              text += ("\n" + s.getClipboardValue());
            }
          } else {
            if (text.length() == 0) {
              text = obj.toString();
            } else {
              text += ("\n" + obj.toString());
            }
          }
        }
      }
    }

    getToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(text),
        null);
  }

  /*
   * (non-Javadoc)
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  public void keyPressed(KeyEvent e) {
    if ((e.getModifiers() == InputEvent.CTRL_MASK)
        && ((e.getKeyCode() == KeyEvent.VK_C) || (e.getKeyCode() == KeyEvent.VK_INSERT))) {
      shortCut_Copy();
    }
  }

  /*
   * (non-Javadoc)
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped(KeyEvent e) {
  }

  /*
   * (non-Javadoc)
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(KeyEvent e) {
  }
}
