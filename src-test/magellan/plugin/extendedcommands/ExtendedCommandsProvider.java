/**
 * 
 */
package magellan.plugin.extendedcommands;

import magellan.client.Client;
import magellan.library.*;

/**
 * Helper class to get a Helper. Needed for testing only!
 * 
 * @author stm
 */
public class ExtendedCommandsProvider {

  /**
   * Returns a new helper object.
   * 
   * @param client
   * @param world
   * @param unit
   * @param container
   * @return a new helper object.
   */
  public static ExtendedCommandsHelper createHelper(Client client, GameData world, Unit unit,
      UnitContainer container) {
    return new ExtendedCommandsHelper(client, world, unit, container);
  }

}
