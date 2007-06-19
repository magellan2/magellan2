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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.KeyStroke;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.swing.OrderWriterDialog;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class SaveOrdersAction extends MenuAction implements ShortcutListener,GameDataListener {
	private List<KeyStroke> shortCuts;

	/**
	 * Creates new OpenCRAction
	 *
	 * @param client
	 */
	public SaveOrdersAction(Client client) {
        super(client);

		shortCuts = new ArrayList<KeyStroke>(2);
		shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK |
											 KeyEvent.SHIFT_MASK));
		shortCuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK |
											 KeyEvent.SHIFT_MASK));
		DesktopEnvironment.registerShortcutListener(this);
        setEnabled(false);
        client.getDispatcher().addGameDataListener(this);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void menuActionPerformed(ActionEvent e) {
		OrderWriterDialog d = new OrderWriterDialog(client, true, client.getData(),
													client.getProperties(),
													client.getSelectedRegions().values());
		d.setVisible(true);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void shortCut(KeyStroke shortcut) {
		int index = shortCuts.indexOf(shortcut);

		if((index >= 0) && (index < 3)) {
			switch(index) {
			case 0:
				new OrderWriterDialog(client, true, client.getData(), client.getProperties(),
									  client.getSelectedRegions().values()).runClipboard();

				break;

			case 1:
				new OrderWriterDialog(client, true, client.getData(), client.getProperties(),
									  client.getSelectedRegions().values()).runMail();

				break;
			}
		}
	}

	/**
	 * Should return all short cuts this class want to be informed. The elements should be of type
	 * javax.swing.KeyStroke
	 *
	 * 
	 */
	public Iterator<KeyStroke> getShortCuts() {
		return shortCuts.iterator();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String getShortcutDescription(java.lang.Object obj) {
		int index = shortCuts.indexOf(obj);

		return Resources.get("actions.saveordersaction.shortcuts.description." + String.valueOf(index));
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public java.lang.String getListenerDescription() {
		return Resources.get("actions.saveordersaction.shortcuts.title");
	}
	
	public void gameDataChanged(GameDataEvent e) {
		int i = e.getGameData().regions().size();
		if (i>0) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}
  
  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.saveordersaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.saveordersaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.saveordersaction.name");
  }


  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.saveordersaction.tooltip",false);
  }
}
