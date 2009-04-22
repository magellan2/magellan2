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

package magellan.library.gamebinding;

import magellan.library.ID;
import magellan.library.StringID;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class EresseaConstants {
	/** DOCUMENT-ME */
	public static final ID OPT_AUSWERTUNG = StringID.create("AUSWERTUNG");

	/** DOCUMENT-ME */
	public static final ID OPT_COMPUTER = StringID.create("COMPUTER");

	/** DOCUMENT-ME */
	public static final ID OPT_ZUGVORLAGE = StringID.create("ZUGVORLAGE");

	/** DOCUMENT-ME */
	public static final ID OPT_SILBERPOOL = StringID.create("SILBERPOOL");

	/** DOCUMENT-ME */
	public static final ID OPT_STATISTIK = StringID.create("STATISTIK");

	/** DOCUMENT-ME */
	public static final ID OPT_DEBUG = StringID.create("DEBUG");

	/** DOCUMENT-ME */
	public static final ID OPT_ZIPPED = StringID.create("ZIPPED");

	/** DOCUMENT-ME */
	public static final ID OPT_ZEITUNG = StringID.create("ZEITUNG");

	/** DOCUMENT-ME */
	public static final ID OPT_MATERIALPOOL = StringID.create("MATERIALPOOL");

	/** DOCUMENT-ME */
	public static final ID OPT_ADRESSEN = StringID.create("ADRESSEN");

	/** DOCUMENT-ME */
	public static final ID OPT_BZIP2 = StringID.create("BZIP2");

	/** DOCUMENT-ME */
	public static final ID OPT_PUNKTE = StringID.create("PUNKTE");

	/** DOCUMENT-ME */
	public static final ID C_WEAPONS = StringID.create("WEAPONS");

	/** DOCUMENT-ME */
	public static final ID C_ARMOUR = StringID.create("ARMOUR");

	/** DOCUMENT-ME */
	public static final ID C_RESOURCES = StringID.create("RESOURCES");

	/** DOCUMENT-ME */
	public static final ID C_LUXURIES = StringID.create("LUXURIES");

	/** DOCUMENT-ME */
	public static final ID C_HERBS = StringID.create("HERBS");

	/** DOCUMENT-ME */
	public static final ID C_MISC = StringID.create("MISC");
	
	/** DOCUMENT-ME */
	public static final String O_ADDRESSES = "ADDRESSES";

	/** 
	 * @deprecated replaced by COMBAT_AGGRESSIVE
	 */
	public static final String O_AGGRESSIVE = "AGGRESSIVE";

	/** DOCUMENT-ME */
	public static final String O_AFTER = "AFTER";

	/** DOCUMENT-ME */
	public static final String O_ALL = "ALL";

	/** DOCUMENT-ME */
	public static final String O_ATTACK = "ATTACK";

	/** DOCUMENT-ME */
	public static final String O_AURA = "AURA";

	/** DOCUMENT-ME */
	public static final String O_BANNER = "BANNER";

	/** DOCUMENT-ME */
	public static final String O_BEFORE = "BEFORE";

	/** DOCUMENT-ME */
	public static final String O_BUY = "BUY";

	/** DOCUMENT-ME */
	public static final String O_BZIP2 = "BZIP2";

	/** DOCUMENT-ME */
	public static final String O_CARRY = "CARRY";

	/** DOCUMENT-ME */
	public static final String O_CAST = "CAST";

	/** DOCUMENT-ME */
	public static final String O_CASTLE = "CASTLE";

	/** DOCUMENT-ME */
	public static final String O_CLAIM = "CLAIM";

	/** "KÄMPFE [AGGRESSIV|HINTEN|DEFENSIV...]" */
	public static final String O_COMBAT = "COMBAT";

  /** argument of COMBAT order */
  public static final String O_COMBAT_AGGRESSIVE = "COMBAT_AGGRESSIVE";

  /** argument of COMBAT order */
  public static final String O_COMBAT_DEFENSIVE = "COMBAT_DEFENSIVE";

  /** argument of COMBAT order */
  public static final String O_COMBAT_FLEE = "COMBAT_FLEE";

  /** argument of COMBAT order */
  public static final String O_COMBAT_FRONT = "COMBAT_FRONT";

  /** argument of COMBAT order */
	public static final String O_COMBAT_REAR = "COMBAT_REAR";

  /** argument of COMBAT order */
  public static final String O_COMBAT_NOT = "COMBAT_NOT";

	/** DOCUMENT-ME */
	public static final String O_COMBATSPELL = "COMBATSPELL";

	/** DOCUMENT-ME */
	public static final String O_COMPUTER = "COMPUTER";

	/** DOCUMENT-ME */
	public static final String O_CONTACT = "CONTACT";

	/** DOCUMENT-ME */
	public static final String O_CONTROL = "CONTROL";

	/** DOCUMENT-ME */
	public static final String O_DEFAULT = "DEFAULT";

	/** 
	 * @deprecated replaced by O_COMBAT_DEFENSIVE
	 */
	public static final String O_DEFENSIVE = "DEFENSIVE";

	/** DOCUMENT-ME */
	public static final String O_DESCRIBE = "DESCRIBE";

	/** DOCUMENT-ME */
	public static final String O_DESTROY = "DESTROY";
	
	/** EACH/JE keyword */
	public static final String O_EACH = "EACH";

	/** DOCUMENT-ME */
	public static final String O_EMAIL = "EMAIL";

	/** DOCUMENT-ME */
	public static final String O_END = "END";

	/** DOCUMENT-ME */
	public static final String O_ENTER = "ENTER";

	/** DOCUMENT-ME */
	public static final String O_ENTERTAIN = "ENTERTAIN";

	/** DOCUMENT-ME */
	public static final String O_ERESSEA = "ERESSEA";

	/** DOCUMENT-ME */
	public static final String O_FACTION = "FACTION";

	/**
	 * @deprecated replaced by HELP_FACTIONSTEALTH
	 */
	public static final String O_FACTIONSTEALTH = "FACTIONSTEALTH";

	/** 
	 * @deprecated replaced by COMBAT_FLEE 
	 */
	public static final String O_FLEE = "FLEE";

	/** DOCUMENT-ME */
	public static final String O_FOLLOW = "FOLLOW";

	/** DOCUMENT-ME */
	public static final String O_FOREIGN = "FOREIGN";

	/** DOCUMENT-ME */
	public static final String O_FORGET = "FORGET";

	/** 
	 * @deprecated replaced by COMBAT_FRONT
	 */
	public static final String O_FRONT = "FRONT";

	/** DOCUMENT-ME */
	public static final String O_GIVE = "GIVE";

	/** DOCUMENT-ME */
	public static final String O_GROUP = "GROUP";

	/** DOCUMENT-ME */
	public static final String O_GROW = "GROW";

	/** DOCUMENT-ME */
	public static final String O_GUARD = "GUARD";

	/** DOCUMENT-ME */
	public static final String O_HELP = "HELP";

  /** The "GIB" from HELFE GIB */
  public static final String O_HELP_GIVE = "HELP_GIVE";

  /** The "KÄMPFE" from HELFE KÄMPFE */
  public static final String O_HELP_COMBAT = "HELP_COMBAT";

  /** The "SILBER" from HELFE SILBER */
  public static final String O_HELP_SILVER = "HELP_SILVER";

  /** The "BEWACHE" from HELFE BEWACHE */
  public static final String O_HELP_GUARD = "HELP_GUARD";

  /** The "PARTEITARNUNG" from HELFE PARTEITARNUNG */
  public static final String O_HELP_FACTIONSTEALTH = "HELP_FACTIONSTEALTH";

	// FIXME(pavkovic) 2003.04.16: this is only used for german to distinguish between "HELFEN" and "HELFE"

	/** The "HELFE" from KÄMPFE HELFE [NICHT] */
	public static final String O_COMBAT_HELP = "COMBAT_HELP";

	/** DOCUMENT-ME */
	public static final String O_HERBS = "HERBS";

	/** DOCUMENT-ME */
	public static final String O_HIDE = "HIDE";

	/** DOCUMENT-ME */
	public static final String O_HORSES = "HORSES";

	/** DOCUMENT-ME */
	public static final String O_ITEMPOOL = "ITEMPOOL";

	/** DOCUMENT-ME */
	public static final String O_LEARN = "LEARN";

	/** DOCUMENT-ME */
	public static final String O_LEAVE = "LEAVE";

	/** DOCUMENT-ME */
	public static final String O_LEVEL = "LEVEL";

	/** DOCUMENT-ME */
	public static final String O_LOCALE = "LOCALE";

	/** DOCUMENT-ME */
	public static final String O_MAKE = "MAKE";

	/** DOCUMENT-ME */
	public static final String O_MEN = "MEN";

	/** DOCUMENT-ME */
	public static final String O_MESSAGE = "MESSAGE";

	/** DOCUMENT-ME */
	public static final String O_MOVE = "MOVE";

	/** DOCUMENT-ME */
	public static final String O_NAME = "NAME";

	/** DOCUMENT-ME */
	public static final String O_NEXT = "NEXT";

	/** DOCUMENT-ME */
	public static final String O_NOT = "NOT";

	/** DOCUMENT-ME */
	public static final String O_NUMBER = "NUMBER";

	/** DOCUMENT-ME */
	public static final String O_OPTION = "OPTION";

	/** DOCUMENT-ME */
	public static final String O_ORIGIN = "ORIGIN";

	/** DOCUMENT-ME */
	public static final String O_PASSWORD = "PASSWORD";

	/** DOCUMENT-ME */
	public static final String O_PAUSE = "PAUSE";

	/** DOCUMENT-ME */
	public static final String O_PIRACY = "PIRACY";

	/** DOCUMENT-ME */
	public static final String O_PLAINTEXT = "PLAINTEXT";

	/** DOCUMENT-ME */
	public static final String O_PLANT = "PLANT";

	/** DOCUMENT-ME */
	public static final String O_POTIONS = "POTIONS";

	/** DOCUMENT-ME */
	public static final String O_PREFIX = "PREFIX";

	/** DOCUMENT-ME */
	public static final String O_PRIVATE = "PRIVATE";

	/** DOCUMENT-ME */
	public static final String O_PROMOTION = "PROMOTION";

	/** DOCUMENT-ME */
	public static final String O_QUIT = "QUIT";

	/** 
   * @deprecated replaced by O_COMBAT_REAR
  */
	public static final String O_REAR = "REAR";

	/** DOCUMENT-ME */
	public static final String O_RECRUIT = "RECRUIT";

	/** DOCUMENT-ME */
	public static final String O_REGION = "REGION";

	/** DOCUMENT-ME */
	public static final String O_REPORT = "REPORT";

	/** DOCUMENT-ME */
	public static final String O_RESEARCH = "RESEARCH";

	/** DOCUMENT-ME */
	public static final String O_RESERVE = "RESERVE";

	/** DOCUMENT-ME */
	public static final String O_RESTART = "RESTART";

	/** DOCUMENT-ME */
	public static final String O_RIDE = "RIDE";

	/** DOCUMENT-ME */
	public static final String O_ROAD = "ROAD";

	/** DOCUMENT-ME */
	public static final String O_ROUTE = "ROUTE";

  /** TODO: DOCUMENT ME! */
  public static final String O_SCORE = "SCORE";
  
	/** DOCUMENT-ME */
	public static final String O_SEED = "SEED";

	/** DOCUMENT-ME */
	public static final String O_SELL = "SELL";

	/** DOCUMENT-ME */
	public static final String O_SABOTAGE = "SABOTAGE";

	/** DOCUMENT-ME */
	public static final String O_SHIP = "SHIP";

	/** DOCUMENT-ME */
	public static final String O_SHOW = "SHOW";

	/** DOCUMENT-ME */
	public static final String O_SIEGE = "SIEGE";

	/** 
	 * @deprecated use rules.getItemType(EresseaConstants.I_SILVER) instead
	 */
	public static final String O_SILVER = "SILVER";

	/** DOCUMENT-ME */
	public static final String O_SILVERPOOL = "SILVERPOOL";

	/** DOCUMENT-ME */
	public static final String O_SORT = "SORT";

	/** DOCUMENT-ME */
	public static final String O_SPELLS = "SPELLS";

	/** DOCUMENT-ME */
	public static final String O_SPY = "SPY";

	/** DOCUMENT-ME */
	public static final String O_STATISTICS = "STATISTICS";

	/** DOCUMENT-ME */
	public static final String O_STEAL = "STEAL";

	/** DOCUMENT-ME */
	public static final String O_SUPPLY = "SUPPLY";

	/** DOCUMENT-ME */
	public static final String O_TAX = "TAX";

	/** DOCUMENT-ME */
	public static final String O_TEACH = "TEACH";

	/** DOCUMENT-ME */
	public static final String O_TEMP = "TEMP";

	/** DOCUMENT-ME */
	public static final String O_TEMPLATE = "TEMPLATE";

	/** DOCUMENT-ME */
	public static final String O_UNIT = "UNIT";

	/** DOCUMENT-ME */
	public static final String O_USE = "USE";

	/** DOCUMENT-ME */
	public static final String O_WORK = "WORK";
	
	/** DOCUMENT-ME */
	public static final String O_ZIPPED = "ZIPPED";

	// directions:

	/** DOCUMENT-ME */
	public static final String O_NORTHWEST = "NORTHWEST";

	/** DOCUMENT-ME */
	public static final String O_NORTHEAST = "NORTHEAST";

	/** DOCUMENT-ME */
	public static final String O_EAST = "EAST";

	/** DOCUMENT-ME */
	public static final String O_SOUTHEAST = "SOUTHEAST";

	/** DOCUMENT-ME */
	public static final String O_SOUTHWEST = "SOUTHWEST";

	/** DOCUMENT-ME */
	public static final String O_WEST = "WEST";

	/** DOCUMENT-ME */
	public static final String O_NW = "NW";

	/** DOCUMENT-ME */
	public static final String O_NE = "NE";

	/** DOCUMENT-ME */
	public static final String O_E = "E";

	/** DOCUMENT-ME */
	public static final String O_SE = "SE";

	/** DOCUMENT-ME */
	public static final String O_SW = "SW";

	/** DOCUMENT-ME */
	public static final String O_W = "W";

	/** DOCUMENT-ME */
	public static final ID R_DAEMONEN = StringID.create("DAEMONEN");

	/** DOCUMENT-ME */
	public static final ID R_ELFEN = StringID.create("ELFEN");

	/** DOCUMENT-ME */
	public static final ID R_GOBLINS = StringID.create("GOBLINS");

	/** DOCUMENT-ME */
	public static final ID R_HALBLINGE = StringID.create("HALBLINGE");

	/** DOCUMENT-ME */
	public static final ID R_INSEKTEN = StringID.create("INSEKTEN");

	/** DOCUMENT-ME */
	public static final ID R_KATZEN = StringID.create("KATZEN");

	/** DOCUMENT-ME */
	public static final ID R_MEERMENSCHEN = StringID.create("MEERMENSCHEN");

	/** DOCUMENT-ME */
	public static final ID R_MENSCHEN = StringID.create("MENSCHEN");

	/** DOCUMENT-ME */
	public static final ID R_ORKS = StringID.create("ORKS");

	/** DOCUMENT-ME */
	public static final ID R_TROLLE = StringID.create("TROLLE");

	/** DOCUMENT-ME */
	public static final ID R_ZWERGE = StringID.create("ZWERGE");

	/** DOCUMENT-ME */
	public static final ID S_ALCHEMIE = StringID.create("ALCHEMIE");

	/** DOCUMENT-ME */
	public static final ID S_ARMBRUSTSCHIESSEN = StringID.create("ARMBRUSTSCHIESSEN");

	/** DOCUMENT-ME */
	public static final ID S_AUSDAUER = StringID.create("AUSDAUER");

	/** DOCUMENT-ME */
	public static final ID S_BERGBAU = StringID.create("BERGBAU");

	/** DOCUMENT-ME */
	public static final ID S_BOGENSCHIESSEN = StringID.create("BOGENSCHIESSEN");

	/** DOCUMENT-ME */
	public static final ID S_BURGENBAU = StringID.create("BURGENBAU");

	/** DOCUMENT-ME */
	public static final ID S_HANDELN = StringID.create("HANDELN");

	/** DOCUMENT-ME */
	public static final ID S_HIEBWAFFEN = StringID.create("HIEBWAFFEN");

	/** DOCUMENT-ME */
	public static final ID S_HOLZFAELLEN = StringID.create("HOLZFAELLEN");

	/** DOCUMENT-ME */
	public static final ID S_KATAPULTBEDIENUNG = StringID.create("KATAPULTBEDIENUNG");

	/** DOCUMENT-ME */
	public static final ID S_KRAEUTERKUNDE = StringID.create("KRAEUTERKUNDE");

	/** DOCUMENT-ME */
	public static final ID S_MAGIE = StringID.create("MAGIE");

	/** DOCUMENT-ME */
	public static final ID S_PFERDEDRESSUR = StringID.create("PFERDEDRESSUR");

	/** DOCUMENT-ME */
	public static final ID S_REITEN = StringID.create("REITEN");

	/** DOCUMENT-ME */
	public static final ID S_RUESTUNGSBAU = StringID.create("RUESTUNGSBAU");

	/** DOCUMENT-ME */
	public static final ID S_SCHIFFBAU = StringID.create("SCHIFFBAU");

	/** DOCUMENT-ME */
	public static final ID S_SEGELN = StringID.create("SEGELN");

	/** DOCUMENT-ME */
	public static final ID S_SPIONAGE = StringID.create("SPIONAGE");

	/** DOCUMENT-ME */
	public static final ID S_STANGENWAFFEN = StringID.create("STANGENWAFFEN");

	/** DOCUMENT-ME */
	public static final ID S_STEINBAU = StringID.create("STEINBAU");

	/** DOCUMENT-ME */
	public static final ID S_STEUEREINTREIBEN = StringID.create("STEUEREINTREIBEN");

	/** DOCUMENT-ME */
	public static final ID S_STRASSENBAU = StringID.create("STRASSENBAU");

	/** DOCUMENT-ME */
	public static final ID S_TAKTIK = StringID.create("TAKTIK");

	/** DOCUMENT-ME */
	public static final ID S_TARNUNG = StringID.create("TARNUNG");

	/** DOCUMENT-ME */
	public static final ID S_UNTERHALTUNG = StringID.create("UNTERHALTUNG");

	/** DOCUMENT-ME */
	public static final ID S_WAFFENBAU = StringID.create("WAFFENBAU");

	/** DOCUMENT-ME */
	public static final ID S_WAGENBAU = StringID.create("WAGENBAU");

	/** DOCUMENT-ME */
	public static final ID S_WAHRNEHMUNG = StringID.create("WAHRNEHMUNG");

	/** A state selector for the "Helfe Silber" state. */

	//public static final int A_SILVER = 1 << 0;

	/** A state selector for the "Helfe Kämpfe" state. */

	//public static final int A_COMBAT = 1 << 1;

	/** A state selector for the "Helfe Gib" state. */
	public static final int A_GIVE = 1 << 3;

	/** A state selector for the "Helfe Bewache" state. */
	public static final int A_GUARD = 1 << 4;

	/** A state selector for the "Helfe Parteitarnung" state. */

	//public static final int A_GUISE = 1 << 5;

	/** A state selector for the "Helfe ?" state. */

	//public static final int A_WHATEVER = 1 << 6;

	/** A state selector for all of the alliance states. */

	// public static final int A_ALL = 0x003B;
	//public static final int A_ALL = A_SILVER | A_COMBAT | A_GIVE | A_GUARD | A_GUISE | A_WHATEVER; // (binary value should be: 111101 (#123) )
  
  /** Bag of negative weight - this bad weights 1 GE but stores 200 GE. However not all items can be stored in the bag. */
  public static final ID I_BONW = StringID.create("Zauberbeutel");
  
  /** DOCUMENT-ME */
  public static final ID I_CART = StringID.create("Wagen");
  
  /** DOCUMENT-ME */
  public static final ID I_GOTS = StringID.create("Gürtel der Trollstärke");
  
	public static final ID I_HORSE = StringID.create("Pferd");

  public static final ID I_IRON = StringID.create("Eisen");
  
  public static final ID I_LAEN = StringID.create("Laen");
  
  public static final ID I_PEASANTS = StringID.create("Bauern");

  /** DOCUMENT-ME */
  public static final ID I_SILVER = StringID.create("Silber");

  public static final ID I_SPROUTS = StringID.create("Schösslinge");
  
  public static final ID I_STONES = StringID.create("Steine");
  
  public static final ID I_TREES = StringID.create("Bäume");
  
	/** DOCUMENT-ME */
	public static final ID RT_OCEAN = StringID.create("Ozean");

  /** The RegionType firewall */
  public static final ID RT_FIREWALL = StringID.create("Feuerwand");

  /** The RegionType plain */
  public static final ID RT_PLAIN = StringID.create("Ebene");
  
  /** The RegionType forest */
  public static final ID RT_FOREST = StringID.create("Wald");
  
  /** The RegionType glacier */
  public static final ID RT_GLACIER = StringID.create("Gletscher");

  /** The RegionType swamp */
  public static final ID RT_SWAMP = StringID.create("Sumpf");

  /** The RegionType highland */
  public static final ID RT_HIGHLAND = StringID.create("Hochland");
  
  /** The RegionType dessert */
  public static final ID RT_DESSERT = StringID.create("Wüste");
  
  /** The RegionType mountain */
  public static final ID RT_MOUNTAIN = StringID.create("Berge");

  /** The RegionType volcano */
  public static final ID RT_VOLCANO = StringID.create("Vulkan");

  /** The RegionType active volcano */
  public static final ID RT_ACTIVE_VOLCANO = StringID.create("Aktiver Vulkan");

  /** DOCUMENT-ME */
	public static final ID B_LIGHTTOWER = StringID.create("Leuchtturm");

  /** Shiptype Boat */
  public static final ID ST_BOAT = StringID.create("Boot");
  
  /** Shiptype longboat */
  public static final ID ST_LONGBOAT = StringID.create("Langboot");
  
}
