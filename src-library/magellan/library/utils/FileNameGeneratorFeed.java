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

package magellan.library.utils;

/**
 * @author Fiete just a class to collect needed parameters to generate a AutoFileName
 */
public class FileNameGeneratorFeed {
  private int round = -1;
  private String faction = null;
  private String factionnr = null;
  private String group = null;

  /**
   * constructs a new Feed
   * 
   * @param round int round (from GameData)
   */
  public FileNameGeneratorFeed(int round) {
    this(round, null, null, null);
  }

  /**
   * constructs a new Feed
   * 
   * @param round int round (from GameData)
   * @param faction String (Name of faction)
   * @param factionnr String (Number of faction, in base36 &rarr; String)
   */
  public FileNameGeneratorFeed(int round, String faction, String factionnr) {
    this(round, faction, factionnr, null);
  }

  /**
   * constructs a new Feed
   * 
   * @param round int round (from GameData)
   * @param faction String (Name of faction)
   * @param factionnr String (Number of faction, in base36 &rarr; String)
   * @param group String (Name of the selected Group - )
   */
  public FileNameGeneratorFeed(int round, String faction, String factionnr, String group) {
    this.round = round;
    this.faction = faction;
    this.factionnr = factionnr;
    this.group = group;
  }

  /**
   * @return the faction
   */
  public String getFaction() {
    return faction;
  }

  /**
   * @param faction the faction to set
   */
  public void setFaction(String faction) {
    this.faction = faction;
  }

  /**
   * @return the factionnr
   */
  public String getFactionnr() {
    return factionnr;
  }

  /**
   * @param factionnr the factionnr to set
   */
  public void setFactionnr(String factionnr) {
    this.factionnr = factionnr;
  }

  /**
   * @return the group
   */
  public String getGroup() {
    return group;
  }

  /**
   * @param group the group to set
   */
  public void setGroup(String group) {
    this.group = group;
  }

  /**
   * @return the round
   */
  public int getRound() {
    return round;
  }

  /**
   * @param round the round to set
   */
  public void setRound(int round) {
    this.round = round;
  }

}
