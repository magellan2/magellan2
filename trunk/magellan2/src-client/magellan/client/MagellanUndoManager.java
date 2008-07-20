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
import java.util.ArrayList;

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

  private ArrayList<UndoableEdit> eventList = new ArrayList<UndoableEdit>();

  private ArrayList<UndoableEdit> undoneList = new ArrayList<UndoableEdit>();

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
	@Override
  public synchronized void undo() {
		// TODO: implement undo history?
    undoneList.add(eventList.remove(eventList.size()-1));
    
		// String oldUndo=getUndoPresentationName(),oldRedo=getRedoPresentationName();
		try {
			super.undo();
			list.firePropertyChange(MagellanUndoManager.REDO, false, canRedo());
			list.firePropertyChange(MagellanUndoManager.UNDO, false, canUndo());
		} catch(CannotUndoException e) {
			MagellanUndoManager.log.info("MagellanUndoManager.undo: cannot undo");
			MagellanUndoManager.log.debug("", e);
		}
	}

	/**
	 * DOCUMENT-ME
	 */
	@Override
  public synchronized void redo() {
    eventList.add(undoneList.remove(eventList.size()-1));
		try {
			// TODO: implement redo history?
			// String oldUndo=getUndoPresentationName(),oldRedo=getRedoPresentationName();
			super.redo();
			list.firePropertyChange(MagellanUndoManager.REDO, false, canRedo());
			list.firePropertyChange(MagellanUndoManager.UNDO, false, canUndo());
		} catch(CannotRedoException e) {
			MagellanUndoManager.log.info("MagellanUndoManager.redo: cannot redo");
			MagellanUndoManager.log.debug("", e);
		}
	}

	/**
   * DOCUMENT-ME
	 * @see javax.swing.undo.UndoManager#addEdit(javax.swing.undo.UndoableEdit)
	 */
	@Override
  public synchronized boolean addEdit(UndoableEdit e) {
    // FIXME stm 10.08.07 This class is broken, so we deactivate it for the time being
    if (true) {
      return false;
    }
		// TODO: implement undo/redo history?
    eventList.add(e);
    undoneList.clear();
		// String oldUndo=getUndoPresentationName(),oldRedo=getRedoPresentationName();
    boolean oldUndo = canUndo();
    boolean oldRedo = canRedo();
		boolean b = super.addEdit(e);

		if(b) {
			list.firePropertyChange(MagellanUndoManager.UNDO, oldUndo, canUndo());
			list.firePropertyChange(MagellanUndoManager.REDO, oldRedo, canRedo());
		}

		return b;
	}

  /**
   * @see javax.swing.undo.UndoManager#discardAllEdits()
   */
  @Override
  public synchronized void discardAllEdits() {
    eventList.clear();
    undoneList.clear();
    boolean oldUndo = canUndo();
    boolean oldRedo = canRedo();
    super.discardAllEdits();
    // notify listeners that there are no more edits
    list.firePropertyChange(MagellanUndoManager.UNDO, oldUndo, canUndo());
    list.firePropertyChange(MagellanUndoManager.REDO, oldRedo, canRedo());
  }
}
