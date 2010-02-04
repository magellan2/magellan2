package magellan.client.swing.context;

import java.util.Collection;

import javax.swing.JMenuItem;

import magellan.client.event.EventDispatcher;
import magellan.library.GameData;
import magellan.library.UnitContainer;

/**
 * PlugIns should implement this interface if the provide a JMenu as contextMenu after right-click
 * on a Unit.
 * 
 * @author Fiete
 */
public interface UnitContainerContextMenuProvider {

  /**
   * Creates a JMenuItem. Will be called on right-clicking unit containers.
   * 
   * @param dispatcher EventDispatcher
   * @param data the actual GameData or World
   * @param unitContainer last selected unit - is not required to be in selected objects
   * @param selectedObjects null or Collection of selected objects which may be of different types
   */
  public JMenuItem createContextMenu(EventDispatcher dispatcher, GameData data,
      UnitContainer unitContainer, Collection<?> selectedObjects);
}
