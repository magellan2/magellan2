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

import java.util.Collections;
import java.util.Properties;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.swing.tree.FactionNodeWrapper;
import magellan.client.swing.tree.IslandNodeWrapper;
import magellan.client.swing.tree.RegionNodeWrapper;
import magellan.client.swing.tree.UnitContainerNodeWrapper;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Island;
import magellan.library.Region;
import magellan.library.UnitContainer;

/**
 * Context Factory for unit-container contexts.
 * 
 * @author Andreas
 * @version 1.0rn
 */
public class UnitContainerContextFactory implements ContextFactory {
  protected Properties settings;

  /**
   * Creates a new UnitContainerContextFactory object.
   */
  public UnitContainerContextFactory(Properties settings) {
    this.settings = settings;
  }

  /**
   * Creates a context menu based on the type of argument.
   * 
   * @see magellan.client.swing.context.ContextFactory#createContextMenu(EventDispatcher, GameData,
   *      Object, SelectionEvent, DefaultMutableTreeNode)
   */
  public JPopupMenu createContextMenu(EventDispatcher dispatcher, GameData data, Object argument,
      SelectionEvent selectedObjects, DefaultMutableTreeNode node) {
    if (argument instanceof UnitContainer)
      return new UnitContainerContextMenu((UnitContainer) argument, dispatcher, data, settings,
          selectedObjects == null ? Collections.singletonList(argument) : selectedObjects
              .getSelectedObjects());
    else if (argument instanceof RegionNodeWrapper) {
      Region realArgument = ((RegionNodeWrapper) argument).getRegion();
      return new UnitContainerContextMenu(realArgument, dispatcher, data, settings,
          selectedObjects == null ? Collections.singletonList(realArgument) : selectedObjects
              .getSelectedObjects());
    } else if (argument instanceof FactionNodeWrapper) {
      Faction realArgument = ((FactionNodeWrapper) argument).getFaction();
      return new UnitContainerContextMenu(realArgument, dispatcher, data, settings,
          selectedObjects == null ? Collections.singletonList(realArgument) : selectedObjects
              .getSelectedObjects());
    } else if (argument instanceof UnitContainerNodeWrapper) {
      UnitContainer realArgument = ((UnitContainerNodeWrapper) argument).getUnitContainer();
      return new UnitContainerContextMenu(realArgument, dispatcher, data, settings,
          selectedObjects == null ? Collections.singletonList(realArgument) : selectedObjects
              .getSelectedObjects());
    } else if (argument instanceof IslandNodeWrapper) {
      Island realArgument = ((IslandNodeWrapper) argument).getIsland();
      return new IslandContextMenu(realArgument, dispatcher, data, settings,
          selectedObjects == null ? Collections.singletonList(realArgument) : selectedObjects
              .getSelectedObjects());
    }
    return null;
  }
}
