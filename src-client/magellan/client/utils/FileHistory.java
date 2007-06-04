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

package magellan.client.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JMenu;

import magellan.client.Client;
import magellan.client.actions.file.FileHistoryAction;
import magellan.client.event.SelectionEvent;
import magellan.library.GameData;
import magellan.library.utils.Bucket;
import magellan.library.utils.PropertiesHelper;


/**
 * A kind of wrapper for the file history (menu) functionality.
 *
 * @author Andreas
 * @version
 */
public class FileHistory {
	protected Bucket<FileHistoryAction> history;
	protected JMenu historyMenu;
	protected int insertionIndex;
	protected Properties settings;
	protected Client client;

	/**
	 * Creates new FileHistory
	 *
	 * 
	 * 
	 * 
	 * 
	 */
	public FileHistory(Client parent, Properties settings, JMenu menu, int index) {
		this.settings = settings;
		client = parent;
		historyMenu = menu;
		insertionIndex = index;
	}

	/**
	 * Adds a single file to the file history, as well to the file history bucket as well to the
	 * file history menu.
	 *
	 * 
	 */
	public void addFileToHistory(File f) {
		if(history == null) {
			loadFileHistory();
		}

		clearFileHistoryMenu();
		history.add(new FileHistoryAction(this, f));
		buildFileHistoryMenu();
	}

	/**
	 * Stores the current contents of the file history bucket to the settings.
	 */
	public void storeFileHistory() {
		List<String> files = new ArrayList<String>((history == null) ? 0 : history.size());

		if(history != null) {
			for(Iterator<FileHistoryAction> iter = history.iterator(); iter.hasNext();) {
				files.add(iter.next().getFile().getAbsolutePath());
			}

			Collections.reverse(files);
		}

		PropertiesHelper.setList(settings, "Client.fileHistory", files);
	}

	/**
	 * Fills the file history bucket from what is stored in the settings.
	 */
	private void loadFileHistory() {
		if(history == null) {
			history = new Bucket<FileHistoryAction>(getMaxFileHistorySize());
		}

		for(Iterator iter = PropertiesHelper.getList(settings, "Client.fileHistory").iterator();
				iter.hasNext();) {
			String file = (String) iter.next();
			File f = new File(file);

			if(f.exists()) {
				history.add(new FileHistoryAction(this, f));
			}
		}
	}

	/**
	 * Uses the current contents of the file history bucket to remove these menu items from the
	 * file history menu.
	 */
	public void clearFileHistoryMenu() {
		if(history != null) {
			for(Iterator iter = history.iterator(); iter.hasNext();) {
				iter.next();
				historyMenu.remove(insertionIndex);
			}
		}
	}

	/**
	 * Inserts the contents of the fileHistory bucket to the file history menu (it assumes that the
	 * menu has been cleared previously.
	 */
	public void buildFileHistoryMenu() {
		if(history == null) {
			loadFileHistory();
		}

		int iIndex = insertionIndex;

		for(Iterator iter = history.iterator(); iter.hasNext(); iIndex++) {
			FileHistoryAction item = (FileHistoryAction) iter.next();
			historyMenu.insert(item, iIndex);
		}
	}

	/**
	 * Returns the maximum number of entries in the history of loaded files.
	 *
	 * 
	 */
	public int getMaxFileHistorySize() {
		return Integer.parseInt(settings.getProperty("Client.fileHistory.size", "4"));
	}

	/**
	 * Allows to set the maximum number of files appearing in the file history.
	 *
	 * 
	 */
	public void setMaxFileHistorySize(int size) {
		if(size != getMaxFileHistorySize()) {
			clearFileHistoryMenu();
			history.setMaxSize(size);
			buildFileHistoryMenu();
			settings.setProperty("Client.fileHistory.size", Integer.toString(size));
		}
	}

	/**
	 * Loads the the given file. This method should only be called by FileHistoryAction objects.
	 *
	 * 
	 */
	public void loadFile(File file) {
		if(!client.askToSave()) {
			return;
		}

		settings.setProperty("Client.lastCROpened", file.getAbsolutePath());
		addFileToHistory(file);

		boolean bOpenEqualsSave = Boolean.valueOf(settings.getProperty("Client.openEqualsSave",
																	   "false")).booleanValue();

		if(bOpenEqualsSave) {
			settings.setProperty("Client.lastCRSaved", file.getAbsolutePath());
		}

		Collection selectedObjects = client.getSelectedObjects();
		
		GameData data = client.loadCR(file);

		if(data != null) {
			client.setData(data);
			client.setReportChanged(false);
			if (selectedObjects!=null){
				client.getDispatcher().fire(new SelectionEvent(this,selectedObjects,null));
			}
		}
	}
}
