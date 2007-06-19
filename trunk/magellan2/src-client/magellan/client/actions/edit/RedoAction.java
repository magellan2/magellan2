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

package magellan.client.actions.edit;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.Action;

import magellan.client.Client;
import magellan.client.MagellanUndoManager;
import magellan.client.actions.MenuAction;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class RedoAction extends MenuAction implements PropertyChangeListener {
	private MagellanUndoManager undo;
	private String name = null;

	/**
	 * Creates a new RedoAction object.
	 *
	 * 
	 */
	public RedoAction(Client client, MagellanUndoManager m) {
        super(client);
		name = this.getName();
		undo = m;
		setEnabled(undo.canUndo());

		if(isEnabled()) {
			putValue(Action.NAME, name + ": " + undo.getUndoPresentationName());
		}

		undo.addPropertyChangeListener(MagellanUndoManager.REDO, this);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getIconName() {
		return "redo_edit";
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void menuActionPerformed(ActionEvent e) {
		if (undo.canRedo())
			undo.redo();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void propertyChange(PropertyChangeEvent p1) {
		boolean enabled = ((Boolean) p1.getNewValue()).booleanValue();

		if(enabled) {
			putValue(Action.NAME, name + ": " + undo.getRedoPresentationName());
		} else {
			putValue(Action.NAME, name);
		}
		setEnabled(enabled);
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
			defaultTranslations = new Hashtable<String,String>();
			defaultTranslations.put("name", "Redo");
			defaultTranslations.put("mnemonic", "r");
			defaultTranslations.put("accelerator", "ctrl Y");
			defaultTranslations.put("tooltip", "");
		}

		return defaultTranslations;
	}
  
  

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.redoaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.redoaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.redoaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.redoaction.tooltip",false);
  }

}
