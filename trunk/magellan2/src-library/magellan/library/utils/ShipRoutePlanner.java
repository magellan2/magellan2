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

package magellan.library.utils;

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JOptionPane;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.BuildingType;
import magellan.library.utils.guiwrapper.RoutingDialogData;
import magellan.library.utils.guiwrapper.RoutingDialogDataPicker;


/**
 * Works together with com.eressea.swing.RoutingDialog to calculate the route for a ship.
 *
 * @author Ulrich K�ster
 * @author Andreas
 */
public class ShipRoutePlanner {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public static boolean canPlan(Ship ship) {
		if(ship.getSize() < ship.getShipType().getMaxSize()) {
			return false;
		}

		return (ship.getOwnerUnit() != null) && ship.getOwnerUnit().getFaction().isPrivileged();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public static Unit planShipRoute(Ship ship, GameData data, Component ui, RoutingDialogDataPicker picker) {
		// fetch all coast regions
		// Map oceans = Regions.getOceanRegionTypes(data.rules);
		Collection<Region> coast = new LinkedList<Region>();

		try {
			Map<CoordinateID,Region> regionMap = data.regions();
			Iterator cIt = regionMap.values().iterator();

			while(cIt.hasNext()) {
				try {
					Region region = (Region) cIt.next();
					Map<CoordinateID,Region> m = Regions.getAllNeighbours(regionMap, region.getCoordinate(), 1, null);
					Iterator<Region> cIt2 = m.values().iterator();

					while(cIt2.hasNext()) {
						Region r2 = cIt2.next();

						//if(oceans.values().contains(r2.getRegionType())) {
						if(r2.getRegionType().isOcean()) {
							coast.add(region);

							break;
						}
					}
				} catch(Exception exc) {
				}
			}
		} catch(Exception coastException) {
		}

		// get the data:
    if (coast.size() == 0) {
      picker.initialize(data,null,true);
    } else {
      picker.initialize(data,coast,true);
    }
    RoutingDialogData v = picker.showRoutingDialog();

		if(v != null) {
			Unit shipOwner = ship.getOwnerUnit();

			if(shipOwner != null) {
				if((shipOwner.getFaction() != null) && shipOwner.getFaction().isPrivileged()) {
					int meerManBonus = 0;

					try {
						meerManBonus = shipOwner.getFaction().getRace().getAdditiveShipBonus();
					} catch(Exception exc) {
					}

					BuildingType harbour = data.rules.getBuildingType(StringID.create("Hafen"));

					List<Region> path = Regions.planShipRoute(ship, v.getDestination(), data.regions(), harbour, meerManBonus);

					if(path != null) {
						// Now try to calculate the orders:
						int shipRange = 0;

						try {
							shipRange = ship.getShipType().getRange() + meerManBonus;
						} catch(Exception exc) {
						}

						if(!v.useRange()) {
							shipRange = Integer.MAX_VALUE;
						} else if(shipRange <= 0) {
							// couldn't determine shiprange
							JOptionPane.showMessageDialog(ui,
														  Resources.get("util.shiprouteplanner.msg.shiprangeiszero.text"),
														  Resources.get("util.shiprouteplanner.msg.title"),
														  JOptionPane.WARNING_MESSAGE);
							shipRange = Integer.MAX_VALUE;
						}

						List<Region> curPath = new LinkedList<Region>();
						List<String> orders = new LinkedList<String>();
						String order = "";

						if(v.makeRoute()) {
							// TODO(pavkovic): move to EresseaOrderChanger
							order = Resources.getOrderTranslation(EresseaConstants.O_ROUTE);
							order += (" " + Regions.getDirections(path));
							order += (" " +
                  Resources.getOrderTranslation(EresseaConstants.O_PAUSE));
							Collections.reverse(path);
							order += (" " + Regions.getDirections(path));
							order += (" " +
                  Resources.getOrderTranslation(EresseaConstants.O_PAUSE));
						} else {
							order = Resources.getOrderTranslation(EresseaConstants.O_MOVE) +
									" ";

							int count = shipRange;
							int after = 0;
							String temp = ""; // saves whether a closing bracket must be added: "}"

							for(Iterator<Region> iter = path.iterator(); iter.hasNext();) {
								curPath.add(iter.next());

								if(curPath.size() > 1) {
									String dir = Regions.getDirections(curPath);

									if(dir != null) {
										if((count == 0) ||
											   ((count != shipRange) &&
											   Regions.containsHarbour(curPath.get(0),
																		   harbour))) {
											after++;
											count = shipRange;

											if(v.useVorlage()) {
												order += temp;
												orders.add(0, order);
												order = "// #after " + after + " { " + Resources.getOrderTranslation(EresseaConstants.O_MOVE) + " ";
												temp = "}";
											} else {
												orders.add(0, order);
												order = "// " + Resources.getOrderTranslation(EresseaConstants.O_MOVE) + " ";
											}
										}

										order += (dir + " ");
										count--;
									}

									curPath.remove(0);
								}
							}

							order += temp;
						}

						orders.add(0, order);

						if(v.replaceOrders()) {
							shipOwner.setOrders(orders);
						} else {
							for(ListIterator iter = orders.listIterator(); iter.hasNext();) {
								shipOwner.addOrder((String) iter.next(), false, 0);
							}
						}

						return shipOwner;
					} else {
						// No path could be found from start to destination region.
						JOptionPane.showMessageDialog(ui, Resources.get("util.shiprouteplanner.msg.nopathfound.text"),
													  Resources.get("util.shiprouteplanner.msg.title"),
													  JOptionPane.WARNING_MESSAGE);
					}
				} else {
					// Captain of the ship does not belong to a privileged faction.
					// No orders can be given.
					JOptionPane.showMessageDialog(ui, Resources.get("util.shiprouteplanner.msg.captainnotprivileged.text"),
												  Resources.get("util.shiprouteplanner.msg.title"),
												  JOptionPane.WARNING_MESSAGE);
				}
			} else {
				// Ship has no captain. No orders will be given.
				JOptionPane.showMessageDialog(ui, Resources.get("util.shiprouteplanner.msg.captainnotfound.text"),
											  Resources.get("util.shiprouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
			}
		}

		return null;
	}

}