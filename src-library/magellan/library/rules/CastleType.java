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

package magellan.library.rules;

import java.util.Collection;
import java.util.Collections;

import magellan.library.ID;
import magellan.library.Item;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 203 $
 */
public class CastleType extends BuildingType implements Comparable {
	private int minSize;
	private int wage = -1;
	private int tax = -1;

  protected ItemType stone;

	/**
	 * Creates a new CastleType object.
	 *
	 * 
	 */
	public CastleType(ID id) {
		super(id);
	}
	
	public void init(ItemType stone){
	  this.stone = stone;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setPeasantWage(int w) {
		wage = w;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getPeasantWage() {
		return wage;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setTradeTax(int t) {
		tax = t;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getTradeTax() {
		return tax;
	}

	/**
	 * Sets the minimum size. 
	 */
	public void setMinSize(int s) {
		this.minSize = s;
	}

	/**
	 * Returns the minimum size of this type.
	 */
	public int getMinSize() {
		return minSize;
	}

	/**
	 * Imposes a natural ordering on CastleType objects according to their minimum size attribute.
	 * If obj is an instance of class BuildingType the return value reflects the natural ordering
	 * of the ids of this object and obj.
	 *
	 * 
	 *
	 * 
	 */
	@Override
  public int compareTo(Object obj) {
		if(obj instanceof CastleType) {
			CastleType c = (CastleType) obj;

			if(this.minSize < c.minSize) {
				return -1;
			}

			if(this.minSize > c.minSize) {
				return 1;
			}

			return 0;
		} else {
			return super.compareTo(obj);
		}
	}

  protected boolean fallBackMaterial = true;

  /**
   * @see magellan.library.rules.ConstructibleType#addRawMaterial(magellan.library.Item)
   */
  @Override
  public void addRawMaterial(Item i) {
    fallBackMaterial = false;
    super.addRawMaterial(i);
  }

  /**
   * @see magellan.library.rules.ConstructibleType#getRawMaterial(magellan.library.ID)
   */
  @Override
  public Item getRawMaterial(ID id) {
    // if no raw materials have been added we fall back to old behavior: 1 wood per size point
    if (fallBackMaterial) {
      if (id.equals(stone.getID()))
        return new Item(new ItemType(id), 1);
      else
        return null;
    } else {
      return super.getRawMaterial(id);
    }
  }

  /**
   * @see magellan.library.rules.ConstructibleType#getRawMaterials()
   */
  @Override
  public Collection<Item> getRawMaterials() {
    if (fallBackMaterial)
      return Collections.singletonList(new Item(stone, 1));
    else
      return super.getRawMaterials();
  }

}
