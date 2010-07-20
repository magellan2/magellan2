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

package magellan.client.actions.edit;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import magellan.client.Client;
import magellan.client.MagellanUndoManager;
import magellan.client.actions.MenuAction;
import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class UndoAction extends MenuAction implements PropertyChangeListener {
  private MagellanUndoManager undoManager;
  private String name = null;

  /**
   * Creates a new UndoAction object.
   */
  public UndoAction(Client client, MagellanUndoManager m) {
    super(client);
    name = getName();
    undoManager = m;
    setEnabled(undoManager.canUndo());

    if (isEnabled()) {
      putValue(Action.NAME, name + ": " + undoManager.getUndoPresentationName());
    }

    undoManager.addPropertyChangeListener(MagellanUndoManager.UNDO, this);
  }

  /**
   * @see magellan.client.actions.MenuAction#getIconName()
   */
  @Override
  public String getIconName() {
    return "undo_edit";
  }

  /**
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    if (undoManager.canUndo()) {
      undoManager.undo();
    }
  }

  /**
   * Updates the menu item.
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent p1) {
    boolean enabled = (Boolean) p1.getNewValue();

    if (enabled) {
      putValue(Action.NAME, name + ": " + undoManager.getUndoPresentationName());
    } else {
      putValue(Action.NAME, name);
    }
    setEnabled(enabled);

  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.undoaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.undoaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.undoaction.name");
  }

  /**
   * @see magellan.client.actions.MenuAction#getTooltipTranslated()
   */
  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.undoaction.tooltip", false);
  }
}
