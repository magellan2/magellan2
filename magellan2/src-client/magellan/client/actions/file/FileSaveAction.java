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

package magellan.client.actions.file;

import magellan.client.Client;
import magellan.library.io.file.FileType;
import magellan.library.utils.Resources;


/**
 * DOCUMENT-ME
 *
 * @author Andreas
 * @version 1.0
 */
public class FileSaveAction extends FileSaveAsAction {
	// pavkovic 2003.05.20: this object is essentially doing the same as FileSaveAsAction
	/**
	 * The standard constructor.
	 *
	 * @param parent The client object for this action.
	 */
	public FileSaveAction(Client parent) {
		super(parent);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getIconName() {
		return "save_edit";
	}

	/**
	 * This function delivers the target file. In FileSaveAction use  the possibly well known
	 * FileType of the gamedata object
	 *
	 * 
	 */
	protected FileType getFile() {
		return (client.getData() == null) ? null : (client.getData().filetype);
	}

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.filesaveaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.filesaveaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.filesaveaction.name");
  }


  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.filesaveaction.tooltip",false);
  }
}
