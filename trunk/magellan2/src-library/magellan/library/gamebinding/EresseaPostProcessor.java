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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Scheme;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.Region.Visibility;
import magellan.library.rules.BuildingType;
import magellan.library.rules.ItemType;
import magellan.library.rules.RegionType;
import magellan.library.utils.Regions;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.SortIndexComparator;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 345 $
 */
public class EresseaPostProcessor {
  private static final Logger log = Logger.getInstance(EresseaPostProcessor.class);
  
  protected EresseaPostProcessor() {
	}
  
	private static final EresseaPostProcessor singleton = new EresseaPostProcessor();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static EresseaPostProcessor getSingleton() {
		return EresseaPostProcessor.singleton;
	}

	/**
   * This method tries to fix some issues that arise right after reading a report file. It scans
   * messages for herbs, removes dummy units, creates temp units and tries to detect if resources
   * should be set to zero because they are not in the report.
   * 
   * @param data
   */
  public void postProcess(GameData data) {
    if (data == null)
      throw new NullPointerException();
    cleanAstralSchemes(data);
    /* scan the messages for additional information */
    if (data.factions() != null) {
      for (Faction faction : data.factions().values()) {

        if (faction.getMessages() != null) {
          for (Message message : faction.getMessages()) {

            if (message.getMessageType() != null) {
              switch ((((IntegerID) message.getMessageType().getID()).intValue())) {
              case 1511758069:
              case 18362:

                // a herb was found in a region
              case 1349776898:

                // a certain amount of herbs has been detected in a region
                if ((message.getAttributes() != null)
                    && message.getAttributes().containsKey("region")) {
                  String str = message.getAttributes().get("region");
                  CoordinateID coord = CoordinateID.parse(str, ",");

                  if (coord == null) {
                    coord = CoordinateID.parse(str, " ");
                  }

                  Region r = data.getRegion(coord);

                  if (r != null) {
                    String value = message.getAttributes().get("herb");

                    if (value != null) {
                      ItemType type = data.rules.getItemType(StringID.create(value), true);
                      r.setHerb(type);
                    }

                    if ((((IntegerID) message.getMessageType().getID()).intValue()) == 1349776898) {
                      // a certain amount of herbs has been detected in a region
                      String amount = message.getAttributes().get("amount");

                      if (amount != null) {
                        r.setHerbAmount(amount);
                      }
                    }
                  }
                }

                break;
              case 865172808: /// X gelang es, folgende Informationen über Y herauszufinden
                // fix spy messages which lack the spy attribute (see Magellan bug #333 and Eressea bug 1604)
                Unit spy = null;
                Unit target = null;
                String id = message.getAttributes().get("spy");
                if (id != null)
                  spy = data.getUnit(UnitID.createUnitID(id, 10, data.base));
                id = message.getAttributes().get("target");
                if (id != null)
                  target = data.getUnit(UnitID.createUnitID(id, 10, data.base));
                if (spy == null || target == null || spy.getFaction() == null) {
                  log.warn("spy message without spy: " + message);
                } else {
                  for (Message msg2 : spy.getFaction().getMessages()) {
                    switch ((((IntegerID) msg2.getMessageType().getID()).intValue())) {
                    case 387085007: // Y gehört der Partei F an 
                    case 467205397: // Y beherrscht ...
                    case 743495578: // Im Gepäck von Y sind ...
                      msg2.getAttributes().put("spy",
                          String.valueOf(((IntegerID) spy.getID()).intValue()));
                      break;
                    }
                  }
                }
                break;
              }
            }
          }
        }
      }
    }

		// there can be dummy units (UnitContainer owners and such), find and remove these
		if(data.units() != null) {
			Collection<ID> dummyUnitIDs = new LinkedList<ID>();

			for(Iterator iter = data.units().values().iterator(); iter.hasNext();) {
				Unit unit = (Unit) iter.next();

				if(unit.getName() == null) {
					dummyUnitIDs.add(unit.getID());
				}
			}

			for(Iterator iter = dummyUnitIDs.iterator(); iter.hasNext();) {
				data.units().remove(iter.next());
			}
		}

		/* retrieve the temp units mentioned in the orders and
		 create them as TempUnit objects */
		int sortIndex = 0;
		List<Unit> sortedUnits = new LinkedList<Unit>(data.units().values());
		Collections.sort(sortedUnits, new SortIndexComparator<Unit>(IDComparator.DEFAULT));

		for(Iterator unitIter = sortedUnits.iterator(); unitIter.hasNext();) {
			Unit unit = (Unit) unitIter.next();
			unit.setSortIndex(sortIndex++);
			sortIndex = unit.extractTempUnits(sortIndex);
		}

		/* 'known' information does not necessarily show up in the
		report. e.g. depleted region resources are not mentioned
		although we actually know that the resource is available with
		an amount of 0. Resolve this ambiguity here: */
		if((data != null) && (data.regions() != null)) {
			/*ItemType sproutResourceID = */data.rules.getItemType("Schößlinge",true);
			/*ItemType treeResourceID = */data.rules.getItemType("Bäume",true);
			/*ItemType mallornSproutResourceID = */data.rules.getItemType("Mallornschößlinge",true);
			/*ItemType mallornTreeResourceID = */data.rules.getItemType("Mallorn",true);

			for(Iterator regionIter = data.regions().values().iterator(); regionIter.hasNext();) {
				Region region = (Region) regionIter.next();

        // ------------------------------------------------------------------
        // the following tags seem to be visible for "lighthouse";visibility:
        // DURCHSCHIFFUNG
        // SCHIFF: Name, Beschr, Typ, Groesse, Kapitaen, Partei, (Kueste)
        // EINHEIT: Name, Beschr, Partei, Anderepartei, typprefix, Typ, Anzahl, Schiff, (Burg)
        // GEGENSTÄNDE
        // ------------------------------------------------------------------
        // the following tags seem to be visible for "travel";visibility:
        // Baeume, Schoesslinge, Bauern, Pferde, Effects 
        // DURCHSCHIFFUNG
        // DURCHREISE
        // BURG: Typ, Name, Beschr, Groesse, Besitzer, Partei
        // SCHIFF: Name, Beschr, Typ, Groesse, Kapitaen, Partei, Kueste
        // EINHEIT: Name, Beschr, Partei, Anderepartei, typprefix, Typ, Anzahl, Burg, Schiff
        // ------------------------------------------------------------------
        // the following tags seem to be visible even for "neighbour";visibilty:
        // Name, Terrain, Beschr 
        // GRENZE -- in the direction of the region with own unit.

        /* first determine whether we know everything about
				this region */
				if(region.getVisibility()==Visibility.UNIT) {
					/* now patch as much missing information as
					possible */
					// FIXME (stm) 2006-10-28: this has bitten us already
					// check what is visible in what visibility 
					// (lighthouse, neigbbour, travel)   
					
					// the following tags seem to be present under undefined visibility
          // even if they are zero (but only if region is not an ocean):
          // Bauern, Silber, Unterh, Rekruten, Pferde, (Lohn)
					{
						if(region.getPeasants() < 0) {
							region.setPeasants(0);
						}

						if(region.getSilver() < 0) {
							region.setSilver(0);
						}

						if(region.getWage() < 0) {
							// TODO: should we set this to 10 instead?
							region.setWage(0);
						}

						if(region.getHorses() < 0) {
							region.setHorses(0);
						}
					}
					
					if(region.getSprouts() < 0) {
						region.setSprouts(0);
					}
					if(region.getTrees() < 0) {
						region.setTrees(0);
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void postProcessAfterTrustlevelChange(GameData data) {
		// initialize fog-of-war cache (FIXME(pavkovic): Do it always?)
		// clear all fog-of-war caches
		if(data.regions() != null) {
			for(Iterator iter = data.regions().values().iterator(); iter.hasNext();) {
				Region r = (Region) iter.next();
				r.setFogOfWar(-1);
			}
		}

		// intialize the fog-of-war cache for all regions that are covered by lighthouses
		if(data.buildings() != null) {
			BuildingType type = data.rules.getBuildingType(EresseaConstants.B_LIGHTHOUSE);
			RegionType oceanType = data.rules.getRegionType(EresseaConstants.RT_OCEAN);
			Comparator<Unit> sortIndexComparator = new SortIndexComparator<Unit>(IDComparator.DEFAULT);

			// FIXME(stm) broken, if a unit's faction's report was not added but the skill is still known...
			if(type != null) {
				for(Iterator iter = data.buildings().values().iterator(); iter.hasNext();) {
					Building b = (Building) iter.next();

					if(type.equals(b.getType()) && (b.getSize() >= 10)) {
						int personCounter = 0;
						int perceptionSkillLevel = 0;
						List<Unit> sortedInmates = new LinkedList<Unit>(b.units());
						Collections.sort(sortedInmates, sortIndexComparator);

						for(Iterator<Unit> inmates = sortedInmates.iterator(); inmates.hasNext() && (personCounter < 4); personCounter++) {
							Unit inmate = inmates.next();
							Skill perceptionSkill = inmate.getSkill(data.rules.getSkillType(EresseaConstants.S_WAHRNEHMUNG,
																							true));

							if(perceptionSkill != null) {
								perceptionSkillLevel = Math.max(perceptionSkill.getLevel(),
																perceptionSkillLevel);
							}
						}

						int maxRadius = (int) Math.min(Math.log10(b.getSize()) + 1,
													   perceptionSkillLevel / 3);

						if(maxRadius > 0) {
							Map regions = Regions.getAllNeighbours(data.regions(),
																   b.getRegion().getCoordinate(),
																   maxRadius, null);

							for(Iterator regionIter = regions.values().iterator();
									regionIter.hasNext();) {
								Region r = (Region) regionIter.next();

								if((oceanType == null) || oceanType.equals(r.getType())) {
									r.setFogOfWar(0);
								}
							}
						}
					}
				}
			}
		}

		// intialize the fog-of-war cache for all regions where units or ships traveled through
		for(Iterator iterator = data.regions().values().iterator(); iterator.hasNext();) {
			Region r = (Region) iterator.next();

			if(r.getTravelThru() != null) {
				initTravelThru(data, r, r.getTravelThru());
			}

			if(r.getTravelThruShips() != null) {
				initTravelThru(data, r, r.getTravelThruShips());
			}
		}
	}

	private void initTravelThru(GameData data, Region region, Collection travelThru) {
		for(Iterator iter = travelThru.iterator(); iter.hasNext();) {
			Message mes = (Message) iter.next();

			// fetch ID of Unit or Ship from Message of type "<name> (<id>)"
			String s = mes.getText();
			int startpos = s.lastIndexOf("(") + 1;
			int endpos = s.length() - 1;

			if((startpos > -1) && (endpos > startpos)) {
				try {
				  // message text always use the report base
					ID id = EntityID.createEntityID(s.substring(startpos, endpos),data.base);

					if((data.getUnit(id) != null) &&
						   (data.getUnit(id).getFaction().isPrivileged())) {
						// fast return
						region.setFogOfWar(0);

						return;
					} else {
						Ship ship = data.getShip(id);

						if(ship != null) {
							for(Iterator i = ship.units().iterator(); i.hasNext();) {
								if(((Unit) i.next()).getFaction().isPrivileged()) {
									// fast return
									region.setFogOfWar(0);

									return;
								}
							}
						}
					}
				} catch(NumberFormatException e) {
				}
			}
		}
	}
  
  /**
   * Deletes the schemes of astral regions in the case one of the following
   * inconsistencies is detected
   * 
   * 1. more than 19 schemes per astral region
   * 2. the region with the scheme coordinates has terrain ocean or 
   *    firewall
   * 3. layout, i.e. two scheme of the same astral region with a distance of >4
   * 4. a scheme is seen from more than 2 astral regions
   * 
   * Only the scheme of the affected astral regions are deleted the others 
   * will remain to allow a mapping of astral to real space.
   * 
   * Not implemented:
   * 
   * 5. global layout (similar alorithm as layout but normalizing all schemes
   *    into the area of one astral region before) 
   *    -> nearly impossible to determine the wrong schemes
   * 6. diferent scheme/region name of same coordinate
   *    -> may not be an wrong scheme, only outdated name.
   * @param gd
   */
  private void cleanAstralSchemes(GameData gd) {
    RegionType firewall = gd.rules.getRegionType(EresseaConstants.RT_FIREWALL);
    Map<CoordinateID, Collection<Region>> schemeMap = new HashMap<CoordinateID, Collection<Region>>();
    for (Region region : gd.regions().values()) {
      if ((region.getCoordinate().z == 1)&&(region.schemes().size()>0)) {
        // Check 1. (number)
        if (region.schemes().size()>19) {
          // Inconsistency of type 1. found
          EresseaPostProcessor.log.warn("EresseaPostProcessor: Astral schemes inconsistency type: to many schemes");
          EresseaPostProcessor.log.warn(region);
          region.clearSchemes();
          continue;
        }
        // Check 2. (terrain) and 3. (extension)
        boolean inconsistent = false;
        CoordinateID min = null;
        CoordinateID max = null;
        
        for (Scheme scheme : region.schemes()) {
          CoordinateID schemeID = scheme.getCoordinate();
          Region schemeRegion = gd.getRegion(schemeID);
          // Check 2. (terrain)
          if (schemeRegion != null) {
            RegionType rt = schemeRegion.getRegionType();
            if (rt.isOcean() || rt.equals(firewall)) {
              inconsistent = true;
              break;
            }
          }
          // Check 3. (extension)
          if (min == null || max == null) {
            min = new CoordinateID(schemeID);
            max = new CoordinateID(schemeID);
            min.z = max.z = schemeID.x + schemeID.y;
          } else {
            min.x = Math.min(min.x, schemeID.x);
            min.y = Math.min(min.y, schemeID.y);
            min.z = Math.min(min.z, schemeID.x + schemeID.y);
            max.x = Math.max(max.x, schemeID.x);
            max.y = Math.max(max.y, schemeID.y);
            max.z = Math.max(max.z, schemeID.x + schemeID.y);
          }
        }
        if (inconsistent) {
          // Inconsistency of type 2. found
          EresseaPostProcessor.log.warn("EresseaPostProcessor: Astral schemes inconsistency type: terrain");
          EresseaPostProcessor.log.warn(region);
          region.clearSchemes();
          continue;          
        }
        boolean centerFound = false;
        for (int x = max.x-2; (x <= min.x+2) && !centerFound; x++) {
          for (int y = max.y-2; (y <= min.y+2) && !centerFound; y++) {
            if ((max.z-2 <= x+y ) && (x+y <= min.z+2)) {
              centerFound = true;
            }
          }
        }
        if (!centerFound) {
          // Inconsistency of type 3. found
          EresseaPostProcessor.log.warn("EresseaPostProcessor: Astral schemes inconsistency type: layout");
          EresseaPostProcessor.log.warn(region);
          region.clearSchemes();
          continue;                   
        }
        /*
         * none of the other inconsistency checks fail. 
         * therefore we can check for type 4 now.
         * (this check eleminates the schemes from all astral
         * regions having the scheme. Therefore eleminating other
         * inconsistencies first allow to probably save some
         * right schemes from beeing deleted)
         * We only prepare the a map here that maps schemeIDs to regions
         */  
        for (Scheme scheme : region.schemes()) {
          Collection<Region> regionCol = schemeMap.get(scheme.getCoordinate());
          if (regionCol == null) {
            regionCol = new ArrayList<Region>(2);
            schemeMap.put(scheme.getCoordinate(), regionCol);
          }
          regionCol.add(region);
        }
      }
    }
    for (Collection<Region> regionCol : schemeMap.values()) {
      if (regionCol.size() > 2) {
        // Inconsistency of type 4. found
        EresseaPostProcessor.log.warn("EresseaPostProcessor: Astral schemes inconsistency type: scheme too often");        
        for (Region region : regionCol) {
          EresseaPostProcessor.log.warn(region);
          region.clearSchemes();
        }
      }
    }
  }
//  private void postProcessMessages(GameData data) {
    // herb information from FIND HERBS/FORSCHE KRÄUTER or MAKE HERBS/MACHE KRÄUTER
    // item information from SHOW/ZEIGE
    // race information from SHOW/ZEIGE
    // unit information from SPY/SPIONIERE
//  }
}
