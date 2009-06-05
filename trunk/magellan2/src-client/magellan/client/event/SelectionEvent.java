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

package magellan.client.event;

import java.util.Collection;
import java.util.EventObject;

import javax.swing.JComponent;

import magellan.library.utils.logging.Logger;

/**
 * An event issued when the user activates a different object or selects a number of objects. This
 * might occur for example when the user clicks on a region on the map.
 *
 * @see SelectionListener
 * @see EventDispatcher
 */
public class SelectionEvent extends EventObject {
  private static final Logger log = Logger.getInstance(SelectionEvent.class);
  
	/** Default selection type. 
	 *  
	 *   @see #ST_REGIONSs
	 */
	public static final int ST_DEFAULT = 0;

	/**
   * Indicates, that some regions on the map have been selected or shall be
   * selected.
   * <p>
   * Used by the SelectionActions classes and in the Mapper class. These
   * selections have to be ignored by some components (like EMapOverviewPanel)
   * as the selectionstate of the map is not mirrored in the tree in
   * EMapOverviewPanel. On the other hand the Mapper class should ignore all
   * SelectionEvents with a type different to <code>ST_REGIONS</code> (This
   * makes it possible for the user to treat selections of regions on the map
   * and other selections in different ways.)
   * </p>
   */
	public static final int ST_REGIONS = 1;


	private Collection<?> selectedObjects;
	private Object activeObject;
  private Collection<Object> path;
	private int selectionType;

	/**
	 * Constructs a new selection event with selectionType = ST_DEFAULT and empty path.
   * 
   * @param source
   *          the object issuing the event.
   * @param selectedObjects
   *          the objects selected by the user. This collection does not
   *          necessarily contain activeObject. Specifying null for this
   *          parameter indicates that the selected objects did actually not
   *          change.
   * @param activeObject
   *          the single object activated by the user.
	 */
	public SelectionEvent(Object source, Collection<?> selectedObjects, Object activeObject) {
		this(source, selectedObjects, activeObject, null, SelectionEvent.ST_DEFAULT);
	}

  /**
   * Constructs a new selection event with empty selection path.
   * 
   * @param source
   *          the object issuing the event.
   * @param selectedObjects
   *          the objects selected by the user. This collection does not
   *          necessarily contain activeObject. Specifying null for this
   *          parameter indicates that the selected objects did actually not
   *          change.
   * @param activeObject
   *          the single object activated by the user.
   * @param selectionType
   *          The type of selection event. Currently supported types are
   *          {@link SelectionEvent#ST_DEFAULT},
   *          {@link SelectionEvent#ST_REGIONS}, 
	 */
	public SelectionEvent(Object source, Collection<?> selectedObjects, Object activeObject, int selectionType) {
    this(source, selectedObjects, activeObject, null, selectionType);
  }


  /**
   * Constructs a new selection event with empty path with <code>selectionType ST_DEFAULT</code>.
   * 
   * @param source
   *          the object issuing the event.
   * @param selectedObjects
   *          the objects selected by the user. This collection does not
   *          necessarily contain activeObject. Specifying null for this
   *          parameter indicates that the selected objects did actually not
   *          change.
   * @param activeObject
   *          the single object activated by the user.
   * @param selectionPath
   *          a sequence of object that are "parents" of the active object.
   */          
 public SelectionEvent(Object source, Collection<?> selectedObjects, Object activeObject, Collection<Object> selectionPath) {
    this(source, selectedObjects, activeObject, selectionPath, SelectionEvent.ST_DEFAULT);
  }

  /**
   * Constructs a new selection event.
   * <p>
   * Usually such an event indicates only a change of the active object which is
   * indicated by activeObject != null and selectedObjects == null.
   * </p>
   * 
   * @param source
   *          the object issuing the event.
   * @param selectedObjects
   *          the objects selected by the user. This collection does not
   *          necessarily contain activeObject. Specifying null for this
   *          parameter indicates that the selected objects did actually not
   *          change.
   * @param activeObject
   *          the single object activated by the user.
   * @param path
   *          a sequence of object that are "parents" of the active object.
   * @param selectionType
   *          The type of selection event. Currently supported types are
   *          {@link SelectionEvent#ST_DEFAULT} if some game object(s) are selected, and
   *          {@link SelectionEvent#ST_REGIONS} if one or more regions are selected. 
   */          
  public SelectionEvent(Object source, Collection<?> selectedObjects, Object activeObject, Collection<Object> path,
      int selectionType) {
    super(source);
		this.selectedObjects = selectedObjects;
		this.activeObject = activeObject;
		this.selectionType = selectionType;
    this.path=path;
		if (SelectionEvent.log.isDebugEnabled()) {
      SelectionEvent.log.debug("selected "+activeObject+","+selectedObjects+" by "+source.getClass()+", "+((source instanceof JComponent)?((JComponent) source).getName():""));
    }
	}

  /**
	 * Returns the possibly multiple objects selected by the user. They do not necessarily include
	 * the active object. A value of null indicates that previously selected objects are not
	 * affected by this event.
	 *
	 * 
	 */
	public Collection<?> getSelectedObjects() {
		return selectedObjects;
	}


	/**
	 * Returns the one single object activated by the user.
	 *
	 * 
	 */
	public Object getActiveObject() {
		return activeObject;
	}

	/**
	 * Returns the type of the SelectionEvent. This has to be one of final int-values defined above
	 * (like ST_REGIONS, ST_DEFAULT).
	 *
	 * 
	 */
	public int getSelectionType() {
		return selectionType;
	}
  
  /**
   * Returns the path for the active object. This information can be used to
   * help identify the active object by specifying its "parents".
   */
  public Collection<Object> getPath(){
    return path;
  }
  
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("SelectionEvent{\n");
    buffer.append("selectedObjects:").append(selectedObjects).append("\n");
    buffer.append("activeObject:").append(activeObject).append("\n");
    buffer.append("path:").append(path).append("\n");
    buffer.append("selectionType:").append(selectionType).append("\n");
    buffer.append("}\n");
    return buffer.toString();
  }

  public <S> void addSelectedObjects(Collection<S> previousObjects, Class<S> class1) {
    for (Object o : selectedObjects){
      if (class1.isInstance(o)){
        previousObjects.add(class1.cast(o));
      }
    }
  }
}
