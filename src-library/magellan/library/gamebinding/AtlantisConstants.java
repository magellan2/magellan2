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
 * Constants for atlantis game.
 */
public class AtlantisConstants extends GameConstants {
  /** Order MAKE */
  public static final StringID OC_MAKE = StringID.create("MAKE");

  /** Order FORM */
  public static final StringID OC_FORM = StringID.create("FORM");
  /** Order ACCEPT */
  public static final StringID OC_ACCEPT = StringID.create("ACCEPT");
  /** Order ADDRESS */
  public static final StringID OC_ADDRESS = StringID.create("ADDRESS");
  /** Order ADMIT */
  public static final StringID OC_ADMIT = StringID.create("ADMIT");
  /** Order ALLY */
  public static final StringID OC_ALLY = StringID.create("ALLY");
  /** Order BEHIND */
  public static final StringID OC_BEHIND = StringID.create("BEHIND");
  /** Order COMBAT */
  public static final StringID OC_COMBAT = StringID.create("COMBAT");
  /** Order DISPLAY */
  public static final StringID OC_DISPLAY = StringID.create("DISPLAY");
  /** Order GUARD */
  public static final StringID OC_GUARD = StringID.create("GUARD");
  /** Order NAME */
  public static final StringID OC_NAME = StringID.create("NAME");
  /** Order PASSWORD */
  public static final StringID OC_PASSWORD = StringID.create("PASSWORD");
  /** Order RESHOW */
  public static final StringID OC_RESHOW = StringID.create("RESHOW");
  /** Order FIND */
  public static final StringID OC_FIND = StringID.create("FIND");
  /** Order BOARD */
  public static final StringID OC_BOARD = StringID.create("BOARD");
  /** Order ENTER */
  public static final StringID OC_ENTER = StringID.create("ENTER");
  /** Order LEAVE */
  public static final StringID OC_LEAVE = StringID.create("LEAVE");
  /** Order PROMOTE */
  public static final StringID OC_PROMOTE = StringID.create("PROMOTE");
  /** Order ATTACK */
  public static final StringID OC_ATTACK = StringID.create("ATTACK");
  /** Order DEMOLISH */
  public static final StringID OC_DEMOLISH = StringID.create("DEMOLISH");
  /** Order GIVE */
  public static final StringID OC_GIVE = StringID.create("GIVE");
  /** Order PAY */
  public static final StringID OC_PAY = StringID.create("PAY");
  /** Order SINK */
  public static final StringID OC_SINK = StringID.create("SINK");
  /** Order TRANSFER */
  public static final StringID OC_TRANSFER = StringID.create("TRANSFER");
  /** Order TAX */
  public static final StringID OC_TAX = StringID.create("TAX");
  /** Order RECRUIT */
  public static final StringID OC_RECRUIT = StringID.create("RECRUIT");
  /** Order QUIT */
  public static final StringID OC_QUIT = StringID.create("QUIT");
  /** Order MOVE */
  public static final StringID OC_MOVE = StringID.create("MOVE");
  /** Order SAIL */
  public static final StringID OC_SAIL = StringID.create("SAIL");
  /** Order BUILD */
  public static final StringID OC_BUILD = StringID.create("BUILD");
  /** Order ENTERTAIN */
  public static final StringID OC_ENTERTAIN = StringID.create("ENTERTAIN");
  /** Order PRODUCE */
  public static final StringID OC_PRODUCE = StringID.create("PRODUCE");
  /** Order RESEARCH */
  public static final StringID OC_RESEARCH = StringID.create("RESEARCH");
  /** Order STUDY */
  public static final StringID OC_STUDY = StringID.create("STUDY");
  /** Order TEACH */
  public static final StringID OC_TEACH = StringID.create("TEACH");
  /** Order WORK */
  public static final StringID OC_WORK = StringID.create("WORK");
  /** Order CAST */
  public static final StringID OC_CAST = StringID.create("CAST");

  /** Order constant UNIT */
  public static final StringID OC_UNIT = StringID.create("UNIT");

  /** Order constant END */
  public static final StringID OC_END = StringID.create("END");

  /** Order constant NEW */
  public static final StringID OC_NEW = StringID.create("NEW");

  /** The item silver */
  public static final StringID I_USILVER = StringID.create("Silber");

  /** The resource silver */
  public static final StringID I_RSILVER = StringID.create("Silber");

  public static StringID I_WOOD = StringID.create("wood");

  /** Building type 2building" */
  public static final StringID B_BUILDING = StringID.create("Building");

  /** The {@link #O_COMBAT_FRONT} combat status */
  public static final int CS_FRONT = 1;
  /** The {@link #O_COMBAT_REAR} combat status */
  public static final int CS_REAR = 2;

}
