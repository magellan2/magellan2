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

package magellan.library.impl;

import java.util.Map;
import java.util.Locale;

import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Resources;
import magellan.library.utils.SpellSyntax;


/**
 * Container class for a spell based on its representation in a cr version >= 42.
 */
public class MagellanSpellImpl extends MagellanDescribedImpl implements Spell {
	private int blockID = -1; // this is the id of the ZAUBER block in the cr
	private int level = -1; // a mage's level has to be at least this value to be able to cast this spell
	private int rank = -1;
	private String type = null; // represents the 'class' tag, can't be named like that, though
	private boolean onShip = false;
	private boolean onOcean = false;
	private boolean isFamiliar = false;
	private boolean isFar = false;
	
	private GameData data = null;
	
	private Map<String,String> components = null; // map of String objects
  private Locale locale = null;
	
	private String syntax = null; // FF 20070221 new CR tag syntax
	/**
	 * the spellsytnax object
	 */
	private SpellSyntax spellSyntax = null;

	/**
	 * Creates a new Spell object.
	 *
	 * 
	 */
	public MagellanSpellImpl(ID id, GameData _data) {
		super(id);
		this.data = _data;
	}

	// TODO: this is bad, but right now i dont have a better idea
	/** 
	 * @see magellan.library.Unique#getID()
	 */
	public ID getID() {
	    return StringID.create(getName());
	}

	/**
	 * Returns the integer serving as the block id in the cr.
	 *
	 * 
	 */
	public int getBlockID() {
		return blockID;
	}

	/**
	 * Sets the integer serving as the block id in the cr.
	 *
	 * 
	 */
	public void setBlockID(int id) {
		this.blockID = id;
	}

	/**
	 * Returns the level of this spell which indicates the lowest skill level a mage must have to
	 * be able to cast this spell.
	 *
	 * 
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Sets the level of this spell which indicates the lowest skill level a mage must have to be
	 * able to cast this spell.
	 *
	 * 
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

	/**
	 * Returns the class attribute of this spell.
	 *
	 * 
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Sets the class attribute of this spell.
	 *
	 * 
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean getOnOcean() {
		return onOcean;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setOnOcean(boolean onOcean) {
		this.onOcean = onOcean;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean getOnShip() {
		return onShip;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setOnShip(boolean onShip) {
		this.onShip = onShip;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean getIsFamiliar() {
		return isFamiliar;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setIsFamiliar(boolean isFamiliar) {
		this.isFamiliar = isFamiliar;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean getIsFar() {
		return isFar;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setIsFar(boolean _isFar) {
		isFar = _isFar;
	}

	/**
	 * Returns the components of this spell as Strings.
	 *
	 * 
	 */
	public Map<String,String> getComponents() {
		if (components==null)
			components = new OrderedHashtable<String,String>();
		return components;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setComponents(Map<String,String> components) {
		this.components = components;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String toString() {
		return this.getName();
	}

	/**
	 * Returns a name for this spell's type.
	 *
	 * 
	 */
	public String getTypeName() {
		if(this.type != null) {
			return Resources.get("spell."+this.type);
		} else {
			return Resources.get("spell.unspecified");
		}
	}

	/**
	 * A string with information about the syntax of the spell (FF)
	 * @return
	 */
	public String getSyntaxString(){
		String retVal = "";
		
		// Region, if is far
		if (isFar){
			retVal = "[" + Resources.get("spell.region") + " X Y]";
		}
		
		// Level...allways possible, but not allways usefull
		// we have no info, how to decide here - we add it.
		if (retVal.length()>0){
			retVal += " ";
		}
		retVal += "[" + Resources.get("spell.level") + " n]";
		
		// name of spell in "
		if (retVal.length()>0){
			retVal += " ";
		}
		
		String spellName = this.getName();
		if (this.data!=null){
		  spellName = this.data.getTranslation(spellName);
		}
		
		retVal += "\"" + spellName + "\"";
		
		// Syntax
		if (this.getSpellSyntax()!=null && this.getSpellSyntax().toString()!=null){
			if (retVal.length()>0){
				retVal += " ";
			}
			retVal += this.getSpellSyntax().toString();
		}
		
		// if nothing was added, return null
		if (retVal.length()==0) {
			retVal = null;
		} else {
			// pr?fix: 
			retVal = "Syntax: " + Resources.getOrderTranslation(EresseaConstants.O_CAST) + " " + retVal;
		}
		return retVal;
	}
	
	

	/**
	 * Enno in e-client about the syntax:
	 * 'c' = Zeichenkette
	 * 'k' = REGION|EINHEIT|STUFE|SCHIFF|GEBAEUDE
	 * 'i' = Zahl
	 * 's' = Schiffsnummer
	 * 'b' = Gebaeudenummer
	 * 'r' = Regionskoordinaten (x, y)
	 * 'u' = Einheit
	 * '+' = Wiederholung des vorangehenden Parameters
	 * '?' = vorangegangener Parameter ist nicht zwingend

	 *	Syntaxcheks, die der Server auf dieser Basis macht, sind nicht perfekt;
	 *	es ist notwendig, aber nicht hinreichend, dass die Syntax erfuellt wird.
	 *	Aber in den vielen Faellen kann man damit schonmal sagen, was denn
	 *	falsch war.
	 *
	 * 
	 * @return the syntax
	 */
	public String getSyntax() {
		return syntax;
	}
	

	/**
	 * @param syntax the syntax to set
	 */
	public void setSyntax(String syntax) {
		this.syntax = syntax; 
	}
	
	/**
	 * returns the spellsyntax object of this spell
	 * @return a SpellSyntax object
	 */
	public SpellSyntax getSpellSyntax(){
		if (this.syntax==null || this.syntax.length()==0){
			return null;
		}
		
		// creating a new one if it does not exists
		if (this.spellSyntax==null){
			this.spellSyntax = new SpellSyntax(this.syntax);
		}
		
		return this.spellSyntax;
	}

  /**
   * Only returns the locale of the spell
   * 
   * @see magellan.library.Localized#getLocale()
   * 
   * @return the locale of the spell
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Sets the locale and invalidates the description if required.
   * 
   * @see magellan.library.Localized#setLocale(java.util.Locale)
   */
  public void setLocale(Locale locale) {
    if ((locale != null) && (!locale.equals(this.locale))) {
      super.setDescription(null);
      this.locale = locale;
    } 
  }
	
	
}
