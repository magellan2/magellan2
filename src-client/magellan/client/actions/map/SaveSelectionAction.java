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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.swing.EresseaFileFilter;
import magellan.library.CoordinateID;
import magellan.library.io.file.FileBackup;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * Lets the user save a selection as file.
 *
 * @author Ilja Pavkovic
 */
public class SaveSelectionAction extends AbstractSelectionAction {
	private static final Logger log = Logger.getInstance(SaveSelectionAction.class);

	/** The file extension for selecion files. */
	public static final String EXTENSION = "sel";

	/** delimiter used for coordinates */
	public static final String DELIMITER = " ";

	/** comment character */
	public static final char COMMENT = ';';

	/**
	 * Creates a new SaveSelectionAction object.
	 */
	public SaveSelectionAction(Client client) {
	  super(client);
	}


	protected String getPropertyName() {
		return "Client.lastSELSaved";
	}

	/**
	 * Opens a file dialog and saves current selection.
	 * 
	 * @see magellan.client.actions.MenuAction#menuActionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
  public void menuActionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
    fc.addChoosableFileFilter(new EresseaFileFilter(SaveSelectionAction.EXTENSION, Resources
        .get("actions.saveselectionaction.selectionfilter.name")));
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
						SaveSelectionAction.log.info("Created backupfile " + backup);
					} catch(IOException ie) {
						SaveSelectionAction.log.warn("Could not create backupfile for file " + fc.getSelectedFile());
					}
				} 
        if (fc.getSelectedFile().exists() && !fc.getSelectedFile().canWrite()){
          throw new IOException("cannot write "+fc.getSelectedFile());
        }else{
          pw = new PrintWriter(new BufferedWriter(new FileWriter(fc.getSelectedFile())));

          for(CoordinateID c : getSelectedRegions().keySet()) {
            pw.println((c).toString(SaveSelectionAction.DELIMITER));
          }

          pw.close();
        }
			} catch(IOException exc) {
				SaveSelectionAction.log.error(exc);
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

}
