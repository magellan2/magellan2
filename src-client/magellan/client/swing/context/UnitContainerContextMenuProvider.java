package magellan.client.swing.context;

import javax.swing.JMenuItem;

import magellan.client.event.EventDispatcher;
import magellan.library.GameData;
import magellan.library.UnitContainer;

/**
 * 
 * PlugIns should implement this interface if the provide
 * a JMenu as contextMenu after right-click on a Unit.
 *
 * @author Fiete
 * @version 1.0, 05.07.2007
 */
public interface UnitContainerContextMenuProvider {
    
  /**
   * creates a JMenuItem. Will be called on right-clicking unit containers.
   */
  public JMenuItem createContextMenu(EventDispatcher dispatcher, GameData data, UnitContainer container);
}
