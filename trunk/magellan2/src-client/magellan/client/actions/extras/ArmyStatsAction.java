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
import magellan.client.actions.MenuAction;
import magellan.client.swing.ArmyStatsDialog;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class ArmyStatsAction extends MenuAction implements GameDataListener {

  /**
   * Creates a new ArmyStatsAction object.
   * 
   * @param client
   */
  public ArmyStatsAction(Client client) {
    super(client);
    setEnabled(false);
    client.getDispatcher().addGameDataListener(this);
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    new ArmyStatsDialog(client, client.getDispatcher(), client.getData(), client.getProperties())
        .setVisible(true);
  }

  public void gameDataChanged(GameDataEvent e) {
    int i = e.getGameData().regions().size();
    if (i > 0) {
      setEnabled(true);
    } else {
      setEnabled(false);
    }
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.armystatsaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.armystatsaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.armystatsaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.armystatsaction.tooltip", false);
  }
}
