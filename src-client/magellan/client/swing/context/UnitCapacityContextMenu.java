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

package magellan.client.swing.context;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import magellan.client.EMapDetailsPanel;
import magellan.client.EMapDetailsPanel.ShowItems;
import magellan.client.event.EventDispatcher;
import magellan.library.GameData;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Resources;

/**
 * Context menu for the capacity nodes (in EmapDetailsPanel)
 * 
 * @author Fiete
 * @version $Revision: 313 $
 */
public class UnitCapacityContextMenu extends JPopupMenu {

  private GameData data;
  private EventDispatcher dispatcher;

  private EMapDetailsPanel details;

  /**
   * Creates new UnitCapacityContextMenu
   * 
   * @param details
   * @param dispatcher EventDispatcher
   * @param data the actual GameData or World
   */
  public UnitCapacityContextMenu(EMapDetailsPanel details, EventDispatcher dispatcher,
      GameData data, Properties settings) {
    super(":-)");

    this.details = details;
    this.data = data;
    this.dispatcher = dispatcher;

    init();
  }

  private void init() {

    // new: all Items in CR/rules.cr
    JMenuItem toogleAllItems = null;
    toogleAllItems =
        new JMenuItem(Resources
            .get("context.unitcapacitycontextmenu.menu.toggleShowAllItems.caption"));
    toogleAllItems.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        details.setShowCapacityItems(ShowItems.SHOW_ALL);
        GameDataEvent newE = new GameDataEvent(this, data, false);
        dispatcher.fire(newE);
      }
    });
    add(toogleAllItems);

    // new: all Items in region, regardless which faction (= some)
    JMenuItem toogleSomeItems = null;
    toogleSomeItems =
        new JMenuItem(Resources
            .get("context.unitcapacitycontextmenu.menu.toggleShowSomeItems.caption"));
    toogleSomeItems.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        details.setShowCapacityItems(ShowItems.SHOW_ALL_FACTIONS);
        GameDataEvent newE = new GameDataEvent(this, data, false);
        dispatcher.fire(newE);
      }
    });
    add(toogleSomeItems);

    // old = normal behaviour
    JMenuItem toogleFriendlyItems = null;
    toogleFriendlyItems =
        new JMenuItem(Resources
            .get("context.unitcapacitycontextmenu.menu.toggleShowFriendlyItems.caption"));
    toogleFriendlyItems.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        details.setShowCapacityItems(ShowItems.SHOW_PRIVILEGED_FACTIONS);
        GameDataEvent newE = new GameDataEvent(this, data, false);
        dispatcher.fire(newE);
      }
    });
    add(toogleFriendlyItems);

    // new: all Items in region, only own faction
    JMenuItem toogleMyItems = null;
    toogleMyItems =
        new JMenuItem(Resources
            .get("context.unitcapacitycontextmenu.menu.toggleShowMyItems.caption"));
    toogleMyItems.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        details.setShowCapacityItems(ShowItems.SHOW_MY_FACTION);
        GameDataEvent newE = new GameDataEvent(this, data, false);
        dispatcher.fire(newE);
      }
    });
    add(toogleMyItems);

  }
}
