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

package magellan.client.swing.tree;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import magellan.library.ID;
import magellan.library.Ship;
import magellan.library.UnitContainer;
import magellan.library.utils.Resources;
import magellan.library.utils.StringFactory;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 393 $
 */
public class UnitContainerNodeWrapper extends DefaultNodeWrapper implements SupportsClipboard {
  private UnitContainer uc;
  private boolean showFreeLoad;
  private boolean hasOwner;
  private String prefix;

  /**
   * Creates a new container node wrapper.
   */
  public UnitContainerNodeWrapper(UnitContainer uc) {
    // this(uc, false);
    this(uc, true, false, null);
  }

  /**
   * Creates a new UnitContainerNodeWrapper object.
   *
   * @param uc
   * @param showFreeLoad If this is true, the free space is returned in the text
   */
  public UnitContainerNodeWrapper(UnitContainer uc, boolean showFreeLoad) {
    this(uc, showFreeLoad, false, null);
  }

  /**
   * Creates a new UnitContainerNodeWrapper object.
   *
   * @param uc
   * @param showFreeLoad If this is true, the free space is returned in the text
   * @param hasOwner if <code>true</code>, the text indicates that the node is displayed for the
   *          owner
   * @param prefix Text that is prepended the container name
   */
  public UnitContainerNodeWrapper(UnitContainer uc, boolean showFreeLoad, boolean hasOwner,
      String prefix) {
    this.uc = uc;
    this.showFreeLoad = showFreeLoad;
    this.hasOwner = hasOwner;
    this.prefix = prefix;
  }

  /**
   * @return the corresponding unit container
   */
  public UnitContainer getUnitContainer() {
    return uc;
  }

  /**
   * Returns a text like "Name: free capacity; free person capacity (!!!) (Besitzer)"
   */
  @Override
  public String toString() {
    // NOTE:
    // in some NodeWrappers the string is set from outside (e.g. in EMapDetailsPanel)
    // sometimes it is built here
    final NumberFormat weightNumberFormat = NumberFormat.getNumberInstance();
    StringBuffer text = new StringBuffer();
    if (prefix != null) {
      text.append(prefix);
    }
    text.append(uc.toString());
    if (showFreeLoad && uc instanceof Ship) {
      text.append(": ");

      Ship s = (Ship) uc;
      float free = (s.getMaxCapacity() - s.getModifiedLoad()) / 100F;
      text.append(weightNumberFormat.format(free));

      if (s.getModifiedMaxCapacity() != s.getMaxCapacity()) {
        free = (s.getModifiedMaxCapacity() - s.getModifiedLoad()) / 100F;
        text.append(" (" + weightNumberFormat.format(free) + ")");
      }

      float pFree = 0;
      if (s.getShipType().getMaxPersons() >= 0) {
        int personWeight = 10;
        int silverPerWeightUnit = 100;
        pFree =
            (s.getMaxPersons() * personWeight * silverPerWeightUnit - s.getModifiedPersonLoad())
                / 100F;
        text.append("; ");
        text.append(weightNumberFormat.format(pFree));
      }

      // overloading
      if (free < 0 || pFree < 0) {
        text.append(" (!!!)");
      }
    }
    if (hasOwner) {
      text.append(" (" + Resources.get("tree.unitcontainernodewrapper.owner") + ")");
    }
    return text.toString();
  }

  /** A cache for the quasi-static icon names */
  private static Map<ID, List<String>> iconNamesLists = new Hashtable<ID, List<String>>();

  /**
   * @see magellan.client.swing.tree.CellObject#getIconNames()
   */
  public List<String> getIconNames() {
    ID key = uc.getType().getID();
    List<String> iconNames = UnitContainerNodeWrapper.iconNamesLists.get(key);

    if (iconNames == null) {
      iconNames =
          Collections.singletonList(StringFactory.getFactory().intern(uc.getType().getIcon()));
      UnitContainerNodeWrapper.iconNamesLists.put(key, iconNames);
    }

    return iconNames;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#emphasized()
   */
  @Override
  public boolean emphasized() {
    return false;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#propertiesChanged()
   */
  public void propertiesChanged() {
    // no changeable properties
  }

  /**
   * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
   */
  public String getClipboardValue() {
    // Fiete: I prefer to have just the same in the clipboard like in
    // the tree (toString())
    return toString();

    // old:

    /**
     * if(this.uc != null) { return uc.toString(); } else { return toString(); }
     **/
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return null;
  }

  /**
   * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String,
   *      magellan.client.swing.tree.NodeWrapperDrawPolicy)
   */
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy adapter) {
    return null;
  }
}
