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

package magellan.client.actions.map;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.event.SelectionEvent;
import magellan.client.swing.EresseaFileFilter;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Ilja Pavkovic
 */
public class OpenSelectionAction extends MenuAction implements GameDataListener {
	private static final Logger log = Logger.getInstance(OpenSelectionAction.class);
	protected Map<CoordinateID,Region> selectedRegions = new Hashtable<CoordinateID, Region>();

	/**
	 * Creates a new OpenSelectionAction object.
	 *
	 * 
	 */
	public OpenSelectionAction(Client client) {
        super(client);
		client.getDispatcher().addGameDataListener(this);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void gameDataChanged(GameDataEvent e) {
		selectedRegions.clear();
	}

	/* will be void in AddSelectionAction */
	protected void preSetCleanSelection() {
		selectedRegions.clear();
	}

	/* will be overwritten by AddSelectionAction*/
	protected String getPropertyName() {
		return "Client.lastSELOpened";
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public void menuActionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new EresseaFileFilter(SaveSelectionAction.EXTENSION,
														SaveSelectionAction.DESCRIPTION));
		fc.setSelectedFile(new File(client.getProperties().getProperty(getPropertyName(), "")));
		fc.setDialogTitle(Resources.get("actions.openselectionaction.title"));

		if(fc.showOpenDialog(client) == JFileChooser.APPROVE_OPTION) {
			client.getProperties().setProperty(getPropertyName(),
											 fc.getSelectedFile().getAbsolutePath());

			List<CoordinateID> coordinates = new LinkedList<CoordinateID>();

			try {
				BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()));

				while(true) {
					String line = br.readLine();

					if(line == null) {
						break;
					}

					if(line.indexOf(SaveSelectionAction.COMMENT) != -1) {
						// remove trailing comment
						line = line.substring(0, line.indexOf(SaveSelectionAction.COMMENT));
					}

					coordinates.add(CoordinateID.parse(line, SaveSelectionAction.DELIMITER));
				}

				br.close();
			} catch(Exception exc) {
				OpenSelectionAction.log.error(exc);
				JOptionPane.showMessageDialog(client,
											  Resources.get("actions.openselectionaction.msg.fileordersopen.error.text") +
											  e.toString(),
                        Resources.get("actions.openselectionaction.msg.fileordersopen.error.title"),
											  JOptionPane.ERROR_MESSAGE);
			}

			// load successful, now fill up selection
			preSetCleanSelection();

			for(Iterator iter = coordinates.iterator(); iter.hasNext();) {
				CoordinateID c = (CoordinateID) iter.next();

				if(client.getData().regions().get(c) != null) {
					selectedRegions.put(c, client.getData().regions().get(c));
				}
			}

			// fire change event
			client.getData().setSelectedRegionCoordinates(selectedRegions);
			client.getDispatcher().fire(new SelectionEvent(this, selectedRegions.values(), null,
														   SelectionEvent.ST_REGIONS));
		}
	}

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.openselectionaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.openselectionaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.openselectionaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.openselectionaction.tooltip",false);
  }
}
