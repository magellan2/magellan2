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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.swing.tree.UnitNodeWrapper;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.RegionType;
import magellan.library.utils.Islands;
import magellan.library.utils.Regions;
import magellan.library.utils.Resources;

/**
 * A context menu for Islands.
 * 
 * @author fiete
 */
public class PathfinderMapContextMenu extends JMenu implements SelectionListener, GameDataListener {
  // private static final NumberFormat weightNumberFormat = NumberFormat.getNumberInstance();

  private static final int MOVE_NACH = 1;
  private static final int MOVE_ROUTE = 2;
  private static final int MOVE_ROUTEBACK = 3;

  private EventDispatcher dispatcher = null;

  private GameData data;

  /**
   * nicht die selektierten Regionen, sondern die angewählten Objekte im Tree (mehrere units
   * z.B....)
   */
  private Collection<?> selectedObjects;

  /**
   * wahr, wenn unter den selektierten Objekten min 1 Unit ist (nur dann macht dieses Menü sinn
   */
  private boolean unitsSelected = false;

  /**
   * Die Zielregion....
   */
  private Region destRegion = null;

  /**
   * ist die Zielregion eine Küstenregion oder eine Ozeanregion?
   */
  private boolean isShipableRegion = false;

  private JMenuItem nachLand = null;
  private JMenuItem routeLand = null;
  private JMenuItem routebackLand = null;
  private JMenuItem nachSea = null;
  private JMenuItem routeSea = null;
  private JMenuItem routebackSea = null;

  /**
   * Creates a new UnitContainerContextMenu object.
   */
  public PathfinderMapContextMenu(EventDispatcher dispatcher) {
    super(Resources.get("context.mapcontextmenu.menu.pathfinder"));
    this.dispatcher = dispatcher;

    nachLand = new JMenuItem(Resources.get("context.mapcontextmenu.menu.pathfinder.land.move"));
    nachLand.setEnabled(false);
    nachLand.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doNachLand(MOVE_NACH);
      }
    });
    add(nachLand);

    routeLand = new JMenuItem(Resources.get("context.mapcontextmenu.menu.pathfinder.land.route"));
    routeLand.setEnabled(false);
    routeLand.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doNachLand(MOVE_ROUTE);
      }
    });
    add(routeLand);

    routebackLand =
        new JMenuItem(Resources.get("context.mapcontextmenu.menu.pathfinder.land.routeback"));
    routebackLand.setEnabled(false);
    routebackLand.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doNachLand(MOVE_ROUTEBACK);
      }
    });
    add(routebackLand);

    nachSea = new JMenuItem(Resources.get("context.mapcontextmenu.menu.pathfinder.land.move"));
    nachSea.setEnabled(false);
    nachSea.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doNachSea(MOVE_NACH);
      }
    });
    add(nachSea);

    routeSea = new JMenuItem(Resources.get("context.mapcontextmenu.menu.pathfinder.land.route"));
    routeSea.setEnabled(false);
    routeSea.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doNachSea(MOVE_ROUTE);
      }
    });
    add(routeSea);

    routebackSea =
        new JMenuItem(Resources.get("context.mapcontextmenu.menu.pathfinder.land.routeback"));
    routebackSea.setEnabled(false);
    routebackSea.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doNachSea(MOVE_ROUTEBACK);
      }
    });
    add(routebackSea);

  }

  /**
   * versucht, Aktion umzusetzen: Landbewegung
   * 
   * @param mode
   */
  private void doNachLand(int mode) {
    Region actRegion = null;
    String path = "";
    List<Region> regionList = null;
    Map<ID, RegionType> excludeMap = Regions.getNonLandRegionTypes(data.rules);
    for (Unit u : getSelectedUnits()) {
      if (onSameIsland(u.getRegion(), destRegion)) {
        if (actRegion == null || !u.getRegion().equals(actRegion)) {
          // String path = Regions.getDirections(u.getScriptMain().gd_ScriptMain.regions(), act,
          // dest, excludeMap);
          actRegion = u.getRegion();
          int streetRadius =
              data.rules.getGameSpecificStuff().getMovementEvaluator().getModifiedRadius(u, true);
          if (streetRadius == 0) {
            streetRadius = 2;
          }
          int radius =
              data.rules.getGameSpecificStuff().getMovementEvaluator().getModifiedRadius(u, false);
          if (radius == 0) {
            radius = 1;
          }
          regionList =
              Regions.getLandPath(data, actRegion.getCoordinate(), destRegion.getCoordinate(),
                  excludeMap, radius, streetRadius);
          path = Regions.getDirections(regionList);
        }
        if (path != null && path.length() > 0) {
          // Pfad gefunden
          setOrder(u, regionList, mode, path);
        }
      }
    }
  }

  /**
   * versucht, Aktion umzusetzen: Seebewegung
   * 
   * @param mode
   */
  private void doNachSea(int mode) {

    String path = "";
    List<Region> regionList = null;
    for (Unit u : getSelectedUnits()) {
      if (isSeaConnPossible(u) && u.getModifiedShip() != null) {
        regionList = Regions.planShipRoute(u.getModifiedShip(), data, destRegion.getCoordinate());
        path = Regions.getDirections(regionList);
        if (path != null && path.length() > 0) {
          // Pfad gefunden
          setOrder(u, regionList, mode, path);
        }
      }
    }
  }

  /**
   * Setzt den gefundenen path als order
   * 
   * @param u
   * @param regionList
   * @param mode
   * @param path
   */
  private void setOrder(Unit u, List<Region> regionList, int mode, String path) {
    String order = "";

    if (mode == MOVE_NACH) {
      order = PathfinderMapContextMenu.getOrder(EresseaConstants.O_MOVE) + " " + path;
    } else {
      order =
          PathfinderMapContextMenu.getOrder(EresseaConstants.O_ROUTE) + " " + path + " "
              + PathfinderMapContextMenu.getOrder(EresseaConstants.O_PAUSE);
    }
    if (mode == MOVE_ROUTEBACK) {
      // Route zurück ergänzen
      Collections.reverse(regionList);
      order += " " + Regions.getDirections(regionList);
      order += " " + PathfinderMapContextMenu.getOrder(EresseaConstants.O_PAUSE);
      Collections.reverse(regionList);
    }

    // ?? alle anderen langen Befehle löschen?
    // ToDo
    // Order setzen, anderes NACH ersetzen
    u.addOrder(order, true, 1);
    // Fiete: addOn : give as comment number of regions to travel
    u.addOrder("; path is " + (regionList.size() - 1) + " regions long.", true, 1);
    dispatcher.fire(new UnitOrdersEvent(this, u));
  }

  /**
   * enables / disables according to selectedObjects and given Region
   * 
   * @param r
   */
  public void updateMenu(Region r) {
    setDestRegion(r);
    // System.out.println("updating PathfinderMapCM" );
    if (!unitsSelected) {
      // Keine Units in der selektion
      setEnabled(false);
    } else {
      // Eine oder mehrere Units in der Selektion
      // checken, ob LandVerbindung existiert...
      setEnabled(true);
      int i = countLandConnPossible(getSelectedUnits());
      if (i == 0) {
        nachLand.setEnabled(false);
        nachLand.setText(Resources.get("context.mapcontextmenu.menu.pathfinder.land.move"));
        routeLand.setEnabled(false);
        routeLand.setText(Resources.get("context.mapcontextmenu.menu.pathfinder.land.route"));
        routebackLand.setEnabled(false);
        routebackLand.setText(Resources
            .get("context.mapcontextmenu.menu.pathfinder.land.routeback"));
      } else if (i == 1) {
        // genau eine unit
        nachLand.setText(i + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.unit")
            + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.land.move"));
        nachLand.setEnabled(true);
        routeLand.setText(i + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.unit")
            + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.land.route"));
        routeLand.setEnabled(true);
        routebackLand.setText(i + " "
            + Resources.get("context.mapcontextmenu.menu.pathfinder.unit") + " "
            + Resources.get("context.mapcontextmenu.menu.pathfinder.land.routeback"));
        routebackLand.setEnabled(true);
      } else {
        // mehrere units
        nachLand.setText(i + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.units")
            + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.land.move"));
        nachLand.setEnabled(true);
        routeLand.setText(i + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.units")
            + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.land.route"));
        routeLand.setEnabled(true);
        routebackLand.setText(i + " "
            + Resources.get("context.mapcontextmenu.menu.pathfinder.units") + " "
            + Resources.get("context.mapcontextmenu.menu.pathfinder.land.routeback"));
        routebackLand.setEnabled(true);
      }

      // checken, on schiffsverbindung angezeigt werden soll...
      // Nur wenn Kapitän
      // Nur wenn Zielregion am Wasser
      i = countSeaConnPossible(getSelectedUnits());
      if (i == 0) {
        nachSea.setEnabled(false);
        nachSea.setText(Resources.get("context.mapcontextmenu.menu.pathfinder.sea.move"));
        routeSea.setEnabled(false);
        routeSea.setText(Resources.get("context.mapcontextmenu.menu.pathfinder.sea.route"));
        routebackSea.setEnabled(false);
        routebackSea.setText(Resources.get("context.mapcontextmenu.menu.pathfinder.sea.routeback"));
      } else if (i == 1) {
        // genau eine unit
        nachSea.setText(i + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.unit")
            + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.sea.move"));
        nachSea.setEnabled(true);
        routeSea.setText(i + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.unit")
            + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.sea.route"));
        routeSea.setEnabled(true);
        routebackSea.setText(i + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.unit")
            + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.sea.routeback"));
        routebackSea.setEnabled(true);
      } else {
        // mehrere units
        nachSea.setText(i + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.units")
            + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.sea.move"));
        nachSea.setEnabled(true);
        routeSea.setText(i + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.units")
            + " " + Resources.get("context.mapcontextmenu.menu.pathfinder.sea.route"));
        routeSea.setEnabled(true);
        routebackSea.setText(i + " "
            + Resources.get("context.mapcontextmenu.menu.pathfinder.units") + " "
            + Resources.get("context.mapcontextmenu.menu.pathfinder.sea.routeback"));
        routebackSea.setEnabled(true);
      }

    }
  }

  /**
   * Prüfen, für wieviele Units eine Landverbindung möglich ist
   * 
   * @param c
   * @return
   */
  private int countLandConnPossible(Collection<Unit> c) {
    int erg = 0;
    for (Unit u : c) {
      if (isLandConnPossible(u)) {
        erg++;
      }
    }
    return erg;
  }

  /**
   * Prüfen, für wieviele Units eine Landverbindung möglich ist
   * 
   * @param c
   * @return
   */
  private int countSeaConnPossible(Collection<Unit> c) {
    int erg = 0;
    for (Unit u : c) {
      if (isSeaConnPossible(u)) {
        erg++;
      }
    }
    return erg;
  }

  /**
   * Prüfen ob für diese Unit eine Landverbindung zur destRegion möglich ist
   */
  private boolean isLandConnPossible(Unit u) {
    // oder benötigt...bei gleicher Region->false!
    // Region extrahieren
    Region originRegion = u.getRegion();
    // prinzipiell Möglich, wenn auf gleicher Insel
    // und nicht in gleicher Region
    if (originRegion.equals(destRegion))
      return false;
    // beide Region sollten kein Ozean sein
    if (originRegion.getRegionType().isOcean() || destRegion.getRegionType().isOcean())
      return false;
    // wenn jetzt noch auf gleicher Insel: OK
    if (onSameIsland(originRegion, destRegion))
      return true;
    return false;
  }

  /**
   * Prüfen ob für diese Unit eine Seeverbindung zur destRegion prinzipiell möglich ist
   */
  private boolean isSeaConnPossible(Unit u) {
    // oder benötigt...bei gleicher Region->false!
    // Region extrahieren
    Region originRegion = u.getRegion();
    // nicht in gleicher Region
    if (originRegion.equals(destRegion))
      return false;
    // Unit muss Kapitän sein
    boolean capt = false;
    UnitContainer uc = u.getModifiedUnitContainer();
    if (uc != null) {
      if (uc instanceof Ship) {
        Ship s = (Ship) uc;
        if (s.getOwner() != null && s.getOwner().equals(u)) {
          capt = true;
        }
      }
    }
    if (!capt)
      // Kapitän
      return false;

    // Zielregion muss am Meer liegen oder Ozean sein
    if (!isShipableRegion)
      return false;

    // Alle Fehler (prinzipiell) ausgeschlossen (?)

    return true;
  }

  private boolean onSameIsland(Region r1, Region r2) {
    Collection<Region> island = new LinkedList<Region>();
    Map<CoordinateID, ? extends Region> m = Islands.getIsland(data.rules, data.regions(), r1);
    if (m != null) {
      island.addAll(m.values());
      island.remove(r1);
      if (island.contains(r2))
        return true;
    }
    return false;
  }

  /**
   * liefert nur die Units in den selectedObjects
   * 
   * @return
   */
  private List<Unit> getSelectedUnits() {
    ArrayList<Unit> erg = new ArrayList<Unit>();
    if (selectedObjects != null && selectedObjects.size() > 0) {
      for (Object o : selectedObjects) {
        if (o instanceof Unit) {
          addToList(erg, ((Unit) o));
        } else if (o instanceof UnitNodeWrapper) {
          addToList(erg, ((UnitNodeWrapper) o).getUnit());
        } else if (o instanceof Ship) {
          if (((Ship) o).getOwner() != null) {
            addToList(erg, ((Ship) o).getOwner());
          }
        }
      }
    }
    if (erg.size() > 0)
      return erg;
    // don't return null, it's bad style
    return Collections.emptyList();
  }

  private void addToList(ArrayList<Unit> list, Unit u) {
    if (u != null && list != null && !list.contains(u)) {
      list.add(u);
    }
  }

  /**
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent e) {
    // rausfinden, ob units selektiert sind
    if (e.getSelectedObjects() != null) {
      selectedObjects = e.getSelectedObjects();
    }

    // Enable
    if (getSelectedUnits() != null) {
      unitsSelected = true;
    } else {
      unitsSelected = false;
    }
  }

  /**
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  public void gameDataChanged(GameDataEvent e) {
    data = e.getGameData();
  }

  /**
   * Returns the value of destRegion.
   * 
   * @return Returns destRegion.
   */
  public Region getDestRegion() {
    return destRegion;
  }

  /**
   * Sets the value of destRegion. Calculates isShipableRegion
   * 
   * @param destRegion The value for destRegion.
   */
  public void setDestRegion(Region destRegion) {
    if (this.destRegion == null || !destRegion.equals(this.destRegion)) {
      isShipableRegion = false;
      if (destRegion.getRegionType().isOcean()) {
        isShipableRegion = true;
      } else {
        // run through the neighbors
        for (Region n : destRegion.getNeighbors().values()) {
          if (n.getRegionType().isOcean()) {
            isShipableRegion = true;
            break;
          }
        }
      }
    }

    this.destRegion = destRegion;

  }

  /**
   * Returns a translation for the specified order key.
   */
  private static String getOrder(String key) {
    return Resources.getOrderTranslation(key);
  }

}
