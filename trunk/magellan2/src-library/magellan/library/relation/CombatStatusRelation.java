package magellan.library.relation;

import magellan.library.Unit;

/**
 * A relation indicating that the source unit changes its battle status
 */
public class CombatStatusRelation extends UnitRelation {
  public int newCombatStatus;
  public boolean newUnaided = false;
  public boolean newUnaidedSet = false;

  /**
   * Creates a new CombatStatusRelation.
   * 
   * @param s The source unit
   * @param newStatus new Combat status of the unit
   * @param line The line in the source's orders
   */
  public CombatStatusRelation(Unit s, int newStatus, int line) {
    super(s, line);
    newCombatStatus = newStatus;
  }

  /**
   * Creates a new CombatStatusRelation. (unaided status change)
   * 
   * @param s The source unit
   * @param newUnaidedStatus "unaided"-status after processing current orders
   * @param line The line in the source's orders
   */
  public CombatStatusRelation(Unit s, boolean newUnaidedStatus, int line) {
    super(s, line);
    newUnaidedSet = true;
    newUnaided = newUnaidedStatus;
  }

}
