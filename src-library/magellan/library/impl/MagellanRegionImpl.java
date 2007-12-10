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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import magellan.library.Border;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Identifiable;
import magellan.library.Island;
import magellan.library.Item;
import magellan.library.LuxuryPrice;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.Scheme;
import magellan.library.Ship;
import magellan.library.Sign;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.ZeroUnit;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemType;
import magellan.library.rules.RegionType;
import magellan.library.utils.Cache;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.Regions;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 356 $
 */
public class MagellanRegionImpl extends MagellanUnitContainerImpl implements Region {
	private static final Logger log = Logger.getInstance(MagellanRegionImpl.class);

	/** DOCUMENT-ME */
	public int trees = -1;

	/** DOCUMENT-ME */
	public int oldTrees = -1;

	/** DOCUMENT-ME */
	public int sprouts = -1;

	/** DOCUMENT-ME */
	public int oldSprouts = -1;

	/** DOCUMENT-ME */
	public boolean mallorn = false;

	/** DOCUMENT-ME */
	public int iron = -1;

	/** DOCUMENT-ME */
	public int oldIron = -1;

	/** DOCUMENT-ME */
	public int laen = -1;

	/** DOCUMENT-ME */
	public int oldLaen = -1;

	/** DOCUMENT-ME */
	public int peasants = -1;

	/** DOCUMENT-ME */
	public int oldPeasants = -1;

	/** DOCUMENT-ME */
	public int silver = -1;

	/** DOCUMENT-ME */
	public int oldSilver = -1;

	/** DOCUMENT-ME */
	public int horses = -1;

	/** DOCUMENT-ME */
	public int oldHorses = -1;

	/** DOCUMENT-ME */
	public int stones = -1;

	/** DOCUMENT-ME */
	public int oldStones = -1;

	/**
	 * The wage persons can earn by working in this region. Unfortunately this is not the wage
	 * peasants earn but the wage a player's persons earn and to make it worse, the eressea server
	 * puts different values into CRs depending of the race of the 'owner' faction of the report.
	 * I.e. an orc faction gets a different value than factions of other races. Therefore there is
	 * a getPeasantWage() method returning how much a peasant earns in this region depending on
	 * the biggest castle.
	 */
	public int wage = -1;

	/** the wage persons have been able to earn in the past. */
	public int oldWage = -1;

	/** DOCUMENT-ME */
	public ItemType herb = null;

	/** DOCUMENT-ME */
	public String herbAmount = null;

	/** Indicates that there are too many orcs in this region. */
	public boolean orcInfested = false;

	// pavkovic 2002.05.13: for eressea CR-Version >= 64 we do interpret the recruits tag

	/** DOCUMENT-ME */
	public int recruits = -1;

	/** DOCUMENT-ME */
	public int oldRecruits = -1;
	
  protected boolean isActive = false;
	
	// fiete 2007.02.12: we add sign support - 2 lines allowed
	private List<Sign> signs = null;
	
	/**
	 * a flag which indicates if this region is Ozean with a neighboring not-ozean region
	 * used for better pathfindung for ships
	 * -1 -> not computed yet
	 * 0 -> either no ozean or no neighboring land
	 * 1 -> ozean and neighboring land 
	 */
	private int ozeanWithCoast = -1;
	
	
	private ID treesID = StringID.create("Baeume");
	private ID mallornID = StringID.create("Mallorn");
	private ID sproutsID = StringID.create("Schoesslinge");
	private ID mallornSproutsID = StringID.create("Mallornsch?sslinge");
	

	/** Informs about the reason why this region is visible. */
  private String visibility = null;
	
	/**
	 * Constructs a new Region object uniquely identifiable by the specified id.
	 */
	public MagellanRegionImpl(CoordinateID id, GameData data) {
		super(id, data);
	}

	// pavkovic 2003.09.10: moved from Cache to this object to remove
	// Cache objects for empty/ocean regions
	// used in swing.map.RegionImageCellRenderer per region
	private int fogOfWar = -1;

	/**
	 * DOCUMENT-ME
	 */
	public synchronized boolean fogOfWar() {
		if(fogOfWar == -1) {
			fogOfWar = 1;

			for(Iterator<Unit> iter = units().iterator(); iter.hasNext();) {
				Faction f = iter.next().getFaction();

				if(f.isPrivileged()) {
					fogOfWar = 0;

					break;
				}
			}
		}

		return fogOfWar == 1;
	}

	/**
	 * DOCUMENT-ME
	 */
	public synchronized void setFogOfWar(int fog) {
		fogOfWar = fog;
	}

	/** this unit indicated the "0" unit! */
	private ZeroUnit cachedZeroUnit;

	/**
	 * DOCUMENT-ME
	 */
	public Unit getZeroUnit() {
		// only create if needed
		if(cachedZeroUnit == null) {
			// if there are no units in this region we assume that this
			// region is less interesting (there will be NO Relation nor
			// massive interactive view of this region.
			// So we create the ZeroUnit on the fly.
			if(units().isEmpty()) {
				return MagellanFactory.createZeroUnit(this);
			}

			cachedZeroUnit = MagellanFactory.createZeroUnit(this);
		}

		return cachedZeroUnit;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the number of modified persons after "give 0", recruit
	 */
	public int getModifiedPeasants() {
		ZeroUnit zu = (ZeroUnit) getZeroUnit();

		// peasants == peasants - (maxRecruit() - recruited peasants ) + givenPersons
		return (this.peasants == -1) ? (-1)
									 : (this.peasants - zu.getPersons() + zu.getModifiedPersons() +
									 zu.getGivenPersons());
	}

	/**
	 * DOCUMENT-ME
	 */
	public int modifiedRecruit() {
		return getZeroUnit().getModifiedPersons();
	}

	/** Indicates that refreshUnitRelations() has already been called once. */
	private boolean unitRelationsRefreshed = false;

	/** The island this region belongs to. */
	private Island island = null;

	/**
	 * Sets the island this region belongs to.
	 */
	public void setIsland(Island i) {
		if(this.island != null) {
			this.island.invalidateRegions();
		}

		this.island = i;

		if(this.island != null) {
			this.island.invalidateRegions();
		}
	}

	/**
	 * Returns the island this region belongs to.
	 */
	public Island getIsland() {
		return this.island;
	}


	/**
	 * A string constant indicating why this region is visible.
	 *
	 * @return the string object or null, if the visibility is unspecified.
	 */
	public String getVisibility() {
		return this.visibility;
	}

	
	/**
	 * Represents the quality of the visibility as an int value
	 * 0..very poor - no info (->visibility=null) 
	 * 1..neighbour
	 * 2..lighthouse
	 * 3..travel
	 * 4..qualified unit in region (->visibility=null)
	 * @return
	 */
	public int getVisibilityInteger(){
	  // the result, some call it res
	  int erg = 0;
	  if (this.visibility==null){
	    // we have 0 or 4
	    // check for qualified units
	    boolean qualifiedUnitInCurRegion=false;
      if (this.units()!=null && this.units().size()>0){
        for (Iterator<Unit> iter = this.units().iterator();iter.hasNext();){
          Unit actUnit = (Unit)iter.next();
          if (actUnit.getCombatStatus()!=-1){
            // -1 is default for this int and stays, if no info is available
            qualifiedUnitInCurRegion=true;
            break;
          }
        }
      }
      if (qualifiedUnitInCurRegion){
        return 4;
      } else {
        return 0;
      }
	  } else {
	    // we have a visibility ... choose right int
	    if (this.visibility.equalsIgnoreCase("neighbour")){
	      return 1;
	    }
	    if (this.visibility.equalsIgnoreCase("lighthouse")){
        return 2;
      }
	    if (this.visibility.equalsIgnoreCase("travel")){
        return 3;
      }
	  }
	  return erg;
	}
	
	
	/**
	 * Sets a string constant indicating why this region is visible.
	 *
	 * @param vis a String object or null to indicate that the visibility cannot be determined.
	 */
	public void setVisibility(String vis) {
		this.visibility = vis;
	}

	/**
	 * 0..very poor - no info (->visibility=null) 
   * 1..neighbour
   * 2..lighthouse
   * 3..travel
   * 4..qualified unit in region (->visibility=null)
	 * 
	 * @param i
	 */
	public void setVisibility(int i){
	  if (i==0 || i==4){
	    this.visibility=null;
	  }
	  if (i==1){
	    this.visibility="neighbour";
	  }
	  if (i==2){
      this.visibility="lighthouse";
    }
	  if (i==3){
      this.visibility="travel";
    }
	}
	
	/**
	 * The prices for luxury goods in this region. The map contains the name of the luxury good as
	 * instance of class <tt>StringID</tt> as key and instances of class <tt>LuxuryPrice</tt> as
	 * values.
	 */
	public Map<ID,LuxuryPrice> prices = null;

	/** The prices of luxury goods of the last turn. */
	public Map<ID,LuxuryPrice> oldPrices = null;

	/** The messages for this region. The list consists of objects of class <tt>Message</tt>. */
	public List<Message> messages = null;

	/**
	 * Special messages related to this region. The list contains instances of class
	 * <tt>Message</tt> with type -1 and only the text set.
	 */
	public List<Message> events = null;

	/**
	 * Special messages related to this region. The list contains instances of class
	 * <tt>Message</tt> with type -1 and only the text set.
	 */
	public List<Message> playerMessages = null;

	/**
	 * Special messages related to this region. The list contains instances of class
	 * <tt>Message</tt> with type -1 and only the text set.
	 */
	public List<Message> surroundings = null;

	/**
	 * Special messages related to this region. The list contains instances of class
	 * <tt>Message</tt> with type -1 and only the text set.
	 */
	public List<Message> travelThru = null;

	/**
	 * Special messages related to this region. The list contains instances of class
	 * <tt>Message</tt> with type -1 and only the text set.
	 */
	public List<Message> travelThruShips = null;

	/**
	 * RegionResources in this region. The keys in this map are instances of class <tt>ID</tt>
	 * identifying the item type of the resource, the values are instances of class
	 * <tt>RegionResource</tt>.
	 */
	private Map<Identifiable,RegionResource> resources = null;

	/** A collection view of the resources. */
	private Collection<RegionResource> resourceCollection = null;

	/**
	 * Returns all resources of this region.
	 */
	public Collection<RegionResource> resources() {
		if(this.resourceCollection == null) {
			/* since resources appear twice in the map, once with the
			 numerical ID and once with the item type ID, we have to
			 make sure that this collection lists only one of them.
			 Since the hashValue() Method of a RegionResource relates
			 to its numerical ID a HashSet can do the job */

			// 2002.02.18 ip: this.resources can be null
      if (this.resources == null) {
        this.resourceCollection = new ArrayList<RegionResource>();
      } else {
        this.resourceCollection = new HashSet<RegionResource>(this.resources.values());
      }
		}

		return this.resourceCollection;
	}

	/**
	 * Adds a resource to this region.
	 * @throws NullPointerException
	 */
	public RegionResource addResource(RegionResource resource) {
		if(resource == null) {
			throw new NullPointerException();
		}

		if(this.resources == null) {
			this.resources = new OrderedHashtable<Identifiable, RegionResource>();

			// enforce the creation of a new collection view
			this.resourceCollection = null;
		}

		// pavkovic 2002.05.21: If some resources have an amount zero, we ignore it
		if(resource.getAmount() != 0) {
			this.resources.put(resource.getType(), resource);
		}

		// 		if(log.isDebugEnabled()) {
		// 			log.debug("Region.addResource:" + this);
		// 			log.debug("Region.addResource:" + resource);
		// 			log.debug("Region.addResource:" + resources);
		// 		}
		return resource;
	}

	/**
	 * Removes the resource with the specified numerical id or the id of its item type from this
	 * region.
	 *
	 * @return the removed resource or null if no resource with the specified id exists in this
	 * 		   region.
	 */
	public RegionResource removeResource(RegionResource r) {
		return this.removeResource(r.getType());
	}

	/**
	 * DOCUMENT-ME
	 */
	public RegionResource removeResource(ItemType type) {
		if(this.resources == null) {
			return null;
		}

		RegionResource ret = (RegionResource) this.resources.remove(type);

		if(this.resources.isEmpty()) {
			this.resources = null;
			this.resourceCollection = null;
		}

		// 		if(log.isDebugEnabled()) {
		// 			log.debug("Region.removeResource:" + this);
		// 			log.debug("Region.removeResource:" + ret);
		// 			if(ret != null) {
		// 				log.debug("Region.removeResource:" + ret.getID());
		// 				log.debug("Region.removeResource:" + ret.getType().getID());
		// 			}
		// 			log.debug("Region.removeResource:" + resources);
		// 		}
		return ret;
	}

	/**
	 * Removes all resources from this region.
	 */
	public void clearRegionResources() {
		if(this.resources != null) {
			this.resources.clear();
			this.resources = null;
			this.resourceCollection = null;
		}
	}

	/**
	 * Returns the resource with the ID of its item type.
	 *
	 * @return the resource object or null if no resource with the specified ID exists in this
	 * 		   region.
	 */
	public RegionResource getResource(ItemType type) {
		return (this.resources != null) ? (RegionResource) this.resources.get(type) : null;
	}

	/**
	 * Schemes in this region. The keys in this map are instances of class <tt>Coordinate</tt>
	 * identifying the location of the scheme, the values are instances of class <tt>Scheme</tt>.
	 */
	private Map<ID,Scheme> schemes = null;

	/** A collection view of the schemes. */
	private Collection<Scheme> schemeCollection = null;

	/**
	 * Returns all schemes of this region.
	 */
	public Collection<Scheme> schemes() {
		if(schemes == null) {
			return new ArrayList<Scheme>();
		}

		if(schemeCollection == null) {
      if (schemes != null && schemes.values() != null) {
        schemeCollection = Collections.unmodifiableCollection(schemes.values());
      } else {
        schemeCollection = new ArrayList<Scheme>();
      }
		}

		return schemeCollection;
	}

	/**
	 * Adds a scheme to this region.
	 * @throws NullPointerException
	 */
	public Scheme addScheme(Scheme scheme) {
		if(scheme == null) {
			throw new NullPointerException();
		}

		if(this.schemes == null) {
			this.schemes = new OrderedHashtable<ID, Scheme>();

			// enforce the creation of a new collection view
			// AG: Since we just create if the scheme map is non-null not necessary
			//this.schemeCollection = null;
		}

		this.schemes.put(scheme.getID(), scheme);

		return scheme;
	}

	
	// TODO: clean up
	// stm  2006.10.20
	// this is needed by nobody and unclear if it works, so I commented it out
//	/**
//	 * Removes the scheme with the specified id from this region.
//	 *
//	 * 
//	 *
//	 * @return the removed scheme or null if no scheme with the specified id exists in this region.
//	 */
//	public Scheme removeScheme(Scheme s) {
//		if(this.schemes == null) {
//			return null;
//		}
//
//		Scheme ret = (Scheme) this.schemes.remove(id);
//
//		if(this.schemes.isEmpty()) {
//			this.schemes = null;
//			this.schemeCollection = null;
//		}
//
//		return ret;
//	}


	/**
	 * Removes all schemes from this region.
	 */
	public void clearSchemes() {
		if(this.schemes != null) {
			this.schemes.clear();
			this.schemes = null;
			this.schemeCollection = null;
		}
	}

	/**
	 * Returns the scheme with the specified corodinate.
	 *
	 * 
	 *
	 * @return the scheme object or null if no scheme with the specified ID exists in this region.
	 */
	public Scheme getScheme(ID id) {
		return (this.schemes != null) ? (Scheme) this.schemes.get(id) : null;
	}

	/** Border elements of this region. The list contains instances of class <tt>Border</tt>. */
	private Map<ID,Border> borders = null;

	/** A collection view of the borders. */
	private Collection<Border> borderCollection = null;

	/**
	 * Returns all borders of this region.
	 */
	public Collection<Border> borders() {
		if(borders == null) {
			return new ArrayList<Border>();
		}

		if(borderCollection == null) {
      if (borders != null && borders.values() != null) {
        borderCollection = Collections.unmodifiableCollection(borders.values());
      } else {
			  borderCollection = new ArrayList<Border>();
      }
		}

		return borderCollection;
	}

	/**
	 * Adds a border to this region.
	 *
	 * @throws NullPointerException if border is <code>null</code>
	 */
	public Border addBorder(Border border) {
		if(border == null) {
			throw new NullPointerException();
		}

		if(this.borders == null) {
			this.borders = new OrderedHashtable<ID, Border>();

			// enforce the creation of a new collection view
			// AG: Since we just create if the scheme map is non-null not necessary
			// this.borderCollection = null;
		}

		this.borders.put(border.getID(), border);

		return border;
	}

	// TODO: clean up
	// stm
	// see removeScheme above
//	/**
//	 * Removes the border with the specified id from this region.
//	 *
//	 * 
//	 *
//	 * @return the removed border or null if no border with the specified id exists in this region.
//	 */
//	public Border removeBorder(Border b) {
//		if(borders == null) {
//			return null;
//		}
//
//		Border ret = (Border) borders.remove(id);
//
//		if(borders.isEmpty()) {
//			clearBorders();
//		}
//
//		return ret;
//	}
	//// edited by stm

	/**
	 * Removes all borders from this region.
	 */
	public void clearBorders() {
		if(borders != null) {
			borders.clear();
			borders = null;
			borderCollection = null;
		}
	}

	/**
	 * Returns the border with the specified id.
   * 
	 * @return the border object or null if no border with the specified id exists in this region.
	 */
	public Border getBorder(ID key) {
		return (borders != null) ? (Border) borders.get(key) : null;
	}

	/** All ships that are in this container. */
	private Map<ID,Ship> ships = null;

	/** Provides a collection view of the ship map. */
	private Collection<Ship> shipCollection = null;

	/**
	 * Returns an unmodifiable collection of all the ships in this container.
	 */
	public Collection<Ship> ships() {
		if(ships == null) {
			return new ArrayList<Ship>();
		}

		if(shipCollection == null) {
      if (ships != null && ships.values() != null) {
        return Collections.unmodifiableCollection(ships.values());
      } else {
        return new ArrayList<Ship>();
      }
		}

		return shipCollection;
	}

	/**
	 * Retrieve a ship in this container by id.
	 */
	public Ship getShip(ID key) {
		return (ships != null) ? (Ship) ships.get(key) : null;
	}

	/**
	 * Adds a ship to this container. This method should only be invoked by Ship.setXXX() methods.
	 */
	public void addShip(Ship s) {
		if(ships == null) {
			ships = new Hashtable<ID, Ship>();

			// enforce the creation of a new collection view
			// AG: Since we just create if the ship map is non-null not necessary
			// this.shipCollection = null;
		}

		ships.put(s.getID(), s);
	}

	/**
	 * Removes a ship from this container. This method should only be invoked by Ship.setXXX()
	 * methods.
	 */
	public Ship removeShip(Ship s) {
		if(ships == null) {
			return null;
		}

		Ship ret = (Ship) ships.remove(s.getID());

		if(ships.isEmpty()) {
			ships = null;
			shipCollection = null;
		}

		return ret;
	}

	/** All buildings that are in this container. */
	private Map<ID,Building> buildings = null;

	/** Provides a collection view of the building map. */
	private Collection<Building> buildingCollection = null;

	/**
	 * Returns an unmodifiable collection of all the buildings in this container.
	 */
	public Collection<Building> buildings() {
		if(buildings == null) {
			return new ArrayList<Building>();
		}

		if(buildingCollection == null) {
      if (buildings != null && buildings.values() != null) {
        return Collections.unmodifiableCollection(buildings.values());
      } else {
        return new ArrayList<Building>();
      }
		}

		return buildingCollection;
	}

	/**
	 * Retrieve a building in this container by id.
	 */
	public Building getBuilding(ID key) {
		return (buildings != null) ? (Building) buildings.get(key) : null;
	}

	/**
	 * Adds a building to this container. This method should only be invoked by Building.setXXX()
	 * methods.
	 */
	public void addBuilding(Building u) {
		if(buildings == null) {
			buildings = new Hashtable<ID, Building>();

			// enforce the creation of a new collection view
			// AG: Since we just create if the builing map is non-null not necessary
			// this.buildingCollection = null;
		}

		buildings.put(u.getID(), u);
	}

	/**
	 * Removes a building from this container. This method should only be invoked by
	 * Building.setXXX() methods.
	 */
	public Building removeBuilding(Building b) {
		if(buildings == null) {
			return null;
		}

		Building ret = (Building) this.buildings.remove(b.getID());

		if(buildings.isEmpty()) {
			buildings = null;
			buildingCollection = null;
		}

		return ret;
	}

	/**
	 * Returns the items of all units that are stationed in this region and belonging to a faction
	 * that has at least a privileged trust level. The amount of the items of a particular item
	 * type are added up, so two units with 5 pieces of silver yield one silver item of amount 10
	 * here.
	 */
	public Collection<Item> items() {
		if((cache == null) || (cache.regionItems == null)) {
			refreshItems();
		}
    
    if (cache.regionItems != null && cache.regionItems.values() != null) {
      return Collections.unmodifiableCollection(cache.regionItems.values());
    } else {
      return new ArrayList<Item>();
    }
	}
	
	/**
	 * Returns the items of all units that are stationed in this region 
	 * The amount of the items of a particular item
	 * type are added up, so two units with 5 pieces of silver yield one silver item of amount 10
	 * here.
	 */
	public Collection<Item> allItems() {
		if((cache == null) || (cache.allRegionItems == null)) {
			refreshAllItems();
		}

    if (cache.allRegionItems != null && cache.allRegionItems.values() != null) {
      return Collections.unmodifiableCollection(cache.allRegionItems.values());
    } else {
      return new ArrayList<Item>();
    }
	}

	/**
	 * Returns a specific item from the items() collection identified by the item type.
	 */
	public Item getItem(ItemType type) {
		if((cache == null) || (cache.regionItems == null)) {
			refreshItems();
		}

		return ((cache != null) && (cache.regionItems != null))
			   ? (Item) cache.regionItems.get(type.getID()) : null;
	}

	/**
	 * Updates the cache of items owned by privileged factions in this region.
	 * Fiete 20061224: ...and the factions with "GIVE" alliances too.
	 */
	private void refreshItems() {
		if(cache != null) {
			if(cache.regionItems != null) {
				cache.regionItems.clear();
			} else {
				cache.regionItems = new Hashtable<ID, Item>();
			}
		} else {
			cache = new Cache();
			cache.regionItems = new Hashtable<ID, Item>();
		}

		for(Iterator<Unit> iter = units().iterator(); iter.hasNext();) {
			Unit u = iter.next();

			// if(u.getFaction().isPrivileged()) {
			if(u.getFaction().hasGiveAlliance() || u.getFaction().isPrivileged()) {
				for(Iterator unitItemIterator = u.getItems().iterator(); unitItemIterator.hasNext();) {
					Item item = (Item) unitItemIterator.next();
					Item i = (Item) cache.regionItems.get(item.getItemType().getID());

					if(i == null) {
						i = new Item(item.getItemType(), 0);
						cache.regionItems.put(item.getItemType().getID(), i);
					}

					i.setAmount(i.getAmount() + item.getAmount());
				}
			}
		}
	}
	
	/**
	 * Updates the cache of items owned by all factions in this region.
	 * @author Fiete
	 */
	private void refreshAllItems() {
		if(cache != null) {
			if(cache.allRegionItems != null) {
				cache.allRegionItems.clear();
			} else {
				cache.allRegionItems = new Hashtable<ID, Item>();
			}
		} else {
			cache = new Cache();
			cache.allRegionItems = new Hashtable<ID, Item>();
		}

		for(Iterator iter = units().iterator(); iter.hasNext();) {
			Unit u = (Unit) iter.next();
			
			for(Iterator unitItemIterator = u.getItems().iterator(); unitItemIterator.hasNext();) {
				Item item = (Item) unitItemIterator.next();
				Item i = (Item) cache.allRegionItems.get(item.getItemType().getID());

				if(i == null) {
					i = new Item(item.getItemType(), 0);
					cache.allRegionItems.put(item.getItemType().getID(), i);
				}

				i.setAmount(i.getAmount() + item.getAmount());
			}
			
		}
	}

	/**
	 * Returns the maximum number of persons that can be recruited in this region.
	 *
	 * 
	 */
	public int maxRecruit() {
		// pavkovic 2002.05.10: in case we dont have a recruit max set we evaluate it
		return (recruits == -1) ? maxRecruit(peasants) : recruits;
	}

	/**
	 * Returns the maximum number of persons that can be recruited in this region.
	 */
	public int maxOldRecruit() {
		// pavkovic 2002.05.10: in case we dont have a recruit max set we evaluate it
		return (oldRecruits == -1) ? maxRecruit(oldPeasants) : oldRecruits;
	}

	/**
	 * Returns the maximum number of persons available for recruitment in a region with the
	 * specified number of peasants.
	 */
	private static int maxRecruit(int peasants) {
		if(peasants >= 0) {
			return peasants / 40; // 2.5 %
		}

		return -1;
	}

	/**
	 * Returns the silver that can be earned through entertainment in this region.
	 */
	public int maxEntertain() {
		return maxEntertain(silver);
	}

	/**
	 * Returns the silver that could be earned through entertainment in this region.
	 */
	public int maxOldEntertain() {
		return maxEntertain(oldSilver);
	}

	/**
	 * Return the silver that can be earned through entertainment in a region with the given amount
	 * of silver.
	 */
	private static int maxEntertain(int silver) {
		if(silver >= 0) {
			return silver / 20;
		}

		return -1;
	}

	/**
	 * Returns the maximum number of luxury items that can be bought in this region without a price
	 * penalty.
	 */
	public int maxLuxuries() {
		return maxLuxuries(peasants);
	}

	/**
	 * Returns the maximum number of luxury items that could be bought in this region without a
	 * price penalty.
	 */
	public int maxOldLuxuries() {
		return maxLuxuries(oldPeasants);
	}

	/**
	 * Return the maximum number of luxury items that can be bought without a price increase in a
	 * region with the specified number of peasants.
	 */
	private static int maxLuxuries(int peasants) {
		return (peasants >= 0) ? (peasants / 100) : (-1);
	}

	/**
	 * Calculates the wage a peasant earns according to the biggest castle in this region. While
	 * the value of the wage field is directly taken from the report and may be biased by the race
	 * of the owner faction of that report, this function tries to determine the real wage a
	 * peasaent can earn in this region. Wage for player persons can be derived from that value
	 */
	public int getPeasantWage() {
		int realWage = 11;

		if(buildings != null) {
			for(Iterator iter = buildings().iterator(); iter.hasNext();) {
				Building b = (Building) iter.next();

				if(b.getType() instanceof CastleType) {
					CastleType ct = (CastleType) b.getType();
					realWage = Math.max(ct.getPeasantWage(), realWage);
				}
			}
		}

		return realWage;
	}

	/**
	 * Returns a String representation of this Region object. If region has no name the string
	 * representation of the  region type is used.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();

		if(getName() == null) {
			if(getType() != null) {
				sb.append(getType().toString());
			}
		} else {
			sb.append(getName());
		}

		sb.append(" (").append(this.getID().toString()).append(")");

		return sb.toString();
	}

	/**
	 * Returns the coordinate of this region. This method is only a type-safe short cut for
	 * retrieving and converting the ID object of this region.
	 */
	public CoordinateID getCoordinate() {
		return (CoordinateID) this.getID();
	}

	/**
	 * Returns the RegionType of this region. This method is only a type-safe short cut for
	 * retrieving and converting the RegionType of this region.
	 */
	public RegionType getRegionType() {
		return (RegionType) this.getType();
	}

	/**
	 * Refreshes all the relations of all units in this region. It is preferrable to call this
	 * method instead of refreshing the unit relations 'manually'.
	 */
	public void refreshUnitRelations() {
        refreshUnitRelations(false);
    }
    
    /**
     * Refreshes all the relations of all units in this region. It is preferrable to call this
     * method instead of refreshing the unit relations 'manually'.
     * 
     * @param forceRefresh to enforce refreshment, false for one refreshment only
     */

    public synchronized void refreshUnitRelations(boolean forceRefresh) {

        if(unitRelationsRefreshed == false || forceRefresh) {
			unitRelationsRefreshed = true;

			for(Iterator iter = this.units().iterator(); iter.hasNext();) {
				Unit u = (Unit) iter.next();
				u.refreshRelations();
			}

			getZeroUnit().refreshRelations();
            
		}
	}

	/** Guarding units of this region. The list contains instances of class <tt>Unit</tt>. */
	private List<Unit> guards;

	/**
	 * add guarding Unit to region
	 */
	public void addGuard(Unit u) {
		if(guards == null) {
			guards = new ArrayList<Unit>();
		}

		if(!guards.contains(u)) {
			guards.add(u);
		}
	}

	/**
	 * get The List of guarding Units
	 */
	public List<Unit> getGuards() {
		return guards;
	}


	/**
	 * DOCUMENT-ME
	 */
	public Unit getUnit(ID key) {
		if(ZeroUnit.ZERO_ID.equals(key)) {
			return getZeroUnit();
		} else {
			return super.getUnit(key);
		}
	}

	private Collection<CoordinateID> neighbours;

	/**
	 * Sets the collection of ids for reachable regions to <tt>neighbours</tt>. If
	 * <tt>neighbours</tt> is null they will be evaluated.
	 */
	public void setNeighbours(Collection<CoordinateID> neighbours) {
		this.neighbours = neighbours;
	}

	/**
	 * returns a collection of ids for reachable neighbours. This may be set by setNeighbours() if
	 * neighbours is null it will be calculated from the game data). This function may be
	 * necessary for new xml reports.
	 */
	public Collection<CoordinateID> getNeighbours() {
		if(neighbours == null) {
			neighbours = evaluateNeighbours();
		}

		return neighbours;
	}

	private Collection<CoordinateID> evaluateNeighbours() {
		if((getData() == null) || (getData().regions() == null)) {
			return null;
		}

		Collection<CoordinateID> c = Regions.getAllNeighbours(getData().regions(), getID(), 1, null).keySet();
		c.remove(getID());

		return c;
	}

	/**
	 * @return the ozeanWithCoast
	 */
	public int getOzeanWithCoast() {
		if (this.ozeanWithCoast==-1){
			this.ozeanWithCoast = this.calcOzeanWithCoast();
		}
		return ozeanWithCoast;
	}
	
	/**
	 * calculates the OzeanWithCoast-value
	 * @return 1 if this region is ozean and has neighboring non-ozean regions
	 */
	private int calcOzeanWithCoast(){
		// start only if we are a ozean region
		if (!this.getRegionType().isOcean()){
			return 0;
		}
		// run through the neighbors
		for (Iterator iter = this.getNeighbours().iterator();iter.hasNext();){
			CoordinateID checkRegionID = (CoordinateID) iter.next();
			if (!getData().getRegion(checkRegionID).getRegionType().isOcean()){
				return 1;
			}
		}
		return 0;
	}
	
	/**
	 * Used for replacers..showing coordinates of region
	 * @author Fiete
	 */
	public int getCoordX(){
		CoordinateID myCID = this.getCoordinate();
		return myCID.x;
	}
	public int getCoordY(){
		CoordinateID myCID = this.getCoordinate();
		return myCID.y;
	}

	/**
	 * @return the signLines
	 */
	public Collection<Sign> getSigns() {
		return signs;
	}

	/**
	 * @param signLines the signLines to set
	 */
	public void setSigns(List<Sign> signLines) {
		this.signs = signLines;
	}
	
	
	public void addSign(Sign s){
		if (this.signs==null){
			this.signs = new ArrayList<Sign>(1);
		}
		this.signs.add(s);
	}
	
	public void addSigns(Collection<Sign> c){
		if (this.signs==null){
			this.signs = new ArrayList<Sign>(1);
		}
		this.signs.addAll(c);
	}
	
	public void clearSigns(){
		if (this.signs!=null){
			this.signs.clear();
		}
	}
	
  /**
   * Returns the value of events.
   * 
   * @return Returns events.
   */
  public List<Message> getEvents() {
    return events;
  }

  /**
   * Sets the value of events.
   *
   * @param events The value for events.
   */
  public void setEvents(List<Message> events) {
    this.events = events;
  }

  /**
   * Returns the value of herb.
   * 
   * @return Returns herb.
   */
  public ItemType getHerb() {
    return herb;
  }

  /**
   * Sets the value of herb.
   *
   * @param herb The value for herb.
   */
  public void setHerb(ItemType herb) {
    this.herb = herb;
  }

  /**
   * Returns the value of herbAmount.
   * 
   * @return Returns herbAmount.
   */
  public String getHerbAmount() {
    return herbAmount;
  }

  /**
   * Sets the value of herbAmount.
   *
   * @param herbAmount The value for herbAmount.
   */
  public void setHerbAmount(String herbAmount) {
    this.herbAmount = herbAmount;
  }

  /**
   * Returns the value of horses.
   * 
   * @return Returns horses.
   */
  public int getHorses() {
    return horses;
  }

  /**
   * Sets the value of horses.
   *
   * @param horses The value for horses.
   */
  public void setHorses(int horses) {
    this.horses = horses;
  }

  /**
   * Returns the value of iron.
   * 
   * @return Returns iron.
   */
  public int getIron() {
    return iron;
  }

  /**
   * Sets the value of iron.
   *
   * @param iron The value for iron.
   */
  public void setIron(int iron) {
    this.iron = iron;
  }

  /**
   * Returns the value of laen.
   * 
   * @return Returns laen.
   */
  public int getLaen() {
    return laen;
  }

  /**
   * Sets the value of laen.
   *
   * @param laen The value for laen.
   */
  public void setLaen(int laen) {
    this.laen = laen;
  }

  /**
   * Returns the value of mallorn.
   * 
   * @return Returns mallorn.
   */
  public boolean isMallorn() {
    return mallorn;
  }

  /**
   * Sets the value of mallorn.
   *
   * @param mallorn The value for mallorn.
   */
  public void setMallorn(boolean mallorn) {
    this.mallorn = mallorn;
  }

  /**
   * Returns the value of messages.
   * 
   * @return Returns messages.
   */
  public List<Message> getMessages() {
    return messages;
  }

  /**
   * Sets the value of messages.
   *
   * @param messages The value for messages.
   */
  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }

  /**
   * Returns the value of oldHorses.
   * 
   * @return Returns oldHorses.
   */
  public int getOldHorses() {
    return oldHorses;
  }

  /**
   * Sets the value of oldHorses.
   *
   * @param oldHorses The value for oldHorses.
   */
  public void setOldHorses(int oldHorses) {
    this.oldHorses = oldHorses;
  }

  /**
   * Returns the value of oldIron.
   * 
   * @return Returns oldIron.
   */
  public int getOldIron() {
    return oldIron;
  }

  /**
   * Sets the value of oldIron.
   *
   * @param oldIron The value for oldIron.
   */
  public void setOldIron(int oldIron) {
    this.oldIron = oldIron;
  }

  /**
   * Returns the value of oldLaen.
   * 
   * @return Returns oldLaen.
   */
  public int getOldLaen() {
    return oldLaen;
  }

  /**
   * Sets the value of oldLaen.
   *
   * @param oldLaen The value for oldLaen.
   */
  public void setOldLaen(int oldLaen) {
    this.oldLaen = oldLaen;
  }

  /**
   * Returns the value of oldPeasants.
   * 
   * @return Returns oldPeasants.
   */
  public int getOldPeasants() {
    return oldPeasants;
  }

  /**
   * Sets the value of oldPeasants.
   *
   * @param oldPeasants The value for oldPeasants.
   */
  public void setOldPeasants(int oldPeasants) {
    this.oldPeasants = oldPeasants;
  }

  /**
   * Returns the value of oldPrices.
   * 
   * @return Returns oldPrices.
   */
  public Map<ID, LuxuryPrice> getOldPrices() {
    return oldPrices;
  }

  /**
   * Sets the value of oldPrices.
   *
   * @param oldPrices The value for oldPrices.
   */
  public void setOldPrices(Map<ID, LuxuryPrice> oldPrices) {
    this.oldPrices = oldPrices;
  }

  /**
   * Returns the value of oldRecruits.
   * 
   * @return Returns oldRecruits.
   */
  public int getOldRecruits() {
    return oldRecruits;
  }

  /**
   * Sets the value of oldRecruits.
   *
   * @param oldRecruits The value for oldRecruits.
   */
  public void setOldRecruits(int oldRecruits) {
    this.oldRecruits = oldRecruits;
  }

  /**
   * Returns the value of oldSilver.
   * 
   * @return Returns oldSilver.
   */
  public int getOldSilver() {
    return oldSilver;
  }

  /**
   * Sets the value of oldSilver.
   *
   * @param oldSilver The value for oldSilver.
   */
  public void setOldSilver(int oldSilver) {
    this.oldSilver = oldSilver;
  }

  /**
   * Returns the value of oldSprouts.
   * 
   * @return Returns oldSprouts.
   */
  public int getOldSprouts() {
    return oldSprouts;
  }

  /**
   * Sets the value of oldSprouts.
   *
   * @param oldSprouts The value for oldSprouts.
   */
  public void setOldSprouts(int oldSprouts) {
    this.oldSprouts = oldSprouts;
  }

  /**
   * Returns the value of oldStones.
   * 
   * @return Returns oldStones.
   */
  public int getOldStones() {
    return oldStones;
  }

  /**
   * Sets the value of oldStones.
   *
   * @param oldStones The value for oldStones.
   */
  public void setOldStones(int oldStones) {
    this.oldStones = oldStones;
  }

  /**
   * Returns the value of oldTrees.
   * 
   * @return Returns oldTrees.
   */
  public int getOldTrees() {
    return oldTrees;
  }

  /**
   * Sets the value of oldTrees.
   *
   * @param oldTrees The value for oldTrees.
   */
  public void setOldTrees(int oldTrees) {
    this.oldTrees = oldTrees;
  }

  /**
   * Returns the value of oldWage.
   * 
   * @return Returns oldWage.
   */
  public int getOldWage() {
    return oldWage;
  }

  /**
   * Sets the value of oldWage.
   *
   * @param oldWage The value for oldWage.
   */
  public void setOldWage(int oldWage) {
    this.oldWage = oldWage;
  }

  /**
   * Returns the value of orcInfested.
   * 
   * @return Returns orcInfested.
   */
  public boolean isOrcInfested() {
    return orcInfested;
  }

  /**
   * Sets the value of orcInfested.
   *
   * @param orcInfested The value for orcInfested.
   */
  public void setOrcInfested(boolean orcInfested) {
    this.orcInfested = orcInfested;
  }

  /**
   * Returns the value of peasants.
   * 
   * @return Returns peasants.
   */
  public int getPeasants() {
    return peasants;
  }

  /**
   * Sets the value of peasants.
   *
   * @param peasants The value for peasants.
   */
  public void setPeasants(int peasants) {
    this.peasants = peasants;
  }

  /**
   * Returns the value of playerMessages.
   * 
   * @return Returns playerMessages.
   */
  public List<Message> getPlayerMessages() {
    return playerMessages;
  }

  /**
   * Sets the value of playerMessages.
   *
   * @param playerMessages The value for playerMessages.
   */
  public void setPlayerMessages(List<Message> playerMessages) {
    this.playerMessages = playerMessages;
  }

  /**
   * Returns the value of prices.
   * 
   * @return Returns prices.
   */
  public Map<ID, LuxuryPrice> getPrices() {
    return prices;
  }

  /**
   * Sets the value of prices.
   *
   * @param prices The value for prices.
   */
  public void setPrices(Map<ID, LuxuryPrice> prices) {
    this.prices = prices;
  }

  /**
   * Returns the value of recruits.
   * 
   * @return Returns recruits.
   */
  public int getRecruits() {
    return recruits;
  }

  /**
   * Sets the value of recruits.
   *
   * @param recruits The value for recruits.
   */
  public void setRecruits(int recruits) {
    this.recruits = recruits;
  }

  /**
   * Returns the value of silver.
   * 
   * @return Returns silver.
   */
  public int getSilver() {
    return silver;
  }

  /**
   * Sets the value of silver.
   *
   * @param silver The value for silver.
   */
  public void setSilver(int silver) {
    this.silver = silver;
  }

  /**
   * Returns the value of sprouts.
   * 
   * @return Returns sprouts.
   */
  public int getSprouts() {
    return sprouts;
  }

  /**
   * Sets the value of sprouts.
   *
   * @param sprouts The value for sprouts.
   */
  public void setSprouts(int sprouts) {
    this.sprouts = sprouts;
  }

  /**
   * Returns the value of stones.
   * 
   * @return Returns stones.
   */
  public int getStones() {
    return stones;
  }

  /**
   * Sets the value of stones.
   *
   * @param stones The value for stones.
   */
  public void setStones(int stones) {
    this.stones = stones;
  }

  /**
   * Returns the value of surroundings.
   * 
   * @return Returns surroundings.
   */
  public List<Message> getSurroundings() {
    return surroundings;
  }

  /**
   * Sets the value of surroundings.
   *
   * @param surroundings The value for surroundings.
   */
  public void setSurroundings(List<Message> surroundings) {
    this.surroundings = surroundings;
  }

  /**
   * Returns the value of travelThru.
   * 
   * @return Returns travelThru.
   */
  public List<Message> getTravelThru() {
    return travelThru;
  }

  /**
   * Sets the value of travelThru.
   *
   * @param travelThru The value for travelThru.
   */
  public void setTravelThru(List<Message> travelThru) {
    this.travelThru = travelThru;
  }

  /**
   * Returns the value of travelThruShips.
   * 
   * @return Returns travelThruShips.
   */
  public List<Message> getTravelThruShips() {
    return travelThruShips;
  }

  /**
   * Sets the value of travelThruShips.
   *
   * @param travelThruShips The value for travelThruShips.
   */
  public void setTravelThruShips(List<Message> travelThruShips) {
    this.travelThruShips = travelThruShips;
  }

  /**
   * Returns the value of trees.
   * 
   * @return Returns trees.
   */
  public int getTrees() {
    return trees;
  }

  /**
   * Sets the value of trees.
   *
   * @param trees The value for trees.
   */
  public void setTrees(int trees) {
    this.trees = trees;
  }

  /**
   * Returns the value of wage.
   * 
   * @return Returns wage.
   */
  public int getWage() {
    return wage;
  }

  /**
   * Sets the value of wage.
   *
   * @param wage The value for wage.
   */
  public void setWage(int wage) {
    this.wage = wage;
  }

  /**
   * @see magellan.library.Region#isActive()
   */
  public boolean isActive() {
    return isActive;
  }

  /**
   * @see magellan.library.Region#setActive(boolean)
   */
  public void setActive(boolean isActive) {
    if (isActive) {
      // remove old active region...
      for (Region r : getData().regions().values()) r.setActive(false);
    }
    this.isActive = isActive;
  }
}
