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

package magellan.client.actions.map;

import java.awt.event.ActionEvent;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.swing.SetGirthDialog;
import magellan.library.utils.Resources;

/**
 * Changes the maximum x- and y- dimensions of the map.
 * 
 * @author stm
 */
public class SetGirthAction extends MenuAction {

  /**
   * Creates a new SetGirthAction object.
   * 
   * @param client
   */
  public SetGirthAction(Client client) {
    super(client);
  }

  /**
   * Opens the SetGirthDialog, waits for user input if approved, then the map is changed.
   * 
   * @param e
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    SetGirthDialog dialog = new SetGirthDialog(client, client.getDispatcher(), client.getData());
    dialog.setVisible(true);
    if (dialog.approved()) {
      client.setGirth(dialog.getNewBorders());
    }
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.setgirthaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.setgirthaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.setgirthaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.setgirthaction.tooltip", false);
  }
}
