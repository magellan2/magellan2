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

package magellan.client.actions.extras;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.utils.ProfileManager;
import magellan.library.utils.Resources;

/**
 * Shows the profile selection dialog
 * 
 * @author stm
 */
public class ProfileAction extends MenuAction {

  /**
   * Creates a new ProfileAction object.
   * 
   * @param client
   */
  public ProfileAction(Client client) {
    super(client);
  }

  /**
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    if (ProfileManager.showProfileChooser(client)) {
      try {
        ProfileManager.saveSettings();
        JOptionPane.showMessageDialog(client, Resources.get("profileaction.afterrestart.message",
            ProfileManager.getCurrentProfile()));
      } catch (IOException e1) {
        JOptionPane.showMessageDialog(client, Resources.get("profileaction.ioerror.message",
            ProfileManager.getCurrentProfile()));
      }
    }
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.profileaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.profileaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.profileaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.profileaction.tooltip", false);
  }

}
