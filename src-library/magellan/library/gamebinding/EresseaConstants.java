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

import magellan.library.StringID;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class EresseaConstants {
  /** DOCUMENT-ME */
  public static final StringID OPT_AUSWERTUNG = StringID.create("AUSWERTUNG");

  /** DOCUMENT-ME */
  public static final StringID OPT_COMPUTER = StringID.create("COMPUTER");

  /** DOCUMENT-ME */
  public static final StringID OPT_ZUGVORLAGE = StringID.create("ZUGVORLAGE");

  /** DOCUMENT-ME */
  public static final StringID OPT_SILBERPOOL = StringID.create("SILBERPOOL");

  /** DOCUMENT-ME */
  public static final StringID OPT_STATISTIK = StringID.create("STATISTIK");

  /** DOCUMENT-ME */
  public static final StringID OPT_DEBUG = StringID.create("DEBUG");

  /** DOCUMENT-ME */
  public static final StringID OPT_ZIPPED = StringID.create("ZIPPED");

  /** DOCUMENT-ME */
  public static final StringID OPT_ZEITUNG = StringID.create("ZEITUNG");

  /** DOCUMENT-ME */
  public static final StringID OPT_MATERIALPOOL = StringID.create("MATERIALPOOL");

  /** DOCUMENT-ME */
  public static final StringID OPT_ADRESSEN = StringID.create("ADRESSEN");

  /** DOCUMENT-ME */
  public static final StringID OPT_BZIP2 = StringID.create("BZIP2");

  /** DOCUMENT-ME */
  public static final StringID OPT_PUNKTE = StringID.create("PUNKTE");

  /** DOCUMENT-ME */
  public static final StringID C_WEAPONS = StringID.create("WEAPONS");

  /** DOCUMENT-ME */
  public static final StringID C_ARMOUR = StringID.create("ARMOUR");

  /** DOCUMENT-ME */
  public static final StringID C_RESOURCES = StringID.create("RESOURCES");

  /** DOCUMENT-ME */
  public static final StringID C_LUXURIES = StringID.create("LUXURIES");

  /** DOCUMENT-ME */
  public static final StringID C_HERBS = StringID.create("HERBS");

  /** DOCUMENT-ME */
  public static final StringID C_MISC = StringID.create("MISC");

  /** An end-of-line comment */
  public static final String O_COMMENT = ";";

  /** A persistent comment */
  public static final String O_PCOMMENT = "//";

  /** A persistent order marker */
  public static final String O_PERSISTENT = "@";

  /** DOCUMENT-ME */
  public static final String O_ADDRESSES = "ADDRESSES";

  /**
   * @deprecated replaced by COMBAT_AGGRESSIVE
   */
  @Deprecated
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

  /** BENNEN GEBÄUDE xyz */
  public static final String O_BUILDING = "BUILDING";

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

  /**
   * argument of COMBAT order
   * 
   * @deprecated "KÄMPFE" is now preferred to "KÄMPFE VORNE"
   */
  @Deprecated
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
  @Deprecated
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
  @Deprecated
  public static final String O_FACTIONSTEALTH = "FACTIONSTEALTH";

  /**
   * @deprecated replaced by COMBAT_FLEE
   */
  @Deprecated
  public static final String O_FLEE = "FLEE";

  /** DOCUMENT-ME */
  public static final String O_FOLLOW = "FOLLOW";

  /** @deprecated you should use one of <code>O_FOREIGNUNIT, -FACTION, -BUILDING, -SHIP</code> */
  @Deprecated
  public static final String O_FOREIGN = "FOREIGN";

  /** BENENNE FREMDE EINHEIT */
  public static final String O_FOREIGNUNIT = "FOREIGNUNIT";

  /** BENENNE FREMDE PARTEI */
  public static final String O_FOREIGNFACTION = "FOREIGNFACTION";

  /** BENENNE FREMDE BURG */
  public static final String O_FOREIGNBUILDING = "FOREIGNBUILDING";

  /** BENENNE FREMDES SCHIFF */
  public static final String O_FOREIGNSHIP = "FOREIGNSHIP";

  /** DOCUMENT-ME */
  public static final String O_FORGET = "FORGET";

  /**
   * @deprecated replaced by COMBAT_FRONT
   */
  @Deprecated
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

  // FIXME(pavkovic) 2003.04.16: this is only used for german to distinguish between "HELFEN" and
  // "HELFE"

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
  public static final String O_MALLORNSEED = "MALLORNSEED";

  /** As in GIB 123 2 PERSONEN */
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
  @Deprecated
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
  @Deprecated
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
  public static final String O_TREES = "TREES";

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
  public static final StringID R_DAEMONEN = StringID.create("DAEMONEN");

  /** DOCUMENT-ME */
  public static final StringID R_ELFEN = StringID.create("ELFEN");

  /** DOCUMENT-ME */
  public static final StringID R_GOBLINS = StringID.create("GOBLINS");

  /** DOCUMENT-ME */
  public static final StringID R_HALBLINGE = StringID.create("HALBLINGE");

  /** DOCUMENT-ME */
  public static final StringID R_INSEKTEN = StringID.create("INSEKTEN");

  /** DOCUMENT-ME */
  public static final StringID R_KATZEN = StringID.create("KATZEN");

  /** DOCUMENT-ME */
  public static final StringID R_MEERMENSCHEN = StringID.create("MEERMENSCHEN");

  /** DOCUMENT-ME */
  public static final StringID R_MENSCHEN = StringID.create("MENSCHEN");

  /** DOCUMENT-ME */
  public static final StringID R_ORKS = StringID.create("ORKS");

  /** DOCUMENT-ME */
  public static final StringID R_TROLLE = StringID.create("TROLLE");

  /** DOCUMENT-ME */
  public static final StringID R_ZWERGE = StringID.create("ZWERGE");

  /** DOCUMENT-ME */
  public static final StringID S_ALCHEMIE = StringID.create("ALCHEMIE");

  /** DOCUMENT-ME */
  public static final StringID S_ARMBRUSTSCHIESSEN = StringID.create("ARMBRUSTSCHIESSEN");

  /** DOCUMENT-ME */
  public static final StringID S_AUSDAUER = StringID.create("AUSDAUER");

  /** DOCUMENT-ME */
  public static final StringID S_BERGBAU = StringID.create("BERGBAU");

  /** DOCUMENT-ME */
  public static final StringID S_BOGENSCHIESSEN = StringID.create("BOGENSCHIESSEN");

  /** DOCUMENT-ME */
  public static final StringID S_BURGENBAU = StringID.create("BURGENBAU");

  /** DOCUMENT-ME */
  public static final StringID S_HANDELN = StringID.create("HANDELN");

  /** DOCUMENT-ME */
  public static final StringID S_HIEBWAFFEN = StringID.create("HIEBWAFFEN");

  /** DOCUMENT-ME */
  public static final StringID S_HOLZFAELLEN = StringID.create("HOLZFAELLEN");

  /** DOCUMENT-ME */
  public static final StringID S_KATAPULTBEDIENUNG = StringID.create("KATAPULTBEDIENUNG");

  /** DOCUMENT-ME */
  public static final StringID S_KRAEUTERKUNDE = StringID.create("KRAEUTERKUNDE");

  /** DOCUMENT-ME */
  public static final StringID S_MAGIE = StringID.create("MAGIE");

  /** DOCUMENT-ME */
  public static final StringID S_PFERDEDRESSUR = StringID.create("PFERDEDRESSUR");

  /** DOCUMENT-ME */
  public static final StringID S_REITEN = StringID.create("REITEN");

  /** DOCUMENT-ME */
  public static final StringID S_RUESTUNGSBAU = StringID.create("RUESTUNGSBAU");

  /** DOCUMENT-ME */
  public static final StringID S_SCHIFFBAU = StringID.create("SCHIFFBAU");

  /** DOCUMENT-ME */
  public static final StringID S_SEGELN = StringID.create("SEGELN");

  /** DOCUMENT-ME */
  public static final StringID S_SPIONAGE = StringID.create("SPIONAGE");

  /** DOCUMENT-ME */
  public static final StringID S_STANGENWAFFEN = StringID.create("STANGENWAFFEN");

  /** DOCUMENT-ME */
  public static final StringID S_STEINBAU = StringID.create("STEINBAU");

  /** DOCUMENT-ME */
  public static final StringID S_STEUEREINTREIBEN = StringID.create("STEUEREINTREIBEN");

  /** DOCUMENT-ME */
  public static final StringID S_STRASSENBAU = StringID.create("STRASSENBAU");

  /** DOCUMENT-ME */
  public static final StringID S_TAKTIK = StringID.create("TAKTIK");

  /** DOCUMENT-ME */
  public static final StringID S_TARNUNG = StringID.create("TARNUNG");

  /** DOCUMENT-ME */
  public static final StringID S_UNTERHALTUNG = StringID.create("UNTERHALTUNG");

  /** DOCUMENT-ME */
  public static final StringID S_WAFFENBAU = StringID.create("WAFFENBAU");

  /** DOCUMENT-ME */
  public static final StringID S_WAGENBAU = StringID.create("WAGENBAU");

  /** DOCUMENT-ME */
  public static final StringID S_WAHRNEHMUNG = StringID.create("WAHRNEHMUNG");

  /** A state selector for the "Helfe Silber" state. */

  public static final int A_SILVER = 1 << 0;

  /** A state selector for the "Helfe Kämpfe" state. */

  public static final int A_COMBAT = 1 << 1;

  /** A state selector for the "Helfe Gib" state. */
  public static final int A_GIVE = 1 << 3;

  /** A state selector for the "Helfe Bewache" state. */
  public static final int A_GUARD = 1 << 4;

  /** A state selector for the "Helfe Parteitarnung" state. */

  public static final int A_GUISE = 1 << 5;

  /** A state selector for the "Helfe ?" state. */

  // public static final int A_WHATEVER = 1 << 6;

  /** A state selector for all of the alliance states. */

  // public static final int A_ALL = 0x003B;
  // public static final int A_ALL = A_SILVER | A_COMBAT | A_GIVE | A_GUARD | A_GUISE | A_WHATEVER;
  // // (binary value should be: 111101 (#123) )

  /**
   * Bag of negative weight - this bad weights 1 GE but stores 200 GE. However not all items can be
   * stored in the bag.
   */
  public static final StringID I_BONW = StringID.create("Zauberbeutel");

  /** DOCUMENT-ME */
  public static final StringID I_CART = StringID.create("Wagen");

  /** DOCUMENT-ME */
  public static final StringID I_GOTS = StringID.create("Gürtel der Trollstärke");

  /** @deprecated use either {@link #I_UHORSE} or {@link #I_RHORSES}. */
  @Deprecated
  public static final StringID I_HORSE = StringID.create("Pferd");

  /** The item Pferd */
  public static final StringID I_UHORSE = StringID.create("Pferd");

  /** The resource Pferde */
  public static final StringID I_RHORSES = StringID.create("Pferde");

  /** The item Eisen */
  public static final StringID I_UIRON = StringID.create("Eisen");

  /** The resource Eisen */
  public static final StringID I_RIRON = StringID.create("Eisen");

  /** @deprecated use either {@link #I_UIRON} or {@link #I_RIRON}. */
  @Deprecated
  public static final StringID I_IRON = StringID.create("Eisen");

  /** The item Laen */
  public static final StringID I_ULAEN = StringID.create("Laen");

  /** The resource Laen */
  public static final StringID I_RLAEN = StringID.create("Laen");

  /** @deprecated use either {@link #I_ULAEN} or {@link #I_RLAEN}. */
  @Deprecated
  public static final StringID I_LAEN = StringID.create("Laen");

  /** The resource peasants */
  public static final StringID I_PEASANTS = StringID.create("Bauern");

  /** The item peasant */
  public static final StringID I_UPEASANT = StringID.create("Bauer");

  /** The item silver */
  public static final StringID I_USILVER = StringID.create("Silber");

  /** The resource silver */
  public static final StringID I_RSILVER = StringID.create("Silber");

  /** @deprecated use either {@link #I_USILVER} or {@link #I_RSILVER}. */
  @Deprecated
  public static final StringID I_SILVER = StringID.create("Silber");

  public static final StringID I_SPROUTS = StringID.create("Schösslinge");

  /** The item Stein */
  public static final StringID I_USTONE = StringID.create("Stein");

  /** The resource Steine */
  public static final StringID I_RSTONES = StringID.create("Steine");

  /** @deprecated use either {@link #I_USTONE} or {@link #I_RSTONES}. */
  @Deprecated
  public static final StringID I_STONES = StringID.create("Steine");

  public static final StringID I_TREES = StringID.create("Bäume");

  public static final StringID I_WOOD = StringID.create("Holz");

  /** The item Mallorn */
  public static final StringID I_UMALLORN = StringID.create("Mallorn");

  /** The resource Mallorn */
  public static final StringID I_RMALLORN = StringID.create("Mallorn");

  /** @deprecated use either {@link #I_UMALLORN} or {@link #I_RMALLORN}. */
  @Deprecated
  public static final StringID I_MALLORN = StringID.create("Mallorn");

  public static final StringID I_MALLORNSPROUTS = StringID.create("Mallornschößlinge");

  /** DOCUMENT-ME */
  public static final StringID RT_OCEAN = StringID.create("Ozean");

  /** The RegionType firewall */
  public static final StringID RT_FIREWALL = StringID.create("Feuerwand");

  /** The RegionType plain */
  public static final StringID RT_PLAIN = StringID.create("Ebene");

  /** The RegionType forest */
  public static final StringID RT_FOREST = StringID.create("Wald");

  /** The RegionType glacier */
  public static final StringID RT_GLACIER = StringID.create("Gletscher");

  /** The RegionType swamp */
  public static final StringID RT_SWAMP = StringID.create("Sumpf");

  /** The RegionType highland */
  public static final StringID RT_HIGHLAND = StringID.create("Hochland");

  /** @deprecated you can't eat terrain types! */
  @Deprecated
  public static final StringID RT_DESSERT = StringID.create("Wüste");

  /** The RegionType dessert */
  public static final StringID RT_DESERT = StringID.create("Wüste");

  /** The RegionType mountain */
  public static final StringID RT_MOUNTAIN = StringID.create("Berge");

  /** The RegionType volcano */
  public static final StringID RT_VOLCANO = StringID.create("Vulkan");

  /** The RegionType Nebel */
  public static final StringID RT_FOG = StringID.create("Nebel");

  /** The RegoinType "the void" */
  public static final StringID RT_VOID = StringID.create("Magellan_Leere");

  /** The RegoinType "the void" */
  public static final StringID RT_UNKNOWN = StringID.create("unbekannt");

  /** "wrap around" regions */
  public static final StringID RT_WRAP = StringID.create("Übergang");

  /** The RegionType active volcano */
  public static final StringID RT_ACTIVE_VOLCANO = StringID.create("Aktiver Vulkan");

  /** Building type academy */
  public static final StringID B_ACADEMY = StringID.create("Akademie");

  /** Building type academy */
  public static final StringID B_CARAVANSEREI = StringID.create("Karawanserei");

  /** Building type harbour */
  public static final StringID B_HARBOUR = StringID.create("Hafen");

  /** building type lighthouse */
  public static final StringID B_LIGHTHOUSE = StringID.create("Leuchtturm");

  /** Shiptype Boat */
  public static final StringID ST_BOAT = StringID.create("Boot");

  /** Shiptype longboat */
  public static final StringID ST_LONGBOAT = StringID.create("Langboot");

  // pre 57:
  // 0: VORNE
  // 1: HINTEN
  // 2: NICHT
  // 3: FLIEHE
  //
  // 57 and later:
  // 0 AGGRESSIV: 1. Reihe, flieht nie.
  // 1 VORNE: 1. Reihe, kämpfen bis 20% HP
  // 2 HINTEN: 2. Reihe, kämpfen bis 20% HP
  // 3 DEFENSIV: 2. Reihe, kämpfen bis 90% HP
  // 4 NICHT: 3. Reihe, kämpfen bis 90% HP
  // 5 FLIEHE: 4. Reihe, flieht immer.

  /** The {@link #O_COMBAT_AGGRESSIVE} combat status */
  public static final int CS_AGGRESSIVE = 0;
  /** The {@link #O_COMBAT_FRONT} combat status */
  public static final int CS_FRONT = 1;
  /** The {@link #O_COMBAT_REAR} combat status */
  public static final int CS_REAR = 2;
  /** The {@link #O_COMBAT_DEFENSIVE} combat status */
  public static final int CS_DEFENSIVE = 3;
  /** The {@link #O_COMBAT_NOT} combat status */
  public static final int CS_NOT = 4;
  /** The {@link #O_COMBAT_FLEE} combat status */
  public static final int CS_FLEE = 5;
  /** The {@link #O_COMBAT_HELP} NOT combat status */
  public static final int CS_HELPNOT = -2;
  /** The {@link #O_COMBAT_HELP} combat status */
  public static final int CS_HELPYES = -1;

}
