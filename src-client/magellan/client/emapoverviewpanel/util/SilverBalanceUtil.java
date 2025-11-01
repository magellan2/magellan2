// class magellan.client.emapoverviewpanel.util.SilverBalanceUtil
// created on 18.12.2025
//
// Copyright 2003-2025 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.client.emapoverviewpanel.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import magellan.library.Faction;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemType;

/**
 * Utility class for calculating silver balance and financial data for regions and factions.
 *
 * <p>
 * This class provides methods to analyze the economic situation in a region by calculating
 * income sources (taxation, entertainment, trade), expenses (building maintenance, unit upkeep),
 * and the overall financial balance for individual factions and the entire region.
 * </p>
 *
 * @author $Author: $
 */
public final class SilverBalanceUtil {

  /**
   * Data container for financial information of a single faction within a region.
   *
   * <p>
   * This class holds all relevant financial data for one faction, including:
   * <ul>
   * <li>Silver owned by units</li>
   * <li>Potential income from entertainment, taxation, and trade</li>
   * <li>Expenses for building maintenance and unit upkeep</li>
   * <li>Net financial balance</li>
   * </ul>
   */
  public static final class RegionFactionFinances {
    /** Silver maintenance costs for buildings owned by this faction */
    public int costsOfBuildings;

    /** Total silver currently owned by all units of this faction in the region */
    public int totalUnitsSilver;

    /** Upkeep costs for all persons in units of this faction (typically 10 silver per person) */
    public int costsOfPersonal;

    /** Potential income from entertainment based on entertainment skill levels */
    public int potentialEntertainingIncome;

    /** Potential income from taxation based on taxation skill levels */
    public int potentialTaxationIncome;

    /** Potential income from trading luxury goods */
    public int totalTradePotential;

    /** Sum of all expenses (building costs + personal costs) */
    public int expensesSum;

    /** Sum of all potential income sources */
    public int incomeSum;

    /** Net financial balance (total silver + income - expenses) */
    public int netFinances;

    /** Map of luxury item IDs to their potential trade income */
    public Map<StringID, Integer> itemIds2Outcome;
  }

  /**
   * Data container for aggregated financial information of all factions in a region.
   *
   * <p>
   * This class provides a complete financial overview of a region by aggregating
   * data from all factions present. It includes both per-faction details and region-wide
   * totals that account for resource limitations (e.g., maximum entertainment income,
   * maximum trade volume).
   * </p>
   *
   * <p>
   * The overall calculations respect regional constraints:
   * <ul>
   * <li>Taxation income is limited by available silver in the region</li>
   * <li>Entertainment income is limited by the region's maximum entertainment capacity</li>
   * <li>Trade income is limited by the region's maximum luxury trade volume</li>
   * </ul>
   */
  public static final class RegionFinances {
    /** Financial data for each faction in the region, sorted by faction name */
    public TreeMap<Faction, RegionFactionFinances> factionFinances;

    /**
     * Effective trade potential for the entire region, accounting for the region's
     * maximum luxury trade limit and competition between factions
     */
    public int overallTradePotential;

    /**
     * Total taxation income for the region, limited by available silver.
     * This is the minimum of region silver and sum of all faction taxation potentials.
     */
    public int overallTaxationIncome;

    /**
     * Total entertainment income for the region, limited by maximum entertainment capacity.
     * This is the minimum of max entertainment and sum of all faction entertainment potentials.
     */
    public int overallEntertainmentIncome;

    /** Total silver currently owned by all units in the region across all factions */
    public int totalDistributedSilver;

    /** Total upkeep costs for all factions (buildings + personnel) */
    public int totalUpkeepCosts;

    /**
     * Overall financial balance for the region.
     * Calculated as: totalDistributedSilver + all incomes - totalUpkeepCosts
     */
    public int overallSum;
  }

  /**
   * Calculates complete financial data for all factions in a region.
   *
   * <p>
   * This method provides a comprehensive financial analysis of a region by:
   * <ol>
   * <li>Identifying all factions present in the region</li>
   * <li>Calculating detailed finances for each faction using {@link #calculateFactionFinances}</li>
   * <li>Computing region-wide aggregates that respect resource limitations</li>
   * <li>Determining the overall financial balance</li>
   * </ol>
   *
   * <p>
   * The method accounts for regional constraints such as maximum entertainment capacity,
   * available silver for taxation, and luxury trade limits. This ensures that the calculated
   * incomes reflect realistic values rather than theoretical maximums.
   * </p>
   *
   * @param region the region to analyze; must not be null
   * @return RegionFinances object containing all calculated financial data for the region
   * @see RegionFinances
   * @see #calculateFactionFinances(Faction, Region)
   * @see #getRegionsEffectiveTradePotential(Region, TreeMap)
   */
  public static RegionFinances calculateRegionFinances(Region region) {
    RegionFinances finances = new RegionFinances();

    // Collect all factions present in the region and calculate their individual finances
    // Factions are sorted by name for consistent ordering in the UI
    finances.factionFinances = new TreeMap<>((faction1, faction2) -> faction1.getName().compareTo(faction2.getName()));

    if (region.getUnits() != null) {
      for (Unit unit : region.getUnits().values()) {
        // Calculate finances for each faction only once, even if multiple units belong to it
        finances.factionFinances.put(
            unit.getFaction(),
            calculateFactionFinances(unit.getFaction(), region));
      }
    }

    // Calculate effective trade potential considering regional trade limits and competition
    finances.overallTradePotential = getRegionsEffectiveTradePotential(
        region,
        finances.factionFinances);

    // Calculate taxation income: limited by available silver in the region
    // Multiple factions may compete for the same silver pool
    finances.overallTaxationIncome = Math.min(
        region.getSilver(),
        finances.factionFinances.values().stream()
            .mapToInt(f -> f.potentialTaxationIncome)
            .sum());

    // Calculate entertainment income: limited by region's entertainment capacity
    // The region can only support a certain amount of entertainment income
    finances.overallEntertainmentIncome = Math.min(
        region.maxEntertain(),
        finances.factionFinances.values().stream()
            .mapToInt(f -> f.potentialEntertainingIncome)
            .sum());

    // Sum up silver currently distributed among all units
    finances.totalDistributedSilver = finances.factionFinances.values().stream()
        .mapToInt(f -> f.totalUnitsSilver)
        .sum();

    // Sum up all upkeep costs (buildings + personnel)
    finances.totalUpkeepCosts = finances.factionFinances.values().stream()
        .mapToInt(f -> f.expensesSum)
        .sum();

    // Calculate net balance: current silver + potential income - expenses
    finances.overallSum = finances.totalDistributedSilver
        + finances.overallTaxationIncome
        + finances.overallEntertainmentIncome
        + finances.overallTradePotential
        - finances.totalUpkeepCosts;

    return finances;
  }

  /**
   * Calculates detailed financial data for a specific faction within a region.
   *
   * <p>
   * This method analyzes all economic aspects of a faction's presence in a region:
   * <ul>
   * <li><b>Expenses:</b> Building maintenance costs and unit upkeep (10 silver per person)</li>
   * <li><b>Assets:</b> Silver currently owned by the faction's units</li>
   * <li><b>Income potential:</b> Entertainment, taxation, and luxury trade capabilities</li>
   * </ul>
   *
   * <p>
   * Note that the calculated potentials represent maximum possible income based on
   * skills and resources, but actual income may be limited by regional constraints
   * (calculated in {@link #calculateRegionFinances}).
   * </p>
   *
   * @param faction the faction to analyze; must not be null
   * @param region the region where the faction operates; must not be null
   * @return RegionFactionFinances object with all calculated financial data
   * @see RegionFactionFinances
   */
  public static RegionFactionFinances calculateFactionFinances(Faction faction, Region region) {
    RegionFactionFinances finances = new RegionFactionFinances();

    // Get silver item type for financial calculations
    final ItemType silverItemType = region.getData().getRules().getItemType(EresseaConstants.I_RSILVER);
    RegionResource silverResource = region.getResource(silverItemType);
    int silverAmount = silverResource != null ? silverResource.getAmount() : 0;

    // Calculate building maintenance costs for buildings owned by this faction
    // Only buildings where this faction's unit is the maintainer are counted
    finances.costsOfBuildings = region.buildings().stream()
        .filter(building -> {
          Unit maintainer = region.getData().getGameSpecificRules().getMaintainer(building);
          return maintainer != null && maintainer.getFaction().equals(
              faction);
        })
        .map(building -> {
          Optional<Item> silverCostsOfMaintenanceIfAny = building.getBuildingType().getMaintenanceItems()
              .stream().filter(item -> item.getItemType() == silverItemType).findFirst();
          return silverCostsOfMaintenanceIfAny.isPresent() ? silverCostsOfMaintenanceIfAny.get().getAmount() : 0;
        }).collect(Collectors.reducing(0, (x, y) -> x + y));

    // Collect all units belonging to this faction in the region
    Collection<Unit> factionUnits = region.getUnits() != null ? region.getUnits().values().stream()
        .filter(unit -> unit.getFaction().equals(faction)).collect(Collectors.toList()) : Collections.emptyList();

    // Sum up all silver owned by this faction's units
    finances.totalUnitsSilver = factionUnits.stream().map(unit -> {
      return unit.getItems().stream().filter(item -> item.getItemType() == silverItemType)
          .map(itemSilver -> itemSilver.getAmount()).findFirst().orElse(0);
    }).collect(Collectors.reducing(0, (x, y) -> x + y));

    // Calculate personnel upkeep costs (10 silver per person)
    finances.costsOfPersonal = factionUnits.stream().map(unit -> unit.getPersons() * 10)
        .collect(Collectors.reducing(0, (x, y) -> x + y));

    // Calculate entertainment income potential based on entertainment skill
    // Formula: persons * skill_level * 20 silver per skill point
    Function<? super Unit, Integer> unit2EntertainmentLevel = unit -> {
      Map<StringID, Skill> skillMap = unit.getSkillMap();
      Skill entertainmentSkill = skillMap == null ? null : skillMap.get(EresseaConstants.S_UNTERHALTUNG);
      return unit.getPersons() * (entertainmentSkill == null ? 0 : entertainmentSkill.getLevel());
    };
    int totalEntertainmentPotential = factionUnits.stream().map(unit2EntertainmentLevel)
        .collect(Collectors.reducing(0, (x, y) -> x + y * 20));

    // Limit entertainment income by region's maximum entertainment capacity
    finances.potentialEntertainingIncome = Math.min(region.maxEntertain(), totalEntertainmentPotential);

    // Calculate taxation income potential based on taxation skill
    // Formula: persons * skill_level * 20 silver per skill point
    Function<? super Unit, Integer> unit2TaxationLevel = unit -> {
      Map<StringID, Skill> skillMap = unit.getSkillMap();
      Skill taxationSkill = skillMap == null ? null : skillMap.get(EresseaConstants.S_STEUEREINTREIBEN);
      return unit.getPersons() * (taxationSkill == null ? 0 : taxationSkill.getLevel());
    };
    int totalTaxationPotential = factionUnits.stream().map(unit2TaxationLevel)
        .collect(Collectors.reducing(0, (x, y) -> x + y * 20));
    // Limit taxation income by available silver in the region
    finances.potentialTaxationIncome = Math.min(silverAmount, totalTaxationPotential);

    // Extract all luxury items owned by this faction's units for trade calculations
    Function<? super Unit, List<Item>> tradableItemsGetter = unit -> {
      Collection<Item> itemMap = unit.getItems();

      // Filtering for the category != null should not be necessary, but some items seem to have no category after
      // loading.
      List<Item> listOfLuxuries = itemMap == null ? Collections.emptyList() : itemMap.stream().filter(
          item -> item.getItemType() != null && item.getItemType().getCategory() != null && item.getItemType()
              .getCategory().getID().equals(EresseaConstants.C_LUXURIES)).collect(Collectors.toList());
      return listOfLuxuries;
    };

    // Group all luxury items by item type ID
    Map<StringID, List<Item>> tradableItemsMap = factionUnits.stream()
        .map(tradableItemsGetter).flatMap(list -> list.stream()).collect(Collectors.groupingBy(itm -> itm
            .getItemType().getID(), () -> new HashMap<StringID, List<Item>>(), Collectors.toList()));

    // Calculate potential trade income for each luxury item type
    Map<StringID, Integer> itemIds2Outcome = tradableItemsMap.entrySet().stream().collect(
        Collectors.<Map.Entry<StringID, List<Item>>, StringID, Integer> toMap(entry -> entry
            .getKey(), entry -> {
              StringID itemID = entry.getKey();
              List<Item> items = entry.getValue();
              final int maxLuxuries = region.maxLuxuries();
              final Map<StringID, LuxuryPrice> prices = region.getPrices();
              int sum = 0;
              for (Item item : items) {
                LuxuryPrice itemPrice = prices.get(itemID);
                if (itemPrice != null && itemPrice.getPrice() > 0) {
                  sum = Math.min(maxLuxuries * itemPrice.getPrice(), item.getAmount() * itemPrice.getPrice());
                }
              }
              return sum;
            }));

    finances.itemIds2Outcome = itemIds2Outcome;

    // Sum up all trade potentials across all luxury item types
    finances.totalTradePotential = itemIds2Outcome.values().stream().collect(Collectors.reducing(0, (x, y) -> x + y));

    // Calculate total expenses and income
    finances.expensesSum = finances.costsOfBuildings + finances.costsOfPersonal;
    finances.incomeSum = finances.potentialEntertainingIncome + finances.potentialTaxationIncome
        + finances.totalTradePotential;

    // Calculate net financial balance for this faction
    finances.netFinances = finances.totalUnitsSilver + finances.incomeSum - finances.expensesSum;

    return finances;
  }

  /**
   * Calculates the effective trade potential for a region considering all factions.
   *
   * <p>
   * This method determines how much silver can actually be earned from luxury trade
   * in the region, accounting for:
   * <ul>
   * <li>The region's maximum luxury trade volume (maxLuxuries)</li>
   * <li>Competition between factions for the same trade opportunities</li>
   * <li>Current luxury prices in the region</li>
   * <li>Available luxury items across all factions</li>
   * </ul>
   *
   * <p>
   * The calculation aggregates luxury items from all factions and then applies
   * the regional trade limit, ensuring that the total trade income doesn't exceed
   * what the region can support.
   * </p>
   *
   * @param r the region to analyze
   * @param factions map of factions to their financial data
   * @return the effective trade potential in silver, limited by regional constraints
   */
  private static int getRegionsEffectiveTradePotential(Region r, TreeMap<Faction, RegionFactionFinances> factions) {
    // Aggregate trade amounts from all factions by luxury item type
    Map<StringID, Integer> totalTradeAmounts = new HashMap<>();
    for (Faction faction : factions.keySet()) {
      Map<StringID, Integer> itemIds2Outcome = factions.get(faction).itemIds2Outcome;
      for (Entry<StringID, Integer> entry : itemIds2Outcome.entrySet()) {
        // Merge trade amounts for the same luxury type across factions
        totalTradeAmounts.merge(entry.getKey(), entry.getValue(), Integer::sum);
      }
    }

    // Calculate actual trade income respecting regional trade limits
    int overallTradePotential = 0;
    final Map<StringID, LuxuryPrice> prices = r.getPrices();
    final int maxLuxuries = r.maxLuxuries();

    for (Entry<StringID, Integer> entry : totalTradeAmounts.entrySet()) {
      LuxuryPrice price = prices.get(entry.getKey());
      if (price != null && price.getPrice() > 0) {
        // Trade income is limited by either the region's max trade volume or available items
        overallTradePotential += Math.min(maxLuxuries * price.getPrice(), entry.getValue() * price.getPrice());
      }
    }

    return overallTradePotential;
  }

}
