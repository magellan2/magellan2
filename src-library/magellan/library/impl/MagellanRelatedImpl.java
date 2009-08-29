package magellan.library.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import magellan.library.ID;
import magellan.library.Related;
import magellan.library.relation.RenameNamedRelation;
import magellan.library.relation.UnitRelation;


/** 
 * A RelatedObject knows concepts of relations
 */
public abstract class MagellanRelatedImpl extends MagellanDescribedImpl implements Related {

  /** Contains all attributes */
  private Map<String,String> attributes = new HashMap<String,String>();

  /**
   * Constructs a new described object that is uniquely identifiable by the specified id.
   *
   * 
   */
  public MagellanRelatedImpl(ID id) {
      super(id);
  }

  /**
   * @see magellan.library.Related#addRelation(magellan.library.relation.UnitRelation)
   */
  public void addRelation(UnitRelation rel) {
      getRelations().add(rel);
  }

  /**
   * @see magellan.library.Related#removeRelation(magellan.library.relation.UnitRelation)
   */
  public UnitRelation removeRelation(UnitRelation rel) {
      if(getRelations().remove(rel)) {
          return rel;
      } else {
          return null;
      }
  }
  
  protected abstract Collection<UnitRelation> getRelations();

  /**
   * Returns a Collection over the relations this unit has to other units. The collection consist
   * of  <tt>UnitRelation</tt> objects.  The UnitRelation objects are filtered by the given
   * relation class.
   *
   * 
   *
   * 
   */
  public List<UnitRelation> getRelations(Class relationClass) {
  	List<UnitRelation> ret = new LinkedList<UnitRelation>();
  
  	for(Iterator<UnitRelation> iter = getRelations().iterator(); iter.hasNext();) {
      UnitRelation relation = iter.next();
  
  		if(relationClass.isInstance(relation)) {
  			ret.add(relation);
  		}
  	}
  
  	return ret;
  }

  
  /**
   * @see magellan.library.Named#getModifiedName()
   */
  @Override
  public String getModifiedName() {
    List renameRelations = getRelations(RenameNamedRelation.class);
    if(renameRelations.isEmpty()) {
        return null;
    } else {
        // return first rename relation
        return ((RenameNamedRelation) renameRelations.get(0)).name;
    }
  }


  /**
   * @see magellan.library.Addeable#addAttribute(java.lang.String, java.lang.String)
   */
  public void addAttribute(String key, String value) {
    attributes.put(key, value);
  }

  /**
   * @see magellan.library.Addeable#containsAttribute(java.lang.String)
   */
  public boolean containsAttribute(String key) {
    return attributes.containsKey(key);
  }

  /**
   * @see magellan.library.Addeable#getAttribute(java.lang.String)
   */
  public String getAttribute(String key) {
    return attributes.get(key);
  }

  /**
   * @see magellan.library.Addeable#getAttributeKeys()
   */
  public List<String> getAttributeKeys() {
    return new ArrayList<String>(attributes.keySet());
  }

  /**
   * @see magellan.library.Addeable#getAttributeSize()
   */
  public int getAttributeSize() {
    return attributes.size();
  }
}