/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.actions.extras;

import java.awt.event.ActionEvent;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.swing.AlchemyDialog;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;

/**
 * Just a little class to invoke the alchemy organizer
 * 
 * @author Ulrich Küster, stm
 */
public class AlchemyAction extends MenuAction implements GameDataListener {

  /**
   * Creates a new AlchemyAction object.
   * 
   * @param client
   */
  public AlchemyAction(Client client) {
    super(client);
    setEnabled(false);
    client.getDispatcher().addGameDataListener(this);
  }

  /**
   * Calls the dialog.
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    new AlchemyDialog(null, client.getDispatcher(), client.getData(), client.getProperties(),
        client.getSelectedRegions().values()).setVisible(true);
  }

  /**
   * Enables/disables the action.
   * 
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  public void gameDataChanged(GameDataEvent e) {
    int i = e.getGameData().getRegions().size();
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
    return Resources.get("actions.alchemydialog.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.alchemydialog.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.alchemydialog.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.alchemydialog.tooltip", false);
  }
}
