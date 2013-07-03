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

package magellan.library.gamebinding.e3a;

import magellan.library.StringID;
import magellan.library.gamebinding.EresseaConstants;

/**
 * Constants specific to E3.
 */
public class E3AConstants extends EresseaConstants {

  /** Der ALLIANZ-Befehl */
  public static final String OS_ALLIANCE = "ALLIANCE";
  /** Order constant ALLIANCE */
  public static final StringID OC_ALLIANCE = StringID.create("ALLIANCE");
  /**
   * ALLIANZ AUSSTOSSEN <partei-nr> -- eine Partei aus der Allianz ausschlie�en (nur f�r den
   * Administrator).
   */
  public static final String OS_ALLIANCE_KICK = "ALLIANCE_KICK";
  /** Order constant ALLIANCE_KICK */
  public static final StringID OC_ALLIANCE_KICK = StringID.create("ALLIANCE_KICK");
  /** ALLIANZ VERLASSEN -- aus der aktuellen Allianz austreten. */
  public static final String OS_ALLIANCE_LEAVE = "ALLIANCE_LEAVE";
  /** Order constant ALLIANCE_LEAVE */
  public static final StringID OC_ALLIANCE_LEAVE = StringID.create("ALLIANCE_LEAVE");
  /**
   * ALLIANZ KOMMANDO <partei-nr> -- eine andere Partei zum Administrator machen (nur f�r den
   * Administrator).
   */
  public static final String OS_ALLIANCE_COMMAND = "ALLIANCE_COMMAND";
  /** Order constant ALLIANCE_COMMAND */
  public static final StringID OC_ALLIANCE_COMMAND = StringID.create("ALLIANCE_COMMAND");
  /** ALLIANZ NEU -- eine neue Allianz mit der eigenen Partei als Administrator erstellen. */
  public static final String OS_ALLIANCE_NEW = "ALLIANCE_NEW";
  /** Order constant ALLIANCE_NEW */
  public static final StringID OC_ALLIANCE_NEW = StringID.create("ALLIANCE_NEW");
  /**
   * ALLIANZ EINLADEN <partei-nr> -- eine andere Partei in dieser Runde ins B�ndnis einladen (nur
   * Administratoren).
   */
  public static final String OS_ALLIANCE_INVITE = "ALLIANCE_INVITE";
  /** Order constant ALLIANCE_INVITE */
  public static final StringID OC_ALLIANCE_INVITE = StringID.create("ALLIANCE_INVITE");
  /**
   * ALLIANZ BEITRETEN <allianz-nr> -- einer anderen Allianz beitreten (wenn sie eine Einladung in
   * der gleichen Runde erh�lt).
   */
  public static final String OS_ALLIANCE_JOIN = "ALLIANCE_JOIN";
  /** Order constant ALLIANCE_JOIN */
  public static final StringID OC_ALLIANCE_JOIN = StringID.create("ALLIANCE_JOIN");
  /** BEZAHLE (NICHT) */
  public static final String OS_PAY = "PAY";
  /** Order constant PAY */
  public static final StringID OC_PAY = StringID.create("PAY");
  /** MACHE Wache */
  public static final String OS_WATCH = "WATCH";
  /** Order constant WATCH */
  public static final StringID OC_WATCH = StringID.create("WATCH");

  /** Ger�st */
  public static final StringID B_FRAME = StringID.create("Ger�st");
  /** Wachturm */
  public static final StringID B_GUARDTOWER = StringID.create("Wachturm");
  /** Wachstube */
  public static final StringID B_GUARDHOUSE = StringID.create("Wachstube");

  /** Streitross */
  public static final StringID I_STREITROSS = StringID.create("Streitross");

  /** Schiffstyp */
  public static final StringID ST_EINBAUM = StringID.create("Einbaum");
  /** Schiffstyp */
  public static final StringID ST_KUTTER = StringID.create("Kutter");
  /** Schiffstyp */
  public static final StringID ST_BARKE = StringID.create("Barke");
  /** Schiffstyp */
  public static final StringID ST_KOENIGSBARKE = StringID.create("K�nigsbarke");
  /** Schiffstyp */
  public static final StringID ST_FLOSS = StringID.create("Flo�");
}
