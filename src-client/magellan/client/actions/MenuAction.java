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

package magellan.client.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import magellan.client.Client;
import magellan.client.utils.ErrorWindow;
import magellan.library.utils.logging.Logger;

/**
 * A common super class for all menu actions. It offers all necessary information to build a menu
 * with it.
 */
public abstract class MenuAction extends AbstractAction {
  private static final Logger log = Logger.getInstance(MenuAction.class);

  protected Client client;

  /**
   * Creates a new MenuAction object reading its name, mnemonic and accelerator from the dictionary.
   * 
   * @param client The client for this MenuAction object.
   */
  public MenuAction(Client client) {
    this.client = client;
    setName(getNameTranslated());

    setIcon(getIconName());

    if (getMnemonicTranslated() != null && !getMnemonicTranslated().trim().equals("")) {
      putValue("mnemonic", Character.valueOf(getMnemonicTranslated().charAt(0)));
    }

    if (getAcceleratorTranslated() != null && !getAcceleratorTranslated().trim().equals("")) {
      putValue("accelerator", KeyStroke.getKeyStroke(getAcceleratorTranslated()));
    }

    if (getTooltipTranslated() != null && !getTooltipTranslated().trim().equals("")) {
      putValue("tooltip", getTooltipTranslated());
    }
  }

  /**
   * Returns an name which should depend on the current GUI locale for a menu action. May return
   * <code>null</code>!
   */
  protected abstract String getNameTranslated();

  protected abstract String getMnemonicTranslated();

  /**
   * Returns an accelerator key combination which should depend on the current GUI locale for a menu
   * action. May return <code>null</code>!
   * 
   * @return A KeyStroke description
   * @see KeyStroke#getKeyStroke(String)
   */
  protected abstract String getAcceleratorTranslated();

  /**
   * Returns a tool tip text which should depend on the current GUI locale for a menu action. May
   * return <code>null</code>!
   */
  protected abstract String getTooltipTranslated();

  /**
   * This method is called whenever this action is invoked.
   */
  public final void actionPerformed(ActionEvent e) {
    try {
      menuActionPerformed(e);
    } catch (Throwable t) {
      MenuAction.log.error(t.getMessage(), t);
      ErrorWindow errorWindow = new ErrorWindow(Client.INSTANCE, t);
      errorWindow.setVisible(true);
    }
  }

  /**
   * This method is called whenever this action is invoked.
   */
  public abstract void menuActionPerformed(ActionEvent e);

  /**
   * Sets the name of this menu action.
   */
  protected void setName(String name) {
    putValue(Action.NAME, name);
  }

  /**
   * Returns the name of this menu action.
   */
  protected String getName() {
    return (String) getValue(Action.NAME);
  }

  /**
   * Sets the icon of this menu action by iconname.
   */
  public void setIcon(String aName) {
    Icon icon = null;

    MenuAction.log.debug("MenuAction.setIcon(" + aName + ") called");

    if (aName != null) {
      String name = "etc/images/gui/actions/" + aName;
      icon = client.getMagellanContext().getImageFactory().loadImage(name);

      // log.info("Image for "+aName+" ("+name+") is "+icon);
    }

    putValue(Action.SMALL_ICON, icon);
  }

  /**
   * Returns the name of an appropriate icon for this action.
   */
  public String getIconName() {
    return null;
  }

  /**
   * Returns the mnemonic of the menu this menu action is to be associated with. The return value is
   * a key code which should be one of the values specified in {@link java.awt.event.KeyEvent}.
   * 
   * @return the mnemonic, a value of 0 means that no mnemonic is set.
   */
  public char getMnemonic() {
    Character c = (Character) getValue("mnemonic");

    if (c != null)
      return c.charValue();
    else
      return 0;
  }

  /**
   * Returns the shortcut {@link KeyStroke} this menu action is to be invokable with.
   * 
   * @return the accelerator or null, if the menu has no accelerator.
   */
  public KeyStroke getAccelerator() {
    return (KeyStroke) getValue("accelerator");
  }

  /**
   * Returns the tool tip for this menu action.
   * 
   * @return the tool tip String or null, if no tool tip is set.
   */
  public String getToolTip() {
    return (String) getValue("tooltip");
  }

  /**
   * Returns a String representation of this MenuAction object.
   */
  @Override
  public String toString() {
    return getName();
  }
}
