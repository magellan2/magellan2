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

import java.util.HashMap;
import java.util.Map;

import magellan.library.ID;

public class SkillType extends ObjectType {
	protected SkillCategory category;
  private int cost = 0;
  Map<Integer, Integer> costMap = new HashMap<Integer, Integer>(0);

	/**
	 * Creates a new SkillType object.
	 *
	 * 
	 */
	public SkillType(ID id) {
		super(id);
	}

	/**
	 * Returns the skill category. 
	 */
	public SkillCategory getCategory() {
		return category;
	}

	/**
	 * Adds this skill to the specified category. 
	 */
	public void setCategory(SkillCategory sc) {
		category = sc;

		if(sc != null) {
			sc.addInstance(this);
		}
	}

  /**
   * Sets the default cost of this SkillType to <code>cost</code>. If no level cost ist specified,
   * this value will be returned by getCost.
   * 
   * @param cost
   */
  public void setCost(int cost) {
    this.cost  = cost;
  }
  
  /**
   * Sets the cost of learning to <code>level</code> to cost. 
   * 
   * @param level
   * @param cost
   */
  public void setCost(int level, int cost){
    this.costMap.put(level, cost);
  }
  
  /**
   * Returns the cost of learning to <code>level</code>. If no costs have ben set, the default is 0. 
   * 
   * @param level
   * @return
   */
  public int getCost(int level){
    if (costMap.get(level)==null)
      return this.cost;
    else
      return costMap.get(level);
  }
}
