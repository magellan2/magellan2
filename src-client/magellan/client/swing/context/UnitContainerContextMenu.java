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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.HasRegion;
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
import magellan.library.utils.Taggable;

/**
 * A context menu for UnitContainers like ships or buildings. Providing copy ID and copy ID+name.
 * 
 * @author Ulrich Küster
 */
public class UnitContainerContextMenu extends JPopupMenu {
  private static final NumberFormat weightNumberFormat = NumberFormat.getNumberInstance();
  private UnitContainer uc;
  private EventDispatcher dispatcher;
  private GameData data;
  private Properties settings;
  private Collection<?> selectedObjects;

  /**
   * Creates a new UnitContainerContextMenu object.
   */
  public UnitContainerContextMenu(UnitContainer uc, EventDispatcher dispatcher, GameData data,
      Properties settings, Collection<?> selectedObjects) {
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
    // if (selectedObjects.contains(uc))
    // name.setEnabled(false);
    name.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispatcher.fire(SelectionEvent.create(UnitContainerContextMenu.this, uc,
            SelectionEvent.ST_DEFAULT));
      }
    });
    add(name);

    if (!selectedObjects.contains(uc))
      return;

    JMenuItem copyID =
        new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.copyid.caption"));
    copyID.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copyID();
      }
    });
    add(copyID);

    JMenuItem copyName =
        new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.copyname.caption"));
    copyName.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copyName();
      }
    });
    add(copyName);

    JMenuItem copyNameID =
        new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.copyidandname.caption"));
    copyNameID.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copyNameID();
      }
    });
    add(copyNameID);

    // context.unitcontainercontextmenu.menu.copyidandnameanduid.caption
    if (uc instanceof Region) {
      Region r = (Region) uc;
      if (r.hasUID()) {
        JMenuItem copyNameRegionID =
            new JMenuItem(Resources
                .get("context.unitcontainercontextmenu.menu.copynameanduid.caption"));
        copyNameRegionID.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            copyNameRegionID();
          }
        });
        add(copyNameRegionID);

        JMenuItem copyNameIDRegionID =
            new JMenuItem(Resources
                .get("context.unitcontainercontextmenu.menu.copyidandnameanduid.caption"));
        copyNameIDRegionID.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            copyNameIDRegionID();
          }
        });
        add(copyNameIDRegionID);
      }

      JMenuItem addToIsland =
          new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.addToIsland.caption"));
      addToIsland.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addToIsland();
        }
      });
      add(addToIsland);

      JMenuItem removeFromIsland =
          new JMenuItem(Resources
              .get("context.unitcontainercontextmenu.menu.removeFromIsland.caption"));
      removeFromIsland.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeFromIsland();
        }
      });
      add(removeFromIsland);
    }

    if (uc instanceof Ship) {
      addSeparator();
      JMenuItem planShipRoute =
          new JMenuItem(Resources
              .get("context.unitcontainercontextmenu.menu.planshiproute.caption"));
      planShipRoute.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          planShipRoute();
        }
      });
      planShipRoute.setEnabled(ShipRoutePlanner.canPlan((Ship) uc));
      add(planShipRoute);
    } else if (uc instanceof Faction) {
      addSeparator();
      JMenuItem copyMail =
          new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.copymail.caption"));
      copyMail.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          copyMail();
        }
      });

      JMenuItem factionStats =
          new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.factionstats.caption"));
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
    if (selectedObjects != null) {
      for (Object o : selectedObjects) {
        if (o instanceof Ship) {
          shipsInSelection = true;
          break;
        }
      }
    }
    if (shipsInSelection) {
      JMenuItem shipOrders =
          new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.shiporders.caption"));
      shipOrders.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_addShipOrder();
        }
      });
      add(shipOrders);

      JMenuItem shipList =
          new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.shiplist.caption"));
      shipList.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_shipList();
        }
      });
      add(shipList);
    }

    // add tag menu
    JMenuItem addTag = new JMenuItem(Resources.get("context.unitcontainercontextmenu.menu.addtag.caption"));
    addTag.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_addTag();
      }
    });
    add(addTag);

    initContextMenuProviders(uc);
  }

  protected void removeFromIsland() {
    boolean changed = false;
    for (Object o : selectedObjects) {
      if (o instanceof Region) {
        changed = true;
        ((Region) o).setIsland(null);
      }
    }
    if (changed) {
      dispatcher.fire(new GameDataEvent(this, data));
    }
  }

  public Island newIsland = null;

  protected class AddToIslandDialog extends InternationalizedDialog {
    private JComboBox islandBox;
    private JButton ok;
    private JButton cancel;

    private class IslandComperator implements Comparator<Island> {
      public int compare(Island o1, Island o2) {
        return o1.getName().compareTo(o2.getName());
      }
    }

    /**
     * Creates a new GiveOrderDialog object.
     */
    public AddToIslandDialog(Frame owner, String caption) {
      super(owner, true);
      setTitle(Resources.get("addtoislanddialog.window.title"));

      Container cp = getContentPane();
      cp.setLayout(new GridBagLayout());

      GridBagConstraints c =
          new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
              GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0);

      c.gridwidth = 2;
      JLabel captionLabel = new JLabel(caption);
      captionLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
      captionLabel.setHorizontalAlignment(SwingConstants.CENTER);
      captionLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      cp.add(captionLabel, c);

      c.gridwidth = 1;
      c.gridy++;

      cp.add(new JLabel(Resources.get("addtoislanddialog.window.message")), c);

      List<Island> islandList = new ArrayList<Island>(data.getIslands().size() + 2);
      newIsland.setName(Resources.get("addtoislanddialog.newisland.caption"));

      islandList.addAll(data.getIslands());

      Collections.sort(islandList, new IslandComperator());
      // add newIsland at index 0 - if List is populated
      if (islandList.size() > 0) {
        islandList.add(0, newIsland);
      } else {
        islandList.add(newIsland);
      }

      islandBox = new JComboBox<Object>(islandList.toArray());

      c.gridx = 1;
      cp.add(islandBox, c);

      c.gridx = 0;

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
    // search free ID
    for (int i = 1; newIsland == null; ++i) {
      if (data.getIsland(IntegerID.create(i)) == null) {
        newIsland = MagellanFactory.createIsland(IntegerID.create(i), data);
      }
    }
    AddToIslandDialog dialog =
        new AddToIslandDialog(JOptionPane.getFrameForComponent(this), getCaption());
    Island island = dialog.showDialog();
    if (island != null) {
      if (island == newIsland) {
        data.addIsland(newIsland);
        island.setName(island.getID().toString());
      }
      for (Object o : selectedObjects) {
        if (o instanceof Region) {
          ((Region) o).setIsland(island);
        }
      }
      dispatcher.fire(new GameDataEvent(this, data));
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
    Collection<UnitContainerContextMenuProvider> cmpList =
        new ArrayList<UnitContainerContextMenuProvider>();
    for (MagellanPlugIn plugIn : Client.INSTANCE.getPlugIns()) {
      if (plugIn instanceof UnitContainerContextMenuProvider) {
        cmpList.add((UnitContainerContextMenuProvider) plugIn);
      }
    }
    return cmpList;
  }

  /**
   * Copies the ID of the UnitContainer to the clipboard.
   */
  private void copyID() {
    StringBuffer idString = new StringBuffer("");

    for (Iterator<?> iter = selectedObjects.iterator(); iter.hasNext();) {
      Object o = iter.next();
      if (o instanceof Identifiable) {
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

    for (Object o : selectedObjects) {
      if (o instanceof Named) {
        idString.append(((Named) o).getName());
        idString.append(" (");
        idString.append(((Named) o).getID());
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

    for (Object o : selectedObjects) {
      if (o instanceof Named) {
        idString.append(((Named) o).getName() + "\n");
      }
    }

    StringSelection strSel = new StringSelection(idString.toString());
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);
  }

  /**
   * Copies name and id and regionID to the sytem clipboard.
   */
  private void copyNameIDRegionID() {
    Region r = (Region) uc;
    StringSelection strSel =
        new StringSelection(uc.toString() + " ("
            + Integer.toString((int) r.getUID(), data.base).replace("l", "L") + ")");
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);
  }

  /**
   * Copies name and id and regionID to the sytem clipboard.
   */
  private void copyNameRegionID() {
    Region r = (Region) uc;
    StringSelection strSel =
        new StringSelection(r.getName() + " ("
            + Integer.toString((int) r.getUID(), data.base).replace("l", "L") + ")");
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
    FactionStatsDialog d =
        new FactionStatsDialog(JOptionPane.getFrameForComponent(this), false, dispatcher, data,
            settings, (Faction) uc);
    d.setVisible(true);
  }

  /**
   * Plans a route for a ship (typically over several weeks)
   * 
   * @see ShipRoutingDialog
   */
  private void planShipRoute() {
    Unit unit =
        (new ShipRoutePlanner()).planShipRoute((Ship) uc, data, this, new RoutingDialog(JOptionPane
            .getFrameForComponent(this), data, false));

    if (unit != null) {
      dispatcher.fire(new UnitOrdersEvent(this, unit));
    }
  }

  /**
   * Gives an order (optional replacing the existing ones) to the selected units. Gives the orders
   * only to actual captns of selected ships
   */
  private void event_addShipOrder() {
    Collection<Unit> captains = new ArrayList<Unit>();
    for (Object o : selectedObjects) {
      if (o instanceof Ship) {
        Unit u = ((Ship) o).getModifiedOwnerUnit();

        if (u != null && (isEditAll() || magellan.library.utils.Units.isPrivilegedAndNoSpy(u))) {
          captains.add(u);
        }
      }
    }
    if (captains.size() > 0) {
      GiveOrderDialog giveOderDialog =
          new GiveOrderDialog(JOptionPane.getFrameForComponent(this), captains, data, settings,
              dispatcher);
      String s[] = giveOderDialog.showGiveOrderDialog();
      for (Unit u : captains) {
        magellan.client.utils.Units.addOrders(u, s, false);
        dispatcher.fire(new UnitOrdersEvent(this, u));
      }
    }
  }

  private boolean isEditAll() {
    return settings.getProperty(PropertiesHelper.ORDEREDITOR_EDITALLFACTIONS,
        Boolean.FALSE.toString()).equals("true");
  }

  /**
   * Copies Info about selected ships to the clipboard
   */
  private void event_shipList() {
    StringBuilder s = new StringBuilder();
    int cntShips = 0;
    int cntActModifiedLoad = 0;
    int cntMaxLoad = 0;
    for (Object o : selectedObjects) {
      if (o instanceof Ship) {
        Ship ship = (Ship) o;
        cntShips++;
        cntActModifiedLoad += ship.getModifiedLoad();
        cntMaxLoad += ship.getMaxCapacity();
        s.append(ship.toString(true));
        s.append(":");
        s.append(UnitContainerContextMenu.weightNumberFormat.format(Float.valueOf((ship
            .getMaxCapacity() - ship.getModifiedLoad()) / 100.0F)));
        s.append("\n");
      }
    }
    if (cntShips > 0) {
      s.append(cntShips).append(" ships with ").append(
          UnitContainerContextMenu.weightNumberFormat.format(Float
              .valueOf((cntMaxLoad - cntActModifiedLoad) / 100.0F))).append(" free space.");
      s.append("\n");
    } else {
      s = new StringBuilder("no ships.");
    }
    StringSelection strSel = new StringSelection(s.toString());
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);
  }

  /**
   * Adds a tag to selected objects.
   */
  private void event_addTag() {
    Map<String, Collection<String>> keys = new HashMap<String, Collection<String>>();
    Collection<Region> regions = new HashSet<Region>();

    boolean unitsSelected = false;
    boolean regionsSelected = false;
    boolean buildingsSelected = false;
    boolean shipsSelected = false;
    for (Object o : selectedObjects) {
      if (o instanceof Taggable) {
        for (String tag : ((Taggable) o).getTagMap().keySet()) {
          keys.put(tag, new HashSet<String>());
        }
      }
      if (o instanceof HasRegion) {
        regions.add(((HasRegion) o).getRegion());
      }
      if (o instanceof Unit) {
        unitsSelected = true;
      } else if (o instanceof Region) {
        regionsSelected = true;
      } else if (o instanceof Building) {
        buildingsSelected = true;
      } else if (o instanceof Ship) {
        shipsSelected = true;
      }
    }

    for (Region r : regions) {
      if (unitsSelected) {
        for (Unit u : r.units()) {
          addTags(keys, u);
        }
      }
      if (regionsSelected) {
        for (Region r2 : data.getRegions()) {
          addTags(keys, r2);
        }
      }
      if (shipsSelected) {
        for (Ship s : r.ships()) {
          addTags(keys, s);
        }
      }
      if (buildingsSelected) {
        for (Building b : r.buildings()) {
          addTags(keys, b);
        }
      }
    }

    // present key selection
    String key = UnitContextMenu.showInputDialog(dispatcher.getMagellanContext().getClient(),
        Resources.get("context.unitcontextmenu.addtag.tagname.message"), UnitContextMenu.sort(keys.keySet()));

    if ((key != null) && (key.length() > 0)) {
      String value = null;

      Collection<String> vs = keys.get(key);

      value =
          UnitContextMenu.showInputDialog(dispatcher.getMagellanContext().getClient(),
              Resources.get("context.unitcontextmenu.addtag.tagvalue.message"), UnitContextMenu.sort(vs));

      if (value != null) {
        for (Object o : selectedObjects) {
          if (o instanceof Taggable) {
            ((Taggable) o).putTag(key, value);
            // TODO: Coalesce unitordersevent
            if (o instanceof Unit) {
              dispatcher.fire(new UnitOrdersEvent(this, (Unit) o));
            }
          }
        }
      }

      selectedObjects.clear();
      uc = null;
    }
  }

  private void addTags(Map<String, Collection<String>> keys, Taggable taggable) {
    for (String tag : taggable.getTagMap().keySet()) {
      Collection<String> values = keys.get(tag);
      if (values == null) {
        keys.put(tag, values = new HashSet<String>());
      }
      values.add(taggable.getTag(tag));
    }
  }
}
