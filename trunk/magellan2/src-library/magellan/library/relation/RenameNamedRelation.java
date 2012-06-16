package magellan.library.relation;

import magellan.library.Named;
import magellan.library.Related;
import magellan.library.Unit;

/**
 * A relation indicating that the source unit renames the Named Object
 */
public class RenameNamedRelation extends UnitRelation {
  /** The object that is named */
  public Named named;
  /** The given name */
  public String name;

  /**
   * Creates a new RenameRelation.
   * 
   * @param s The source unit
   * @param named The target unit
   * @param name The new name of named
   * @param line The line in the source's orders
   */
  public RenameNamedRelation(Unit s, Named named, String name, int line) {
    super(s, line);
    this.named = named;
    this.name = name;
  }

  /**
   * Attaches an order to all report objects it is relevant to. source and named.
   */
  @Override
  public void add() {
    super.add();
    if (named instanceof Related && named != source && named != origin) {
      ((Related) named).addRelation(this);
    }
  }

  @Override
  public boolean isRelated(Object object) {
    return super.isRelated(object) || named == object;
  }

}
