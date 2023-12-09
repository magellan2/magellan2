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

package magellan.library.impl;

import magellan.library.ID;
import magellan.library.Identifiable;
import magellan.library.Unique;

/**
 * A template class for objects to be uniquely identifiable by other objects.
 */
public abstract class MagellanIdentifiableImpl implements Identifiable, Unique, Comparable<Object>,
    Cloneable {

  /** The object imposing the unique identifiability. This is immutable. */
  protected final ID id;

  // only for memory profiling
  // public class Counter {
  //
  // public Counter(String name) {
  // this.name = name;
  // created = 0;
  // deleted = 0;
  // }
  // protected String name;
  // protected long created;
  // protected long deleted;
  //
  // }

  // protected static Map<String, Counter> counters;
  // private static Timer timer;

  // static {
  // counters = new HashMap<String, Counter>();
  // timer = new Timer("memory tracker");
  // TimerTask task = new TimerTask() {
  //
  // @Override
  // public void run() {
  // for (Counter counter : counters.values()){
  // System.err.println(counter.name+": "+counter.created+" - "+counter.deleted);
  // }
  // System.err.println("--------------------------------------------------------");
  // }
  // };
  // timer.scheduleAtFixedRate(task , 10, 10000);
  // }

  /**
   * Creates a new identifiable object with the specified id.
   * 
   * @param id ID of the Identifiable
   * @throws NullPointerException if <kbd>ID</kbd> is <code>null</code>
   */
  public MagellanIdentifiableImpl(ID id) {
    if (id == null)
      throw new NullPointerException();

    this.id = id;
    // Counter counter = counters.get(this.getClass().getName());
    // if (counter==null){
    // counter = new Counter(this.getClass().getName());
    // counters.put(this.getClass().getName(), counter);
    // }
    // counter.created++;
  }

  // @Override
  // protected void finalize() throws Throwable {
  // Counter counter = counters.get(this.getClass().getName());
  // if (counter==null){
  // System.err.println("class "+this.getClass().getName()+" should have counter.");
  // counter = new Counter(this.getClass().getName());
  // }
  // counter.deleted++;
  // super.finalize();
  // }

  /**
   * Returns the id uniquely identifying this object.
   */
  public ID getID() {
    return id;
  }

  /**
   * Returns a copy of this object identified by a copy of the orignial's id. I.e., the following
   * statement holds true: this.getID() != this.clone().getID()
   * 
   * @throws CloneNotSupportedException DOCUMENT-ME
   */
  // FIXME this is nonsense (?)
  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  /**
   * Indicates that this object is to be regarded as equal to some other object. Especially with
   * implementing sub classes of Identifiable, equality will often be established through the
   * equality of IDs.
   * <p>
   * <b>Attention</b>: Overriding this method could break the general contract of equals!
   * </p>
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o instanceof MagellanIdentifiableImpl)
      return getID().equals(((MagellanIdentifiableImpl) o).getID())
          && (o.getClass().isInstance(this) || getClass().isInstance(o));
    return false;
  }

  /**
   * As we want to use the hashCode/equals contract we need to force the implementation of hashCode.
   * 
   * @return the hashCode of the current object
   */
  @Override
  public int hashCode() {
    return getID().hashCode();
  }

  /**
   * Returns <code>Object</code>'s hash code.
   */
  public int superHashCode() {
    return super.hashCode();
  }

  /**
   * Imposes a natural ordering on Identifiable objects. Especially with implementing sub classes of
   * Identifiable, such orderings will often be established by the natural order of ids.
   * <p>
   * <b>Attention</b>: Overriding this method could break the general contract of equals!
   * </p>
   * 
   * @see magellan.library.Identifiable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    if (!(getClass().isInstance(o) || o.getClass().isInstance(this)))
      throw new ClassCastException("invariant types");

    return getID().compareTo(((MagellanIdentifiableImpl) o).getID());
    // if (result == 0 && getClass() != o.getClass())
    // return getClass().toString().compareTo(o.getClass().toString());
    //
    // return result;
  }

}
