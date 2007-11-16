package magellan.library.relation;

import magellan.library.Named;
import magellan.library.Unit;

/**
 * A relation indicating that the source unit renames the Named Object
 */
public class RenameNamedRelation extends UnitRelation {
    public Named named;
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

}
