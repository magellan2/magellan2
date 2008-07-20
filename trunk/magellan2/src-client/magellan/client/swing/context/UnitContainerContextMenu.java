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

package magellan.client.swing.context;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import magellan.client.Client;
import magellan.client.EMapDetailsPanel;
import magellan.client.event.EventDispatcher;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.FactionStatsDialog;
import magellan.client.swing.GiveOrderDialog;
import magellan.client.swing.RoutingDialog;
import magellan.client.utils.Units;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.Resources;
import magellan.library.utils.ShipRoutePlanner;


/**
 * A context menu for UnitContainers like ships or buildings. Providing copy
 * ID and copy ID+name.
 *
 * @author Ulrich Küster 
 */
public class UnitContainerContextMenu extends JPopupMenu {
  private static final NumberFormat weightNumberFormat = NumberFormat.getNumberInstance();
	private UnitContainer uc;
	private EventDispatcher dispatcher;
	private GameData data;
	private Properties settings;
	private Collection selectedObjects;

	/**
	 * Creates a new UnitContainerContextMenu object.
	 */
	public UnitContainerContextMenu(UnitContainer uc, EventDispatcher dispatcher, GameData data,
									Properties settings,Collection selectedObjects) {
		super(uc.toString());
		this.uc = uc;
		this.dispatcher = dispatcher;
		this.data = data;
		this.settings = settings;
		this.selectedObjects = selectedObjects;

		initMenu();
	}

	private void initMenu() {
		JMenuItem name = new JMenuItem(uc.toString());
		name.setEnabled(false);
		add(name);

		JMenuItem copyID = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.copyid.caption"));
		copyID.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					copyID();
				}
			});
		add(copyID);
    
    JMenuItem copyName = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.copyname.caption"));
    copyName.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          copyName();
        }
      });
    add(copyName);
    

		JMenuItem copyNameID = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.copyidandname.caption"));
		copyNameID.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					copyNameID();
				}
			});
		add(copyNameID);

    // context.unitcontainercontextmenu.menu.copyidandnameanduid.caption
		if (uc instanceof Region){
      Region r = (Region)uc;
      if (r.getUID()>0){
        JMenuItem copyNameRegionID = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.copynameanduid.caption"));
        copyNameRegionID.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              copyNameRegionID();
            }
          });
        add(copyNameRegionID);
        
        JMenuItem copyNameIDRegionID = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.copyidandnameanduid.caption"));
        copyNameIDRegionID.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              copyNameIDRegionID();
            }
          });
        add(copyNameIDRegionID);
      }
    }
    
    
		if(uc instanceof Ship) {
			JMenuItem planShipRoute = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.planshiproute.caption"));
			planShipRoute.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						planShipRoute();
					}
				});
			planShipRoute.setEnabled(ShipRoutePlanner.canPlan((Ship) uc));
			add(planShipRoute);
		} else if(uc instanceof Faction) {
			JMenuItem copyMail = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.copymail.caption"));
			copyMail.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						copyMail();
					}
				});

			JMenuItem factionStats = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.factionstats.caption"));
			factionStats.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						factionStats();
					}
				});
			add(copyMail);
			add(factionStats);
		}
		
		// check, if we have ships in the selection...
		// we want to offer: give orders to ship-captns
		boolean shipsInSelection = false;
		if (this.selectedObjects!=null){
			for (Iterator iter = this.selectedObjects.iterator();iter.hasNext();){
				Object o = iter.next();
				if (o instanceof Ship) {
					shipsInSelection = true;
					break;
				}
			}
		}
		if (shipsInSelection){
			JMenuItem shipOrders = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.shiporders.caption"));
			shipOrders.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					event_addShipOrder();
				}
			});
			add(shipOrders);
			
			JMenuItem shipList = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.shiplist.caption"));
      shipList.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_shipList();
        }
      });
      add(shipList);
		}
		
    initContextMenuProviders(uc);
	}
  

  private void initContextMenuProviders(UnitContainer unitContainer) {
    Collection<UnitContainerContextMenuProvider> cmpList = getContextMenuProviders();
    if (!cmpList.isEmpty()) {
      addSeparator();
    }
    
    for (UnitContainerContextMenuProvider cmp : cmpList) {
      add(cmp.createContextMenu(dispatcher, data, unitContainer));
    }

  }

  /**
   * Searchs for Context Menu Providers in the plugins and adds them to the menu.
   */
  private Collection<UnitContainerContextMenuProvider> getContextMenuProviders() {
    Collection<UnitContainerContextMenuProvider> cmpList = new ArrayList<UnitContainerContextMenuProvider>();
    for (MagellanPlugIn plugIn : Client.INSTANCE.getPlugIns()) {
      if (plugIn instanceof UnitContainerContextMenuProvider) {
        cmpList.add((UnitContainerContextMenuProvider)plugIn);
      }
    }
    return cmpList;
  }


	/**
	 * Copies the ID of the UnitContainer to the clipboard.
	 */
	private void copyID() {
		StringSelection strSel = new StringSelection(uc.getID().toString());
		Clipboard cb = getToolkit().getSystemClipboard();
		cb.setContents(strSel, null);
	}

	/**
	 * Copies name and id to the sytem clipboard.
	 */
	private void copyNameID() {
        StringSelection strSel = new StringSelection(uc.toString());
		Clipboard cb = getToolkit().getSystemClipboard();
		cb.setContents(strSel, null);
	}
  
  /**
   * Copies name to the sytem clipboard.
   */
  private void copyName() {
    StringSelection strSel = new StringSelection(uc.getName());
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);
  }

  /**
   * Copies name and id and regionID to the sytem clipboard.
   */
  private void copyNameIDRegionID() {
    Region r = (Region)uc;
    StringSelection strSel = new StringSelection(uc.toString() + " (" + Integer.toString((int)r.getUID(),36).replace("l","L") + ")");
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);
  }
  
  /**
   * Copies name and id and regionID to the sytem clipboard.
   */
  private void copyNameRegionID() {
    Region r = (Region)uc;
    StringSelection strSel = new StringSelection(r.getName() + " (" + Integer.toString((int)r.getUID(),36).replace("l","L") + ")");
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);
  }
  
	/**
	 * Copies the mailadress of a faction to the clipboard.
	 */
	private void copyMail() {
		Faction f = (Faction) uc;

		// pavkovic 2002.11.12: creating mail addresses in a form like: Noeskadu <noeskadu@gmx.de>
		StringSelection strSel = new StringSelection(f.getName() + " <" + f.getEmail() + ">");
		Clipboard cb = getToolkit().getSystemClipboard();
		cb.setContents(strSel, null);
	}

	/**
	 * Calls the factionstats
	 */
	private void factionStats() {
		FactionStatsDialog d = new FactionStatsDialog(JOptionPane.getFrameForComponent(this),
													  false, dispatcher, data, settings,
													  (Faction) uc);
		d.setVisible(true);
	}

	/**
	 * Plans a route for a ship (typically over several weeks)
	 *
	 * @see ShipRoutingDialog
	 */
	private void planShipRoute() {
		Unit unit = ShipRoutePlanner.planShipRoute((Ship) uc, data, this, new RoutingDialog(JOptionPane.getFrameForComponent(this),data,false));

		if(unit != null) {
			dispatcher.fire(new UnitOrdersEvent(this, unit));
		}
	}

	/**
	 * Gives an order (optional replacing the existing ones) to the selected units.
	 * Gives the orders only to actual captns of selected ships
	 */
	private void event_addShipOrder() {
		GiveOrderDialog giveOderDialog = new GiveOrderDialog(JOptionPane.getFrameForComponent(this));
		String s[] = giveOderDialog.showGiveOrderDialog();
		for(Iterator iter = this.selectedObjects.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof Ship){
				Ship ship = (Ship)o;
				Unit u = ship.getOwnerUnit();

				if(u!=null && EMapDetailsPanel.isPrivilegedAndNoSpy(u)) {
					Units.changeOrders(u, s);
					dispatcher.fire(new UnitOrdersEvent(this, u));
				}
			}
		}
	}
	
	/**
   * Copies Info about selected ships to the clipboard
   */
  private void event_shipList() {
    String s = "";
    int cntShips = 0;
    int cntActModifiedLoad = 0;
    int cntMaxLoad = 0;
    for(Iterator iter = this.selectedObjects.iterator(); iter.hasNext();) {
      Object o = iter.next();
      if (o instanceof Ship){
        Ship ship = (Ship)o;
        cntShips++;
        cntActModifiedLoad+=ship.getModifiedLoad();
        cntMaxLoad += ship.getMaxCapacity();
        s+=ship.toString(true);
        s+=":";
        s+=UnitContainerContextMenu.weightNumberFormat.format(new Float((ship.getMaxCapacity()-ship.getModifiedLoad()) / 100.0F));
        s+="\n";
      }
    }
    if (cntShips>0){
      s+=cntShips + " ships with " + UnitContainerContextMenu.weightNumberFormat.format(new Float((cntMaxLoad - cntActModifiedLoad) / 100.0F)) + " free space.";
      s+="\n";
    } else {
      s="no ships.";
    }
    StringSelection strSel = new StringSelection(s);
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);
  }
	
}
