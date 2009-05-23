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
import magellan.client.swing.tree.ItemCategoryNodeWrapper;
import magellan.client.swing.tree.ItemNodeWrapper;
import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.client.swing.tree.SimpleNodeWrapper;
import magellan.client.swing.tree.UnitNodeWrapper;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
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
   * A Map&lt;ItemCategory, StatItemContainer&gt; mapping the item categories to
   * containers with items of the corresponding category.
   */
  private Map<ItemCategory, StatItemContainer> itemCategoriesMap = new Hashtable<ItemCategory, StatItemContainer>();

  private static ItemType silberbeutel = new ItemType(StringID.create("Silberbeutel"));
  private static ItemType silberkassette = new ItemType(StringID.create("Silberkassette"));

  /**
   * Creates a new Units object.
   */
  public Units(Rules rules) {
    setRules(rules);
  }

  /**
   * DOCUMENT-ME
   */
  public void setRules(Rules rules) {
    this.rules = rules;

    if (rules != null) {
      initItemCategories();
    }
  }

  /**
   * Calculates the amounts of all items of all units and records the amount in
   * the itemCategoriesMap.
   * 
   * @param units
   *          All items of all units in this Collection are accounted for.
   * @return The sorted list of categories.
   */
  public Collection<StatItemContainer> categorizeUnitItems(Collection<Unit> units) {
    if ((itemCategoriesMap == null) || (itemCategoriesMap.size() == 0)) {
      Units.log.warn("categorizeUnitItems(): the category map is not initialized!");

      return null;
    }

    clearItemContainers();

    // iterate over all units...
    for (Iterator<Unit> it = units.iterator(); it.hasNext();) {
      Unit u = it.next();

      // ...and their items
      for (Iterator<Item> i = u.getModifiedItems().iterator(); i.hasNext();) {
        Item item = i.next();

        // get the container this item is stored in
        Map<ID, StatItem> container = getItemContainer(item.getItemType());

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

        if (item.getItemType().equals(Units.silberbeutel) || item.getItemType().equals(Units.silberkassette)) {
          amount *= u.getPersons();
        }

        stored.setAmount(stored.getAmount() + amount);
        
        // try to remember unmodifiedAmount
        Item unmodifiedItem = u.getItem(item.getItemType());
        int unmodifiedAmount=0;
        if (unmodifiedItem!=null){
          unmodifiedAmount=unmodifiedItem.getAmount();
        }
        stored.setUnmodifiedAmount(stored.getUnmodifiedAmount() + unmodifiedAmount);
        
        UnitWrapper uW = new UnitWrapper(u, amount);
        uW.setUnmodifiedAmount(unmodifiedAmount);
        // add the unit owning the item to the stat item
        stored.units.add(uW);
      }
    }

    List<StatItemContainer> sortedCategories = new LinkedList<StatItemContainer>(itemCategoriesMap.values());
    Collections.sort(sortedCategories);

    return sortedCategories;
  }

  

  /**
   * This method takes all items carried by units in the units Collection and
   * sorts them by their category. Then it adds the non-empty categories to the
   * specified parent node and puts the corresponding items in each category
   * node. Optionally, the units carrying an item are listed as child nodes of
   * the respective item nodes.
   * 
   * @param units
   *          a collection of Unit objects carrying items.
   * @param parentNode
   *          a tree node to add the new nodes to.
   * @param itemComparator
   *          a comparator to sort the items with. If itemComparator is null the
   *          items are sorted by name.
   * @param unitComparator
   *          a comparator to sort the units with. If unitComparator is null the
   *          units are sorted by the amount of the item carried underneath
   *          which they appear.
   * @param showUnits
   *          if true each item node gets child nodes containing the unit(s)
   *          carrying that item.
   * @return a collection of DefaultMutableTreeNode objects with user objects of
   *         class ItemCategory or null if the categorization of the items
   *         failed.
   */
  public Collection addCategorizedUnitItems(Collection<Unit> units,
      DefaultMutableTreeNode parentNode, Comparator<Item> itemComparator,
      Comparator<Unit> unitComparator, boolean showUnits, NodeWrapperFactory factory) {

    DefaultMutableTreeNode categoryNode = null;
    Collection<TreeNode> categoryNodes = new LinkedList<TreeNode>();

    Collection<StatItemContainer> listOfCategorizedItems = categorizeUnitItems(units);

    if (listOfCategorizedItems == null) {
      Units.log.warn("addCategorizedUnitItems(): categorizing unit items failed!");

      return null;
    }

    for (Iterator<StatItemContainer> contIter = listOfCategorizedItems.iterator(); contIter
        .hasNext();) {
      StatItemContainer currentCategoryMap = contIter.next();

      if (currentCategoryMap.size() > 0) {

        String catIconName =
            magellan.library.utils.Umlaut
                .convertUmlauts(currentCategoryMap.getCategory().getName());
        String nodeName = Resources.get("util.units." + catIconName);
        ItemCategoryNodeWrapper wrapper =
            new ItemCategoryNodeWrapper(currentCategoryMap.getCategory(), -1, nodeName);
        wrapper.setIcons(catIconName);
        categoryNode = new DefaultMutableTreeNode(wrapper);

        /**
         * catNode = new
         * DefaultMutableTreeNode(factory.createSimpleNodeWrapper(wrapper,
         * catIconName));
         */
        parentNode.add(categoryNode);
        categoryNodes.add(categoryNode);

        List<StatItem> sortedItems = new LinkedList<StatItem>(currentCategoryMap.values());

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
        for (Iterator<StatItem> iter = sortedItems.iterator(); iter.hasNext();) {
          StatItem currentItem = iter.next();
          addItemNode(currentItem, categoryNode, u, units, unitComparator, showUnits, factory);
          catNumber += currentItem.getAmount();
          unmodifiedCatNumber += currentItem.getUnmodifiedAmount();
        }

        if ((catNumber > 0)
            && !currentCategoryMap.category.equals(rules.getItemCategory(StringID.create("misc")))) {
          wrapper.setAmount(catNumber);
        }
        if ((unmodifiedCatNumber > 0)
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
      NodeWrapperFactory factory) {
    categorizeUnitItems(units);
    for (StatItemContainer itemContainer : itemCategoriesMap.values()) {
      if (itemContainer.get(item.getID()) != null) {
        addItemNode(itemContainer.get(item.getID()), categoryNode, u, units, unitComparator,
            showUnits, factory);
      }
    }

    return 0;
  }

  /**
   * @param currentItem
   */
  private void addItemNode(StatItem currentItem, DefaultMutableTreeNode categoryNode, Unit u,
      Collection<Unit> units, Comparator<Unit> unitComparator, boolean showUnits,
      NodeWrapperFactory factory) {

    ItemNodeWrapper itemNodeWrapper = factory.createItemNodeWrapper(u, currentItem,currentItem.getUnmodifiedAmount());
    DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(itemNodeWrapper);

    categoryNode.add(itemNode);

    if (!showUnits && units.size() == 1) {
      boolean addItemNode = false;

      for (Iterator reservedIterator =
          u.getItemReserveRelations(currentItem.getItemType()).iterator(); reservedIterator
          .hasNext();) {
        ReserveRelation itr = (ReserveRelation) reservedIterator.next();
        String text = String.valueOf(itr.amount) + " ";
        List<String> icons = new LinkedList<String>();
        if (itr.warning) {
          itemNodeWrapper.setWarningFlag(true);
          text = String.valueOf(itr.amount) + " (!!!) "; // TODO: use append
          icons.add("warnung");
        }
        text = text + Resources.get("util.units.node.reserved");
        icons.add("reserve");

        SimpleNodeWrapper reserveNodeWrapper = factory.createSimpleNodeWrapper(text, icons);

        itemNode.add(new DefaultMutableTreeNode(reserveNodeWrapper));

        addItemNode = true;
      }

      for (Iterator iter2 = u.getItemTransferRelations(currentItem.getItemType()).iterator(); iter2
          .hasNext();) {
        ItemTransferRelation currentRelation = (ItemTransferRelation) iter2.next();
        String prefix = String.valueOf(currentRelation.amount) + " ";
        if (currentRelation.warning) {
          itemNodeWrapper.setWarningFlag(true);
          // TODO: use append
          prefix = String.valueOf(currentRelation.amount) + " (!!!) ";

        }

        String addIcon = null;
        Unit u2 = null;

        if (currentRelation.source == u) {
          addIcon = "get";
          u2 = currentRelation.target;
        } else if (currentRelation.target == u) {
          addIcon = "give";
          u2 = currentRelation.source;
        }

        UnitNodeWrapper giveNodeWrapper =
            factory.createUnitNodeWrapper(u2, prefix, u2.getPersons(), u2.getModifiedPersons());
        giveNodeWrapper.setReverseOrder(true);
        giveNodeWrapper.setAdditionalIcon(addIcon);

//        if (currentRelation.warning) {
//          giveNodeWrapper.setAdditionalIcon("warnung");
//        }
        itemNode.add(new DefaultMutableTreeNode(giveNodeWrapper));

        addItemNode = true;
      }
      if (addItemNode) {
        // FIXME: we return different objects here!!
        categoryNode.add(itemNode);
      }
    }

    if (showUnits && (currentItem.units != null)) {
      Collections.sort(currentItem.units, new UnitWrapperComparator<Unit>(unitComparator));

      for (Iterator it = currentItem.units.iterator(); it.hasNext();) {
        UnitWrapper uw = (UnitWrapper) it.next();
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
    
    private int unmodifiedAmount=0;
    
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
      return this.getItemType().getName().compareTo((o).getItemType().getName());
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
        if (unmodifiedNumber > -1 && unmodifiedNumber!=number){
          return unit.toString() + ": " + unmodifiedNumber + " (" + number + ")";
        } else {
          return unit.toString() + ": " + number;
        }
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

  private static class UnitWrapperComparator<E> implements Comparator<UnitWrapper> {
    private Comparator<E> unitCmp = null;

    /**
     * Creates a new UnitWrapperComparator object.
     */
    public UnitWrapperComparator(Comparator<E> unitCmp) {
      this.unitCmp = unitCmp;
    }

    /**
     * DOCUMENT-ME
     */
    public int compare(UnitWrapper o1, UnitWrapper o2) {
      if (unitCmp != null) {
        return unitCmp.compare((E) o1.getUnit(), (E) o2.getUnit());
      } else {
        return o2.getAmount() - o1.getAmount();
      }
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public boolean equals(Object o) {
      return false;
    }
  }

  /**
   * This will be a Map&lt;ItemType.id, StatItem&gt;, which is a Map of items of
   * one category.
   */
  private static class StatItemContainer extends Hashtable<ID, StatItem> implements Comparable {
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
    public int compareTo(Object o) {
      return this.category.compareTo(((StatItemContainer) o).getCategory());
    }
  }

  private void initItemCategories() {
    for (Iterator iter = rules.getItemCategoryIterator(); iter.hasNext();) {
      ItemCategory cat = (ItemCategory) iter.next();
      StatItemContainer sic = new StatItemContainer(cat);
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
   * @param type
   *          The item whose category you want.
   * @return The Container if items of type <code>type</code>.
   */
  private StatItemContainer getItemContainer(ItemType type) {
    if ((type.getCategory() == null) || (itemCategoriesMap.get(type.getCategory()) == null)) {
      return catLessContainer;
    }

    return itemCategoriesMap.get(type.getCategory());
  }

  private void clearItemContainers() {
    for (Iterator<StatItemContainer> iter = itemCategoriesMap.values().iterator(); iter.hasNext();) {
      StatItemContainer sic = iter.next();
      sic.clear();
    }
  }

  /**
   * Modifies <code>u</code>'s orders as specified in <code>s</code>.
   * 
   * @see GiveOrderDialog#showGiveOrderDialog()
   * @param s A string array with the following values: <br/>
   *       [0] : The order that was given <br/>
   *       [1] : A String representative of the boolean value for "Replace orders" <br/>
   *       [2] : A String representative of the boolean value for "Keep comments" <br/>
   *       [3] : One of {@link GiveOrderDialog#FIRST_POS}, {@link GiveOrderDialog#LAST_POS}
   */
  public static void addOrders(Unit u, String[] s) {
    if (s == null || s.length != 4) {
      throw new IllegalArgumentException("expecting exactly 4 arguments");
    }
  
    if (s[0] != null) {
      boolean replace = Boolean.valueOf(s[1]).booleanValue();
      boolean keepComments = Boolean.valueOf(s[2]).booleanValue();
      String position = s[3];
      String[] newOrderArray = s[0].split("\n");
  
      if (replace) {
        if (keepComments) {
          Collection oldOrders = u.getOrders();
          List<String> newOrders = new LinkedList<String>();

          for (Iterator iterator = oldOrders.iterator(); iterator.hasNext();) {
            String order = (String) iterator.next();

            if (order.trim().startsWith("//") || order.trim().startsWith(";")) {
              newOrders.add(order);
            }
          }

          if (position.equals(GiveOrderDialog.FIRST_POS)) {
            for (int i = newOrderArray.length-1; i>=0; --i) { 
              newOrders.add(0, newOrderArray[i]);
            }
          } else {
            for (String sHelp : newOrderArray) {
              newOrders.add(newOrders.size(), sHelp);
            }
          }
          u.setOrders(newOrders);
        } else {

          List<String> newOrders = new LinkedList<String>();
          for (String sHelp : newOrderArray) {
            newOrders.add(sHelp);
          }
          u.setOrders(newOrders);
        }
      } else {
        if (position.equals(GiveOrderDialog.FIRST_POS)) {
          for (int i = newOrderArray.length-1; i>=0; --i) { 
            u.addOrderAt(0, newOrderArray[i], true);
          }
        } else {
          for (String sHelp : newOrderArray) {
            u.addOrderAt(u.getOrders().size(), sHelp, true);
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
   *         [0] : The order fragment that was given <br/>
   *         [1] : One of {@link RemoveOrderDialog#BEGIN_ACTION},
   *         {@link RemoveOrderDialog#CONTAINS_ACTION} <br/>
   *         [2] : "true" if case should not be ignored
   */
  public static void removeOrders(Unit u, String[] s) {
    if (s == null || s.length != 3) {
      throw new IllegalArgumentException("expecting exactly 3 arguments");
    }
  
    if (s[0] != null) {
      String pattern = s[0];
      String mode = s[1];
      String matchCase = s[2];
      
      Collection oldOrders = u.getOrders();
      List<String> newOrders = new LinkedList<String>();

      for (Iterator iterator = oldOrders.iterator(); iterator.hasNext();) {
        String order = (String) iterator.next();
        String casedOrder;
        if (matchCase.equals("false"))
          casedOrder = order.toLowerCase();
        else
          casedOrder = order;
        if (!((mode.equals(RemoveOrderDialog.BEGIN_ACTION) && casedOrder.startsWith(pattern)) 
            || (mode.equals(RemoveOrderDialog.CONTAINS_ACTION) && casedOrder.contains(pattern))))
          newOrders.add(order);
      }
      u.setOrders(newOrders);
    }
  }
}

