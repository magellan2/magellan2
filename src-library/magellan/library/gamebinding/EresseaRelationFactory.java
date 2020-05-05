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

package magellan.library.gamebinding;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.relation.ControlRelation;
import magellan.library.relation.EnterRelation;
import magellan.library.relation.FollowUnitRelation;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.LeaveRelation;
import magellan.library.relation.MovementRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.TransportRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.tasks.OrderSyntaxInspector;
import magellan.library.tasks.OrderSyntaxInspector.OrderSemanticsProblemTypes;
import magellan.library.utils.Resources;
import magellan.library.utils.Units;
import magellan.library.utils.logging.Logger;

/**
 * Responsible for creating relations stemming from unit orders.
 */
public class EresseaRelationFactory implements RelationFactory {

  private static final Logger log = Logger.getInstance(EresseaRelationFactory.class);
  private Rules rules;

  private Updater updater;

  /** Order priority */
  public static final int P_BENENNE = 301;
  /** Order priority */
  public static final int P_KAEMPFE = 302;
  /** Order priority */
  public static final int P_BETRETE = 701;
  /** Order priority */
  public static final int P_GIB_KOMMANDO = 901;
  /** Order priority */
  public static final int P_VERLASSE = 1001;
  /** Order priority */
  public static final int P_ATTACKIERE = 1201;
  /** Order priority */
  public static final int P_RESERVIERE = 1301;
  /** Order priority */
  public static final int P_FOLGE = 1401;
  /** Order priority */
  public static final int P_GIB = 1501;
  /** Order priority */
  public static final int P_REKRUTIERE = 1701;
  /** Order priority */
  public static final int P_BEFOERDERE = 1801;
  /** Order priority */
  public static final int P_LEHRE = 2301;
  /** Order priority */
  public static final int P_FAHRE = 2898;
  /** Order priority */
  public static final int P_TRANSPORTIERE = 2899;
  /** Order priority */
  public static final int P_NACH = 2900;
  /** Order priority */
  public static final int P_BEWACHE = 3001;

  /** Order priority */
  public static final int P_BUILDING_MAINTENANCE = 2001;

  /** Order priority */
  public static final int P_UNIT_MAINTENANCE = 3501;

  protected EresseaRelationFactory(Rules rules) {
    this.rules = rules;
    updater = new Updater();
  }

  /**
   * Time in ms to wait before orders are processed after an order change event.
   */
  public static final int PROCESS_DELAY = 100;

  protected class Processor implements Runnable {
    private Region region;

    public Processor(Region r) {
      region = r;
    }

    public void run() {
      // long time = System.currentTimeMillis();
      // log.finest(0);
      processOrders(region);
      // log.finest("rr " + (System.currentTimeMillis() - time));
    }

  }

  protected class Updater implements ActionListener {

    Set<Region> regions;
    private Timer timer;
    private boolean stopped;

    public Updater() {
      regions = new HashSet<Region>();
      timer = new Timer(PROCESS_DELAY, this);
    }

    public synchronized void add(Region region) {
      regions.add(region);
      if (!stopped) {
        timer.restart();
        // log.finer("add " + region);
      }
    }

    /**
     * Called when timer fired.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public synchronized void actionPerformed(ActionEvent e) {
      if (!regions.isEmpty()) {
        log.finer("updating " + regions.size());
        for (Region r : regions) {
          SwingUtilities.invokeLater(new Processor(r));
        }
      }
      regions.clear();
    }

    /**
     * Stop timer.
     */
    public synchronized void stop() {
      stopped = true;
      timer.stop();
    }

    /**
     * Restart timer.
     */
    public synchronized void restart() {
      stopped = false;
      timer.restart();
    }

    public synchronized boolean isStopped() {
      return stopped;
    }

  }

  /**
   * stops the updater
   */
  public synchronized void stopUpdating() {
    if (updater.isStopped())
      return;
    updater.stop();
  }

  /**
   * restarts the updater
   */
  public synchronized void restartUpdating() {
    if (updater.isStopped()) {
      updater.restart();
    }
  }

  /**
   * Recreates all relations in this region. Updater has to be stopped.
   *
   * @param r - the Region to be processed
   */
  public void processRegionNow(Region r) {
    if (updater.isStopped()) {
      processOrders(r);
    } else {
      log.warn("processRegionNow called while updater is running - aborted");
    }
  }

  /**
   * @return true, if updater is stopped
   */
  public boolean isUpdaterStopped() {
    return updater.isStopped();
  }

  /**
   * Returns the value of rules.
   *
   * @return Returns rules.
   */
  protected Rules getRules() {
    return rules;
  }

  /**
   * Creates a list of com.eressea.util.Relation objects for a unit starting at order position
   * <tt>from</tt>. Note: The parameter <code>from</code> is ignored by this implementation!
   *
   * @param u The unit
   * @param from The line of the <code>unit</code>'s orders where to start. Must be > 0
   * @return null
   * @deprecated
   */
  // TODO (stm 2007-02-24) Should we remove the parameter from from the interface? It violates the
  // unit execution order but it might be useful for other games.
  @Deprecated
  public List<UnitRelation> createRelations(Unit u, int from) {
    processOrders(u.getRegion());
    return null;
    // return createRelations(u, u.getOrders2(), from);
  }

  public void createRelations(Region region) {
    updater.add(region);

    // processOrders(region);
  }

  private Set<Region> affected;

  /**
   * Ensures that {@link ReserveRelation}s are sorted before all other relations.
   */
  public static class OrderPreferenceComparator implements Comparator<UnitRelation> {

    public int compare(UnitRelation o1, UnitRelation o2) {
      if (o1 instanceof ReserveRelation && !(o2 instanceof ReserveRelation))
        return -1;
      if (!(o1 instanceof ReserveRelation) && (o2 instanceof ReserveRelation))
        return 1;
      return 0;
    }
  }

  protected static final class OrderInfo implements Comparable<OrderInfo> {
    public Order order;
    public Unit unit;
    public int line;
    public int priority;

    public OrderInfo(Order order, int priority, Unit unit, int line) {
      this.order = order;
      this.priority = priority;
      this.unit = unit;
      this.line = line;
    }

    @Override
    public String toString() {
      return order.toString();
    }

    public int compareTo(OrderInfo o) {
      return priority - o.priority;
    }
  }

  protected synchronized void processOrders(Region r) {
    // long time = System.currentTimeMillis();
    GameData data = r.getData();
    affected = new HashSet<Region>();
    affected.add(r);

    // count orders
    int count = 0; // two for maintenance orders
    for (Unit u : r.units()) {
      count += u.getOrders2().size();
      if (u.getNewRegion() != null) {
        affected.add(data.getRegion(u.getNewRegion()));
        // for (Order o : u.getOrders2()) {
        // // FIXME create TEMP unit!!
        // // if (u.getOrders2().isToken(o, 0, EresseaConstants.OC_MAKE)
        // // && (u.getOrders2().isToken(o, 1, EresseaConstants.OC_TEMP))) {
        // // } else {
        // count++;
        // // }
        // }
      }
    }
    // use array to save memory while sorting, because Collections.sort() makes a copy

    UnitOrdering orders = new UnitOrdering();
    orders.reset(count);

    for (Unit u : r.units()) {
      int line = 0;
      for (Order o : u.getOrders2()) {
        // FIXME create TEMP unit!!
        // if (u.getOrders2().isToken(o, 0, EresseaConstants.OC_MAKE)
        // && (u.getOrders2().isToken(o, 1, EresseaConstants.OC_TEMP))) {
        // } else {
        orders.add(o, getPriority(o), u, ++line);
        // }
      }

      u.clearRelations();
      if (u.getFaction() != null) {
        u.getFaction().clearRelations();
      }
    }

    r.getZeroUnit().clearRelations();
    for (UnitContainer uc : r.buildings()) {
      uc.clearRelations();
    }
    for (UnitContainer uc : r.ships()) {
      uc.clearRelations();
    }
    r.clearRelations();

    orders.sort(r.units());
    // Arrays.sort(orders);
    EresseaExecutionState state = new EresseaExecutionState(r.getData(), orders);
    boolean bmExecuted = false, umExecuted = false, resExecuted = false;
    for (; orders.hasNext(); orders.consume()) {
      if (!bmExecuted && orders.getPriority() > P_BUILDING_MAINTENANCE) {
        BuildingMaintenanceOrder.execute(r, state, r.getData());
        bmExecuted = true;
      }
      if (!umExecuted && orders.getPriority() > P_UNIT_MAINTENANCE) {
        UnitMaintenanceOrder.execute(r, state, r.getData());
        umExecuted = true;
      }
      if (orders.getPriority() == P_RESERVIERE && !resExecuted) {
        ReserveOwnOrder.execute(r, state, r.getData());
        resExecuted = true;
      }
      Order o = orders.getOrder();
      o.setProblem(null);
      o.execute(state, data, orders.getUnit(), orders.getLine());
    }
    if (!bmExecuted) {
      BuildingMaintenanceOrder.execute(r, state, r.getData());
      bmExecuted = true;
    }
    if (!umExecuted) {
      UnitMaintenanceOrder.execute(r, state, r.getData());
      umExecuted = true;
    }
    if (!resExecuted) {
      ReserveOwnOrder.execute(r, state, r.getData());
      resExecuted = true;
    }

    postProcess(r);
  }

  protected void postProcess(Region r) {
    // long time = System.currentTimeMillis();
    // log.finest(0);
    for (Unit u : r.units()) {
      checkTransport(u);
      // updateSilver(u);
    }

    GameData data = r.getData();

    // log.finest(System.currentTimeMillis() - time);
    Object cause = new Object();
    for (Region r2 : affected) {
      if (r2 != null) {
        if (!updater.isStopped()) {
          data.fireOrdersChanged(this, r2, cause);
        }
      }
    }
    affected.clear();
    // log.finest(System.currentTimeMillis() - time);
  }

  private void checkTransport(Unit u) {
    TransportRelation carrying = null, riding = null;
    for (UnitRelation rel : u.getRelations()) {
      // issue a warning if a unit that passes a command leaves the building
      if (rel instanceof ControlRelation) {
        ControlRelation cRel = (ControlRelation) rel;
        if (cRel.source == u) {
          List<LeaveRelation> leaveRelations = u.getRelations(LeaveRelation.class);
          for (LeaveRelation lrel : leaveRelations)
            if (lrel.origin == u && lrel.line > 0 && !lrel.isImplicit()) {
              // 2018-08-17: Warnung ist nur berechtigt, wenn target nicht das Gebäude betritt...
              boolean showWarning = true;
              List<EnterRelation> enterRelations = cRel.target.getRelations(EnterRelation.class);
              for (EnterRelation erel : enterRelations) {
                if (lrel.target.equals(erel.target)) {
                  // Das Gebäude, welches verlassen wird, wird von der Kommandoempfangenen Einheit
                  // betreten
                  showWarning = false;
                }
              }
              if (showWarning) {
                cRel.setWarning(Resources.get("order.leave.warning.control"),
                    OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
              }
            }
        }
      }

      if (rel instanceof TransportRelation) {
        TransportRelation tRel = (TransportRelation) rel;
        if (tRel.origin == u) {
          if (tRel.target == null) {
            riding = tRel;
            tRel.target = tRel.origin;
            if (tRel.problem == null) {
              tRel.setWarning(Resources.get("order.transport.warning.notcarrying", tRel.source),
                  OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
            }
          } else {
            carrying = tRel;
            if (tRel.source == null) {
              tRel.source = tRel.origin;
            } else if (tRel.source.getRelations(MovementRelation.class).isEmpty()) {
              if (tRel.source.getRelations(FollowUnitRelation.class).isEmpty()) {
                tRel.setWarning(Resources.get("order.transport.warning.notmoving"),
                    OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
              }
            }
          }
        } else if (tRel.target == u && tRel.source != null) {
          riding = tRel;
        }

      }
    }
    if (riding != null && carrying != null) {
      riding.setWarning(Resources.get("order.transport.warning.rideandcarry"),
          OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
    }
  }

  protected Comparator<OrderInfo> getOrderComparator() {
    return new Comparator<OrderInfo>() {
      public int compare(OrderInfo o1, OrderInfo o2) {
        return o1.priority - o2.priority;
      }
    };
  }

  protected int getPriority(Order order) {
    if (order instanceof RenameOrder)
      return P_BENENNE;
    else if (order instanceof ReserveOrder)
      return P_RESERVIERE;
    else if (order instanceof FollowUnitOrder)
      return P_FOLGE;
    else if (order instanceof GiveOrder) {
      if (((GiveOrder) order).getType() != null && ((GiveOrder) order).getType().equals(
          EresseaConstants.OC_CONTROL))
        return P_GIB_KOMMANDO;
      return P_GIB;
    } else if (order instanceof RecruitmentOrder)
      return P_REKRUTIERE;
    else if (order instanceof PromoteOrder)
      return P_BEFOERDERE;
    else if (order instanceof EnterOrder)
      return P_BETRETE;
    else if (order instanceof CombatOrder)
      return P_KAEMPFE;
    else if (order instanceof GuardOrder)
      return P_BEWACHE;
    else if (order instanceof LeaveOrder)
      return P_VERLASSE;
    else if (order instanceof MovementOrder)
      return P_NACH;
    else if (order instanceof RenameOrder)
      return P_BENENNE;
    else if (order instanceof TeachOrder)
      return P_LEHRE;
    else if (order instanceof AttackOrder)
      return P_ATTACKIERE;
    else if (order instanceof RideOrder)
      return P_FAHRE;
    else if (order instanceof TransportOrder)
      return P_TRANSPORTIERE;

    return P_FIRST;
  }

  /**
   * A class that stores state information during the execution of the orders.
   *
   * @author stm
   */
  public static class EresseaExecutionState implements ExecutionState {

    private Collection<ItemType> herbTypes;
    private GameData data;
    private Map<Unit, Map<StringID, Integer>> reserves;
    private UnitOrdering orders;

    public EresseaExecutionState(GameData data) {
      this(data, null);
    }

    /**
     * @param data
     */
    public EresseaExecutionState(GameData data, UnitOrdering orders) {
      this.data = data;
      this.orders = orders;
    }

    /**
     * Tries to reserve a certain amount of an item. This method tries to get it from the material
     * pool if possible and may produce and register additional relations.
     *
     * @param unit The unit that requires the item
     * @param type The required item type
     * @param requiredAmount The required amount
     * @param all <code>true</code> if ALL of the unit's item should be acquired
     * @param includeReserved <code>true</code> if the amount of item that was reserved by the unit
     *          counts toward the acquired amount.
     * @param getReserved <code>true</code> if the amount of item that the unit has received counts
     *          toward the acquired amount.
     * @param line The line number of the order (used for relations)
     * @param order The order that caused this
     * @return A list of ReserveRelations and TransferRelations caused be the request. The last
     *         order is a reserve relation of the unit itself. These relations have not been added,
     *         yet.
     */
    public List<UnitRelation> acquireItem(Unit unit, ItemType type, int requiredAmount,
        boolean all, boolean includeReserved, boolean getReserved, int line, Order order) {
      List<UnitRelation> result = null;

      int reservedAmount = getFreeAmount(unit, type, includeReserved);
      if (!all) {
        reservedAmount = Math.min(reservedAmount, requiredAmount);
      } else {
        requiredAmount = 0;
      }
      reservedAmount = Math.max(0, reservedAmount);
      if (reservedAmount < requiredAmount) {
        if (unit.getData().getGameSpecificRules().isPooled(unit, type)) {
          result = new LinkedList<UnitRelation>();
          // avoid NPE for "lost" units
          if (unit.getRegion() != null) {
            for (Unit u : unit.getRegion().units()) {
              if (u.getFaction() == unit.getFaction() && u != unit) {
                int givenAmount =
                    Math.min(getFreeAmount(u, type, getReserved), requiredAmount - reservedAmount);
                if (givenAmount > 0) {
                  UnitRelation giveRelation =
                      new ItemTransferRelation(unit, u, unit, givenAmount, type, line);
                  result.add(giveRelation);
                  reservedAmount += givenAmount;
                }
                if (reservedAmount >= requiredAmount) {
                  break;
                }
              }
            }
          }
        }
      }
      ReserveRelation ownRelation = new ReserveRelation(unit, reservedAmount, type, line);
      if (reservedAmount != requiredAmount && !all) {
        ownRelation.setWarning(
            Resources.get("order.reserve.warning.insufficient", type.toString()),
            OrderSyntaxInspector.OrderSemanticsProblemTypes.GIVE_WARNING.type);
      }
      if (result != null) {
        result.add(ownRelation);
        return result;
      } else
        return Collections.<UnitRelation> singletonList(ownRelation);
    }

    private int getFreeAmount(Unit unit, ItemType type, boolean includeReserved) {
      Item item = unit.getModifiedItem(type);
      int amount = 0;
      if (item != null) {
        amount = item.getAmount();
      }
      if (!includeReserved) {
        // remove what is reserved
        for (ReserveRelation itr : unit.getRelations(ReserveRelation.class)) {
          if (itr.source == unit && itr.itemType == type) {
            amount -= itr.amount;
          }
        }
      }

      return amount;
    }

    /**
     * Tries to reserve a certain amount of an item. This method tries to get it from the material
     * pool if possible and may produce and register additional relations.
     *
     * @param type
     * @param requiredAmount
     * @param unit
     * @param line
     * @return The relations created for satisfying this event; The last relation is the main
     *         relation.
     */
    public List<UnitRelation> reserveItem(ItemType type, boolean all, boolean includeReserved,
        int requiredAmount, Unit unit, int line, Order order) {
      List<UnitRelation> result = null;

      int reservedAmount = getFreeAmount(type, unit, includeReserved, !all);
      if (!all) {
        reservedAmount = Math.min(reservedAmount, requiredAmount);
      } else {
        requiredAmount = 0;
      }
      reservedAmount = Math.max(0, reservedAmount);
      if (reservedAmount < requiredAmount) {
        if (unit.getData().getGameSpecificRules().isPooled(unit, type)) {
          result = new LinkedList<UnitRelation>();
          for (Unit u : unit.getRegion().units()) {
            if (u.getFaction() == unit.getFaction() && u != unit) {
              int givenAmount =
                  Math.min(getFreeAmount(type, u, includeReserved, true), requiredAmount
                      - reservedAmount);
              if (givenAmount > 0) {
                UnitRelation giveRelation =
                    new ItemTransferRelation(unit, u, unit, givenAmount, type, line);
                // new ReserveRelation(unit, u, givenAmount, type, -1, false);
                result.add(giveRelation);
                reservedAmount += givenAmount;
              }
            }
            if (reservedAmount == requiredAmount) {
              break;
            }
          }
        }
      }
      ReserveRelation ownRelation = new ReserveRelation(unit, reservedAmount, type, line);
      if (reservedAmount != requiredAmount && !all) {
        ownRelation.setWarning(
            Resources.get("order.reserve.warning.insufficient", type.toString()),
            OrderSyntaxInspector.OrderSemanticsProblemTypes.GIVE_WARNING.type);
      }
      if (result != null) {
        result.add(ownRelation);
        return result;
      } else
        return Collections.<UnitRelation> singletonList(ownRelation);
    }

    /**
     * Tries to reserve a certain amount of an item. This method tries to get it from the material
     * pool if possible and may produce and register additional relations.
     *
     * @param source
     * @param target
     * @param all
     * @param requiredAmount
     * @param type
     * @param line
     * @param order
     * @return A list of all created relations
     */
    public List<UnitRelation> giveItem(Unit source, Unit target, boolean all, int requiredAmount,
        ItemType type, int line, Order order) {
      List<UnitRelation> result = null;
      int ownAmount = getFreeAmount(type, source, false, !all);
      if (!all) {
        ownAmount = Math.min(requiredAmount, ownAmount);
      } else {
        requiredAmount = 0;
      }
      ownAmount = Math.max(0, ownAmount);

      int reservedAmount = ownAmount;
      if (!all && reservedAmount < requiredAmount) {
        if (source.getData().getGameSpecificRules().isPooled(source, type)) {
          result = new LinkedList<UnitRelation>();
          for (Unit u : source.getRegion().units()) {
            if (u.getFaction() == source.getFaction() && u != source) {
              int givenAmount =
                  Math.min(getFreeAmount(type, u, false, true), requiredAmount - reservedAmount);
              if (givenAmount > 0) {
                UnitRelation giveRelation =
                    new ItemTransferRelation(source, u, target, givenAmount, type, line);
                // new ReserveRelation(unit, u, givenAmount, type, -1, false);
                result.add(giveRelation);
                reservedAmount += givenAmount;
              }
            }
            if (reservedAmount == requiredAmount) {
              break;
            }
          }
        }
      }
      UnitRelation ownRelation =
          new ItemTransferRelation(source, target, ownAmount, type, line, false);
      if (!all && reservedAmount != requiredAmount) {
        ownRelation.setWarning(Resources.get("order.give.warning.insufficient", type.toString()),
            OrderSyntaxInspector.OrderSemanticsProblemTypes.GIVE_WARNING.type);
      }
      if (result != null) {
        result.add(ownRelation);
        return result;
      } else
        return Collections.<UnitRelation> singletonList(ownRelation);
    }

    protected int getFreeAmount(ItemType type, Unit unit, boolean includeReserved,
        boolean includeReceived) {
      Item item = unit.getItem(type);
      int amount = 0;
      if (item != null) {
        amount = item.getAmount();
      }
      if (!includeReserved) {
        for (ReserveRelation itr : unit.getRelations(ReserveRelation.class)) {
          if (itr.source == unit && itr.itemType == type) {
            amount -= itr.amount;
          }
        }
      }
      for (ItemTransferRelation itr : unit.getRelations(ItemTransferRelation.class)) {
        if (itr.itemType == type) {
          if (itr.source == unit) {
            amount -= itr.amount;
          }
          if (itr.target == unit && includeReserved) {
            amount += itr.amount;
          }
        }
      }
      return amount;
    }

    /**
     * Returns all known item types in the HERBS category.
     */
    public Collection<ItemType> getHerbTypes() {
      if (herbTypes == null) {
        herbTypes = new ArrayList<ItemType>(10);
        ItemCategory herbCat = data.getRules().getItemCategory(StringID.create(("HERBS")));
        if (herbCat == null) {
          log.warn("herb category unknown");
        } else {
          for (ItemType type : getData().getRules().getItemTypes()) {
            if (herbCat.equals(type.getCategory())) {
              herbTypes.add(type);
            }
          }
        }
        herbTypes = Collections.unmodifiableCollection(herbTypes);
      }
      return herbTypes;
    }

    private GameData getData() {
      return data;
    }

    public void addReserve(Unit unit, StringID itemID, Integer reserved) {
      if (reserves == null) {
        reserves = new HashMap<Unit, Map<StringID, Integer>>();
      }
      Map<StringID, Integer> map = reserves.get(unit);
      if (map == null) {
        map = new HashMap<StringID, Integer>();
        reserves.put(unit, map);
      }
      map.put(itemID, reserved);
    }

    public Integer getReserve(Unit unit, StringID itemID) {
      if (reserves == null)
        return null;
      Map<StringID, Integer> map = reserves.get(unit);
      if (map == null)
        return null;
      return map.get(itemID);
    }

    public void leave(Unit unit, UnitContainer leftUC) {
      if (unit == null || leftUC == null)
        throw new NullPointerException();
      Unit oldOwner = leftUC.getModifiedOwnerUnit();

      unit.enter(null);

      if (unit.equals(oldOwner)) {
        updateOwner(leftUC, unit);
      }
    }

    private Unit updateOwner(UnitContainer leftUC, Unit lastOwner) {
      Unit newOwner = null;
      Faction lastFaction = lastOwner.getFaction();
      for (Unit u : leftUC.modifiedUnits()) {
        if (Units.isAllied(lastFaction, u.getFaction(), EresseaConstants.A_GUARD)) {
          newOwner = u;
          break;
        } else if (newOwner == null) {
          newOwner = u;
        }
      }
      leftUC.setModifiedOwnerUnit(newOwner);
      return newOwner;
    }

    public void enter(Unit unit, UnitContainer newUC) {
      if (unit == null || newUC == null)
        throw new NullPointerException();

      Unit lastUnit = null;
      for (Unit u : data.getUnits()) {
        if (u.getModifiedUnitContainer() == newUC) {
          lastUnit = u;
        }
      }

      if (lastUnit != unit && lastUnit != null) {
        orders.insert(unit, lastUnit);
      }
      unit.enter(newUC);
      // newUC.enter(unit);

      if (newUC.getModifiedOwnerUnit() == null) {
        newUC.setModifiedOwnerUnit(unit);
      }
    }
  }

}
