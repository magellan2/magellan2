package magellan.client.swing.context.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import magellan.client.event.EventDispatcher;

/**
 * This class encapsulates basic functions for contextmenu actions.
 * 
 * @author Ilja Pavkovic - illsen@gumblfarz.de
 */
public abstract class ContextAction extends AbstractAction {

  protected EventDispatcher dispatcher;
  protected Object selected;
  protected List<?> selectedObjects;

  public ContextAction(Object selected, EventDispatcher dispatcher) {
    if (selected == null)
      throw new NullPointerException();
    this.selected = selected;
    this.dispatcher = dispatcher;

    setName(getNameTranslated());
  }

  public ContextAction(Object selected, List<?> selectedObjects, EventDispatcher dispatcher) {
    this(selected, dispatcher);
    this.selectedObjects = selectedObjects;
  }

  /**
   * This method filters
   * 
   * @param selectedObjects collection of selected objects
   * @param clazz class to filter objects
   * @return list of filtered objects
   */
  public static <T> List<T> filterObjects(Collection<?> selectedObjects, Class<T> clazz) {
    if (selectedObjects == null)
      return new ArrayList<T>();
    List<T> filteredObjects = new ArrayList<T>(selectedObjects.size());
    for (Object o : selectedObjects) {
      if (clazz.isInstance(o)) {
        filteredObjects.add(clazz.cast(o));
      }
    }
    return filteredObjects;
  }

  public abstract void actionPerformed(java.awt.event.ActionEvent e);

  @Override
  public String toString() {
    return getName();
  }

  /**
   * Sets the name of this menu action.
   */
  protected void setName(String name) {
    putValue(Action.NAME, name);
  }

  /**
   * Returns the name of this menu action.
   */
  protected String getName() {
    return (String) getValue(Action.NAME);
  }

  /**
   * These methods are now needed to keep translation in the corresponding class. they MAY deliver
   * null!
   */
  protected abstract String getNameTranslated();/*
                                                 * { return Translations.getTranslation(this,
                                                 * "name"); }
                                                 */

}
