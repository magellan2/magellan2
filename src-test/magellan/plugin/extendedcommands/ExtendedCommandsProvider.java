/**
 *
 */
package magellan.plugin.extendedcommands;

import magellan.client.Client;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;

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
    return new ExtendedCommandsHelper(null, world, unit, container);
  }

}
