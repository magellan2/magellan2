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

package magellan.library;

import java.util.ArrayList;
import java.util.List;

import magellan.library.rules.Date;
import magellan.library.rules.EresseaDate;
import magellan.library.rules.ItemType;

/**
 * A class representing a resource and its attributes in a region. The id of RegionResource objects
 * is numerical and does not change between reports of different turns, it can therefore be used
 * for merging reports. In order to access a resource in a region this id proves unuseful, though.
 * Instead, the id of the resource's type makes more sense as it also satisfies the uniqueness
 * condition within a region.
 */
public class RegionResource implements Unique {
	private ID id = null; // the numerical id of this resource, also the block id in the cr
	private int skillLevel = -1; // the minimum skill level required to access the resource
	private ItemType type = null; // the type of resource
	private int amount = -1; // the amount of the resource available
  private Date date = null; // the (game)round the information was originated

	/**
	 * Constructs a new region resource with the specified id and type. There is no default
	 * constructor in order to enforce a valid id and type set for every RegionResource object.
	 *
	 * 
	 * 
	 */
	public RegionResource(ID id, ItemType type) {
		this.id = id;
		this.type = type;
	}

	/**
	 * This method allows to set the id of this resource even after object creation. It should be
	 * use with care as ids are often used as map keys or similar objects and changing them will
	 * have non-obvious side effects.
	 *
	 * 
	 *
	 * @throws IllegalArgumentException DOCUMENT-ME
	 */
	public void setID(ID id) {
		if(id == null) {
			throw new IllegalArgumentException("RegionResource.setID(): specified id is null!");
		}

		this.id = id;
	}

	/**
	 * Returns the id uniquely identifying this resource.
	 *
	 * 
	 */
	public ID getID() {
		return this.id;
	}

	/**
	 * Specifies the type of the resource. Semantically, only a small range of item types are valid
	 * for a resource (iron, trees, etc.) Note that the type may server as a hash object for this
	 * resource and changing it may require re-hashing.
	 *
	 * 
	 *
	 * @throws IllegalArgumentException DOCUMENT-ME
	 */
	public void setType(ItemType type) {
		if(type == null) {
			throw new IllegalArgumentException("RegionResource.setType(): specified item type is null!");
		}

		this.type = type;
	}

  /**
   * Returns the type of this resource.
   *
   * 
   */
  public ItemType getType() {
    return type;
  }
  
  /**
   * Specifies the round, in which the information was "new" 
   *
   * @throws IllegalArgumentException DOCUMENT-ME
   */
  public void setDate(int Round) {
    if (this.date==null){
      this.date = new EresseaDate(Round);
    } else {
      this.date.setDate(Round);
    }
  }
  
  /**
   * Returns the Date, the ressource info was last updated
   *
   */
  public Date getDate(){
    return this.date;
  }
  
  
	/**
	 * Sets the amount of the resource visible or available.
	 *
	 * 
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * Returns the amount of the resource visible or available.
	 *
	 * 
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * Sets the minimum skill level that is required to access this resource.
	 *
	 * 
	 */
	public void setSkillLevel(int level) {
		this.skillLevel = level;
	}

	/**
	 * Returns the minimum skill level that is required to access this resource.
	 *
	 * 
	 */
	public int getSkillLevel() {
		return this.skillLevel;
	}

	/**
	 * Returns a string representation of this resource object.
	 *
	 * 
	 */
	@Override
  public String toString() {
		return type.toString();
	}

	/**
	 * This method is a shortcut for calling this.getType().getName()
	 *
	 * 
	 */
	public String getName() {
		return type.getName();
	}

	/**
	 * Indicates whether some other object is "equal to" this one based on the ID of this object
	 *
	 * 
	 *
	 * 
	 */
	@Override
  public boolean equals(Object o) {
		try {
			// pavkovic 2003.01.16: even if the id seems to be unique 
			// use the item type for uniqueness
			return this.getType().getID().equals(((RegionResource) o).getType().getID());
			
			// return this.getID().equals(((RegionResource)o).getID());
		} catch(ClassCastException e) {
			return false;
		}
	}

	/**
	 * Returns a hash code value for the object based on the ID of this object.
	 *
	 * 
	 */
	@Override
  public int hashCode() {
		// pavkovic 2003.01.16: even if the id seems to be unique 
		// use the item type for uniqueness
		return this.getType().getID().hashCode();

		//return this.getID().hashCode();
	}

	/**
	 * Merges the information of curRes into newRes.
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public static void merge(GameData curGD, RegionResource curRes, GameData newGD,
							 RegionResource newRes, boolean sameTurn) {
		/* the constructor enforces a valid id and type, so we do not
		 need to set them here */
		if(sameTurn) {
			// only add higher skill level if amount is well known
			if(curRes.getSkillLevel() >= newRes.getSkillLevel() && curRes.getAmount() != -1) {
				newRes.setSkillLevel(curRes.getSkillLevel());
				newRes.setAmount(curRes.getAmount());
        // set the round as actual game data date, because with curRes.amount!=-1 we
        // are sure to have some info
        if (curRes.getDate()!=null){
          newRes.setDate(curRes.getDate().getDate());
        }
			}
		} else {
			if(curRes.getAmount() != -1 && newRes.getSkillLevel() == -1) {
				newRes.setSkillLevel(curRes.getSkillLevel());
				newRes.setAmount(curRes.getAmount());
        if (curRes.getDate()!=null){
          newRes.setDate(curRes.getDate().getDate());
        } else {
          newRes.setDate(curGD.getDate().getDate());
        }
			}
		}
	}
	

  /**
   * @see magellan.library.Addeable#addAttribute(java.lang.String, java.lang.String)
   */
  public void addAttribute(String key, String value) {
    throw new RuntimeException("this method is not implemented");
  }

  /**
   * @see magellan.library.Addeable#containsAttribute(java.lang.String)
   */
  public boolean containsAttribute(String key) {
    return false;
  }

  /**
   * @see magellan.library.Addeable#getAttribute(java.lang.String)
   */
  public String getAttribute(String key) {
    throw new RuntimeException("this method is not implemented");
  }

  /**
   * @see magellan.library.Addeable#getAttributeKeys()
   */
  public List<String> getAttributeKeys() {
    return new ArrayList<String>();
  }

  /**
   * @see magellan.library.Addeable#getAttributeSize()
   */
  public int getAttributeSize() {
    return 0;
  }
}
