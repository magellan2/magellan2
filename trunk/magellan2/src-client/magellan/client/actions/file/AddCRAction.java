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
import java.io.File;
import java.util.Collection;
import java.util.Properties;

import javax.swing.JFileChooser;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.event.SelectionEvent;
import magellan.client.swing.EresseaFileFilter;
import magellan.client.swing.HistoryAccessory;
import magellan.library.GameData;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.ReportMerger;
import magellan.library.utils.Resources;



/**
 * DOCUMENT ME!
 *
 * @author Andreas, Ulrich Küster
 */
public class AddCRAction extends MenuAction implements GameDataListener{

	

	/**
	 * Creates new AddCRAction
	 *
	 * @param client
	 */
	public AddCRAction(Client client) {
        super(client);
        // Test..disabled at start
        setEnabled(false);
        client.getDispatcher().addGameDataListener(this);
	}

	/**
	 * Called when the file->add menu is selected in order to add a certain cr file to current game
	 * data. Displays a file chooser and adds the selected cr file to the current game data.
	 *
	 * 
	 */
	public void menuActionPerformed(ActionEvent e) {
		final Client theclient = client;
		Collection selectedObjects = client.getSelectedObjects();
		Properties settings = client.getProperties();
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);

		fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.CR_FILTER));
		fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.GZ_FILTER));
		fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.BZ2_FILTER));
		fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.ZIP_FILTER));
		fc.addChoosableFileFilter(new EresseaFileFilter(EresseaFileFilter.ALLCR_FILTER));

		int lastFileFilter = Integer.parseInt(settings.getProperty("Client.lastSelectedAddCRFileFilter",
																   "3"));
		// bugzilla #861
        if(lastFileFilter < 0) {
            lastFileFilter = 0;
        } else {
            lastFileFilter = Math.min(lastFileFilter,fc.getChoosableFileFilters().length-1);
        }
        
		fc.setFileFilter(fc.getChoosableFileFilters()[lastFileFilter]);

		File file = new File(settings.getProperty("Client.lastCRAdded", ""));
		fc.setSelectedFile(file);

		if(file.exists()) {
			fc.setCurrentDirectory(file.getParentFile());
		}

		HistoryAccessory acc = new HistoryAccessory(settings, fc);
		fc.setAccessory(acc);
		fc.setDialogTitle(Resources.get("magellan.actions.addcraction.title"));

		if(fc.showOpenDialog(client) == JFileChooser.APPROVE_OPTION) {
			// find selected FileFilter
			int i = 0;

			while(!fc.getChoosableFileFilters()[i].equals(fc.getFileFilter())) {
				i++;
			}

			settings.setProperty("Client.lastSelectedAddCRFileFilter", String.valueOf(i));

			// force user to choose a file on save
			//client.setDataFile(null);
			ReportMerger merger = null;
			File files[] = fc.getSelectedFiles();

			if(files.length == 0) {
				merger = new ReportMerger(client.getData(), fc.getSelectedFile(),
										  new ReportMerger.Loader() {
						// pavkovic 2002.11.05: prevent name clash with variable "file"
						public GameData load(File aFile) {
							return theclient.loadCR(aFile);
						}
					},
										  new ReportMerger.AssignData() {
						public void assign(GameData _data) {
							theclient.setData(_data);
						}
					});
				settings.setProperty("Client.lastCRAdded", fc.getSelectedFile().getAbsolutePath());
			} else {
				merger = new ReportMerger(client.getData(), files,
										  new ReportMerger.Loader() {
						// pavkovic 2002.11.05: prevent name clash with variable "file"
						public GameData load(File aFile) {
							return theclient.loadCR(aFile);
						}
					},
										  new ReportMerger.AssignData() {
						public void assign(GameData _data) {
							theclient.setData(_data);
						}
					});
				settings.setProperty("Client.lastCRAdded", files[files.length - 1].getAbsolutePath());
			}

			merger.merge(client);
			if (selectedObjects!=null){
				client.getDispatcher().fire(new SelectionEvent(this,selectedObjects,null));
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.eressea.event.GameDataListener#gameDataChanged(com.eressea.event.GameDataEvent)
	 */
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
    return Resources.get("magellan.actions.addcraction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("magellan.actions.addcraction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("magellan.actions.addcraction.name");
  }
  
  @Override
  protected String getTooltipTranslated() {
    return Resources.get("magellan.actions.addcraction.tooltip",false);
  }

}
