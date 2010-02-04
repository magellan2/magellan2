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

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.swing.MapperPanel;
import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class TileSetAction extends MenuAction {
  private MapperPanel map;

  /**
   * Creates a new TileSetAction object.
   */
  public TileSetAction(Client client, MapperPanel m) {
    super(client);
    map = m;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public void menuActionPerformed(java.awt.event.ActionEvent e) {
    map.reloadGraphicSet();
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.tilesetaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.tilesetaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.tilesetaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.tilesetaction.tooltip", false);
  }
}
