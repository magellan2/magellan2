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
 * Constants for the Eressea game
 */
public class EresseaConstants extends GameConstants {
  /** Option category ID AUSWERTUNG */
  public static final StringID OPT_AUSWERTUNG = StringID.create("AUSWERTUNG");

  /** Option category ID COMPUTER */
  public static final StringID OPT_COMPUTER = StringID.create("COMPUTER");

  /** Option category ID ZUGVORLAGE */
  public static final StringID OPT_ZUGVORLAGE = StringID.create("ZUGVORLAGE");

  /** Option category ID SILBERPOOL */
  public static final StringID OPT_SILBERPOOL = StringID.create("SILBERPOOL");

  /** Option category ID STATISTIK */
  public static final StringID OPT_STATISTIK = StringID.create("STATISTIK");

  /** Option category ID DEBUG */
  public static final StringID OPT_DEBUG = StringID.create("DEBUG");

  /** Option category ID ZIPPED */
  public static final StringID OPT_ZIPPED = StringID.create("ZIPPED");

  /** Option category ID ZEITUNG */
  public static final StringID OPT_ZEITUNG = StringID.create("ZEITUNG");

  /** Option category ID MATERIALPOOL */
  public static final StringID OPT_MATERIALPOOL = StringID.create("MATERIALPOOL");

  /** Option category ID ADRESSEN */
  public static final StringID OPT_ADRESSEN = StringID.create("ADRESSEN");

  /** Option category ID BZIP2 */
  public static final StringID OPT_BZIP2 = StringID.create("BZIP2");

  /** Option category ID PUNKTE */
  public static final StringID OPT_PUNKTE = StringID.create("PUNKTE");

  /** Item category ID WEAPONS */
  public static final StringID C_WEAPONS = StringID.create("WEAPONS");

  /** Item category ID DISTANCE WEAPONS */
  public static final StringID C_RANGED_WEAPONS = StringID.create("DISTANCE WEAPONS");

  /** Item category ID ARMOUR */
  public static final StringID C_ARMOUR = StringID.create("ARMOUR");

  /** Item category ID RESOURCES */
  public static final StringID C_RESOURCES = StringID.create("RESOURCES");

  /** Item category ID LUXURIES */
  public static final StringID C_LUXURIES = StringID.create("LUXURIES");

  /** Item category ID HERBS */
  public static final StringID C_HERBS = StringID.create("HERBS");

  /** Item category ID MISC */
  public static final StringID C_MISC = StringID.create("MISC");

  // /** An end-of-line comment */
  // public static final StringID O_COMMENT = StringID.create(";");
  //
  // /** A persistent comment */
  // public static final StringID O_PCOMMENT = StringID.create("//");
  // /** A persistent order marker */
  // public static final StringID O_PERSISTENT = StringID.create("@");

  public static final String OS_COMMENT = ";";
  public static final String OS_PCOMMENT = "//";
  public static final String OS_PERSISTENT = "@";

  /** Order constant ADDRESSES */
  public static final StringID O_ADDRESSES = StringID.create("ADDRESSES");

  /**
   * @deprecated replaced by COMBAT_AGGRESSIVE
   */
  @Deprecated
  public static final StringID O_AGGRESSIVE = StringID.create("AGGRESSIVE");

  /** Order constant AFTER */
  public static final StringID O_AFTER = StringID.create("AFTER");

  /** Order constant ALL */
  public static final StringID O_ALL = StringID.create("ALL");

  /** Order constant ATTACK */
  public static final StringID O_ATTACK = StringID.create("ATTACK");

  /** Order constant AURA */
  public static final StringID O_AURA = StringID.create("AURA");

  /** Order constant BANNER */
  public static final StringID O_BANNER = StringID.create("BANNER");

  /** Order constant BEFORE */
  public static final StringID O_BEFORE = StringID.create("BEFORE");

  /** Order constant BUY */
  public static final StringID O_BUY = StringID.create("BUY");

  /** Order constant BZIP2 */
  public static final StringID O_BZIP2 = StringID.create("BZIP2");

  /** Order constant CARRY */
  public static final StringID O_CARRY = StringID.create("CARRY");

  /** Order constant CAST */
  public static final StringID O_CAST = StringID.create("CAST");

  /** Order constant CASTLE */
  public static final StringID O_CASTLE = StringID.create("CASTLE");

  /** BENNEN GEBÄUDE xyz */
  public static final StringID O_BUILDING = StringID.create("BUILDING");

  /** Order constant CLAIM */
  public static final StringID O_CLAIM = StringID.create("CLAIM");

  /** "KÄMPFE [AGGRESSIV|HINTEN|DEFENSIV...]" */
  public static final StringID O_COMBAT = StringID.create("COMBAT");

  /** argument of COMBAT order */
  public static final StringID O_COMBAT_AGGRESSIVE = StringID.create("COMBAT_AGGRESSIVE");

  /** argument of COMBAT order */
  public static final StringID O_COMBAT_DEFENSIVE = StringID.create("COMBAT_DEFENSIVE");

  /** argument of COMBAT order */
  public static final StringID O_COMBAT_FLEE = StringID.create("COMBAT_FLEE");

  /**
   * argument of COMBAT order NOTE: "KÄMPFE" is now preferred to "KÄMPFE VORNE"
   */
  public static final StringID O_COMBAT_FRONT = StringID.create("COMBAT_FRONT");

  /** argument of COMBAT order */
  public static final StringID O_COMBAT_REAR = StringID.create("COMBAT_REAR");

  /** argument of COMBAT order */
  public static final StringID O_COMBAT_NOT = StringID.create("COMBAT_NOT");

  /** Order constant COMBATSPELL */
  public static final StringID O_COMBATSPELL = StringID.create("COMBATSPELL");

  /** Order constant COMPUTER */
  public static final StringID O_COMPUTER = StringID.create("COMPUTER");

  /** Order constant CONTACT */
  public static final StringID O_CONTACT = StringID.create("CONTACT");

  /** Order constant CONTROL */
  public static final StringID O_CONTROL = StringID.create("CONTROL");

  /** Order constant DEFAULT */
  public static final StringID O_DEFAULT = StringID.create("DEFAULT");

  /**
   * @deprecated replaced by O_COMBAT_DEFENSIVE
   */
  @Deprecated
  public static final StringID O_DEFENSIVE = StringID.create("DEFENSIVE");

  /** Order constant DESCRIBE */
  public static final StringID O_DESCRIBE = StringID.create("DESCRIBE");

  /** Order constant DESTROY */
  public static final StringID O_DESTROY = StringID.create("DESTROY");

  /** EACH/JE keyword */
  public static final StringID O_EACH = StringID.create("EACH");

  /** Order constant EMAIL */
  public static final StringID O_EMAIL = StringID.create("EMAIL");

  /** Order constant END */
  public static final StringID O_END = StringID.create("END");

  /** Order constant ENTER */
  public static final StringID O_ENTER = StringID.create("ENTER");

  /** Order constant ENTERTAIN */
  public static final StringID O_ENTERTAIN = StringID.create("ENTERTAIN");

  /** Order constant ERESSEA */
  public static final StringID O_ERESSEA = StringID.create("ERESSEA");

  /** Order constant FACTION */
  public static final StringID O_FACTION = StringID.create("FACTION");

  /**
   * @deprecated replaced by HELP_FACTIONSTEALTH
   */
  @Deprecated
  public static final StringID O_FACTIONSTEALTH = StringID.create("FACTIONSTEALTH");

  /**
   * @deprecated replaced by COMBAT_FLEE
   */
  @Deprecated
  public static final StringID O_FLEE = StringID.create("FLEE");

  /** Order constant FOLLOW */
  public static final StringID O_FOLLOW = StringID.create("FOLLOW");

  /** @deprecated you should use one of <code>O_FOREIGNUNIT, -FACTION, -BUILDING, -SHIP</code> */
  @Deprecated
  public static final StringID O_FOREIGN = StringID.create("FOREIGN");

  /** BENENNE FREMDE EINHEIT */
  public static final StringID O_FOREIGNUNIT = StringID.create("FOREIGNUNIT");

  /** BENENNE FREMDE PARTEI */
  public static final StringID O_FOREIGNFACTION = StringID.create("FOREIGNFACTION");

  /** BENENNE FREMDE BURG */
  public static final StringID O_FOREIGNBUILDING = StringID.create("FOREIGNBUILDING");

  /** BENENNE FREMDES SCHIFF */
  public static final StringID O_FOREIGNSHIP = StringID.create("FOREIGNSHIP");

  /** Order constant FORGET */
  public static final StringID O_FORGET = StringID.create("FORGET");

  /**
   * @deprecated replaced by COMBAT_FRONT
   */
  @Deprecated
  public static final StringID O_FRONT = StringID.create("FRONT");

  /** Order constant GIVE */
  public static final StringID O_GIVE = StringID.create("GIVE");

  /** Order constant GROUP */
  public static final StringID O_GROUP = StringID.create("GROUP");

  /** Order constant GROW */
  public static final StringID O_GROW = StringID.create("GROW");

  /** Order constant GUARD */
  public static final StringID O_GUARD = StringID.create("GUARD");

  /** The "HELFE" command */
  public static final StringID O_HELP = StringID.create("HELP");

  /** The "GIB" from HELFE GIB */
  public static final StringID O_HELP_GIVE = StringID.create("HELP_GIVE");

  /** The "KÄMPFE" from HELFE KÄMPFE */
  public static final StringID O_HELP_COMBAT = StringID.create("HELP_COMBAT");

  /** The "SILBER" from HELFE SILBER */
  public static final StringID O_HELP_SILVER = StringID.create("HELP_SILVER");

  /** The "BEWACHE" from HELFE BEWACHE */
  public static final StringID O_HELP_GUARD = StringID.create("HELP_GUARD");

  /** The "PARTEITARNUNG" from HELFE PARTEITARNUNG */
  public static final StringID O_HELP_FACTIONSTEALTH = StringID.create("HELP_FACTIONSTEALTH");

  // FIXME(pavkovic) 2003.04.16: this is only used for german to distinguish between "HELFEN" and
  // "HELFE"

  /** The "HELFE" from KÄMPFE HELFE [NICHT] */
  public static final StringID O_COMBAT_HELP = StringID.create("COMBAT_HELP");

  /** Order constant HERBS */
  public static final StringID O_HERBS = StringID.create("HERBS");

  /** Order constant HIDE */
  public static final StringID O_HIDE = StringID.create("HIDE");

  /** Order constant HORSES */
  public static final StringID O_HORSES = StringID.create("HORSES");

  /** Order constant ITEMPOOL */
  public static final StringID O_ITEMPOOL = StringID.create("ITEMPOOL");

  /** Order constant LEARN */
  public static final StringID O_LEARN = StringID.create("LEARN");

  /** Order constant LEAVE */
  public static final StringID O_LEAVE = StringID.create("LEAVE");

  /** Order constant LEVEL */
  public static final StringID O_LEVEL = StringID.create("LEVEL");

  /** Order constant LOCALE */
  public static final StringID O_LOCALE = StringID.create("LOCALE");

  /** Order constant MAKE */
  public static final StringID O_MAKE = StringID.create("MAKE");

  /** Order constant MALLORNSEED */
  public static final StringID O_MALLORNSEED = StringID.create("MALLORNSEED");

  /** As in GIB 123 2 PERSONEN */
  public static final StringID O_MEN = StringID.create("MEN");

  /** Order constant MESSAGE */
  public static final StringID O_MESSAGE = StringID.create("MESSAGE");

  /** Order constant MOVE */
  public static final StringID O_MOVE = StringID.create("MOVE");

  /** Order constant NAME */
  public static final StringID O_NAME = StringID.create("NAME");

  /** Order constant NEXT */
  public static final StringID O_NEXT = StringID.create("NEXT");

  /** Order constant NOT */
  public static final StringID O_NOT = StringID.create("NOT");

  /** Order constant NUMBER */
  public static final StringID O_NUMBER = StringID.create("NUMBER");

  /** Order constant OPTION */
  public static final StringID O_OPTION = StringID.create("OPTION");

  /** Order constant ORIGIN */
  public static final StringID O_ORIGIN = StringID.create("ORIGIN");

  /** Order constant PASSWORD */
  public static final StringID O_PASSWORD = StringID.create("PASSWORD");

  /** Order constant PAUSE */
  public static final StringID O_PAUSE = StringID.create("PAUSE");

  /** Order constant PIRACY */
  public static final StringID O_PIRACY = StringID.create("PIRACY");

  /** @deprecated Same as O_REPORT */
  @Deprecated
  public static final StringID O_PLAINTEXT = StringID.create("PLAINTEXT");

  /** Order constant PLANT */
  public static final StringID O_PLANT = StringID.create("PLANT");

  /** Order constant POTIONS */
  public static final StringID O_POTIONS = StringID.create("POTIONS");

  /** Order constant PREFIX */
  public static final StringID O_PREFIX = StringID.create("PREFIX");

  /** Order constant PRIVATE */
  public static final StringID O_PRIVATE = StringID.create("PRIVATE");

  /** Order constant PROMOTION */
  public static final StringID O_PROMOTION = StringID.create("PROMOTION");

  /** Order constant QUIT */
  public static final StringID O_QUIT = StringID.create("QUIT");

  /**
   * @deprecated replaced by O_COMBAT_REAR
   */
  @Deprecated
  public static final StringID O_REAR = StringID.create("REAR");

  /** Order constant RECRUIT */
  public static final StringID O_RECRUIT = StringID.create("RECRUIT");

  /** Order constant REGION */
  public static final StringID O_REGION = StringID.create("REGION");

  /** Order constant REPORT */
  public static final StringID O_REPORT = StringID.create("REPORT");

  /** Order constant RESEARCH */
  public static final StringID O_RESEARCH = StringID.create("RESEARCH");

  /** Order constant RESERVE */
  public static final StringID O_RESERVE = StringID.create("RESERVE");

  /** Order constant RESTART */
  public static final StringID O_RESTART = StringID.create("RESTART");

  /** Order constant RIDE */
  public static final StringID O_RIDE = StringID.create("RIDE");

  /** Order constant ROAD */
  public static final StringID O_ROAD = StringID.create("ROAD");

  /** Order constant ROUTE */
  public static final StringID O_ROUTE = StringID.create("ROUTE");

  /** TODO: DOCUMENT ME! */
  public static final StringID O_SCORE = StringID.create("SCORE");

  /** Order constant SEED */
  public static final StringID O_SEED = StringID.create("SEED");

  /** Order constant SELL */
  public static final StringID O_SELL = StringID.create("SELL");

  /** Order constant SABOTAGE */
  public static final StringID O_SABOTAGE = StringID.create("SABOTAGE");

  /** Order constant SHIP */
  public static final StringID O_SHIP = StringID.create("SHIP");

  /** Order constant SHOW */
  public static final StringID O_SHOW = StringID.create("SHOW");

  /** Order constant SIEGE */
  public static final StringID O_SIEGE = StringID.create("SIEGE");

  /**
   * @deprecated use rules.getItemType(EresseaConstants.I_SILVER) instead
   */
  @Deprecated
  public static final StringID O_SILVER = StringID.create("SILVER");

  /** Order constant SILVERPOOL */
  public static final StringID O_SILVERPOOL = StringID.create("SILVERPOOL");

  /** Order constant SORT */
  public static final StringID O_SORT = StringID.create("SORT");

  /** Order constant SPELLS */
  public static final StringID O_SPELLS = StringID.create("SPELLS");

  /** Order constant SPY */
  public static final StringID O_SPY = StringID.create("SPY");

  /** Order constant STATISTICS */
  public static final StringID O_STATISTICS = StringID.create("STATISTICS");

  /** Order constant STEAL */
  public static final StringID O_STEAL = StringID.create("STEAL");

  /** Order constant SUPPLY */
  public static final StringID O_SUPPLY = StringID.create("SUPPLY");

  /** Order constant TAX */
  public static final StringID O_TAX = StringID.create("TAX");

  /** Order constant TEACH */
  public static final StringID O_TEACH = StringID.create("TEACH");

  /** Order constant TEMP */
  public static final StringID O_TEMP = StringID.create("TEMP");

  /** Order constant TEMPLATE */
  public static final StringID O_TEMPLATE = StringID.create("TEMPLATE");

  /** Order constant TREES */
  public static final StringID O_TREES = StringID.create("TREES");

  /** Order constant UNIT */
  public static final StringID O_UNIT = StringID.create("UNIT");

  /** Order constant USE */
  public static final StringID O_USE = StringID.create("USE");

  /** Order constant WORK */
  public static final StringID O_WORK = StringID.create("WORK");

  /** Order constant ZIPPED */
  public static final StringID O_ZIPPED = StringID.create("ZIPPED");

  // directions:

  /** Order constant NORTHWEST */
  public static final StringID O_NORTHWEST = StringID.create("NORTHWEST");

  /** Order constant NORTHEAST */
  public static final StringID O_NORTHEAST = StringID.create("NORTHEAST");

  /** Order constant EAST */
  public static final StringID O_EAST = StringID.create("EAST");

  /** Order constant SOUTHEAST */
  public static final StringID O_SOUTHEAST = StringID.create("SOUTHEAST");

  /** Order constant SOUTHWEST */
  public static final StringID O_SOUTHWEST = StringID.create("SOUTHWEST");

  /** Order constant WEST */
  public static final StringID O_WEST = StringID.create("WEST");

  /** Order constant NW */
  public static final StringID O_NW = StringID.create("NW");

  /** Order constant NE */
  public static final StringID O_NE = StringID.create("NE");

  /** Order constant E */
  public static final StringID O_E = StringID.create("E");

  /** Order constant SE */
  public static final StringID O_SE = StringID.create("SE");

  /** Order constant SW */
  public static final StringID O_SW = StringID.create("SW");

  /** Order constant W */
  public static final StringID O_W = StringID.create("W");

  /** Race DAEMONEN */
  public static final StringID R_DAEMONEN = StringID.create("DAEMONEN");

  /** Race ELFEN */
  public static final StringID R_ELFEN = StringID.create("ELFEN");

  /** Race GOBLINS */
  public static final StringID R_GOBLINS = StringID.create("GOBLINS");

  /** Race HALBLINGE */
  public static final StringID R_HALBLINGE = StringID.create("HALBLINGE");

  /** Race INSEKTEN */
  public static final StringID R_INSEKTEN = StringID.create("INSEKTEN");

  /** Race KATZEN */
  public static final StringID R_KATZEN = StringID.create("KATZEN");

  /** Race MEERMENSCHEN */
  public static final StringID R_MEERMENSCHEN = StringID.create("MEERMENSCHEN");

  /** Race MENSCHEN */
  public static final StringID R_MENSCHEN = StringID.create("MENSCHEN");

  /** Race ORKS */
  public static final StringID R_ORKS = StringID.create("ORKS");

  /** Race TROLLE */
  public static final StringID R_TROLLE = StringID.create("TROLLE");

  /** Race ZWERGE */
  public static final StringID R_ZWERGE = StringID.create("ZWERGE");

  /** Skill ALCHEMIE */
  public static final StringID S_ALCHEMIE = StringID.create("ALCHEMIE");

  /** Skill ARMBRUSTSCHIESSEN */
  public static final StringID S_ARMBRUSTSCHIESSEN = StringID.create("ARMBRUSTSCHIESSEN");

  /** Skill AUSDAUER */
  public static final StringID S_AUSDAUER = StringID.create("AUSDAUER");

  /** Skill BERGBAU */
  public static final StringID S_BERGBAU = StringID.create("BERGBAU");

  /** Skill BOGENSCHIESSEN */
  public static final StringID S_BOGENSCHIESSEN = StringID.create("BOGENSCHIESSEN");

  /** Skill BURGENBAU */
  public static final StringID S_BURGENBAU = StringID.create("BURGENBAU");

  /** Skill HANDELN */
  public static final StringID S_HANDELN = StringID.create("HANDELN");

  /** Skill HIEBWAFFEN */
  public static final StringID S_HIEBWAFFEN = StringID.create("HIEBWAFFEN");

  /** Skill HOLZFAELLEN */
  public static final StringID S_HOLZFAELLEN = StringID.create("HOLZFAELLEN");

  /** Skill KATAPULTBEDIENUNG */
  public static final StringID S_KATAPULTBEDIENUNG = StringID.create("KATAPULTBEDIENUNG");

  /** Skill KRAEUTERKUNDE */
  public static final StringID S_KRAEUTERKUNDE = StringID.create("KRAEUTERKUNDE");

  /** Skill MAGIE */
  public static final StringID S_MAGIE = StringID.create("MAGIE");

  /** Skill PFERDEDRESSUR */
  public static final StringID S_PFERDEDRESSUR = StringID.create("PFERDEDRESSUR");

  /** Skill REITEN */
  public static final StringID S_REITEN = StringID.create("REITEN");

  /** Skill RUESTUNGSBAU */
  public static final StringID S_RUESTUNGSBAU = StringID.create("RUESTUNGSBAU");

  /** Skill SCHIFFBAU */
  public static final StringID S_SCHIFFBAU = StringID.create("SCHIFFBAU");

  /** Skill SEGELN */
  public static final StringID S_SEGELN = StringID.create("SEGELN");

  /** Skill SPIONAGE */
  public static final StringID S_SPIONAGE = StringID.create("SPIONAGE");

  /** Skill STANGENWAFFEN */
  public static final StringID S_STANGENWAFFEN = StringID.create("STANGENWAFFEN");

  /** Skill STEINBAU */
  public static final StringID S_STEINBAU = StringID.create("STEINBAU");

  /** Skill STEUEREINTREIBEN */
  public static final StringID S_STEUEREINTREIBEN = StringID.create("STEUEREINTREIBEN");

  /** Skill STRASSENBAU */
  public static final StringID S_STRASSENBAU = StringID.create("STRASSENBAU");

  /** Skill TAKTIK */
  public static final StringID S_TAKTIK = StringID.create("TAKTIK");

  /** Skill TARNUNG */
  public static final StringID S_TARNUNG = StringID.create("TARNUNG");

  /** Skill UNTERHALTUNG */
  public static final StringID S_UNTERHALTUNG = StringID.create("UNTERHALTUNG");

  /** Skill WAFFENBAU */
  public static final StringID S_WAFFENBAU = StringID.create("WAFFENBAU");

  /** Skill WAGENBAU */
  public static final StringID S_WAGENBAU = StringID.create("WAGENBAU");

  /** Skill WAHRNEHMUNG */
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

  /** Item cart */
  public static final StringID I_CART = StringID.create("Wagen");

  /** Item belt of troll strength */
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

  /** The "item" PERSONS */
  public static final StringID I_MEN = StringID.create("Personen");

  /** The item peasant */
  public static final StringID I_UPEASANT = StringID.create("Bauer");

  /** The item silver */
  public static final StringID I_USILVER = StringID.create("Silber");

  /** The resource silver */
  public static final StringID I_RSILVER = StringID.create("Silber");

  /** @deprecated use either {@link #I_USILVER} or {@link #I_RSILVER}. */
  @Deprecated
  public static final StringID I_SILVER = StringID.create("Silber");

  /** The resource sprouts */
  public static final StringID I_SPROUTS = StringID.create("Schösslinge");

  /** The item Stein */
  public static final StringID I_USTONE = StringID.create("Stein");

  /** The resource Steine */
  public static final StringID I_RSTONES = StringID.create("Steine");

  /** @deprecated use either {@link #I_USTONE} or {@link #I_RSTONES}. */
  @Deprecated
  public static final StringID I_STONES = StringID.create("Steine");

  /** The resource trees */
  public static final StringID I_TREES = StringID.create("Bäume");

  /** The item wood */
  public static final StringID I_WOOD = StringID.create("Holz");

  /** The item Mallorn */
  public static final StringID I_UMALLORN = StringID.create("Mallorn");

  /** The resource Mallorn */
  public static final StringID I_RMALLORN = StringID.create("Mallorn");

  /** @deprecated use either {@link #I_UMALLORN} or {@link #I_RMALLORN}. */
  @Deprecated
  public static final StringID I_MALLORN = StringID.create("Mallorn");

  /** The resource mallorn sprouts */
  public static final StringID I_MALLORNSPROUTS = StringID.create("Mallornschößlinge");

  /** The RegionType ocean */
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

  /** "undefined" value for combat status */
  public static final int CS_INIT = -31;
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
