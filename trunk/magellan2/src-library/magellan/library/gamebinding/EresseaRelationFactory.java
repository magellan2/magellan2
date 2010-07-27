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

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Item;
import magellan.library.Region;
import magellan.library.Rules;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.completion.OrderParser;
import magellan.library.relation.AttackRelation;
import magellan.library.relation.CombatStatusRelation;
import magellan.library.relation.ControlRelation;
import magellan.library.relation.EnterRelation;
import magellan.library.relation.GuardRegionRelation;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.LeaveRelation;
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
import magellan.library.utils.Direction;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class EresseaRelationFactory implements RelationFactory {
  private static final Logger log = Logger.getInstance(EresseaRelationFactory.class);
  private Rules rules;

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
  // FIXME (stm 2007-02-24) Should we remove the parameter from from the
  // interface? It violates
  // the unit execution order but it might be useful for other games.
  public List<UnitRelation> createRelations(Unit u, int from) {
    return createRelations(u, u.getOrders(), from);
  }

  /**
   * Creates a list of com.eressea.util.Relation objects for a unit starting at order position
   * <tt>from</tt> using <code>orders</code>.
   * 
   * @param u The unit
   * @param orders Use these orders instead of the unit's orders
   * @return A List of Relations for this unit
   */
  public List<UnitRelation> createRelations(Unit u, List<String> orders) {
    return createRelations(u, orders, 0);
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
  private List<UnitRelation> createRelations(Unit u, List<String> orders, int from) {
    from = 0;
    // NOTE: parameter from is ignored!
    ArrayList<UnitRelation> relations = new ArrayList<UnitRelation>(3);

    GameData data = u.getRegion().getData();
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

    // TODO (stm): sort order according to execution order and process them in
    // that order.
    // In that case, the parameter from should be ignored entirely

    // process RESERVE orders first
    // Collections.sort(ordersCopy, new EresseaOrderComparator(null));
    EresseaRelationFactory.createReserveRelations(u, orders, from, parser, modItems, relations);

    // process all other orders
    int line = 0;
    boolean tempOrders = false;
    line = 0;

    // indicates a possible problem with GIVE CONTROL orders if > 1
    int controlWarning = 0;
    for (String order : orders) {

      line++; // keep track of line

      // from is ignored
      // if(line < from) {
      // continue;
      // }

      // UnitRelation relation = parser.read(new StringReader(order));
      //      
      // relations.add(relation);

      if (!parser.read(new StringReader(order))) {
        continue;
      }

      List<OrderToken> tokens = parser.getTokens();

      // TODO (stm) use a radix tree for faster matching...

      if ((tokens.get(0)).ttype == OrderToken.TT_COMMENT) {
        continue;
      }

      if ((tokens.get(0)).ttype == OrderToken.TT_PERSIST) {
        tokens.remove(0);
      }

      if (tempOrders) {
        // end of temp unit
        if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_END))) {
          tempOrders = false;

          continue;
        }
      }

      // begin of temp unit
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_MAKE))
          && (tokens.get(1)).getText().toUpperCase().startsWith(
              EresseaRelationFactory.getOrder(EresseaConstants.O_TEMP))) {
        tempOrders = true;

        continue;
      }

      // movement relation
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_MOVE))
          || (tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_ROUTE))) {
        List<CoordinateID> modifiedMovement = new ArrayList<CoordinateID>(2);

        // dissect the order into pieces to detect which way the unit
        // is taking
        Region currentRegion = u.getRegion();
        CoordinateID currentCoord = u.getRegion().getCoordinate();

        modifiedMovement.add(currentCoord);

        for (Iterator<OrderToken> iter2 = tokens.listIterator(1); iter2.hasNext();) {
          OrderToken token = iter2.next();
          Direction movement = Direction.toDirection(token.getText());

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

        continue;
      }

      // enter relation
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_ENTER))) {
        OrderToken t = tokens.get(1);
        UnitContainer uc = null;

        if (t.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_CASTLE))) {
          t = tokens.get(2);
          uc = u.getRegion().getBuilding(EntityID.createEntityID(t.getText(), data.base));
        } else if (t.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_SHIP))) {
          t = tokens.get(2);
          uc = u.getRegion().getShip(EntityID.createEntityID(t.getText(), data.base));
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
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_WORK))) {
        // TODO!
        continue;
      }

      // income relation ENTERTAIN
      if ((tokens.get(0))
          .equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_ENTERTAIN))) {
        // TODO!
        continue;
      }

      // income relation TAX
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_TAX))) {
        // TODO!
        continue;
      }

      // guard relation
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_GUARD))) {
        GuardRegionRelation guardRegionRelation = new GuardRegionRelation(u, 1, line);
        if (tokens.size() > 1) {
          if ((tokens.get(1)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_NOT))) {
            guardRegionRelation.guard = 0;
          }
        }
        relations.add(guardRegionRelation);
      }

      // transport relation
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_CARRY))) {
        OrderToken t = tokens.get(1);
        Unit target = getTargetUnit(t, u.getRegion());

        if ((target == null) || u.equals(target)) {
          continue;
        }

        TransportRelation rel = new TransportRelation(u, target, line);
        relations.add(rel);

        continue;
      }

      // transfer relation
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_GIVE))
          || (tokens.get(0))
              .equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_SUPPLY))) {
        // GIB 0|<enr> (ALLES|EINHEIT|KRaeUTER|KOMMANDO|((([JE] <amount>)|ALLES)
        // (SILBER|PERSONEN|<gegenstand>)))
        final int unitIndex = 1;
        int amountIndex = 2;
        int itemIndex = 3;
        boolean hasEach = false;

        OrderToken t = tokens.get(unitIndex);
        Unit target = getTargetUnit(t, u.getRegion());

        if (target != null) {
          if (!target.equals(u)) {
            TransferRelation rel = new TransferRelation(u, target, -1, line);

            t = tokens.get(amountIndex);
            if (t.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_HERBS))) {
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

            } else if (t.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_CONTROL))) {
              controlWarning++;
              UnitRelation r = new ControlRelation(u, target, line);
              relations.add(r);
            } else if (t.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_UNIT))) {
              UnitRelation r = new UnitTransferRelation(u, target, u.getRace(), line);
              relations.add(r);
            } else {
              boolean parseItem = false;
              // order is GIVE bla [EACH] <amount> <something>
              if ((t.ttype == OrderToken.TT_KEYWORD)
                  && t.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_ALL))) {
                // -2 encodes that everything is to be transferred
                rel.amount = EresseaRelationFactory.REFRESHRELATIONS_ALL;
                parseItem = true;
              } else {
                // GIVE bla EACH ALL does not make a lot of sense
                if ((t.ttype == OrderToken.TT_KEYWORD)
                    && t.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_EACH))) {
                  hasEach = true;
                  t = tokens.get(++amountIndex);
                  ++itemIndex;
                }
                if (t.ttype == OrderToken.TT_NUMBER) {
                  // u is a plain number
                  rel.amount = Integer.parseInt(t.getText());
                  parseItem = true;
                }
              }
              if (parseItem) {
                if (rel.amount == -1) { // -1 means that the amount could not
                  // determined
                  // we could issue a warning to the user here
                  EresseaRelationFactory.log
                      .debug("EresseaRelationFactory.createRelations(Unit): cannot parse amount in order "
                          + order);
                } else {
                  t = tokens.get(itemIndex);
                  if (t.getText().equalsIgnoreCase("\"")) {
                    t = tokens.get(++itemIndex);
                  }
                  if (t.ttype != OrderToken.TT_EOC) {
                    // now the order must look something like:
                    // GIVE <unit id> <amount> <object><EOC>
                    String itemName = EresseaRelationFactory.stripQuotes(t.getText());

                    if (t.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_MEN))) {
                      // if the specified amount was 'all':
                      if (rel.amount == EresseaRelationFactory.REFRESHRELATIONS_ALL) {
                        rel.amount = modPersons;
                      } else {
                        // if not, only transfer the minimum amount the unit
                        // has
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
                          } else {
                            // if not, only transfer the minimum amount
                            // the unit has
                            if (i.getAmount() < rel.amount) {
                              rel.warning = true;
                            }
                            // GIVE ... EACH ALL does not make sense
                            rel.amount =
                                Math.min(i.getAmount(), rel.amount
                                    * (hasEach ? target.getModifiedPersons() : 1));
                          }
                        }

                        // create the new transfer relation
                        rel =
                            new ItemTransferRelation(u, target, rel.amount, iType, line,
                                rel.warning);

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
                    // GIVE <unit id> <amount><EOC>
                    if (rel.amount == EresseaRelationFactory.REFRESHRELATIONS_ALL) { // -2 is used
                      // to
                      // encode that
                      // the amount
                      // was 'ALL'

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
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_RECRUIT))) {
        OrderToken t = tokens.get(1);

        if (t.ttype == OrderToken.TT_NUMBER) {
          Race race = (u instanceof TempUnit) ? ((TempUnit) u).getParent().getRace() : u.getRace();
          if (tokens.size() > 3) {
            race = getRace(tokens.get(2).getText());
            if (u.getPersons() == 0 || u.getRace().equals(race)) {
              RecruitmentRelation rel =
                  new RecruitmentRelation(u, Integer.parseInt(t.getText()), race, line);
              relations.add(rel);
            } else {
              race = u.getRace();
            }
          } else {
            RecruitmentRelation rel =
                new RecruitmentRelation(u, Integer.parseInt(t.getText()), line);
            relations.add(rel);
          }
          if (u instanceof TempUnit) {
            ((TempUnit) u).setTempRace(race);
          }
        } else {
          EresseaRelationFactory.log.debug("Unit.updateRelations(): invalid amount in order "
              + order);
        }

        continue;
      }

      // leave relation
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_LEAVE))) {
        UnitContainer uc = u.getBuilding();

        if (uc == null) {
          uc = u.getShip();
        }

        if (uc != null) {
          controlWarning = 2;
          LeaveRelation rel = new LeaveRelation(u, uc, line);
          relations.add(rel);
        } else {
          EresseaRelationFactory.log.debug("Unit.refreshRelations(): unit " + u
              + " cannot leave a ship or a building as indicated by order " + order);
        }

        continue;
      }

      // teach relation
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_TEACH))) {
        int tokCtr = 1;
        OrderToken token = tokens.get(tokCtr);

        while (token.ttype != OrderToken.TT_EOC) {
          Unit pupil = getTargetUnit(token, u.getRegion());

          if (pupil != null) {
            if (!u.equals(pupil)) {
              TeachRelation rel = new TeachRelation(u, pupil, line);
              relations.add(rel);
            }

            // else can't teach myself
          }

          // else pupil not found
          tokCtr++;
          token = tokens.get(tokCtr);
        }

        continue;
      }

      // attack relation
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_ATTACK))) {
        if (tokens.size() > 1) {
          OrderToken enemyToken = tokens.get(1);
          Unit enemy = getTargetUnit(enemyToken, u.getRegion());

          if (enemy != null) {
            AttackRelation rel = new AttackRelation(u, enemy, line);
            relations.add(rel);
          }
        }
      }

      // battle status relation
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_COMBAT))) {
        CombatStatusRelation combatStatusRelation = null;
        if (tokens.size() > 1) {
          // additional info for battle status
          // lets detect new status
          OrderToken newStatusToken = tokens.get(1);
          if (newStatusToken.equalsToken(EresseaRelationFactory
              .getOrder(EresseaConstants.O_COMBAT_AGGRESSIVE))) {
            combatStatusRelation = new CombatStatusRelation(u, 0, line);
          }
          if (newStatusToken.equalsToken(EresseaRelationFactory
              .getOrder(EresseaConstants.O_COMBAT_FRONT))) {
            combatStatusRelation = new CombatStatusRelation(u, 1, line);
          }
          if (newStatusToken.equalsToken(EresseaRelationFactory
              .getOrder(EresseaConstants.O_COMBAT_REAR))) {
            combatStatusRelation = new CombatStatusRelation(u, 2, line);
          }
          if (newStatusToken.equalsToken(EresseaRelationFactory
              .getOrder(EresseaConstants.O_COMBAT_DEFENSIVE))) {
            combatStatusRelation = new CombatStatusRelation(u, 3, line);
          }
          if (newStatusToken.equalsToken(EresseaRelationFactory
              .getOrder(EresseaConstants.O_COMBAT_NOT))) {
            combatStatusRelation = new CombatStatusRelation(u, 4, line);
          }
          if (newStatusToken.equalsToken(EresseaRelationFactory
              .getOrder(EresseaConstants.O_COMBAT_FLEE))) {
            combatStatusRelation = new CombatStatusRelation(u, 5, line);
          }
          if (newStatusToken.getText().length() == 0) {
            // just nothing means "normal" = front
            combatStatusRelation = new CombatStatusRelation(u, 1, line);
          }
          // check additional the unaided order
          if (newStatusToken.equalsToken(EresseaRelationFactory
              .getOrder(EresseaConstants.O_COMBAT_HELP))) {
            // "Kämpfe helfe" would change unaided from true to false
            combatStatusRelation = new CombatStatusRelation(u, false, line);
            if (tokens.size() > 2) {
              // check if we have the NICHT
              OrderToken lastToken = tokens.get(2);
              if (lastToken.equalsToken(EresseaRelationFactory
                  .getOrder(EresseaConstants.O_COMBAT_NOT))) {
                // "Kämpfe helfe nicht" would change unaided from false to true
                combatStatusRelation = new CombatStatusRelation(u, true, line);
              }
            }
          }
        } else {
          // no more info means status "normal" = 1
          combatStatusRelation = new CombatStatusRelation(u, 1, line);
        }
        if (combatStatusRelation != null) {
          relations.add(combatStatusRelation);
        }
      }

      // name relation
      // TODO: Do it right
      if ((tokens.get(0)).equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_NAME))) {
        if (tokens.size() > 2) {
          OrderToken whatToken = tokens.get(1);
          OrderToken nameToken = new OrderToken("---");
          for (OrderToken token : tokens)
            if (token.ttype == OrderToken.TT_STRING) {
              nameToken = token;
            }

          if (whatToken.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_UNIT))) {
            if (tokens.size() > 3) {
              relations.addAll(createRenameUnitRelation(u, nameToken, line));
            }
          } else {
            if (tokens.size() > 4) {
              if (whatToken.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_CASTLE))) {
                relations.addAll(createRenameUnitContainerRelation(u, tokens.get(2), nameToken,
                    line));
              } else if (whatToken.equalsToken(EresseaRelationFactory
                  .getOrder(EresseaConstants.O_FACTION))) {
                relations.addAll(createRenameUnitContainerRelation(u, tokens.get(2), nameToken,
                    line));
              } else if (whatToken.equalsToken(EresseaRelationFactory
                  .getOrder(EresseaConstants.O_REGION))) {
                relations.addAll(createRenameUnitContainerRelation(u, tokens.get(2), nameToken,
                    line));
              } else if (whatToken.equalsToken(EresseaRelationFactory
                  .getOrder(EresseaConstants.O_SHIP))) {
                relations.addAll(createRenameUnitContainerRelation(u, tokens.get(2), nameToken,
                    line));
              }
            }
          }
          if (whatToken.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_FOREIGN))) {
            // rels.addAll(createRenameForeignUnitContainerRelation(u,
            // (OrderToken) tokens.get(2), (OrderToken) tokens.get(3)));
            // retVal = readBenenneFremdes(t);
          }
        }
      }
    }

    // set the warning flag of ControlRelations if unit is leaving
    if (controlWarning > 1) {
      for (int i = 0; i < relations.size(); ++i) {
        UnitRelation r = relations.get(i);
        if (r instanceof ControlRelation) {
          relations.set(i, new ControlRelation(((ControlRelation) r).source,
              ((ControlRelation) r).target, ((ControlRelation) r).line, true));
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
   * Creates ReserveRelations for alle reserve orders of a unit
   * 
   * @param u The unit
   * @param ordersCopy A copy of the unit's orders. TODO: remove this parameter
   * @param from Ignored!
   * @param parser An order parser which has been initialized with the game data
   * @param modItems The Map of the unit's modified items
   * @param rels The newly created {@link ReserveRelation}s are inserted into this list
   */
  private static void createReserveRelations(Unit u, List<String> ordersCopy,  int from,
      OrderParser parser, Map<ID, Item> modItems, List<UnitRelation> rels) {
    from = 0;
    // parameter from is ignored because it violates execution order

    Map<ItemType, Item> reservedItems = new HashMap<ItemType, Item>();
    GameData data = u.getRegion().getData();
    int line = 0;

    for (String order : ordersCopy) {
      int orderIndex = 0;
      int amountIndex = 1;
      int itemIndex = 2;
      line++; // keep track of line

      // parameter from is ignored because it violates execution order
      // if(line < from) {
      // continue;
      // }

      if (!parser.read(new StringReader(order))) {
        continue;
      }

      List<OrderToken> tokens = parser.getTokens();

      if ((tokens.get(0)).ttype == OrderToken.TT_COMMENT) {
        continue;
      }

      if ((tokens.get(0)).ttype == OrderToken.TT_PERSIST) {
        orderIndex++;
        amountIndex++;
        itemIndex++;
      }

      if (tokens.size() > orderIndex
          && (tokens.get(orderIndex)).equalsToken(EresseaRelationFactory
              .getOrder(EresseaConstants.O_RESERVE))) {
        // RESERVE [EACH] <amount> <object><EOC>
        // RESERVIERE[JE] <amount> <object><EOC>
        OrderToken t = tokens.get(amountIndex);
        boolean hasEach = false;
        int amount = -1;
        boolean warning = false;

        ReserveRelation rel = null;
        if (t.ttype == OrderToken.TT_KEYWORD) {
          if (t.equalsToken(EresseaRelationFactory.getOrder(EresseaConstants.O_EACH))) {
            hasEach = true;
            t = tokens.get(++amountIndex);
            ++itemIndex;
          }
        }
        if (t.ttype == OrderToken.TT_NUMBER) {
          amount = Integer.parseInt(t.getText());

          if (amount != -1) { // -1 means that the amount could not determined
            t = tokens.get(itemIndex);
            if (t.ttype != OrderToken.TT_EOC) {
              String itemName = EresseaRelationFactory.stripQuotes(t.getText());

              if (itemName.length() > 0) {
                // TODO(pavkovic): korrigieren!!! Hier soll eigentlich das Item
                // über den
                // übersetzten Namen gefunden werden!!!
                ItemType iType = data.rules.getItemType(itemName);

                if (iType != null) {
                  // get the item from the list of modified items
                  Item i = modItems.get(iType);

                  if (i == null) {
                    // item unknown
                    amount = 0;
                    warning = true;
                  } else {
                    // // if the specified amount is 'all', convert u to a
                    // decent number
                    // if(amount == REFRESHRELATIONS_ALL) {
                    // amount = i.getAmount();
                    // warning = true;
                    // } else {
                    // // if not, only transfer the minimum amount the unit has
                    if (i.getAmount() < amount) {
                      warning = true;
                    }
                    // TODO (stm) should this be persons or modified persons?
                    amount *= hasEach ? u.getModifiedPersons() : 1;
                    // }
                    amount = Math.min(i.getAmount(), amount);
                  }

                  // create the new reserve relation
                  rel = new ReserveRelation(u, amount, iType, line, warning);

                  // update the modified item amount and record reserved amount
                  if (i != null) {
                    i.setAmount(Math.max(0, i.getAmount() - rel.amount));
                    Item rItem = reservedItems.get(iType.getID());
                    if (rItem == null) {
                      rItem = new Item(i.getItemType(), rel.amount);
                      reservedItems.put(i.getItemType(), rItem);
                    } else {
                      rItem.setAmount(rItem.getAmount() + rel.amount);
                    }
                  }
                }
              }
            }

            // let's see whether there is a valid relation to add
            if (rel != null) {
              rels.add(rel);
            }
          }
        } else {
          EresseaRelationFactory.log.debug("Unit.updateRelations(): cannot parse amount in order "
              + order);
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

  private Unit getTargetUnit(OrderToken t, Region r) {
    try {
      UnitID id = UnitID.createUnitID(t.getText(), r.getData().base);

      return r.getUnit(id);
    } catch (NumberFormatException e) {
      EresseaRelationFactory.log.debug("Unit.getTargetUnit(): cannot parse unit id \""
          + t.getText() + "\"!");
    }

    return null;
  }

  /**
   * Removes quotes at the beginning and at the end of str or replaces tilde characters with spaces.
   * 
   * @param str DOCUMENT-ME
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
}
