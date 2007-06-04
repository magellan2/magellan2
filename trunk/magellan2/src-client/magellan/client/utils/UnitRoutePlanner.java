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

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import magellan.client.swing.RoutingDialog;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.RegionType;
import magellan.library.utils.Islands;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class UnitRoutePlanner {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public static boolean canPlan(Unit unit) {
		if(unit.getRegion() == null) {
			return false;
		}

		if(getModifiedRadius(unit) < 1) {
			return false;
		}

		if(unit.getFaction() != null) {
			return unit.getFaction().isPrivileged();
		}

		return false;
	}

	protected static int getModifiedRadius(Unit unit) {
		int load = unit.getModifiedLoad();

		int payload = unit.getPayloadOnHorse();

		if((payload >= 0) && (payload >= load)) {
			return 2;
		}

		payload = unit.getPayloadOnFoot();

		if((payload >= 0) && (payload >= load)) {
			return 1;
		}

		return 0;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public static boolean planUnitRoute(Unit unit, GameData data, Component ui,
										Collection otherUnits) {
		// check for island regions
		Region start = unit.getRegion();
		Collection<Region> island = new LinkedList<Region>();

		if(start.getIsland() != null) {
			island.addAll(start.getIsland().regions());
			island.remove(start);
		} else {
			Map<CoordinateID,Region> m = Islands.getIsland(data.rules, data.regions(), start);

			if(m != null) {
				island.addAll(m.values());
				island.remove(start);
			}
		}

		if(island.size() == 0) {
			return false;
		}

		// get the data:
		RoutingDialog.RetValue v = (new RoutingDialog(JOptionPane.getFrameForComponent(ui), data,
													  island)).showRoutingDialog();

		if(v != null) {
			Map<ID,RegionType> excludeMap = Regions.getOceanRegionTypes(data.rules);

			List<Region> path = Regions.getPath(data.regions(), start.getCoordinate(), v.dest, excludeMap);

			if((path != null) && (path.size() > 1)) {
				int range = getModifiedRadius(unit);

				if(!v.useRange) {
					range = Integer.MAX_VALUE;
				} else {
					if(range <= 0) {
						// couldn't determine shiprange
						JOptionPane.showMessageDialog(ui, Resources.get("magellan.util.unitrouteplanner.msg.unitrangeiszero.text"),
													  Resources.get("magellan.util.unitrouteplanner.msg.title"),
													  JOptionPane.WARNING_MESSAGE);
						range = Integer.MAX_VALUE;
					}
				}

				List<String> orders = new LinkedList<String>();
				String order = "";

				if(v.makeRoute) {
					order = getOrder(EresseaConstants.O_ROUTE);
					order += (" " + Regions.getDirections(path));
					order += (" " + getOrder(EresseaConstants.O_PAUSE));
					Collections.reverse(path);
					order += (" " + Regions.getDirections(path));
					order += (" " + getOrder(EresseaConstants.O_PAUSE));
					orders.add(order);
				} else {
					String nach = getOrder(EresseaConstants.O_MOVE) + " ";
					int count = 0;
					int after = 0;
					List<Region> curPath = new LinkedList<Region>();
					int index = 1;

					do {
						curPath.clear();
						curPath.add(path.get(index - 1));

						count = 0;

						while((index < path.size()) && (count < range)) {
							curPath.add(path.get(index));

							count++;
							index++;
						}

						if(v.useVorlage && (after > 0)) {
							orders.add("// #after " + after + " { " + nach +
									   Regions.getDirections(curPath) + " }");
						} else {
							orders.add(nach + Regions.getDirections(curPath));
						}

						after++;
					} while(index < path.size());

					Collections.reverse(orders);
				}

				if(v.replaceOrders) {
					unit.setOrders(orders);
				} else {
					for(Iterator iter = orders.iterator(); iter.hasNext();) {
						unit.addOrder((String) iter.next(), false, 0);
					}
				}

				if((otherUnits != null) && (otherUnits.size() > 0)) {
					Iterator it = otherUnits.iterator();

					while(it.hasNext()) {
						Unit u = (Unit) it.next();

						if(!u.equals(unit)) {
							if(v.replaceOrders) {
								u.setOrders(orders);
							} else {
								for(Iterator iter = orders.iterator(); iter.hasNext();) {
									u.addOrder((String) iter.next(), false, 0);
								}
							}
						}
					}
				}

				return true;
			} else {
				// No path could be found from start to destination region.
				JOptionPane.showMessageDialog(ui, Resources.get("magellan.util.unitrouteplanner.msg.nopathfound.text"),
											  Resources.get("magellan.util.unitrouteplanner.msg.title"), JOptionPane.WARNING_MESSAGE);
			}
		}

		return false;
	}


	/**
	 * Returns a translation for the specified order key.
	 *
	 * 
	 *
	 * 
	 */
	private static String getOrder(String key) {
		return Resources.getOrderTranslation(key);
	}
}
