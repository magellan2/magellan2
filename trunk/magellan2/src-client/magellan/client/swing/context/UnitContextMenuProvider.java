package magellan.client.swing.context;

import java.util.Collection;

import javax.swing.JMenuItem;

import magellan.client.event.EventDispatcher;
import magellan.library.GameData;
import magellan.library.Unit;

/**
 * 
 * PlugIns should implement this interface if the provide
 * a JMenu as contextMenu after right-click on a Unit.
 *
 * @author Fiete
 * @version 1.0, 05.07.2007
 */
public interface UnitContextMenuProvider {
    
  /**
   * Creates a JMenuItem. Will be called on right-clicking units.
   * 
   * @param dispatcher      EventDispatcher
   * @param data            the actual GameData or World
   * @param unit            last selected unit - is not required to be in selected objects
   * @param selectedObjects null or Collection of selected objects which may be of different types
   * @return
   */
  public JMenuItem createContextMenu(EventDispatcher dispatcher, GameData data, Unit unit, Collection selectedObjects);
}
