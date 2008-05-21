package magellan.library.relation;

import magellan.library.Unit;

/**
 * A relation indicating that the source unit changes its battle status
 */
public class GuardRegionRelation extends UnitRelation {
  public int guard = 0; 

	 /**
	 * Creates a new CombatStatusRelation. 
	 *
	 * @param s The source unit
	 * @param newStatus new guarding bevaviour (true or false)
	 * @param line The line in the source's orders
	 */
	public GuardRegionRelation(Unit s, int newStatus, int line) {
        super(s, line);
        this.guard = newStatus;
    }
}
