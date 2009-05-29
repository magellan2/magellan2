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

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.FactionStatsDialog;
import magellan.client.swing.GiveOrderDialog;
import magellan.client.swing.InternationalizedDialog;
import magellan.client.swing.RoutingDialog;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Identifiable;
import magellan.library.IntegerID;
import magellan.library.Island;
import magellan.library.Named;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.PropertiesHelper;
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
		JMenuItem name = new JMenuItem(getCaption());
		if (selectedObjects.contains(uc))
		  name.setEnabled(false);
		name.addActionListener(new ActionListener() {
    
      public void actionPerformed(ActionEvent e) {
        dispatcher.fire(new SelectionEvent<UnitContainer>(UnitContainerContextMenu.this, null, uc));
      }
    });
		add(name);

    if (!selectedObjects.contains(uc))
      return;

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

      JMenuItem addToIsland = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.addToIsland.caption"));
      addToIsland.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            addToIsland();
          }
        });
      add(addToIsland);

      JMenuItem removeFromIsland = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.removeFromIsland.caption"));
      removeFromIsland.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            removeFromIsland();
          }
        });
      add(removeFromIsland);
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
  
  protected void removeFromIsland() {
    boolean changed = false;
    for (Object o : selectedObjects){
      if (o instanceof Region){
        changed = true;
        ((Region) o).setIsland(null);
      }
    }
    if (changed)
      dispatcher.fire(new GameDataEvent(this,data));
  }

  public Island newIsland = null;

	protected class AddToIslandDialog extends InternationalizedDialog {
	  private JComboBox islandBox;
	  private JButton ok;
	  private JButton cancel;

	  
	  /**
	   * Creates a new GiveOrderDialog object.
	   *
	   * 
	   */
	  public AddToIslandDialog(Frame owner, String caption) {
	    super(owner, true);
	    setTitle(Resources.get("addtoislanddialog.window.title"));

	    Container cp = getContentPane();
	    cp.setLayout(new GridBagLayout());

	    GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
	        GridBagConstraints.BOTH,
	        new Insets(3, 3, 3, 3), 0, 0);

	    c.gridwidth=2;
	    JLabel captionLabel = new JLabel(caption);
	    captionLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
	    captionLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    captionLabel.setHorizontalTextPosition(SwingConstants.CENTER);
	    cp.add(captionLabel, c);

	    c.gridwidth=1;
	    c.gridy++;

	    cp.add(new JLabel(Resources.get("addtoislanddialog.window.message")), c);

	    List<Island> islandList = new ArrayList<Island>(data.islands().size()+2);
      newIsland.setName(Resources.get("addtoislanddialog.newisland.caption"));
	   
	    islandList.add(newIsland);
	    islandList.addAll(data.islands().values());
	    islandBox = new JComboBox(islandList.toArray());

	    c.gridx=1;
	    cp.add(islandBox, c);
	    
	    c.gridx=0;
	    
	    ok = new JButton(Resources.get("giveorderdialog.btn.ok.caption"));
	    ok.setMnemonic(Resources.get("giveorderdialog.btn.ok.mnemonic").charAt(0));

	    // actionListener is added in the show() method
	    c.gridy++;
	    c.anchor = GridBagConstraints.EAST;
	    cp.add(ok, c);

	    cancel = new JButton(Resources.get("giveorderdialog.btn.cancel.caption"));
	    cancel.setMnemonic(Resources.get("giveorderdialog.btn.cancel.mnemonic").charAt(0));
	    cancel.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        quit();
	      }
	    });
	    c.gridx = 1;
	    c.anchor = GridBagConstraints.WEST;
	    cp.add(cancel, c);

	  }

    public Island showDialog() {
      final Island[] retVal = new Island[1];
      ActionListener okButtonAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          retVal[0] = (Island) islandBox.getSelectedItem();
          quit();
        }
      };

      ok.addActionListener(okButtonAction);
      // order.addActionListener(okButtonAction);
      pack();
      setLocationRelativeTo(getOwner());
      setVisible(true);

      return retVal[0];
    }

	}
	
  protected void addToIsland() {
    for (int i=1; newIsland == null ;++i){
      if (data.getIsland(IntegerID.create(i))==null){
        newIsland = MagellanFactory.createIsland(IntegerID.create(i), data);
      }
    }
    AddToIslandDialog dialog = new AddToIslandDialog(JOptionPane.getFrameForComponent(this), getCaption());
    Island island = dialog.showDialog();
    if (island!=null){
      if (island==newIsland) {
        data.addIsland(newIsland);
        island.setName(island.getID().toString());
      }
      for (Object o : selectedObjects){
        if (o instanceof Region){
          ((Region) o).setIsland(island);
        }
      }
      dispatcher.fire(new GameDataEvent(this,data));
    }
  }

  private String getCaption() {
    return uc.toString();
  }

  private void initContextMenuProviders(UnitContainer unitContainer) {
    Collection<UnitContainerContextMenuProvider> cmpList = getContextMenuProviders();
    if (!cmpList.isEmpty()) {
      addSeparator();
    }
    
    for (UnitContainerContextMenuProvider cmp : cmpList) {
      add(cmp.createContextMenu(dispatcher, data, unitContainer, selectedObjects));
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
    StringBuffer idString = new StringBuffer("");

    for (Iterator iter = selectedObjects.iterator(); iter.hasNext();) {
      Object o = iter.next();
      if (o instanceof Identifiable){
        Identifiable idf = (Identifiable) o;
        idString.append(idf.getID());
        if (iter.hasNext()) {
          idString.append(" ");
        }
      }
    }

    StringSelection strSel = new StringSelection(idString.toString());
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);
	}

	/**
	 * Copies name and id to the sytem clipboard.
	 */
	private void copyNameID() {
    StringBuffer idString = new StringBuffer("");

    for (Iterator iter = selectedObjects.iterator(); iter.hasNext();) {
      Object o = iter.next();
      if (o instanceof Named){
        idString.append(((Named)o).getName());
        idString.append(" (");
        idString.append(((Named)o).getID());
        idString.append(")\n");
      }
    }

    StringSelection strSel = new StringSelection(idString.toString());
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);
	}
  
  /**
   * Copies name to the sytem clipboard.
   */
  private void copyName() {
    StringBuffer idString = new StringBuffer("");

    for (Iterator iter = selectedObjects.iterator(); iter.hasNext();) {
      Object o = iter.next();
      if (o instanceof Named){
        idString.append(((Named)o).getName() + "\n");
      }
    }

    StringSelection strSel = new StringSelection(uc.getName());
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);
  }

  /**
   * Copies name and id and regionID to the sytem clipboard.
   */
  private void copyNameIDRegionID() {
    Region r = (Region)uc;
    StringSelection strSel = new StringSelection(uc.toString() + " (" + Integer.toString((int)r.getUID(),r.getData().base).replace("l","L") + ")");
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);
  }
  
  /**
   * Copies name and id and regionID to the sytem clipboard.
   */
  private void copyNameRegionID() {
    Region r = (Region)uc;
    StringSelection strSel = new StringSelection(r.getName() + " (" + Integer.toString((int)r.getUID(),r.getData().base).replace("l","L") + ")");
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
		GiveOrderDialog giveOderDialog = new GiveOrderDialog(JOptionPane.getFrameForComponent(this), getCaption());
		String s[] = giveOderDialog.showGiveOrderDialog();
		for(Iterator iter = this.selectedObjects.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof Ship){
				Ship ship = (Ship)o;
				Unit u = ship.getOwnerUnit();

				if(u!=null && (isEditAll() || magellan.library.utils.Units.isPrivilegedAndNoSpy(u))) {
					magellan.client.utils.Units.addOrders(u, s);
					dispatcher.fire(new UnitOrdersEvent(this, u));
				}
			}
		}
	}
	
  private boolean isEditAll(){
    return settings.getProperty(PropertiesHelper.ORDEREDITOR_EDITALLFACTIONS).equals("true");
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
