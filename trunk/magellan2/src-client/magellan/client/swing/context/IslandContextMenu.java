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
import java.util.Collection;
import java.util.Properties;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import magellan.client.event.EventDispatcher;
import magellan.library.GameData;
import magellan.library.Island;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Resources;

/**
 * A context menu for Islands.
 * 
 * @author stm
 */
public class IslandContextMenu extends JPopupMenu {

  private EventDispatcher dispatcher;
  private GameData data;
  // private Properties settings;
  private Collection<?> selectedObjects;

  private Island island;

  /**
   * Creates a new UnitContainerContextMenu object.
   */
  public IslandContextMenu(Island island, EventDispatcher dispatcher, GameData data,
      Properties settings, Collection<?> selectedObjects) {
    super(island.toString());
    this.island = island;
    this.dispatcher = dispatcher;
    this.data = data;
    // this.settings = settings;
    this.selectedObjects = selectedObjects;

    initMenu();
  }

  private void initMenu() {
    JMenuItem name = new JMenuItem(getCaption());
    name.setEnabled(false);
    add(name);

    JMenuItem removeIsland =
        new JMenuItem(Resources.get("context.islandcontextmenu.menu.removeIsland.caption"));
    removeIsland.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeIsland();
      }
    });
    add(removeIsland);

  }

  protected void removeIsland() {
    int result =
        JOptionPane.showConfirmDialog(this, Resources
            .get("context.islandcontextmenu.confirmation.message"), Resources
            .get("context.islandcontextmenu.confirmation.title"), JOptionPane.YES_NO_OPTION);
    if (result != 0)
      return;
    boolean changed = false;
    for (Object o : selectedObjects) {
      if (o instanceof Island) {
        changed = true;
        Island island = (Island) o;
        for (Region r : island.regions()) {
          r.setIsland(null);
        }
        data.removeIsland(island.getID());
      }
    }
    if (changed) {
      dispatcher.fire(new GameDataEvent(this, data));
    }
  }

  private String getCaption() {
    return island.toString();
  }

}
