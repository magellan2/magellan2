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
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import magellan.client.Client;
import magellan.client.utils.ErrorWindow;
import magellan.client.utils.FileHistory;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class FileHistoryAction extends AbstractAction {
	private File file;
	private FileHistory history;

	/**
	 * Creates a new FileHistoryAction object.
	 *
	 * 
	 * 
	 */
	public FileHistoryAction(FileHistory hist, File cr) {
		file = cr;
		history = hist;
		init();
	}

	private void init() {
		// format the text
		StringBuffer path = new StringBuffer();
		path.append("...").append(File.separatorChar).append(file.getName());

		File parent = file.getParentFile();

		while((parent != null) && ((path.length() + parent.getName().length()) < 30)) {
			path.insert(4, File.separatorChar).insert(4, parent.getName());
			parent = parent.getParentFile();
		}

		putValue(Action.NAME, path.toString());

		// tool tip text
		try {
			putValue(Action.SHORT_DESCRIPTION, file.getCanonicalPath());
		} catch(IOException e) {
			putValue(Action.SHORT_DESCRIPTION, path.toString());
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
    try {
  		history.loadFile(file);
    } catch (Throwable t) {
      ErrorWindow errorWindow = new ErrorWindow(Client.INSTANCE,t.getMessage(),"",t);
      errorWindow.setVisible(true);
    }
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public File getFile() {
		return file;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public boolean equals(Object o) {
		try {
			return o != null && file.equals(((FileHistoryAction) o).getFile());
		} catch(ClassCastException e) {
			return false;
		}
	}
}
