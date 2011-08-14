package magellan.library.utils.replacers;

import java.util.Map;

import magellan.library.LuxuryPrice;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.rules.ItemType;
import magellan.library.utils.Resources;

/**
 * Shows the type of trade in a region
 * 
 * @author Thoralf Rickert-Wendt
 * @version 1.0
 */
public class TradeReplacer extends AbstractRegionReplacer {

  /**
   * 
   * @see magellan.library.utils.replacers.AbstractRegionReplacer#getRegionReplacement(magellan.library.Region)
   */
  @Override
  public Object getRegionReplacement(Region region) {
    Map<StringID, LuxuryPrice> prices = region.getPrices();

    ItemType thisType = null;

    for (LuxuryPrice price : prices.values()) {
      if (price.getPrice() < 0) {
        thisType = price.getItemType();
      }
    }


    if (thisType != null) return thisType.getName();

    return "-?-";
  }

  public String getDescription() {
    return Resources.get("util.replacers.tradereplacer.description");
  }

}
