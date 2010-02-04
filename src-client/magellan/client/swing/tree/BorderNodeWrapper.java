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

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import magellan.library.Border;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class BorderNodeWrapper implements CellObject, SupportsClipboard {
  private Border border = null;
  private List<String> iconNames = null;

  /**
   * Creates a new BorderNodeWrapper object.
   */
  public BorderNodeWrapper(Border border) {
    this.border = border;
  }

  /**
   * DOCUMENT-ME
   */
  public Border getBorder() {
    return border;
  }

  /**
   * DOCUMENT-ME
   */
  @Override
  public String toString() {
    return border.toString();
  }

  /**
   * DOCUMENT-ME
   */
  public List<String> getIconNames() {
    if (iconNames == null) {
      iconNames = Collections.singletonList(border.getType());
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

  /**
   * DOCUMENT-ME
   */
  public String getClipboardValue() {
    if (border != null)
      return border.toString();
    else
      return toString();
  }
}
