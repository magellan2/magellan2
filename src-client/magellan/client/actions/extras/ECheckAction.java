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

package magellan.client.actions.extras;

import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.swing.KeyStroke;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.swing.ECheckDialog;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class ECheckAction extends MenuAction implements ShortcutListener {
	private KeyStroke imStroke;

	/**
	 * Creates a new ECheckAction object.
	 *
	 * @param client
	 */
	public ECheckAction(Client client) {
        super(client);
		imStroke = KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
		DesktopEnvironment.registerShortcutListener(imStroke, this);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void menuActionPerformed(java.awt.event.ActionEvent e) {
	  requestFocus();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void shortCut(javax.swing.KeyStroke shortcut) {
      requestFocus();
	}

	/**
   * 
   */
  private void requestFocus() {
    magellan.client.desktop.DesktopEnvironment.requestFocus("ECHECK");
  }

  /**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator<KeyStroke> getShortCuts() {
		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public java.lang.String getShortcutDescription(java.lang.Object obj) {
		if(imStroke.equals(obj)) {
			return Resources.get( "actions.echeckaction.shortcuts.description.1");
		}

		return Resources.get("actions.echeckaction.shortcuts.description.0");
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public java.lang.String getListenerDescription() {
		return Resources.get("actions.echeckaction.shortcuts.title");
	}

	// pavkovic 2003.01.28: this is a Map of the default Translations mapped to this class
	// it is called by reflection (we could force the implementation of an interface,
	// this way it is more flexible.)
	// Pls use this mechanism, so the translation files can be created automagically
	// by inspecting all classes.
	private static Map<String,String> defaultTranslations;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static synchronized Map<String,String> getDefaultTranslations() {
		if(defaultTranslations == null) {
			defaultTranslations = new Hashtable<String, String>();
			defaultTranslations.put("name", "ECheck...");
			defaultTranslations.put("mnemonic", "e");
			defaultTranslations.put("accelerator", "ctrl E");
			defaultTranslations.put("tooltip", "");
			defaultTranslations.put("shortcuts.description.1", "Run ECheck immediately");
			defaultTranslations.put("shortcuts.description.0", "Show dialog");
			defaultTranslations.put("shortcuts.title", "ECheck");
		}

		return defaultTranslations;
	}
  

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.echeckaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.echeckaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.echeckaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.echeckaction.tooltip",false);
  }


}
