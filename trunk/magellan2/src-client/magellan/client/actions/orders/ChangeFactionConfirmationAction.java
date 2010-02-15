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

package magellan.client.actions.orders;

import java.util.Collection;
import java.util.Iterator;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.event.OrderConfirmEvent;
import magellan.library.Faction;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 * 
 * @author Ilja Pavkovic
 */
public class ChangeFactionConfirmationAction extends MenuAction {
  /** DOCUMENT-ME */
  public static final int SETCONFIRMATION = 0;

  /** DOCUMENT-ME */
  public static final int REMOVECONFIRMATION = 1;

  /** DOCUMENT-ME */
  public static final int INVERTCONFIRMATION = 2;
  private Faction faction;
  private int confirmation; // one of the values above, should be selfexplaining
  private boolean selectedRegionsOnly; // only change confirmation in selected regions
  private boolean spies; // only change confirmation of spies

  /**
   * Creates a new ChangeFactionConfirmationAction object. <code>conf</code> is currently one of
   * {@link ChangeFactionConfirmationAction#SETCONFIRMATION},
   * {@link ChangeFactionConfirmationAction#REMOVECONFIRMATION},
   * {@link ChangeFactionConfirmationAction#INVERTCONFIRMATION}.
   * 
   * @param client
   * @param f The faction for this action. <code>null</code> means all factions.
   * @param conf The type of action
   * @param r If <code>true</code>, only selected regions shall be affected.
   * @param spy If <code>true</code>, only spies will be affected.
   * @throws IllegalArgumentException DOCUMENT-ME
   */
  public ChangeFactionConfirmationAction(Client client, Faction f, int conf, boolean r, boolean spy) {
    super(client);

    faction = f;

    if ((conf < 0) || (conf > 2))
      throw new IllegalArgumentException();

    confirmation = conf;
    selectedRegionsOnly = r;
    spies = spy;

    StringBuffer sb = new StringBuffer(100);
    if (spies) {
      sb.append(Resources.get("actions.changefactionconfirmationaction.spies"));
    } else if (faction == null) {
      sb.append(Resources.get("actions.changefactionconfirmationaction.all"));
    } else {
      sb.append(faction.toString());
    }
    if (selectedRegionsOnly) {
      sb.append(" " + Resources.get("actions.changefactionconfirmationaction.postfix.selected"));
    }
    setName(sb.toString());
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public void menuActionPerformed(java.awt.event.ActionEvent e) {
    Collection<? extends Unit> units = null;

    if (faction == null) {
      if ((client.getData() != null) && (client.getData().getUnits() != null)) {
        units = client.getData().getUnits();
      }
    } else {
      units = faction.units();
    }

    if (units != null) {
      for (Unit unit : units) {
        if (spies == unit.isSpy()) {
          // this is slow but ok for this situation (normally one would iterate over the
          // regions and check the containment once per region)
          if (selectedRegionsOnly
              && !client.getSelectedRegions().containsKey(unit.getRegion().getID())) {
            continue;
          }

          changeConfirmation(unit);

          // (!) temp units are contained in Faction.units(),
          // but not in GameData.units() (!)
          for (Iterator<TempUnit> temps = unit.tempUnits().iterator(); temps.hasNext();) {
            Unit temp = temps.next();
            changeConfirmation(temp);
          }
        }
      }

      client.getDispatcher().fire(
          new OrderConfirmEvent(this, (faction == null) ? client.getData().getUnits() : faction
              .units()));
    }
  }

  private void changeConfirmation(Unit unit) {
    switch (confirmation) {
    case SETCONFIRMATION:
      unit.setOrdersConfirmed(true);

      break;

    case REMOVECONFIRMATION:
      unit.setOrdersConfirmed(false);

      break;

    case INVERTCONFIRMATION:
      unit.setOrdersConfirmed(!unit.isOrdersConfirmed());

      break;

    default:
      break;
    }
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.changefactionconfirmationaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.changefactionconfirmationaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.changefactionconfirmationaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.changefactionconfirmationaction.tooltip", false);
  }
}
