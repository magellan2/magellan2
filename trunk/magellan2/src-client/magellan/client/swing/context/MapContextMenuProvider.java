package magellan.client.swing.context;

import javax.swing.JMenuItem;

import magellan.client.event.EventDispatcher;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;

/**
 * PlugIns should implement this interface if the provide a JMenu as contextMenu after right-click
 * on the Map
 * 
 * @author Fiete
 * @version 1.0, 05.07.2007
 */
public interface MapContextMenuProvider {

  /**
   * creates a JMenuItem. Will be called on right-clicking on map.
   */
  public JMenuItem createMapContextMenu(EventDispatcher dispatcher, GameData data);

  /**
   * MapContextMenu "inits" the compenents when object selected here we update the plugin
   */
  public void update(Region r);

  /**
   * Gives the ContextMenu the chance to react on clicks on non-region areas
   * 
   * @param c the coordinate provided by the mapper (Eressea Coordinates)
   */
  public void updateUnknownRegion(CoordinateID c);

}
