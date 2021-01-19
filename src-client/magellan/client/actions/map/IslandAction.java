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

import java.awt.Cursor;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.swing.ProgressBarUI;
import magellan.library.GameData;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Islands;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;

/**
 * This action will check the whole map and tries to find islands.
 * 
 * @author Andreas
 * @version 1.0
 */
public class IslandAction extends MenuAction {

  /**
   * Creates a new IslandAction object.
   * 
   * @param client
   */
  public IslandAction(Client client) {
    super(client);
  }

  /**
   * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {

    new Thread(new Runnable() {
      public void run() {
        GameData data = client.getData();
        client.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        data.setIslands(Islands.getIslands(new ProgressBarUI(client), data));
        client.getDispatcher().fire(new GameDataEvent(this, data));
        if (!PropertiesHelper.isShowIslands(client.getProperties())) {
          JOptionPane.showMessageDialog(client, Resources.get("actions.islandaction.display.message"), "",
              JOptionPane.INFORMATION_MESSAGE);
        }
        client.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    }).start();
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.islandaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.islandaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.islandaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.islandaction.tooltip", false);
  }

}
