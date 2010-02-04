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

package magellan.client.swing.tree;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This class maintains a collection of WeakReferences to CellObjects. If necessary, their
 * properties can be updated.
 * 
 * @author Andreas
 * @version 1.0
 */
public abstract class AbstractNodeWrapperDrawPolicy implements NodeWrapperDrawPolicy {
  protected Collection<WeakReference<CellObject>> nodes;
  protected ReferenceQueue<CellObject> refQueue;
  protected boolean inUpdate = false;

  /**
   * Creates new NodeWrapperPreferencesDialog
   */
  public AbstractNodeWrapperDrawPolicy() {
    nodes = new HashSet<WeakReference<CellObject>>();
    refQueue = new ReferenceQueue<CellObject>();
  }

  /**
   * DOCUMENT-ME
   */
  public void addCellObject(CellObject co) {
    clearNodes();
    nodes.add(new WeakReference<CellObject>(co, refQueue));
  }

  protected void clearNodes() {
    if (inUpdate)
      return;

    Object o = null;

    do {
      o = refQueue.poll();

      if (o != null) {
        nodes.remove(o);
      }
    } while (o != null);
  }

  /**
   * DOCUMENT-ME
   */
  public void applyPreferences() {
    clearNodes();
    inUpdate = true;

    Iterator<WeakReference<CellObject>> it = nodes.iterator();

    try { // because of deletion of weak ref

      while (it.hasNext()) {
        try { // - || -

          CellObject co = (it.next()).get();
          co.propertiesChanged();
        } catch (Exception inner) {
          try {
            it.remove();
          } // remove the broken weak reference
          catch (Exception exc2) {
          }
        }
      }
    } catch (Exception outer) {
    }

    inUpdate = false;
  }
}
