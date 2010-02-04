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

package magellan.client.actions.file;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.library.utils.Resources;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class AbortAction extends MenuAction {

  /**
   * Creates a new AbortAction object.
   * 
   * @param client
   */
  public AbortAction(Client client) {
    super(client);
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public void menuActionPerformed(java.awt.event.ActionEvent e) {
    client.quit(false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.abortaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.abortaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.abortaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.abortaction.tooltip", false);
  }

}
