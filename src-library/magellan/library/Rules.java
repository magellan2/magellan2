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

import java.util.Iterator;

import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.rules.AllianceCategory;
import magellan.library.rules.BuildingType;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.ObjectType;
import magellan.library.rules.OptionCategory;
import magellan.library.rules.Race;
import magellan.library.rules.RegionType;
import magellan.library.rules.ShipType;
import magellan.library.rules.SkillCategory;
import magellan.library.rules.SkillType;


/**
 * <p>
 * A class summarizing the static information about a game system (a set of rules).
 * </p>
 * 
 * <p>
 * If internationalization is a concern, implementing sub-classes should ensure that the
 * access-methods to the various collections (<tt>getXXX()</tt>) return their objects not only by
 * their (usually language-independent) id but also by their (laguage-dependent) name as it may be
 * supplied by the user.
 * </p>
 * 
 * <p>
 * If necessary, subclasses could also provide additional access methods to distinguish between an
 * access by id or name.
 * </p>
 * 
 * <p>
 * The methods called getXXX(ID id, boolean add) adds and returns  a new Object.
 * </p>
 */
public interface Rules {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public RegionType getRegionType(ID id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public RegionType getRegionType(ID id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator<RegionType> getRegionTypeIterator();

	/**
	 * get RegionType by (possibly localized) name
	 *
	 * 
	 */
	public RegionType getRegionType(String id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public RegionType getRegionType(String id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Race getRace(ID id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public Race getRace(ID id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getRaceIterator();

	/**
	 * get Race by (possibly localized) name
	 *
	 * 
	 */
	public Race getRace(String id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Race getRace(String id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ShipType getShipType(ID id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public ShipType getShipType(ID id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getShipTypeIterator();

	/**
	 * get ShipType by (possibly localized) name
	 *
	 * 
	 */
	public ShipType getShipType(String id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ShipType getShipType(String id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public BuildingType getBuildingType(ID id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public BuildingType getBuildingType(ID id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getBuildingTypeIterator();

	/**
	 * get BuildingType by (possibly localized) name
	 *
	 * 
	 */
	public BuildingType getBuildingType(String id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public BuildingType getBuildingType(String id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public CastleType getCastleType(ID id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public CastleType getCastleType(ID id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getCastleTypeIterator();

	/**
	 * get CastleType by (possibly localized) name
	 *
	 * 
	 */
	public CastleType getCastleType(String id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public CastleType getCastleType(String id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ItemType getItemType(ID id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public ItemType getItemType(ID id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator<ItemType> getItemTypeIterator();

	/**
	 * get ItemType by (possibly localized) name
	 *
	 * 
	 */
	public ItemType getItemType(String id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ItemType getItemType(String id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public SkillType getSkillType(ID id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public SkillType getSkillType(ID id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator<SkillType> getSkillTypeIterator();

	/**
	 * get SkillType by (possibly localized) name
	 *
	 * 
	 */
	public SkillType getSkillType(String id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public SkillType getSkillType(String id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ItemCategory getItemCategory(ID id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public ItemCategory getItemCategory(ID id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getItemCategoryIterator();

	/**
	 * get ItemCategory by (possibly localized) name
	 *
	 * 
	 */
	public ItemCategory getItemCategory(String id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ItemCategory getItemCategory(String id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public SkillCategory getSkillCategory(ID id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public SkillCategory getSkillCategory(ID id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getSkillCategoryIterator();

	/**
	 * get SkillCategory by (possibly localized) name
	 *
	 * 
	 */
	public SkillCategory getSkillCategory(String id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public SkillCategory getSkillCategory(String id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public OptionCategory getOptionCategory(ID id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public OptionCategory getOptionCategory(ID id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getOptionCategoryIterator();

	/**
	 * get OptionCategory by (possibly localized) name
	 *
	 * 
	 */
	public OptionCategory getOptionCategory(String id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public OptionCategory getOptionCategory(String id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public AllianceCategory getAllianceCategory(ID id);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public AllianceCategory getAllianceCategory(ID id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Iterator getAllianceCategoryIterator();

	/**
	 * get AllianceCategory by (possibly localized) name
	 *
	 * 
	 */
	public AllianceCategory getAllianceCategory(String id, boolean add);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public AllianceCategory getAllianceCategory(String id);

	/**
	 * Changes the name of an object identified by the given old name.
	 *
	 * @return the modified object type or null, if no object type is registered with the specified
	 * 		   id.
	 */
	public ObjectType changeName(String from, String to);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setGameSpecificStuffClassName(String className);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public GameSpecificStuff getGameSpecificStuff();
}
