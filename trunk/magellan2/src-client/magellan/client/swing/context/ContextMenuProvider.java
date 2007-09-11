package magellan.client.swing.context;

import java.util.Collection;

import javax.swing.JMenuItem;

import magellan.client.event.EventDispatcher;
import magellan.library.GameData;

/**
 * 
 * PlugIns should implement this interface if the provide
 * a JMenu as contextMenu after right-click on a Unit.
 *
 * @author Fiete
 * @version 1.0, 05.07.2007
 */
public interface ContextMenuProvider {
    
  /**
   * creates a JMenuItem. Will be called on right-clicking units.
   */
  public JMenuItem createContextMenu(EventDispatcher dispatcher, GameData data, Object argument, Collection selectedObjects);
}
