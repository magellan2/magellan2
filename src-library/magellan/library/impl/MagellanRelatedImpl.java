package magellan.library.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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
  private Map<String, String> attributes = new LinkedHashMap<String, String>();

  /**
   * Constructs a new described object that is uniquely identifiable by the specified id.
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
    if (getRelations().remove(rel))
      return rel;
    else
      return null;
  }

  protected abstract Collection<UnitRelation> getRelations();

  /**
   * Delivers all relations of the given class (and its subtypes!). Returns a Collection over the
   * relations this unit has to other units. The collection consist of <tt>UnitRelation</tt>
   * objects. The UnitRelation objects are filtered by the given relation class.
   * 
   * @see magellan.library.Related#getRelations(java.lang.Class)
   */
  public <T extends UnitRelation> List<T> getRelations(Class<T> relationClass) {
    List<T> ret = new LinkedList<T>();

    for (UnitRelation relation : getRelations()) {
      if (relationClass.isInstance(relation)) {
        @SuppressWarnings("unchecked")
        T toAdd = (T) relation;
        ret.add(toAdd);
      }
    }

    return ret;
  }

  /**
   * @see magellan.library.Named#getModifiedName()
   */
  @Override
  public String getModifiedName() {
    List<RenameNamedRelation> renameRelations = getRelations(RenameNamedRelation.class);
    for (RenameNamedRelation rel : renameRelations)
      if (rel.named == this)
        return rel.name;
    return null;
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
