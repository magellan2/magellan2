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
  public static final String O_ALLIANCE = "ALLIANCE";
  /** ALLIANZ AUSSTOSSEN <partei-nr> -- eine Partei aus der Allianz ausschließen (nur für den Administrator). */
  public static final String O_ALLIANCE_KICK = "ALLIANCE_KICK";
  /** ALLIANZ VERLASSEN -- aus der aktuellen Allianz austreten. */
  public static final String O_ALLIANCE_LEAVE = "ALLIANCE_LEAVE";
  /** ALLIANZ KOMMANDO <partei-nr> -- eine andere Partei zum Administrator machen (nur für den Administrator). */
  public static final String O_ALLIANCE_COMMAND = "ALLIANCE_COMMAND";
  /** ALLIANZ NEU -- eine neue Allianz mit der eigenen Partei als Administrator erstellen. */
  public static final String O_ALLIANCE_NEW = "ALLIANCE_NEW";
  /** ALLIANZ EINLADEN <partei-nr> -- eine andere Partei in dieser Runde ins Bündnis einladen (nur Administratoren). */
  public static final String O_ALLIANCE_INVITE = "ALLIANCE_INVITE";
  /** ALLIANZ BEITRETEN <allianz-nr> -- einer anderen Allianz beitreten (wenn sie eine Einladung in der gleichen Runde erhält). */ 
  public static final String O_ALLIANCE_JOIN = "ALLIANCE_JOIN";
  /** BEZAHLE (NICHT) */
  public static final String O_PAY = "PAY";
  /** MACHE Wache */
  public static final String O_WATCH = "WATCH";
  
  /** Gerüst */
  public static final StringID B_FRAME = StringID.create("Gerüst");
  /** Wachturm */
  public static final StringID B_GUARDTOWER = StringID.create("Wachturm");
  /** Wachstube */
  public static final StringID B_GUARDHOUSE = StringID.create("Wachstube");
  
  /** Streitross */
  public static final StringID I_STREITROSS = StringID.create("Streitross");
}
