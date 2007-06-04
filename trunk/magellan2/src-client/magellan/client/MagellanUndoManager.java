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

package magellan.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import magellan.library.utils.logging.Logger;


/**
 * TODO: undo/redo seems broken
 *
 * @author Andreas
 */
public class MagellanUndoManager extends UndoManager {
	private static final Logger log = Logger.getInstance(MagellanUndoManager.class);

	/** DOCUMENT-ME */
	public static final String UNDO = "Undo_Changed";

	/** DOCUMENT-ME */
	public static final String REDO = "Redo_Changed";

	// this is basicall needed to attach RedoAction and UndoAction to this UndoManager
	private PropertyChangeSupport list;

	/**
	 * Creates new MagellanUndoManager
	 */
	public MagellanUndoManager() {
		list = new PropertyChangeSupport(this);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		list.addPropertyChangeListener(l);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void addPropertyChangeListener(String property, PropertyChangeListener l) {
		list.addPropertyChangeListener(property, l);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		list.removePropertyChangeListener(l);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void removePropertyChangeListener(String property, PropertyChangeListener l) {
		list.removePropertyChangeListener(property, l);
	}

	/*
	 This methods must be changed to throw the events.
	 */
	public synchronized void undo() {
		// TODO: implement undo history?
		// String oldUndo=getUndoPresentationName(),oldRedo=getRedoPresentationName();
		try {
			super.undo();
			list.firePropertyChange(REDO, false, canRedo());
			list.firePropertyChange(UNDO, false, canUndo());
		} catch(CannotUndoException e) {
			log.info("MagellanUndoManager.undo: cannot undo");
			log.debug("", e);
		}
	}

	/**
	 * DOCUMENT-ME
	 */
	public synchronized void redo() {
		try {
			// TODO: implement redo history?
			// String oldUndo=getUndoPresentationName(),oldRedo=getRedoPresentationName();
			super.redo();
			list.firePropertyChange(REDO, false, canRedo());
			list.firePropertyChange(UNDO, false, canUndo());
		} catch(CannotRedoException e) {
			log.info("MagellanUndoManager.redo: cannot redo");
			log.debug("", e);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public synchronized boolean addEdit(UndoableEdit e) {
		// TODO: implement undo/redo history?
		// String oldUndo=getUndoPresentationName(),oldRedo=getRedoPresentationName();
		boolean b = super.addEdit(e);

		if(b) {
			list.firePropertyChange(UNDO, false, canUndo());
			list.firePropertyChange(REDO, false, canRedo());
		}

		return b;
	}
}
