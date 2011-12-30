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

package magellan.client.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import magellan.client.swing.GiveOrderDialog;
import magellan.client.swing.RemoveOrderDialog;
import magellan.client.swing.context.ContextFactory;
import magellan.client.swing.tree.CellObject;
import magellan.client.swing.tree.DefaultNodeWrapper;
import magellan.client.swing.tree.ItemCategoryNodeWrapper;
import magellan.client.swing.tree.ItemNodeWrapper;
import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.client.swing.tree.UnitNodeWrapper;
import magellan.client.swing.tree.UnitRelationNodeWrapper;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Order;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.ZeroUnit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.RecruitmentRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * A class providing various utility functions regarding units.
 */
public class Units {
  private static final Logger log = Logger.getInstance(Units.class);
  private Rules rules = null;

  // items without category
  private StatItemContainer catLessContainer = null;
  /**
   * A Map&lt;ItemCategory, StatItemContainer&gt; mapping the item categories to containers with
   * items of the corresponding category.
   */
  private final Map<ItemCategory, StatItemContainer> itemCategoriesMap =
    new Hashtable<ItemCategory, StatItemContainer>();

  private static ItemType silberbeutel = new ItemType(StringID.create("Silberbeutel"));
  private static ItemType silberkassette = new ItemType(StringID.create("Silberkassette"));

  /**
   * Creates a new Units object.
   */
  public Units(Rules rules) {
    setRules(rules);
  }

  /**
   * Changes the game rules.
   */
  public void setRules(Rules rules) {
    this.rules = rules;

    if (rules != null) {
      initItemCategories();
    }
  }

  /**
   * Calculates the amounts of all items of all units and records the amount in the
   * itemCategoriesMap.
   * 
   * @param units All items of all units in this Collection are accounted for.
   * @return The sorted list of categories.
   */
  public Collection<StatItemContainer> categorizeUnitItems(Collection<Unit> units) {
    if ((itemCategoriesMap == null) || (itemCategoriesMap.size() == 0)) {
      Units.log.warn("categorizeUnitItems(): the category map is not initialized!");

      return null;
    }

    clearItemContainers();

    // iterate over all units...
    for (Unit u : units) {
      for (Item item : u.getModifiedItems()) {
        // get the container this item is stored in
        final Map<ID, StatItem> container = getItemContainer(item.getItemType());

        // get the stat item from the category container
        StatItem stored = container.get(item.getItemType().getID());

        if (stored == null) {
          stored = new StatItem(item.getItemType(), 0);
          container.put(stored.getItemType().getID(), stored);
        }

        // add up the amount in the stat item
        // multiply amount with unit.persons if item is
        // silver
        int amount = item.getAmount();

        if (item.getItemType().equals(Units.silberbeutel)
            || item.getItemType().equals(Units.silberkassette)) {
          amount *= u.getPersons();
        }

        stored.setAmount(stored.getAmount() + amount);

        // try to remember unmodifiedAmount
        final Item unmodifiedItem = u.getItem(item.getItemType());
        int unmodifiedAmount = 0;
        if (unmodifiedItem != null) {
          unmodifiedAmount = unmodifiedItem.getAmount();
        }
        stored.setUnmodifiedAmount(stored.getUnmodifiedAmount() + unmodifiedAmount);

        final UnitWrapper uW = new UnitWrapper(u, amount);
        uW.setUnmodifiedAmount(unmodifiedAmount);
        // add the unit owning the item to the stat item
        stored.units.add(uW);
      }
    }

    final List<StatItemContainer> sortedCategories =
      new LinkedList<StatItemContainer>(itemCategoriesMap.values());
    Collections.sort(sortedCategories);

    return sortedCategories;
  }

  /**
   * This method takes all items carried by units in the units Collection and sorts them by their
   * category. Then it adds the non-empty categories to the specified parent node and puts the
   * corresponding items in each category node. Optionally, the units carrying an item are listed as
   * child nodes of the respective item nodes.
   * 
   * @param units a collection of Unit objects carrying items.
   * @param parentNode a tree node to add the new nodes to.
   * @param itemComparator a comparator to sort the items with. If itemComparator is null the items
   *          are sorted by name.
   * @param unitComparator a comparator to sort the units with. If unitComparator is null the units
   *          are sorted by the amount of the item carried underneath which they appear.
   * @param showUnits if true each item node gets child nodes containing the unit(s) carrying that
   *          item.
   * @return a collection of DefaultMutableTreeNode objects with user objects of class ItemCategory
   *         or null if the categorization of the items failed.
   */
  public Collection<TreeNode> addCategorizedUnitItems(Collection<Unit> units,
      DefaultMutableTreeNode parentNode, Comparator<Item> itemComparator,
      Comparator<Unit> unitComparator, boolean showUnits, NodeWrapperFactory factory,
      ContextFactory reserveContextFactory) {
    return addCategorizedUnitItems(units, parentNode, itemComparator, unitComparator, showUnits,
        factory, reserveContextFactory, true);
  }

  /**
   * This method takes all items carried by units in the units Collection and sorts them by their
   * category. Then it adds the non-empty categories to the specified parent node and puts the
   * corresponding items in each category node. Optionally, the units carrying an item are listed as
   * child nodes of the respective item nodes.
   * 
   * @param units a collection of Unit objects carrying items.
   * @param parentNode a tree node to add the new nodes to.
   * @param itemComparator a comparator to sort the items with. If itemComparator is null the items
   *          are sorted by name.
   * @param unitComparator a comparator to sort the units with. If unitComparator is null the units
   *          are sorted by the amount of the item carried underneath which they appear.
   * @param showUnits if true each item node gets child nodes containing the unit(s) carrying that
   *          item.
   * @return a collection of DefaultMutableTreeNode objects with user objects of class ItemCategory
   *         or null if the categorization of the items failed.
   */
  public Collection<TreeNode> addCategorizedUnitItems(Collection<Unit> units,
      DefaultMutableTreeNode parentNode, Comparator<Item> itemComparator,
      Comparator<Unit> unitComparator, boolean showUnits, NodeWrapperFactory factory,
      ContextFactory reserveContextFactory, boolean createCategoryNodes) {
    DefaultMutableTreeNode categoryNode = null;
    final Collection<TreeNode> categoryNodes = new LinkedList<TreeNode>();

    final Collection<StatItemContainer> listOfCategorizedItems = categorizeUnitItems(units);

    if (listOfCategorizedItems == null) {
      Units.log.warn("addCategorizedUnitItems(): categorizing unit items failed!");

      return null;
    }

    for (StatItemContainer currentCategoryMap : listOfCategorizedItems) {
      if (currentCategoryMap.size() > 0) {

        ItemCategoryNodeWrapper wrapper = null;
        if (createCategoryNodes) {
          final String catIconName =
            magellan.library.utils.Umlaut.convertUmlauts(currentCategoryMap.getCategory()
                .getName());
          final String nodeName = Resources.get("util.units." + catIconName);
          wrapper = // TODO use factory
            new ItemCategoryNodeWrapper(currentCategoryMap.getCategory(), -1, nodeName);
          wrapper.setIcons(catIconName);
          categoryNode = new DefaultMutableTreeNode(wrapper);
          parentNode.add(categoryNode);
          categoryNodes.add(categoryNode);
        } else {
          categoryNode = parentNode;
        }

        final List<StatItem> sortedItems = new LinkedList<StatItem>(currentCategoryMap.values());

        if (itemComparator != null) {
          Collections.sort(sortedItems, itemComparator);
        } else {
          Collections.sort(sortedItems);
        }

        int catNumber = 0;
        int unmodifiedCatNumber = 0;

        Unit u = null;
        if (units.size() == 1) {
          u = units.iterator().next();
        }
        for (StatItem currentItem : sortedItems) {
          addItemNode(currentItem, categoryNode, u, units, unitComparator, showUnits, factory,
              reserveContextFactory);
          catNumber += currentItem.getAmount();
          unmodifiedCatNumber += currentItem.getUnmodifiedAmount();
        }

        if (wrapper != null && catNumber > 0
            && !currentCategoryMap.category.equals(rules.getItemCategory(StringID.create("misc")))) {
          wrapper.setAmount(catNumber);
        }
        if (wrapper != null && unmodifiedCatNumber > 0
            && !currentCategoryMap.category.equals(rules.getItemCategory(StringID.create("misc")))) {
          // wrapper.setAmount(catNumber);
          wrapper.setUnmodifiedAmount(unmodifiedCatNumber);
        }
      }
    }

    return categoryNodes;
  }

  public int addItemNode(ItemType item, DefaultMutableTreeNode categoryNode, Unit u,
      Collection<Unit> units, Comparator<Unit> unitComparator, boolean showUnits,
      NodeWrapperFactory factory, ContextFactory reserveContextFactory) {
    categorizeUnitItems(units);
    for (final StatItemContainer itemContainer : itemCategoriesMap.values()) {
      if (itemContainer.get(item.getID()) != null) {
        addItemNode(itemContainer.get(item.getID()), categoryNode, u, units, unitComparator,
            showUnits, factory, reserveContextFactory);
      }
    }

    return 0;
  }

  /**
   * @param currentItem
   * @param reserveContextFactory
   */
  private void addItemNode(StatItem currentItem, DefaultMutableTreeNode categoryNode, Unit u,
      Collection<Unit> units, Comparator<Unit> unitComparator, boolean showUnits,
      NodeWrapperFactory factory, ContextFactory reserveContextFactory) {

    final ItemNodeWrapper itemNodeWrapper =
      factory.createItemNodeWrapper(u, currentItem, currentItem.getUnmodifiedAmount());
    final DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(itemNodeWrapper);

    categoryNode.add(itemNode);

    if (!showUnits && units.size() == 1) {
      boolean addItemNode = false;

      for (final ReserveRelation rrel : u.getItemReserveRelations(currentItem.getItemType())) {
        final StringBuilder text = new StringBuilder().append(rrel.amount).append(" ");
        final List<String> icons = new LinkedList<String>();
        text.append(Resources.get("util.units.node.reserved"));
        if (rrel.problem != null) {
          itemNodeWrapper.setWarningLevel(CellObject.L_WARNING);
          // text.append("(!!!) ");
          icons.add("warnung");
          // reserveNodeWrapper.setAdditionalIcon("warnung");
        }
        icons.add("reserve");

        UnitRelationNodeWrapper reserveNodeWrapper = // factory.createSimpleNodeWrapper(rrel,
          // text.toString(), icons);
          factory.createRelationNodeWrapper(u, rrel, factory.createSimpleNodeWrapper(text
              .toString(), icons));
        itemNode.add(new DefaultMutableTreeNode(reserveNodeWrapper));

        addItemNode = true;
      }

      for (final ItemTransferRelation currentRelation : u.getItemTransferRelations(currentItem
          .getItemType())) {
        final StringBuilder prefix = new StringBuilder().append(currentRelation.amount).append(" ");

        String addIcon = null;
        Unit u2 = null;

        if (currentRelation.source == u) {
          if (currentRelation.target == u) {
            addIcon = "getgive";
          } else if (currentRelation.origin == currentRelation.source) {
            addIcon = "give";
          } else {
            addIcon = "givetrans";
          }
          u2 = currentRelation.target;
        } else if (currentRelation.target == u) {
          if (currentRelation.origin == currentRelation.source) {
            addIcon = "get";
          } else {
            addIcon = "gettrans";
          }
          u2 = currentRelation.source;
        }

        if (u2 != null) {
          DefaultNodeWrapper relationWrapper;
          if (u2 instanceof ZeroUnit) {
            relationWrapper =
              factory.createRegionNodeWrapper(u2.getRegion(), currentRelation.amount);
          } else {
            UnitNodeWrapper giveNodeWrapper =
              factory.createUnitNodeWrapper(u2, prefix.toString(), u2.getPersons(), u2
                  .getModifiedPersons());
            giveNodeWrapper.setReverseOrder(true);

            if (currentRelation.problem != null) {
              itemNodeWrapper.setWarningLevel(CellObject.L_WARNING);
              // prefix.append("(!!!) ");
              giveNodeWrapper.addAdditionalIcon("warnung");
              // relationWrapper.setAdditionalIcon("warnung");
            }
            giveNodeWrapper.addAdditionalIcon(addIcon);
            relationWrapper =
              factory.createRelationNodeWrapper(u2, currentRelation, giveNodeWrapper);
          }

          itemNode.add(new DefaultMutableTreeNode(relationWrapper));

          addItemNode = true;
        }
      }
      if (currentItem.getItemType().getID().equals(EresseaConstants.I_USILVER)) {
        for (final RecruitmentRelation rrel : u.getRelations(RecruitmentRelation.class)) {
          final StringBuilder text = new StringBuilder().append(rrel.costs).append(" ");
          final List<String> icons = new LinkedList<String>();
          text.append(Resources.get("util.units.node.recruit")).append(" ").append(rrel.amount)
          .append(" ").append(rrel.race);

          icons.add("rekruten");
          if (rrel.problem != null) {
            itemNodeWrapper.setWarningLevel(CellObject.L_WARNING);
            // text.append("(!!!) ");
            icons.add("warnung");
            // recruitNodeWrapper.setAdditionalIcon("warnung");
          }

          final UnitRelationNodeWrapper recruitNodeWrapper =
            factory.createRelationNodeWrapper(u, rrel, factory.createSimpleNodeWrapper(text
                .toString(), icons));
          itemNode.add(new DefaultMutableTreeNode(recruitNodeWrapper));

          addItemNode = true;
        }
      }

      if (addItemNode) {
        // FIXME: we return different objects here!!
        categoryNode.add(itemNode);
      }
    }

    if (showUnits && (currentItem.units != null)) {
      Collections.sort(currentItem.units, new UnitWrapperComparator(unitComparator));

      for (UnitWrapper uw : currentItem.units) {
        itemNode.add(new DefaultMutableTreeNode(factory.createUnitNodeWrapper(uw.getUnit(), uw
            .getAmount())));
      }
    }
    // return currentItem.getAmount();
    // return currentItem;

  }

  private static class StatItem extends Item implements Comparable<StatItem> {
    /** DOCUMENT-ME */
    public List<UnitWrapper> units = new LinkedList<UnitWrapper>();

    private int unmodifiedAmount = 0;

    /**
     * Creates a new StatItem object.
     */
    public StatItem(ItemType type, int amount) {
      super(type, amount);
    }

    /**
     * DOCUMENT-ME
     */
    public int compareTo(StatItem o) {
      return getItemType().getName().compareTo((o).getItemType().getName());
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof StatItem)
        return compareTo((StatItem) obj) != 0;
      else
        return false;
    }

    @Override
    public int hashCode() {
      return getItemType().getName().hashCode();
    }

    /**
     * Returns the value of unmodifiedAmount.
     * 
     * @return Returns unmodifiedAmount.
     */
    public int getUnmodifiedAmount() {
      return unmodifiedAmount;
    }

    /**
     * Sets the value of unmodifiedAmount.
     * 
     * @param unmodifiedAmount The value for unmodifiedAmount.
     */
    public void setUnmodifiedAmount(int unmodifiedAmount) {
      this.unmodifiedAmount = unmodifiedAmount;
    }
  }

  private static class UnitWrapper {
    private Unit unit = null;
    private int number = -1;
    private int unmodifiedNumber = -1;

    /**
     * Creates a new UnitWrapper object.
     */
    @SuppressWarnings("unused")
    public UnitWrapper(Unit u) {
      this(u, -1);
    }

    /**
     * Creates a new UnitWrapper object.
     */
    public UnitWrapper(Unit u, int num) {
      unit = u;
      number = num;
    }

    /**
     * DOCUMENT-ME
     */
    public Unit getUnit() {
      return unit;
    }

    /**
     * DOCUMENT-ME
     */
    public int getAmount() {
      return number;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      if (number > -1) {
        if (unmodifiedNumber > -1 && unmodifiedNumber != number)
          return unit.toString() + ": " + unmodifiedNumber + " (" + number + ")";
        else
          return unit.toString() + ": " + number;
      }

      return unit.toString();
    }

    /**
     * Returns the value of unmodifiedNumber.
     * 
     * @return Returns unmodifiedNumber.
     */
    public int getUnmodifiedAmount() {
      return unmodifiedNumber;
    }

    /**
     * Sets the value of unmodifiedNumber.
     * 
     * @param unmodifiedNumber The value for unmodifiedNumber.
     */
    public void setUnmodifiedAmount(int unmodifiedNumber) {
      this.unmodifiedNumber = unmodifiedNumber;
    }
  }

  private static class UnitWrapperComparator implements Comparator<UnitWrapper> {
    private Comparator<? super Unit> unitCmp = null;

    /**
     * Creates a new UnitWrapperComparator object.
     */
    public UnitWrapperComparator(Comparator<? super Unit> unitCmp) {
      this.unitCmp = unitCmp;
    }

    /**
     * If a unit comparator was specified, it is used to compare the arguments, otherwise the
     * getAmout() values are compared.
     */
    public int compare(UnitWrapper o1, UnitWrapper o2) {
      if (unitCmp != null)
        return unitCmp.compare(o1.getUnit(), o2.getUnit());
      else
        return o2.getAmount() - o1.getAmount();
    }

    // /**
    // * DOCUMENT-ME
    // */
    // @Override
    // public boolean equals(Object o) {
    // return false;
    // }
  }

  /**
   * This will be a Map&lt;ItemType.id, StatItem&gt;, which is a Map of items of one category.
   */
  private static class StatItemContainer extends Hashtable<ID, StatItem> implements
  Comparable<StatItemContainer> {
    private ItemCategory category = null;

    /**
     * DOCUMENT-ME
     */
    public ItemCategory getCategory() {
      return category;
    }

    /**
     * Creates a new StatItemContainer object.
     */
    public StatItemContainer(ItemCategory category) {
      this.category = category;
    }

    /**
     * DOCUMENT-ME
     */
    public int compareTo(StatItemContainer o) {
      return category.compareTo(o.getCategory());
    }
  }

  private void initItemCategories() {
    if (rules.getItemCategories().size() == 0) {
      ItemCategory cat = new ItemCategory(StringID.create("misc"));
      cat.setName("misc");
      StatItemContainer sic = new StatItemContainer(cat);
      itemCategoriesMap.put(cat, sic);
      catLessContainer = sic;
      return;
    }

    for (final Iterator<ItemCategory> iter = rules.getItemCategoryIterator(); iter.hasNext();) {
      final ItemCategory cat = iter.next();
      final StatItemContainer sic = new StatItemContainer(cat);
      itemCategoriesMap.put(cat, sic);

      if (!iter.hasNext()) {
        // pavkovic 2003.11.20: we assume that the last item category is the
        // "misc" category
        catLessContainer = sic;
      }
    }
  }

  /**
   * Returns the Container for the specified items type.
   * 
   * @param type The item whose category you want.
   * @return The Container if items of type <code>type</code>.
   */
  private StatItemContainer getItemContainer(ItemType type) {
    if ((type.getCategory() == null) || (itemCategoriesMap.get(type.getCategory()) == null))
      return catLessContainer;

    return itemCategoriesMap.get(type.getCategory());
  }

  private void clearItemContainers() {
    for (StatItemContainer sic : itemCategoriesMap.values()) {
      sic.clear();
    }
  }

  /**
   * Modifies <code>u</code>'s orders as specified in <code>s</code>.
   * 
   * @see GiveOrderDialog#showGiveOrderDialog()
   * @param s A string array with the following values: <br/>
   *          [0] : The order that was given <br/>
   *          [1] : A String representative of the boolean value for "Replace orders" <br/>
   *          [2] : A String representative of the boolean value for "Keep comments" <br/>
   *          [3] : One of {@link GiveOrderDialog#FIRST_POS}, {@link GiveOrderDialog#LAST_POS}
   */
  public static void addOrders(Unit u, String[] s) {
    addOrders(u, s, true);
  }

  /**
   * Modifies <code>u</code>'s orders as specified in <code>s</code>.
   * 
   * @see GiveOrderDialog#showGiveOrderDialog()
   * @param s A string array with the following values: <br/>
   *          [0] : The order that was given <br/>
   *          [1] : A String representative of the boolean value for "Replace orders" <br/>
   *          [2] : A String representative of the boolean value for "Keep comments" <br/>
   *          [3] : One of {@link GiveOrderDialog#FIRST_POS}, {@link GiveOrderDialog#LAST_POS}
   */
  public static void addOrders(Unit u, String[] s, boolean refreshRelations) {
    if (s == null || s.length != 4)
      throw new IllegalArgumentException("expecting exactly 4 arguments");

    if (s[0] != null) {
      final boolean replace = Boolean.valueOf(s[1]).booleanValue();
      final boolean keepComments = Boolean.valueOf(s[2]).booleanValue();
      final String position = s[3];
      final String[] newOrderArray = s[0].split("\n");

      if (replace) {
        if (keepComments) {
          final Collection<Order> oldOrders = u.getOrders2();
          final List<Order> newOrders = new LinkedList<Order>();

          for (Order order : oldOrders) {
            if (!order.isEmpty() && order.getToken(0).ttype == OrderToken.TT_COMMENT) {
              newOrders.add(order);
            }
          }

          if (position.equals(GiveOrderDialog.FIRST_POS)) {
            for (int i = newOrderArray.length - 1; i >= 0; --i) {
              newOrders.add(0, u.createOrder(newOrderArray[i]));
            }
          } else {
            for (final String sHelp : newOrderArray) {
              newOrders.add(newOrders.size(), u.createOrder(sHelp));
            }
          }
          u.setOrders2(newOrders, refreshRelations);
        } else {

          final List<String> newOrders = new LinkedList<String>();
          for (final String sHelp : newOrderArray) {
            newOrders.add(sHelp);
          }
          u.setOrders(newOrders, refreshRelations);
        }
      } else {
        if (position.equals(GiveOrderDialog.FIRST_POS)) {
          for (int i = newOrderArray.length - 1; i >= 0; --i) {
            u.addOrderAt(0, newOrderArray[i], refreshRelations);
          }
        } else {
          for (final String sHelp : newOrderArray) {
            u.addOrderAt(u.getOrders2().size(), sHelp, refreshRelations);
          }
        }
      }
    }
  }

  /**
   * Modifies <code>u</code>'s orders as specified in <code>s</code>.
   * 
   * @see RemoveOrderDialog#showDialog()
   * @param s A string array with the following values: <br/>
   *          [0] : The order fragment that was given <br/>
   *          [1] : One of {@link RemoveOrderDialog#BEGIN_ACTION},
   *          {@link RemoveOrderDialog#CONTAINS_ACTION}, {@link RemoveOrderDialog#REGEX_ACTION} <br/>
   *          [2] : "true" if case should not be ignored
   */
  public static void removeOrders(Unit u, String[] s) {
    if (s == null || s.length != 3)
      throw new IllegalArgumentException("expecting exactly 3 arguments");

    if (s[0] != null) {
      final String pattern = s[0];
      final String mode = s[1];
      final String matchCase = s[2];

      final Collection<Order> oldOrders = u.getOrders2();
      final List<Order> newOrders = new LinkedList<Order>();

      for (Order order : oldOrders) {
        if (mode.equals(RemoveOrderDialog.REGEX_ACTION)) {
          if (order.getText().matches(pattern)) {
            continue;
          }
        } else {
          String casedOrder;
          if (matchCase.equals("false")) {
            casedOrder = order.getText().toLowerCase();
          } else {
            casedOrder = order.getText();
          }
          if (mode.equals(RemoveOrderDialog.BEGIN_ACTION))
            if (casedOrder.startsWith(pattern)) {
              continue;
            }

          if (mode.equals(RemoveOrderDialog.CONTAINS_ACTION))
            if (casedOrder.contains(pattern)) {
              continue;
            }
        }
        newOrders.add(order);

      }
      u.setOrders2(newOrders);
    }
  }
}
