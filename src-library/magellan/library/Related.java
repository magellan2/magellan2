package magellan.library;

import java.util.List;

import magellan.library.relation.UnitRelation;


/**
 * An interface granting access to the relations of an object.
 */
public interface Related extends Described,Addeable {

    /** 
     * add a new relation to this object
     *
     * @param rel
     */
    public void addRelation(UnitRelation rel);
    
    /**
     * removes the given relation
     * 
     * @param rel
     * @return old relation
     */
    public UnitRelation removeRelation(UnitRelation rel);

    /**
     * delivers all relations of the given class
     * 
     * @param relationClass Should be a subclass of {@link UnitRelation}
     * @return list of relations that are instance of relationClass
     */
    public List<UnitRelation> getRelations(Class relationClass);
}
