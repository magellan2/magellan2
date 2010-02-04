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
public class UnitContainerNodeWrapper implements CellObject, SupportsClipboard {
  private UnitContainer uc = null;
  private boolean showFreeLoad = false;
  private boolean hasOwner = false;

  public UnitContainerNodeWrapper(UnitContainer uc) {
    // this(uc, false);
    this(uc, true, false);
  }

  /**
   * Creates a new UnitContainerNodeWrapper object.
   */
  public UnitContainerNodeWrapper(UnitContainer uc, boolean showFreeLoad) {
    this(uc, showFreeLoad, false);
  }

  public UnitContainerNodeWrapper(UnitContainer uc, boolean showFreeLoad, boolean hasOwner) {
    this.uc = uc;
    this.showFreeLoad = showFreeLoad;
    this.hasOwner = hasOwner;
  }

  /**
   * DOCUMENT-ME
   */
  public UnitContainer getUnitContainer() {
    return uc;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public String toString() {
    // TODO (stm 2007-03-16) possible design problem here:
    // in some NodeWrappers the string is set from outside (e.g. in EMapDetailsPanel)
    // sometimes it is built here
    final NumberFormat weightNumberFormat = NumberFormat.getNumberInstance();
    StringBuffer text = new StringBuffer(uc.toString());
    if (showFreeLoad && uc instanceof Ship) {
      text.append(": ");

      Ship s = (Ship) uc;
      float free = (s.getMaxCapacity() - s.getModifiedLoad()) / 100F;
      text.append(weightNumberFormat.format(free));

      float pFree = 0;
      if (s.getShipType().getMaxPersons() >= 0) {
        int personWeight = 10;
        int silverPerWeightUnit = 100;
        pFree =
            (s.getMaxPersons() * personWeight * silverPerWeightUnit - s.getModifiedPersonLoad()) / 100F;
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

  private static Map<ID, List<String>> iconNamesLists = new Hashtable<ID, List<String>>();

  /**
   * DOCUMENT-ME
   */
  public List<String> getIconNames() {
    ID key = uc.getType().getID();
    List<String> iconNames = UnitContainerNodeWrapper.iconNamesLists.get(key);

    if (iconNames == null) {
      iconNames = Collections.singletonList(StringFactory.getFactory().intern(key.toString()));
      UnitContainerNodeWrapper.iconNamesLists.put(key, iconNames);
    }

    return iconNames;
  }

  /**
   * DOCUMENT-ME
   */
  public boolean emphasized() {
    return false;
  }

  /**
   * DOCUMENT-ME
   */
  public void propertiesChanged() {
  }

  /**
   * DOCUMENT-ME
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
   * DOCUMENT-ME
   */
  public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
    return null;
  }

  /**
   * DOCUMENT-ME
   */
  public NodeWrapperDrawPolicy init(Properties settings, String prefix,
      NodeWrapperDrawPolicy adapter) {
    return null;
  }
}
