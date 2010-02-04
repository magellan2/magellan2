/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import magellan.library.HasRegion;
import magellan.library.Region;
import magellan.library.utils.logging.Logger;

/**
 * An event issued when the user activates a different object or selects a number of objects. This
 * might occur for example when the user clicks on a region on the map.
 * 
 * @see SelectionListener
 * @see EventDispatcher
 */
public class SelectionEvent extends EventObject {
  @SuppressWarnings("unused")
  private static final Logger log = Logger.getInstance(SelectionEvent.class);

  /**
   * Default selection type.
   * 
   * @see #ST_REGIONS
   */
  public static final int ST_DEFAULT = 0;

  /**
   * Indicates, that some regions on the map have been selected or shall be selected.
   * <p>
   * Used by the SelectionActions classes and in the Mapper class. These selections have to be
   * ignored by some components (like EMapOverviewPanel) as the selectionstate of the map is not
   * mirrored in the tree in EMapOverviewPanel. On the other hand the Mapper class should ignore all
   * SelectionEvents with a type different to <code>ST_REGIONS</code> (This makes it possible for
   * the user to treat selections of regions on the map and other selections in different ways.)
   * </p>
   */
  public static final int ST_REGIONS = 1;

  private int selectionType;
  private List<Object> selectedObjects;
  private List<List<Object>> contexts; // maybe null?

  /**
   * Returns the possibly multiple objects selected by the user. They do not necessarily include the
   * active object. A value of null indicates that previously selected objects are not affected by
   * this event.
   */
  public List<Object> getSelectedObjects() {
    return selectedObjects;
  }

  /**
   * Returns the possibly multiple objects selected by the user. They do not necessarily include the
   * active object. A value of null indicates that previously selected objects are not affected by
   * this event.
   */
  public List<List<Object>> getContexts() {
    if (contexts == null)
      return Collections.emptyList();
    else
      return Collections.unmodifiableList(contexts);
  }

  /**
   * Returns the one single object activated by the user. A {@link #ST_REGIONS} selection never has
   * an active object.
   */
  public Object getActiveObject() {
    if (getSelectionType() == SelectionEvent.ST_REGIONS)
      return null;
    if (selectedObjects.isEmpty())
      return null;

    return selectedObjects.iterator().next();
  }

  /**
   * Returns the type of the SelectionEvent. This has to be one of final int-values defined above
   * (like ST_REGIONS, ST_DEFAULT).
   */
  public int getSelectionType() {
    return selectionType;
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("SelectionEvent{\n");
    buffer.append("selectedObjects:").append(selectedObjects).append("\n");
    // buffer.append("activeObject:").append(activeObject).append("\n");
    // buffer.append("path:").append(path).append("\n");
    buffer.append("selectionType:").append(selectionType).append("\n");
    buffer.append("}\n");
    return buffer.toString();
  }

  /**
   * @param source should not be <code>null</code>
   * @param contexts may only be null if selectedObjects is empty or <code>null</code>.
   * @param selectionType
   */
  protected SelectionEvent(Object source, List<List<Object>> contexts, int selectionType) {
    super(source);
    if (source == null)
      throw new NullPointerException();

    this.selectionType = selectionType;

    this.contexts = contexts; // maybe null?

    if (contexts == null) {
      selectedObjects = Collections.emptyList();
    } else {
      selectedObjects = new ArrayList<Object>(contexts.size());
      for (List<Object> context : contexts) {
        Object obj = context.get(context.size() - 1);
        if (obj == null)
          throw new NullPointerException();
        selectedObjects.add(obj);
      }
    }
  }

  /**
   * Returns a shallow copy of the argument List.
   */
  @SuppressWarnings("unused")
  private static <T> List<T> copy(Collection<T> coll) {
    return new ArrayList<T>(coll);
  }

  /**
   * Returns a first order deep copy of the argument List.
   */
  private static List<List<Object>> copy2(List<? extends List<?>> coll) {
    ArrayList<List<Object>> result = new ArrayList<List<Object>>(coll.size());
    for (List<?> list : coll) {
      result.add(new ArrayList<Object>(list));
    }
    return result;
  }

  /**
   * Copies an event to an event with a new source.
   */
  public static SelectionEvent create(Object source, SelectionEvent event) {
    return new SelectionEvent(source, event.getContexts(), event.getSelectionType());
  }

  /**
   * Select a single HasRegion.
   * 
   * @param source
   * @param hasRegion
   */
  public static SelectionEvent create(Object source, HasRegion hasRegion) {
    if (hasRegion == null)
      throw new NullPointerException();
    List<Object> context = new ArrayList<Object>(2);
    context.add(hasRegion.getRegion());
    context.add(hasRegion);
    return new SelectionEvent(source, Collections.singletonList(context), SelectionEvent.ST_DEFAULT);
  }

  /**
   * Select a single region.
   * 
   * @param source
   * @param region
   */
  public static SelectionEvent create(Object source, Region region) {
    if (region == null)
      return SelectionEvent.create(source);
    else
      return SelectionEvent.create(source, region, Collections.singletonList(region));
  }

  /**
   * Select a number of Regions with one active Region
   * 
   * @param source
   * @param activeRegion null allowed
   * @param regions
   */
  public static SelectionEvent create(Object source, Region activeRegion, Collection<Region> regions) {
    ArrayList<List<Object>> contexts = new ArrayList<List<Object>>(regions.size());
    for (Region region : regions) {
      List<Object> copy = Collections.<Object> singletonList(region);
      contexts.add(copy);
    }

    return new SelectionEvent(source, contexts, SelectionEvent.ST_DEFAULT);
  }

  /**
   * Select a number of HasRegions with one active object
   * 
   * @param source
   * @param activeUnit <code>null</code> allowed
   * @param selectedObjects
   */
  public static SelectionEvent create(Object source, HasRegion activeUnit,
      Collection<? extends HasRegion> selectedObjects) {
    ArrayList<List<Object>> contexts = new ArrayList<List<Object>>(selectedObjects.size());
    for (HasRegion hr : selectedObjects) {
      List<Object> copy = new ArrayList<Object>(2);
      copy.add(hr.getRegion());
      copy.add(hr);
      contexts.add(copy);
    }

    return new SelectionEvent(source, contexts, SelectionEvent.ST_DEFAULT);
  }

  /**
   * Make a {@link #ST_REGIONS} selection.
   * 
   * @param source
   * @param regions
   */
  public static SelectionEvent create(Object source, Collection<Region> regions) {
    ArrayList<List<Object>> contexts = new ArrayList<List<Object>>(regions.size());
    for (Region region : regions) {
      List<Object> copy = Collections.<Object> singletonList(region);
      contexts.add(copy);
    }

    return new SelectionEvent(source, contexts, SelectionEvent.ST_REGIONS);
  }

  /**
   * Creates a {@link #ST_DEFAULT} SelectionEvent with all the details.
   * 
   * @param source
   * @param contexts
   */
  public static SelectionEvent create(Object source, List<? extends List<?>> contexts) {
    return new SelectionEvent(source, SelectionEvent.copy2(contexts), SelectionEvent.ST_DEFAULT);
  }

  /**
   * Select one arbitrary object
   * 
   * @param source
   * @param selection
   * @param mode
   */
  public static SelectionEvent create(Object source, Object selection, int mode) {
    if (selection == null)
      throw new NullPointerException();
    else
      return new SelectionEvent(source, Collections.singletonList(Collections
          .singletonList(selection)), mode);
  }

  /**
   * Returns <code>true</code> if only a single object has been selected and it is not a
   * {@link #ST_REGIONS} selection.
   */
  public boolean isSingleSelection() {
    return selectionType != SelectionEvent.ST_REGIONS && getSelectedObjects().size() == 1;
  }

  /**
   * Creates an empty selection event.
   * 
   * @param source
   * @return A new event.
   */
  public static SelectionEvent create(Object source) {
    return new SelectionEvent(source, null, SelectionEvent.ST_DEFAULT);
  }

  /**
   * Ignores the source.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj instanceof SelectionEvent) {
      SelectionEvent se = (SelectionEvent) obj;

      if (se.getSelectionType() != getSelectionType())
        return false;

      if (getActiveObject() == null) {
        if (se.getActiveObject() != null)
          return false;
      } else {
        if (!getActiveObject().equals(se.getActiveObject()))
          return false;
      }

      if (getContexts().size() != se.getContexts().size())
        return false;

      Iterator<List<Object>> myIt = getContexts().iterator();
      Iterator<List<Object>> otherIt = se.getContexts().iterator();

      for (; myIt.hasNext();) {
        List<Object> first = myIt.next();
        List<Object> second = otherIt.next();
        if (!first.equals(second))
          return false;
      }
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 42;
    result = result * 31 + selectionType;
    for (List<?> l : contexts) {
      result = result * 31 + l.size();
    }
    for (Object o : selectedObjects) {
      result = result * 31 + o.hashCode();
    }

    return result;
  }
}
