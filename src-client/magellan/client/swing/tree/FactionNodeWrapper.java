/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.swing.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import magellan.library.Alliance;
import magellan.library.Faction;
import magellan.library.ID;
import magellan.library.Region;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Displays a faction node with alliance icon.
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class FactionNodeWrapper extends EmphasizingImpl implements CellObject2, SupportsClipboard,
    SupportsEmphasizing {
  private static final Logger log = Logger.getInstance(FactionNodeWrapper.class);
  private Faction faction = null;
  private Region region = null;
  private List<GraphicsElement> GEs = null;
  private int amount = -1;

  /**
   * This Map is used to respond dynamically to changes of the currently active alliance-state that
   * changes, when the user changes between units/factions/groups etc. in EMapOverviewPanel. Don't
   * change it's contents here!
   */
  private Map<ID, Alliance> activeAlliances;

  /**
   * Creates a new FactionNodeWrapper object.
   * 
   * @param f
   * @param r
   * @param activeAlliances
   */
  public FactionNodeWrapper(Faction f, Region r, Map<ID, Alliance> activeAlliances) {
    if (f == null)
      throw new NullPointerException();
    this.activeAlliances = activeAlliances;
    this.faction = f;
    this.region = r;
  }

  /**
   * Returns the faction represented by this node.
   */
  public Faction getFaction() {
    return faction;
  }

  /**
   * @deprecated used by nobody
   */
  public Region getRegion() {
    return region;
  }

  /**
   * DOCUMENT-ME
   */
  public Alliance getAlliance(ID faction) {
    return activeAlliances.get(faction);
  }

  /**
   * Changes the amount associated with this node.
   */
  public void setAmount(int amount) {
    this.amount = amount;
  }

  /**
   * Returns the amount associated with this node.
   */
  public int getAmount() {
    return this.amount;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if (amount == -1) {
      return faction.toString();
    } else {
      return faction.toString() + ": " + amount;
    }
  }

  /** to stay compatible to CellObject */

  // pavkovic 2003.10.01: prevent multiple Lists to be generated for nearly static code
  private static Map<String, List<String>> iconNamesLists = new Hashtable<String, List<String>>();

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    if (activeAlliances == null) {
      // this should never happen !!
      FactionNodeWrapper.log
          .warn("Found activeAlliances-map to be null in FactionNodeWrapper.getGraphicsElements()! Please report to an magellan developer.");

      return null;
    }

    Alliance alliance = activeAlliances.get(faction.getID());
    String key;

    if (alliance == null) {
      key = "alliancestate_0";
    } else {
      // This is a workaround and indicates, that this faction
      // is that one upon whose alliances the activeAlliances depends
      if (alliance.getState() == Integer.MAX_VALUE) {
        key = "alliancestate_basisfaction";
      } else {
        key = "alliancestate_" + alliance.getState();
      }
    }

    List<String> iconNames = FactionNodeWrapper.iconNamesLists.get(key);

    if (iconNames == null) {
      iconNames = Collections.singletonList(key);
      FactionNodeWrapper.iconNamesLists.put(key, iconNames);
    }

    return iconNames;
  }

  /**
   * Does nothing.
   * 
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
  }

  /**
   * Creates a graphics element consisting of an alliance icon and the content of
   * {@link #toString()}.
   * 
   * @see magellan.client.swing.tree.CellObject2#getGraphicsElements()
   */
  public List<GraphicsElement> getGraphicsElements() {
    if (GEs == null) {
      GEs = new ArrayList<GraphicsElement>();
    } else {
      GEs.clear();
    }

    String icon = null;
    String tooltip = null;

    if (activeAlliances == null) {
      // this should never happen !!
      FactionNodeWrapper.log
          .warn("Warning: Found activeAlliances-map to be null in FactionNodeWrapper.getGraphicsElements()! Please report to an magellan developer.");
    } else {
      Alliance alliance = activeAlliances.get(faction.getID());

      if (alliance != null) {
        // This is a workaround and indicates, that this faction
        // is that one upon whose alliances the activeAlliances depends
        if (alliance.getState() == Integer.MAX_VALUE) {
          icon = "alliancestate_basisfaction";
          tooltip = Resources.get("tree.factionnodewrapper.basis");
        } else {
          icon = "alliancestate_" + alliance.getState();
          tooltip = Resources.get("tree.factionnodewrapper.allied") + alliance.stateToString();
        }
      } else {
        icon = "alliancestate_0";
        tooltip = Resources.get("tree.factionnodewrapper.neutral");
      }
    }

    GraphicsElement ge = new FactionGraphicsElement(toString(), icon);
    ge.setTooltip(tooltip);
    ge.setType(GraphicsElement.MAIN);
    GEs.add(ge);

    return GEs;
  }

  /**
   * Return <code>false</code>.
   * 
   * @see magellan.client.swing.tree.CellObject2#reverseOrder()
   */
  public boolean reverseOrder() {
    return false;
  }

  protected class FactionGraphicsElement extends GraphicsElement {
    /**
     * Creates a new FactionGraphicsElement object.
     */
    public FactionGraphicsElement(String text, String icon) {
      super(text, null, null, icon);
    }

    /**
     * @see magellan.client.swing.tree.GraphicsElement#isEmphasized()
     */
    @Override
    public boolean isEmphasized() {
      return emphasized();
    }
  }

  /**
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  public String getClipboardValue() {
    if (faction != null) {
      return faction.getName();
    } else {
      return toString();
    }
  }

  /**
   * Returns <code>null</code>.
   * 
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return null;
  }

  /**
   * Returns <code>null</code>.
   */
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy adapter) {
    return null;
  }
}
