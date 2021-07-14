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
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.utils.Resources;
import magellan.library.utils.transformation.BoxTransformer.BBox;

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
    GameData data = client.getData();
    BBox box = data.getGameSpecificStuff().getMapMetric().createBBox();
    boolean alternative = false;

    for (Region wrapper : data.wrappers().values()) {
      if (wrapper.getCoordinate().getZ() == 0) {
        Region original = client.getData().getOriginal(wrapper);
        if (original != null) {
          if (original.getCoordY() == wrapper.getCoordY()) {
            int min;
            int max;
            if (wrapper.getCoordX() < original.getCoordX()) {
              min = wrapper.getCoordX() + 1 + original.getCoordY() / 2;
              max = original.getCoordX() + original.getCoordY() / 2;
            } else {
              max = wrapper.getCoordX() - 1 + original.getCoordY() / 2;
              min = original.getCoordX() + original.getCoordY() / 2;
            }
            if (box.getMaxx() != Integer.MIN_VALUE && (box.getMaxx() != max || box.getMinx() != min)) {
              if (box.getMaxx() - box.getMinx() != max - min) {
                // error
                box = data.getGameSpecificStuff().getMapMetric().createBBox();
                break;
              }
              alternative = true;
            }
            if (alternative) {
              box.setX((max - min + 1) / 2 - (max - min), (max - min + 1) / 2);
            } else {
              box.setX(min, max);
            }

          }
          if (original.getCoordX() == wrapper.getCoordX()) {
            if (wrapper.getCoordY() < original.getCoordY()) {
              box.setY(wrapper.getCoordY() + 1, original.getCoordY());
            } else {
              box.setY(original.getCoordY(), wrapper.getCoordY() - 1);
            }
          }
        }
      }
    }

    SetGirthDialog dialog =
        new SetGirthDialog(client, box, null, data.getGameSpecificStuff().getMapMetric());
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
