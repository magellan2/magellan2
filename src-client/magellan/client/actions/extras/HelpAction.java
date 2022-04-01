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

import magellan.client.Client;
import magellan.client.Help;
import magellan.client.actions.MenuAction;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class HelpAction extends MenuAction {
  private static final Logger log = Logger.getInstance(HelpAction.class);
  private Help help;
  private Client client;

  /**
   * Creates a new HelpAction object.
   */
  public HelpAction(Client client) {
    super(client);
    this.client = client;
  }

  /** */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    // SG: had a lot of fun when I implemented this :-)
    try {
      if (help == null) {
        help = Help.getInstance(client.getProperties());
      }

      help.show();

    } catch (Exception ex) {
      HelpAction.log.error(ex);
    }
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.helpaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.helpaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.helpaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.helpaction.tooltip", false);
  }

}
