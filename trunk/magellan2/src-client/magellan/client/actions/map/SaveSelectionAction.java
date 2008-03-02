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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.swing.EresseaFileFilter;
import magellan.library.CoordinateID;
import magellan.library.Region;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.io.file.FileBackup;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Ilja Pavkovic
 */
public class SaveSelectionAction extends MenuAction implements SelectionListener, GameDataListener {
	private static final Logger log = Logger.getInstance(SaveSelectionAction.class);

	// FIXME: 
	// TODO: Move to EresseaFileFilter, 
	// add descriptions to res/lang/com-eressea-swing-eresseafilefilter.properties
	// add descriptions to res/lang/com-eressea-swing-eresseafilefilter_en.properties

	/** DOCUMENT-ME */
	public static final String DESCRIPTION = "Selections";

	/** DOCUMENT-ME */
	public static final String EXTENSION = "sel";

	/** DOCUMENT-ME */
	public static final String DELIMITER = " ";

	/** DOCUMENT-ME */
	public static final char COMMENT = ';';
	private Map<CoordinateID,Region> selectedRegions = new TreeMap<CoordinateID, Region>();

	/**
	 * Creates a new SaveSelectionAction object.
	 *
	 * 
	 */
	public SaveSelectionAction(Client client) {
        super(client);
		this.client.getDispatcher().addSelectionListener(this);
		this.client.getDispatcher().addGameDataListener(this);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void selectionChanged(SelectionEvent e) {
		if(e.getSource() == this) {
			return;
		}

		if((e.getSelectedObjects() != null) && (e.getSelectionType() == SelectionEvent.ST_REGIONS)) {
			selectedRegions.clear();

			for(Iterator iter = e.getSelectedObjects().iterator(); iter.hasNext();) {
				Object o = iter.next();

				if(o instanceof Region) {
					Region r = (Region) o;
					selectedRegions.put((CoordinateID)r.getID(), r);
				}
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void gameDataChanged(GameDataEvent e) {
		selectedRegions.clear();
	}

	protected String getPropertyName() {
		return "Client.lastSELSaved";
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void menuActionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new EresseaFileFilter(EXTENSION, DESCRIPTION));
		fc.setSelectedFile(new File(client.getProperties().getProperty(getPropertyName(), "")));
		fc.setDialogTitle(Resources.get("actions.saveselectionaction.title"));

		if(fc.showSaveDialog(client) == JFileChooser.APPROVE_OPTION) {
			PrintWriter pw = null;
			try {
				client.getProperties().setProperty(getPropertyName(),
												 fc.getSelectedFile().getAbsolutePath());

				if(fc.getSelectedFile().exists() && fc.getSelectedFile().canWrite()) {
					// create backup file
					try {
						File backup = FileBackup.create(fc.getSelectedFile());
						log.info("Created backupfile " + backup);
					} catch(IOException ie) {
						log.warn("Could not create backupfile for file " + fc.getSelectedFile());
					}
				} 
        if (fc.getSelectedFile().exists() && !fc.getSelectedFile().canWrite()){
          throw new IOException("cannot write "+fc.getSelectedFile());
        }else{
          pw = new PrintWriter(new BufferedWriter(new FileWriter(fc.getSelectedFile())));

          for(Iterator iter = selectedRegions.keySet().iterator(); iter.hasNext();) {
            pw.println(((CoordinateID) iter.next()).toString(DELIMITER));
          }

          pw.close();
        }
			} catch(IOException exc) {
				log.error(exc);
				JOptionPane.showMessageDialog(client, exc.toString(),
											  Resources.get("actions.saveselectionaction.msg.filesave.error.title"),
											  JOptionPane.ERROR_MESSAGE);
			} finally {
				if(pw != null) {
					pw.close();
				}
			}
		}
	}

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.saveselectionaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.saveselectionaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.saveselectionaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.saveselectionaction.tooltip",false);
  }
}
