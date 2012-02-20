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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.completion.OrderParser;
import magellan.library.relation.AttackRelation;
import magellan.library.relation.CombatStatusRelation;
import magellan.library.relation.ControlRelation;
import magellan.library.relation.EnterRelation;
import magellan.library.relation.FollowUnitRelation;
import magellan.library.relation.GuardRegionRelation;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.LeaveRelation;
import magellan.library.relation.MaintenanceRelation;
import magellan.library.relation.MovementRelation;
import magellan.library.relation.PersonTransferRelation;
import magellan.library.relation.RecruitmentRelation;
import magellan.library.relation.RenameNamedRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.TeachRelation;
import magellan.library.relation.TransferRelation;
import magellan.library.relation.TransportRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.relation.UnitTransferRelation;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.tasks.MaintenanceInspector;
import magellan.library.tasks.OrderSyntaxInspector;
import magellan.library.tasks.OrderSyntaxInspector.OrderSemanticsProblemTypes;
import magellan.library.tasks.Problem;
import magellan.library.utils.Direction;
import magellan.library.utils.Locales;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Responsible for creating relations stemming from unit orders.
 */
public class EresseaRelationFactory implements RelationFactory {

  private static final Logger log = Logger.getInstance(EresseaRelationFactory.class);
  private Rules rules;

  /** Order priority */
  public static final int P_BENENNE = 301;
  /** Order priority */
  public static final int P_KAEMPFE = 302;
  /** Order priority */
  public static final int P_BETRETE = 701;
  /** Order priority */
  public static final int P_VERLASSE = 801;
  /** Order priority */
  public static final int P_ATTACKIERE = 901;
  /** Order priority */
  public static final int P_RESERVIERE = 1001;
  /** Order priority */
  public static final int P_GIB = 1401;
  /** Order priority */
  public static final int P_REKRUTIERE = 1501;
  /** Order priority */
  public static final int P_LEHRE = 1801;
  /** Order priority */
  public static final int P_FAHRE = 2398;
  /** Order priority */
  public static final int P_TRANSPORTIERE = 2399;
  /** Order priority */
  public static final int P_NACH = 2400;
  /** Order priority */
  public static final int P_BEWACHE = 2501;

  /** Order priority */
  public static final int P_MAINTENANCE = 3001;
  /** Order priority */
  public static final int P_BUILDING_MAINTENANCE = 1601;

  protected EresseaRelationFactory(Rules rules) {
    this.rules = rules;
  }

  private static final int REFRESHRELATIONS_ALL = -2;

  /**
   * Creates a list of com.eressea.util.Relation objects for a unit starting at order position
   * <tt>from</tt>. Note: The parameter <code>from</code> is ignored by this implementation!
   * 
   * @param u The unit
   * @param from The line of the <code>unit</code>'s orders where to start. Must be > 0
   * @return A List of Relations for this unit
   */
  // TODO (stm 2007-02-24) Should we remove the parameter from from the interface? It violates the
  // unit execution order but it might be useful for other games.
  public List<UnitRelation> createRelations(Unit u, int from) {
    processOrders(u.getRegion());
    return null;
    // return createRelations(u, u.getOrders2(), from);
  }

  /**
   * Creates a list of com.eressea.util.Relation objects for a unit starting at order position
   * <tt>from</tt> using <code>orders</code>.
   * 
   * @param u The unit
   * @param orders Use these orders instead of the unit's orders
   * @return A List of Relations for this unit
   */
  public List<UnitRelation> createRelations(Unit u, Orders orders) {
    return createRelations(u, orders, 0);
  }

  public void createRelations(Region region) {
    processOrders(region);
  }

  static class GDEntry {

    public GameData data;
    public OrderParser parser;
  }

  WeakReference<GDEntry> lastData = new WeakReference<GDEntry>(null);

  private OrderParser getParser(GameData data) {
    // we try to reduce the number of instances by caching the last one
    // if the last one was created for the same data, we return that one
    GDEntry last = lastData.get();
    if (last != null && last.data == data)
      return last.parser;

    last = new GDEntry();
    last.data = data;
    last.parser = data.getGameSpecificStuff().getOrderParser(data);
    lastData = new WeakReference<GDEntry>(last);
    return last.parser;
  }

  /**
   * Creates a list of com.eressea.util.Relation objects for a unit using <code>orders</code>.
   * starting at order position <tt>from</tt>. Note: The parameter <code>from</code> is ignored by
   * this implementation!
   * 
   * @param u The unit
   * @param orders Use these orders which may not be the unit's orders
   * @param from The line of the <code>unit</code>'s orders where to start. Must be > 0
   * @return A List of Relations for this unit
   */
  protected List<UnitRelation> createRelations(Unit u, Orders orders, int from) {
    from = 0;
    // NOTE: parameter from is ignored!
    ArrayList<UnitRelation> relations = new ArrayList<UnitRelation>(3);

    GameData data = u.getData();
    Map<ID, Item> modItems = null; // needed to track changes in the items for
    // GIB orders
    int modPersons = u.getPersons();

    // clone u unit's items
    modItems = new Hashtable<ID, Item>();
    for (Item i : u.getItems()) {
      modItems.put(i.getItemType().getID(), new Item(i.getItemType(), i.getAmount()));
    }

    // 4. parse the orders and create new relations
    OrderParser parser = getParser(data);

    Locale locale = u.getFaction().getLocale();
    if (locale == null) {
      locale = Locales.getOrderLocale();
    }

    // TODO (stm): sort order according to execution order and process them in
    // that order.
    // In that case, the parameter from should be ignored entirely

    // process RESERVE orders first
    // Collections.sort(ordersCopy, new EresseaOrderComparator(null));
    createReserveRelations(u, orders, from, parser, modItems, relations);

    // process all other orders
    int line = 0;
    boolean tempOrders = false;
    line = 0;

    // indicates a possible problem with GIVE CONTROL orders if > 1
    int controlWarning = 0;
    orderLoop: for (Order order : orders) {

      line++; // keep track of line

      // from is ignored
      // if(line < from) {
      // continue;
      // }

      if (!order.isValid() || order.isEmpty()) {
        continue;
      }

      List<OrderToken> tokens = order.getTokens();

      // TODO (stm) use a radix tree for faster matching...
      if ((tokens.get(0)).ttype == OrderToken.TT_COMMENT) {
        continue;
      }

      // boolean persistent = false;
      // if (order.isPersistent()) {
      // persistent = true;
      // }

      // begin of temp unit
      if (orders.isToken(order, 0, EresseaConstants.O_MAKE)
          && (orders.isToken(order, 1, EresseaConstants.O_TEMP))) {
        tempOrders = true;

        continue;
      }

      if (tempOrders) {
        // end of temp unit
        if ((orders.isToken(order, 0, EresseaConstants.O_END))) {
          tempOrders = false;
          continue;
        }
      }

      // movement relation
      if (orders.isToken(order, 0, EresseaConstants.O_MOVE)
          || orders.isToken(order, 0, EresseaConstants.O_ROUTE)) {
        List<CoordinateID> modifiedMovement = new ArrayList<CoordinateID>(2);

        // dissect the order into pieces to detect which way the unit
        // is taking
        Region currentRegion = u.getRegion();
        CoordinateID currentCoord = u.getRegion().getCoordinate();

        modifiedMovement.add(currentCoord);

        for (Iterator<OrderToken> iter2 = tokens.listIterator(1); iter2.hasNext();) {
          OrderToken token = iter2.next();
          Direction movement = Direction.toDirection(token.getText(), locale);

          // try to get the next region; take "wrap around" regions into account
          CoordinateID nextCoord = currentCoord;
          Region nextRegion = currentRegion;
          if (movement != Direction.INVALID) {
            nextCoord = currentCoord.translate(movement.toCoordinate());
            if (currentRegion != null) {
              nextRegion = currentRegion.getNeighbors().get(movement);
            }
            if (nextRegion == null) {
              nextRegion = data.getRegion(nextCoord);
            } else {
              nextCoord = nextRegion.getCoordinate();
            }
          }

          modifiedMovement.add(nextCoord);
          currentCoord = nextCoord;
          currentRegion = nextRegion;
        }

        relations.add(new MovementRelation(u, modifiedMovement, line));

        // check whether the unit leaves a container
        UnitContainer leftUC = u.getBuilding();

        if (leftUC == null) {
          leftUC = u.getShip();
          if (leftUC.getModifiedOwnerUnit() == u) {
            leftUC = null;
          }
        }

        if (leftUC != null) {
          controlWarning = 2;
          LeaveRelation rel = new LeaveRelation(u, leftUC, line);
          relations.add(rel);
        }

        if (leftUC != null) {
          LeaveRelation rel = new LeaveRelation(u, leftUC, line);
          relations.add(rel);

          if (leftUC.getModifiedOwnerUnit() == u) {
            for (Unit otherUnit : leftUC.modifiedUnits()) {
              if (otherUnit != u) {
                ControlRelation cRel = new ControlRelation(u, otherUnit, line);
                cRel.setWarning("???", OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
                relations.add(cRel);
                break;
              }
            }
          }
        }

        continue;
      }

      // enter relation
      if (orders.isToken(order, 0, EresseaConstants.O_ENTER)) {
        UnitContainer uc = null;

        if (orders.isToken(order, 1, EresseaConstants.O_CASTLE)) {
          uc = u.getRegion().getBuilding(orders.getEntityID(order, 2));
        } else if (orders.isToken(order, 1, EresseaConstants.O_SHIP)) {
          uc = u.getRegion().getShip(orders.getEntityID(order, 2));
        }

        if (uc != null) {
          EnterRelation rel = new EnterRelation(u, uc, line);
          relations.add(rel);
        } else {
          EresseaRelationFactory.log.debug("Unit.refreshRelations(): cannot find target in order "
              + order);
        }

        // check whether the unit leaves a container
        UnitContainer leftUC = u.getBuilding();

        if (leftUC == null) {
          leftUC = u.getShip();
        }

        if (leftUC != null) {
          controlWarning = 2;
          LeaveRelation rel = new LeaveRelation(u, leftUC, line);
          relations.add(rel);
        }

        continue;
      }

      // income relation WORK
      if (orders.isToken(order, 0, EresseaConstants.O_WORK)) {
        // TODO!
        continue;
      }

      // income relation ENTERTAIN
      if (orders.isToken(order, 0, EresseaConstants.O_ENTERTAIN)) {
        // TODO!
        continue;
      }

      // income relation TAX
      if (orders.isToken(order, 0, EresseaConstants.O_TAX)) {
        // TODO!
        continue;
      }

      // guard relation
      if (orders.isToken(order, 0, EresseaConstants.O_GUARD)) {
        GuardRegionRelation guardRegionRelation =
            new GuardRegionRelation(u, GuardRegionRelation.GUARD, line);
        if (tokens.size() > 1) {
          if (orders.isToken(order, 1, EresseaConstants.O_NOT)) {
            guardRegionRelation.guard = GuardRegionRelation.GUARD_NOT;
          }
        }
        relations.add(guardRegionRelation);
      }

      // transport relation
      if (orders.isToken(order, 0, EresseaConstants.O_CARRY)) {
        Unit target = u.getRegion().getUnit(orders.getUnitID(order, 1));

        if ((target == null) || u.equals(target)) {
          continue;
        }

        TransportRelation rel = new TransportRelation(u, u, target, line);
        relations.add(rel);

        continue;
      }

      // transfer relation
      if (orders.isToken(order, 0, EresseaConstants.O_GIVE)
          || (orders.isToken(order, 0, EresseaConstants.O_SUPPLY))) {
        // GIB 0|<enr> (ALLES|EINHEIT|KRaeUTER|KOMMANDO|((([JE] <amount>)|ALLES)
        // (SILBER|PERSONEN|<gegenstand>)))
        final int unitIndex = 1;
        int amountIndex = 2;
        int itemIndex = 3;
        boolean hasEach = false;

        // OrderToken t = tokens.get(unitIndex);
        // Unit target = getTargetUnit(t, u.getRegion());
        Unit target = u.getRegion().getUnit(orders.getUnitID(order, unitIndex));

        if (target != null) {
          if (!target.equals(u)) {
            TransferRelation rel = new TransferRelation(u, target, -1, line);

            // t = tokens.get(amountIndex);
            if (orders.isToken(order, amountIndex, EresseaConstants.O_HERBS)) {
              // if the 'amount' is HERBS then create relations for all herbs
              // the unit carries
              ItemCategory herbCategory = data.rules.getItemCategory(StringID.create(("HERBS")));

              if ((herbCategory != null)) {
                for (Item i : modItems.values()) {
                  if (herbCategory.equals(i.getItemType().getCategory())) {
                    TransferRelation r =
                        new ItemTransferRelation(u, target, i.getAmount(), i.getItemType(), line);
                    i.setAmount(0);
                    relations.add(r);
                  }
                }
              }

            } else if (orders.isToken(order, amountIndex, EresseaConstants.O_CONTROL)) {
              controlWarning++;
              UnitRelation r = new ControlRelation(u, target, line);
              relations.add(r);
            } else if (orders.isToken(order, amountIndex, EresseaConstants.O_UNIT)) {
              UnitRelation r = new UnitTransferRelation(u, target, u.getRace(), line);
              relations.add(r);
            } else {
              boolean parseItem = false;
              // order is GIVE bla [EACH] <amount> <something>
              if (orders.isToken(order, amountIndex, EresseaConstants.O_ALL)) {
                // -2 encodes that everything is to be transferred
                rel.amount = EresseaRelationFactory.REFRESHRELATIONS_ALL;
                parseItem = true;
              } else {
                // GIVE bla EACH ALL does not make a lot of sense
                if (orders.isToken(order, amountIndex, EresseaConstants.O_EACH)) {
                  hasEach = true;
                  ++amountIndex;
                  ++itemIndex;
                }
                if (order.getToken(amountIndex).ttype == OrderToken.TT_NUMBER) {
                  // u is a plain number
                  rel.amount = orders.getNumber(order, amountIndex);
                  parseItem = true;
                }
              }
              if (parseItem) {
                if (rel.amount == -1) {
                  // -1 means that the amount could not determined we could issue a warning to the
                  // user here
                  EresseaRelationFactory.log
                      .debug("EresseaRelationFactory.createRelations(Unit): cannot parse amount in order "
                          + order);
                } else {
                  if (order.size() > 4) {
                    // now the order must look something like:
                    // GIVE <unit id> <amount> <object><EOC>
                    if (order.getToken(itemIndex).ttype == OrderToken.TT_OPENING_QUOTE) {
                      ++itemIndex;
                    }
                    OrderToken itemToken = order.getToken(itemIndex);

                    // // strip quotes not needed
                    // String itemName = EresseaRelationFactory.stripQuotes(itemToken.getText());
                    String itemName = itemToken.getText();
                    if (orders.isToken(order, amountIndex, EresseaConstants.O_MEN)) {
                      // if the specified amount was 'all':
                      if (rel.amount == EresseaRelationFactory.REFRESHRELATIONS_ALL) {
                        rel.amount = modPersons;
                      } else {
                        // if not, only transfer the minimum amount the unit has
                        rel.amount = Math.min(modPersons, rel.amount);
                      }

                      rel = new PersonTransferRelation(u, target, rel.amount, u.getRace(), line);

                      // update the modified person amount
                      modPersons = Math.max(0, modPersons - rel.amount);
                    } else if (itemName.length() > 0) {
                      // TODO(pavkovic): korrigieren!!! Hier soll eigentlich
                      // das Item über den
                      // übersetzten Namen gefunden werden!!!
                      ItemType iType = data.rules.getItemType(itemName);
                      // ItemType iType =
                      // data.rules.getItemType(StringID.create(itemName));

                      if (iType != null) {
                        // get the item from the list of modified items
                        Item i = modItems.get(iType.getID());

                        if (i == null) {
                          // item unknown
                          rel.amount = 0;
                        } else {
                          // if the specified amount is 'all', convert u
                          // to a decent number
                          if (rel.amount == EresseaRelationFactory.REFRESHRELATIONS_ALL) {
                            rel.amount = i.getAmount();
                            // GIVE ... EACH ALL is invalid
                            if (hasEach)
                              throw new AssertionError("GIB ... JE ALLES");
                          } else {
                            // if not, only transfer the minimum amount
                            // the unit has
                            if (i.getAmount() < rel.amount) {
                              rel.setWarning("???", OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
                            }
                            // FIXME getPersons() or getModifiedPersons()? this is only correct if
                            // units are parsed in order; also, REKRUTIERE is after GIBs
                            rel.amount =
                                Math.min(i.getAmount(), rel.amount
                                    * (hasEach ? target.getModifiedPersons() : 1));
                            if (hasEach && target.getPersonTransferRelations().size() != 0) {
                              // so we add a warning here, to indicate that we're not sure
                              rel.setWarning("???", OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
                            }
                          }
                        }

                        // create the new transfer relation
                        Problem problem = rel.problem;
                        rel = new ItemTransferRelation(u, target, rel.amount, iType, line);
                        rel.problem = problem;

                        // update the modified item amount
                        if (i != null) {
                          i.setAmount(Math.max(0, i.getAmount() - rel.amount));
                        }
                      } else {
                        rel = null;
                      }
                    } else {
                      rel = null;
                    }

                    // let's see whether there is a valid relation to add
                    if (rel != null) {
                      relations.add(rel);
                    }
                  } else {
                    // in u case the order looks like:
                    // GIVE <unit id> ALLES<EOC>
                    if (rel.amount == EresseaRelationFactory.REFRESHRELATIONS_ALL) {

                      for (Item i : modItems.values()) {
                        TransferRelation r =
                            new ItemTransferRelation(u, target, i.getAmount(), i.getItemType(),
                                line);
                        i.setAmount(0);
                        relations.add(r);
                      }
                    }
                  }
                }
              }
            }
          } else {
            // relation to myself? you're sick
          }
        }

        continue;
      }

      // recruitment relation
      if (orders.isToken(order, 0, EresseaConstants.O_RECRUIT)) {
        OrderToken t = tokens.get(1);

        if (t.ttype == OrderToken.TT_NUMBER) {
          Race race = (u instanceof TempUnit) ? ((TempUnit) u).getParent().getRace() : u.getRace();
          if (tokens.size() > 3) {
            race = getRace(tokens.get(2).getText());
            if (u.getPersons() == 0 || u.getRace().equals(race)) {
              int amount = Integer.parseInt(t.getText());

              RecruitmentRelation rel;
              if ((race != null) && (race.getRecruitmentCosts() > 0)) {
                int cost = amount * race.getRecruitmentCosts();
                rel = new RecruitmentRelation(u, amount, cost, race, line);
              } else {
                rel = new RecruitmentRelation(u, amount, 0, race, line);
                rel.setWarning("???", OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
              }
              relations.add(rel);
            } else {
              race = u.getRace();
            }
          } else {
            int amount = Integer.parseInt(t.getText());
            RecruitmentRelation rel;
            if (u.getRace() != null && u.getRace().getRecruitmentCosts() > 0) {
              rel =
                  new RecruitmentRelation(u, amount, amount * u.getRace().getRecruitmentCosts(),
                      line);
            } else {
              rel = new RecruitmentRelation(u, amount, 0, line);
              rel.setWarning("???", OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
            }
            relations.add(rel);
          }
          if (u instanceof TempUnit) {
            ((TempUnit) u).setTempRace(race);
          }
        } else {
          EresseaRelationFactory.log.fine("Unit.updateRelations(): invalid amount in order "
              + order);
        }

        continue;
      }

      // leave relation
      if (orders.isToken(order, 0, EresseaConstants.O_LEAVE)) {
        UnitContainer uc = u.getBuilding();

        if (uc == null) {
          uc = u.getShip();
        }

        if (uc != null) {
          controlWarning = 2;
          LeaveRelation rel = new LeaveRelation(u, uc, line);
          relations.add(rel);
        } else {
          EresseaRelationFactory.log.fine("Unit.refreshRelations(): unit " + u
              + " cannot leave a ship or a building as indicated by order " + order);
        }

        continue;
      }

      // teach relation
      if (orders.isToken(order, 0, EresseaConstants.O_TEACH)) {

        for (int tokCtr = 1; tokCtr + 1 < order.size(); ++tokCtr) {
          Unit pupil = u.getRegion().getUnit(orders.getUnitID(order, tokCtr));

          if (pupil != null) {
            if (!u.equals(pupil)) {
              TeachRelation rel = new TeachRelation(u, pupil, line);
              relations.add(rel);
            }
            // else can't teach myself
          }
          // else pupil not found
        }
        continue orderLoop;
      }

      // attack relation
      if (orders.isToken(order, 0, EresseaConstants.O_ATTACK)) {
        if (tokens.size() > 1) {
          Unit enemy = u.getRegion().getUnit(orders.getUnitID(order, 1));

          if (enemy != null) {
            AttackRelation rel = new AttackRelation(u, enemy, line);
            relations.add(rel);
          }
        }
      }

      // battle status relation
      if (orders.isToken(order, 0, EresseaConstants.O_COMBAT)) {
        CombatStatusRelation combatStatusRelation = null;
        if (tokens.size() > 1) {
          // additional info for battle status
          // lets detect new status
          // OrderToken newStatusToken = tokens.get(1+persistant);
          if (orders.isToken(order, 1, EresseaConstants.O_COMBAT_AGGRESSIVE)) {
            combatStatusRelation =
                new CombatStatusRelation(u, EresseaConstants.CS_AGGRESSIVE, line);
          }
          // FIXME KÄMPFE VORNE deprecated
          if (orders.isToken(order, 1, EresseaConstants.O_COMBAT_FRONT)) {
            combatStatusRelation = new CombatStatusRelation(u, EresseaConstants.CS_FRONT, line);
          }
          if (orders.isToken(order, 1, EresseaConstants.O_COMBAT_REAR)) {
            combatStatusRelation = new CombatStatusRelation(u, EresseaConstants.CS_REAR, line);
          }
          if (orders.isToken(order, 1, EresseaConstants.O_COMBAT_DEFENSIVE)) {
            combatStatusRelation = new CombatStatusRelation(u, EresseaConstants.CS_DEFENSIVE, line);
          }
          if (orders.isToken(order, 1, EresseaConstants.O_COMBAT_NOT)) {
            combatStatusRelation = new CombatStatusRelation(u, EresseaConstants.CS_NOT, line);
          }
          if (orders.isToken(order, 1, EresseaConstants.O_COMBAT_FLEE)) {
            combatStatusRelation = new CombatStatusRelation(u, EresseaConstants.CS_FLEE, line);
          }
          if (order.getToken(1).getText().length() == 0) {
            // just nothing means "normal" = front
            combatStatusRelation = new CombatStatusRelation(u, EresseaConstants.CS_FRONT, line);
          }
          // check additional the unaided order
          if (orders.isToken(order, 1, EresseaConstants.O_COMBAT_HELP)) {
            // "Kämpfe helfe" would change unaided from true to false
            combatStatusRelation = new CombatStatusRelation(u, false, line);
            if (tokens.size() > 2) {
              // check if we have the NICHT
              if (orders.isToken(order, 2, EresseaConstants.O_COMBAT_NOT)) {
                // "Kämpfe helfe nicht" would change unaided from false to true
                combatStatusRelation = new CombatStatusRelation(u, true, line);
              }
            }
          }
        } else {
          // no more info means status "normal" = 1
          combatStatusRelation = new CombatStatusRelation(u, EresseaConstants.CS_FRONT, line);
        }
        if (combatStatusRelation != null) {
          relations.add(combatStatusRelation);
        }
      }

      // name relation
      // TODO: Do it right
      if (orders.isToken(order, 0, EresseaConstants.O_NAME)) {
        if (tokens.size() > 2) {
          // OrderToken whatToken = tokens.get(1+persistant);
          OrderToken nameToken = null;
          for (OrderToken token : tokens)
            if (token.ttype == OrderToken.TT_STRING) {
              nameToken = token;
            }
          if (nameToken != null) {

            if (orders.isToken(order, 1, EresseaConstants.O_UNIT)) {
              relations.addAll(createRenameUnitRelation(u, nameToken, line));
            } else {
              if (orders.isToken(order, 2, EresseaConstants.O_CASTLE)) {
                relations
                    .add(new RenameNamedRelation(u, u.getBuilding(), nameToken.getText(), line));
              } else if (orders.isToken(order, 2, EresseaConstants.O_FACTION)) {
                relations
                    .add(new RenameNamedRelation(u, u.getFaction(), nameToken.getText(), line));
              } else if (orders.isToken(order, 2, EresseaConstants.O_REGION)) {
                if (u.getRegion().getOwnerUnit() == u) {
                  relations
                      .add(new RenameNamedRelation(u, u.getRegion(), nameToken.getText(), line));
                }
              } else if (orders.isToken(order, 2, EresseaConstants.O_SHIP)) {
                relations.add(new RenameNamedRelation(u, u.getShip(), nameToken.getText(), line));
              }
            }

            if (orders.isToken(order, 1, EresseaConstants.O_FOREIGN)) {
              // rels.addAll(createRenameForeignUnitContainerRelation(u,
              // (OrderToken) tokens.get(2), (OrderToken) tokens.get(3)));
              // retVal = readBenenneFremdes(t);
            }
          }
        }
      }
    }

    // set the warning flag of ControlRelations if unit is leaving
    if (controlWarning > 1) {
      for (int i = 0; i < relations.size(); ++i) {
        UnitRelation r = relations.get(i);
        if (r instanceof ControlRelation) {
          relations.get(i).setWarning("???", OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
        }
      }
    }

    // Collections.sort(relations, new OrderPreferenceComparator());

    return relations;
  }

  private Race getRace(String content) {
    for (Race r : rules.getRaces()) {
      if (r.getRecruitmentName() != null
          && content.equalsIgnoreCase(Resources.getOrderTranslation("race."
              + r.getRecruitmentName())))
        return r;
    }
    return null;
  }

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

  /**
   * Creates ReserveRelations for all reserve orders of a unit
   * 
   * @param u The unit
   * @param orders A copy of the unit's orders.
   * @param from Ignored!
   * @param parser An order parser which has been initialized with the game data
   * @param modItems The Map of the unit's modified items
   * @param rels The newly created {@link ReserveRelation}s are inserted into this list
   */
  protected void createReserveRelations(Unit u, Orders orders, int from, OrderParser parser,
      Map<ID, Item> modItems, List<UnitRelation> rels) {
    // parameter from is ignored because it violates execution order
    from = 0;

    int line = 0;
    for (Order order : orders) {
      line++; // keep track of line

      // parameter from is ignored because it violates execution order
      // if(line < from) {
      // continue;
      // }

      if (!order.isValid() || order.isEmpty()) {
        continue;
      }

      if (order.getToken(0).ttype == OrderToken.TT_COMMENT) {
        continue;
      }

      if (order.getToken(0).ttype == OrderToken.TT_PERSIST) {
        log.error("broken assertion");
        continue;
      }

      if (orders.isToken(order, 0, EresseaConstants.O_RESERVE)
      // tokens.get(orderIndex).equalsToken(EresseaRelationFactory
      // .getOrder(EresseaConstants.O_RESERVE))
      ) {
        // RESERVE [EACH] <amount> <object><EOC>
        // RESERVIERE [JE] <amount> <object><EOC>
        // RESERVIERE ALLES <object><EOC>
        ReserveRelation rel = createReserveRelation(u, line, order);

        // let's see whether there is a valid relation to add
        if (rel != null) {
          Item i = modItems.get(rel.itemType.getID());
          if (i != null) {
            if (i.getAmount() < rel.amount) {
              rel.setWarning("???", OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
              rel.amount = i.getAmount();
            }
            i.setAmount(i.getAmount() - rel.amount);
          } else {
            rel.amount = 0;
            rel.setWarning("???", OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
          }
          rels.add(rel);
        }
      }
    }

  }

  private List<UnitRelation> createRenameUnitRelation(Unit unit, OrderToken token, int line) {
    return Collections.singletonList((UnitRelation) new RenameNamedRelation(unit, unit,
        EresseaRelationFactory.stripQuotes(token.getText()), line));
  }

  private List<UnitRelation> createRenameUnitContainerRelation(Unit unit,
      OrderToken containerToken, OrderToken name, int line) {

    return Collections.emptyList();
  }

  // private Unit getTargetUnit(OrderToken t, Region r) {
  // try {
  // UnitID id = UnitID.createUnitID(t.getText(), r.getData().base);
  //
  // return r.getUnit(id);
  // } catch (NumberFormatException e) {
  // EresseaRelationFactory.log.debug("Unit.getTargetUnit(): cannot parse unit id \""
  // + t.getText() + "\"!");
  // }
  //
  // return null;
  // }

  /**
   * Removes quotes at the beginning and at the end of str or replaces tilde characters with spaces.
   * 
   * @param str
   */
  private static String stripQuotes(String str) {
    if (str == null)
      return null;

    int strLen = str.length();

    if ((strLen >= 2) && (str.charAt(0) == '"') && (str.charAt(strLen - 1) == '"'))
      return str.substring(1, strLen - 1);
    else
      return str.replace('~', ' ');
  }

  private static String getOrder(String key) {
    return Resources.getOrderTranslation(key);
  }

  protected ReserveRelation createReserveRelation(Unit source, int line, Order order) {
    ReserveOrder rOrder = (ReserveOrder) order;
    ItemType type = source.getData().getRules().getItemType(rOrder.itemID);
    ReserveRelation rel = new ReserveRelation(source, 0, type, line);

    if (rel.itemType != null) {
      // get the item from the list of modified items
      Item i = source.getModifiedItem(rel.itemType);

      if (i == null) {
        // item unknown
        rel.amount = 0;
        rel.setWarning("???", OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
      } else {
        if (rOrder.amount == Order.ALL) {
          // if the specified amount is 'all', convert u to a decent number
          // TODO how exactly does RESERVIERE ALLES <item> work??
          rel.amount = i.getAmount();
          rel.setWarning("???", OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
        } else {
          // // if not, only transfer the minimum amount the unit has
          // TODO (stm) should this be persons or modified persons?
          rel.amount = rOrder.amount * (rOrder.each ? source.getModifiedPersons() : 1);
          if (i.getAmount() < rel.amount) {
            rel.setWarning("???", OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
          }
          rel.amount = Math.min(i.getAmount(), rel.amount);
        }

      }
    }
    return rel;
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

  public void processOrders(Region r) {
    GameData data = r.getData();

    // count orders
    int count = 0;
    for (Unit u : r.units()) {
      count += u.getOrders2().size();
      // for (Order o : u.getOrders2()) {
      // // FIXME create TEMP unit!!
      // // if (u.getOrders2().isToken(o, 0, EresseaConstants.O_MAKE)
      // // && (u.getOrders2().isToken(o, 1, EresseaConstants.O_TEMP))) {
      // // } else {
      // count++;
      // // }
      // }
    }
    // use array to save memory while sorting, because Collections.sort() makes a copy
    OrderInfo[] orders = new OrderInfo[count];

    count = 0;
    for (Unit u : r.units()) {
      int line = 0;
      for (Order o : u.getOrders2()) {
        // FIXME create TEMP unit!!
        // if (u.getOrders2().isToken(o, 0, EresseaConstants.O_MAKE)
        // && (u.getOrders2().isToken(o, 1, EresseaConstants.O_TEMP))) {
        // } else {
        orders[count++] = new OrderInfo(o, getPriority(o), u, ++line);
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

    // Arrays.sort(orders, getOrderComparator());
    Arrays.sort(orders);
    EresseaExecutionState state = new EresseaExecutionState(r.getData());
    boolean maintenancePaid = false, buildingsPaid = false;
    for (OrderInfo o : orders) {
      if (!maintenancePaid && o.priority > getMaintenancePriority()) {
        updateMaintenance(r, state);
        maintenancePaid = true;
      }
      if (!buildingsPaid && o.priority > getBuildingMaintenancePriority()) {
        updateBuildingMaintenance(r, state);
        buildingsPaid = true;
      }
      o.order.execute(state, data, o.unit, o.line);
    }
    if (!maintenancePaid) {
      updateMaintenance(r, state);
      maintenancePaid = true;
    }
    if (!buildingsPaid) {
      updateBuildingMaintenance(r, state);
      buildingsPaid = true;
    }

    postProcess(r);
  }

  private int getBuildingMaintenancePriority() {
    return P_BUILDING_MAINTENANCE;
  }

  private int getMaintenancePriority() {
    return P_MAINTENANCE;
  }

  protected void postProcess(Region r) {
    for (Unit u : r.units()) {
      checkTransport(u);
      // updateSilver(u);
    }
  }

  private void updateBuildingMaintenance(Region r, EresseaExecutionState state) {
    // HIGHTODO implement
    for (Building b : r.buildings()) {
      if (b.getEffects() != null) {
        for (String eff : b.getEffects()) {
          if (eff.startsWith("Der Zahn der Zeit kann diesen Mauern nichts anhaben.")
              || eff.startsWith("Time cannot touch these walls.")) {
            continue;
          }
        }
      }

      Unit owner = b.getModifiedOwnerUnit();
      if (owner != null) {
        for (Item i : b.getBuildingType().getMaintenanceItems()) {
          // List<UnitRelation> relations =
          // state.reserveItem(i.getItemType(), false, true, i.getAmount(), owner, -1, null);
          List<UnitRelation> relations =
              state.acquireItem(owner, i.getItemType(), i.getAmount(), false, true, true, -1, null);
          MaintenanceRelation mRel =
              new MaintenanceRelation(owner, b, i.getAmount(), i.getItemType(), -1, false);
          for (UnitRelation rel : relations) {
            if (rel instanceof ReserveRelation) {
              // mRel.setReserve((ReserveRelation)rel);
              mRel.costs = ((ReserveRelation) rel).amount;
            } else {
              rel.add();
            }
            if (rel.problem != null) {
              mRel.warning = true;
              mRel.setWarning(Resources.get("order.maintenance.warning.silver"),
                  MaintenanceInspector.MaintenanceProblemTypes.BUILDINGMAINTENANCE.type);
              // mRel.setWarning(Resources.get("order.maintenance.warning.silver"),
              // OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
            }
          }
          mRel.add();
        }
      }
    }
  }

  private void updateMaintenance(Region r, EresseaExecutionState state) {
    GameData data = r.getData();
    MovementEvaluator evaluator = r.getData().getGameSpecificStuff().getMovementEvaluator();
    for (Unit u : r.units()) {
      List<CoordinateID> movement = evaluator.getModifiedMovement(u);

      if (movement.size() == 0) {
        movement = evaluator.getPassiveMovement(u);
      }

      CoordinateID destination;
      if (movement.size() == 0) {
        destination = u.getRegion().getCoordinate();
      } else {
        destination = evaluator.getDestination(u, movement);
      }
      CoordinateID oldDestination = u.getNewRegion();
      if (destination != oldDestination) {
        Region newRegion;
        if (oldDestination != null) {
          newRegion = data.getRegion(oldDestination);
          if (newRegion != null) {
            newRegion.removeMaintenance(u);
          }
        }
        u.setNewRegion(destination);
        newRegion = data.getRegion(destination);
        if (newRegion != null && newRegion != u.getRegion()) {
          newRegion.addMaintenance(u);
        }
      }
    }
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
              cRel.setWarning(Resources.get("order.leave.warning.control"),
                  OrderSemanticsProblemTypes.SEMANTIC_ERROR.type);
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
    else if (order instanceof GiveOrder)
      return P_GIB;
    else if (order instanceof RecruitmentOrder)
      return P_REKRUTIERE;
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

    /**
     * @param data
     */
    public EresseaExecutionState(GameData data) {
      this.data = data;
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
     * @param getReserved <code>true</code> if the amount of item that the unit has reveived counts
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
            }
            if (reservedAmount >= requiredAmount) {
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
      // for (ItemTransferRelation itr : unit.getRelations(ItemTransferRelation.class)) {
      // if (itr.itemType == type) {
      // // remove what we gave
      // if (itr.source == unit) {
      // amount -= itr.amount;
      // }
      // if (itr.target == unit && includeReserved) {
      // amount += itr.amount;
      // }
      // }
      // }
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
     * @return
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
        ItemCategory herbCat = data.rules.getItemCategory(StringID.create(("HERBS")));
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

  }

}
