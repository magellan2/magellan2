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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import magellan.library.Alliance;
import magellan.library.AllianceGroup;
import magellan.library.Battle;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.IntegerID;
import magellan.library.Message;
import magellan.library.TrustLevel;
import magellan.library.rules.Options;
import magellan.library.rules.Race;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.Resources;
import magellan.library.utils.TrustLevels;

/**
 * A class representing a faction in Eressea.
 */
public class MagellanFactionImpl extends MagellanUnitContainerImpl implements Faction {
  /*
   * Implementation note on trust levels: Trust levels have been introduced to replace the
   * inherently wrong concept of owner factions. This way there can for example be more than one
   * faction that can edit its units' orders. TL_DEFAULT must keep a value of 0, so this equivalent
   * to a faction without a specified trust level. Trust levels expressing an increased amount trust
   * or privileges should get ascending positive numbers, and the other way round with negative
   * trust levels. Therefore comparisons for trust levels should in most cases be 'greater than' or
   * 'less than' relations rather than absolute comparisons.
   */

  /**
   * The password of this faction required for authentication of orders sent to the Eressea server.
   */
  protected String password = null;

  /** The email address of the Faction */
  protected String email = null;

  /** Optionen */
  protected Options options = null;

  /** Punkte */
  protected int score = -1;

  /** Durchschnittlicher Punktestand */
  protected int averageScore = -1;

  /** Personen */
  protected int persons = -1;

  /** aktuelle Migranten */
  protected int migrants = -1;

  /** erlaubte Migranten */
  protected int maxMigrants = -1;

  /** Magiegebite */
  protected String spellSchool = null; // Magiegebiet

  /**
   * Indicates to what amount this faction can be trusted. It also influences the privileges of this
   * faction (e.g. being able to edit its units' orders).
   */
  private int trustLevel = TrustLevel.TL_DEFAULT;

  /**
   * Indicates, if one priviliged faction has set the "GIVE" right to this faction. used for showing
   * unit capacity only for items of such factions
   */
  protected boolean hasGiveAlliance = false;

  /**
   * taken from the cr: actual amount of heroes (Fiete)
   */
  protected int heroes = -1;

  /**
   * taken from the cr: actual max amount of heroes (Fiete)
   */
  protected int maxHeroes = -1;

  /**
   * taken from the cr: actual age of faction (Fiete)
   */
  protected int age = -1;

  /**
   * @see magellan.library.Faction#isPrivileged()
   */
  public boolean isPrivileged() {
    return TrustLevels.isPrivileged(this);
  }

  /**
   * true: indicates that this trustlevel was explicitly set by the user or read from a CR-file
   * false: indicates that this is either a default level or was calculated by Magellan based on the
   * alliances of the privileged factions.
   */
  public boolean trustLevelSetByUser = false;

  /** contains all messages for this faction as <tt>Message</tt> objects */
  protected List<Message> messages = null;

  /** contains error messages for this faction as <tt>String</tt> objects */
  protected List<String> errors = null;

  /** contains the battles, this faction had in the current round, as <tt>Battle</tt> objects */
  protected List<Battle> battles = null;

  /**
   * The allies of this faction are stored in this map with the faction ID of the ally as key and an
   * <tt>Alliance</tt> object as value.
   */
  protected Map<EntityID, Alliance> allies = null;

  /**
   * The different groups in this faction. The map contains <tt>ID</tt> objects with the group id as
   * keys and <tt>Group</tt> objects as values.
   */
  protected Map<IntegerID, Group> groups = CollectionFactory.createSyncOrderedMap(0);

  /** The country code indicating the locale for this faction. */
  private Locale locale = null;

  /** The treasury of this faction. If the value is zero this feature is not set. */
  protected int factionTreasury = 0;

  /**
   * Creates a new Faction object with the specified id on top of the specified game data object.
   */
  public MagellanFactionImpl(EntityID id, GameData data) {
    super(id, data);
  }

  /**
   * Assigns this faction a locale indicating the language of its report and the orders.
   */
  public void setLocale(Locale l) {
    locale = l;
  }

  /**
   * Returns the locale of this faction indicating the language of its report and orders.
   */
  public Locale getLocale() {
    return locale;
  }

  /** A faction dependent prefix to be prepended to this faction's race name. */
  private String raceNamePrefix = null;

  private AllianceGroup alliance;

  /**
   * Sets the faction dependent prefix for the race name.
   */
  public void setRaceNamePrefix(String prefix) {
    raceNamePrefix = prefix;
  }

  /**
   * Returns the faction dependent prefix for the race name.
   */
  public String getRaceNamePrefix() {
    return raceNamePrefix;
  }

  /**
   * Returns the race of this faction. This method is an alias for the getType() method.
   */
  public Race getRace() {
    return (Race) getType();
  }

  /**
   * Returns a string representation of this faction.
   */
  @Override
  public String toString() {
    // we could use getModifiedName here but it seems a bit obtrusive (and hard to handle tree
    // updates)
    String myName = getName();
    if (myName == null) {
      myName = Resources.get("crparser.unknownfaction", getID());
    }
    return myName + " (" + getID() + ")";
  }

  /**
   * Returns the value of age.
   * 
   * @return Returns age.
   */
  public int getAge() {
    return age;
  }

  /**
   * Sets the value of age.
   * 
   * @param age The value for age.
   */
  public void setAge(int age) {
    this.age = age;
  }

  /**
   * Returns the value of allies.
   * 
   * @return Returns allies.
   */
  public Map<EntityID, Alliance> getAllies() {
    return allies;
  }

  /**
   * Sets the value of allies.
   * 
   * @param allies The value for allies.
   */
  public void setAllies(Map<EntityID, Alliance> allies) {
    this.allies = allies;
  }

  /**
   * @see magellan.library.Faction#getAlliance()
   */
  public AllianceGroup getAlliance() {
    return alliance;
  }

  /**
   * @see magellan.library.Faction#setAlliance(magellan.library.AllianceGroup)
   */
  public void setAlliance(AllianceGroup alliance) {
    this.alliance = alliance;
  }

  /**
   * Returns the value of averageScore.
   * 
   * @return Returns averageScore.
   */
  public int getAverageScore() {
    return averageScore;
  }

  /**
   * Sets the value of averageScore.
   * 
   * @param averageScore The value for averageScore.
   */
  public void setAverageScore(int averageScore) {
    this.averageScore = averageScore;
  }

  /**
   * Returns the value of battles.
   * 
   * @return Returns battles.
   */
  public List<Battle> getBattles() {
    return battles;
  }

  /**
   * Sets the value of battles.
   * 
   * @param battles The value for battles.
   */
  public void setBattles(List<Battle> battles) {
    this.battles = battles;
  }

  /**
   * Returns the value of email.
   * 
   * @return Returns email.
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the value of email.
   * 
   * @param email The value for email.
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the value of errors.
   * 
   * @return Returns errors.
   */
  public List<String> getErrors() {
    return errors;
  }

  /**
   * Sets the value of errors.
   * 
   * @param errors The value for errors.
   */
  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  /**
   * Returns the value of groups.
   * 
   * @return Returns groups.
   */
  public Map<IntegerID, Group> getGroups() {
    return groups;
  }

  /**
   * Sets the value of groups.
   * 
   * @param groups The value for groups.
   */
  public void setGroups(Map<IntegerID, Group> groups) {
    this.groups = groups;
  }

  /**
   * Returns the value of hasGiveAlliance.
   * 
   * @return Returns hasGiveAlliance.
   */
  public boolean isHasGiveAlliance() {
    return hasGiveAlliance;
  }

  /**
   * Returns the value of hasGiveAlliance.
   * 
   * @return Returns hasGiveAlliance.
   */
  public boolean hasGiveAlliance() {
    return hasGiveAlliance;
  }

  /**
   * Sets the value of hasGiveAlliance.
   * 
   * @param hasGiveAlliance The value for hasGiveAlliance.
   */
  public void setHasGiveAlliance(boolean hasGiveAlliance) {
    this.hasGiveAlliance = hasGiveAlliance;
  }

  /**
   * Returns the value of heroes.
   * 
   * @return Returns heroes.
   */
  public int getHeroes() {
    return heroes;
  }

  /**
   * Sets the value of heroes.
   * 
   * @param heroes The value for heroes.
   */
  public void setHeroes(int heroes) {
    this.heroes = heroes;
  }

  /**
   * Returns the value of maxHeroes.
   * 
   * @return Returns maxHeroes.
   */
  public int getMaxHeroes() {
    return maxHeroes;
  }

  /**
   * Sets the value of maxHeroes.
   * 
   * @param maxHeroes The value for maxHeroes.
   */
  public void setMaxHeroes(int maxHeroes) {
    this.maxHeroes = maxHeroes;
  }

  /**
   * Returns the value of maxMigrants.
   * 
   * @return Returns maxMigrants.
   */
  public int getMaxMigrants() {
    return maxMigrants;
  }

  /**
   * Sets the value of maxMigrants.
   * 
   * @param maxMigrants The value for maxMigrants.
   */
  public void setMaxMigrants(int maxMigrants) {
    this.maxMigrants = maxMigrants;
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
   * Returns the value of migrants.
   * 
   * @return Returns migrants.
   */
  public int getMigrants() {
    return migrants;
  }

  /**
   * Sets the value of migrants.
   * 
   * @param migrants The value for migrants.
   */
  public void setMigrants(int migrants) {
    this.migrants = migrants;
  }

  /**
   * Returns the value of options.
   * 
   * @return Returns options.
   */
  public Options getOptions() {
    return options;
  }

  /**
   * Sets the value of options.
   * 
   * @param options The value for options.
   */
  public void setOptions(Options options) {
    this.options = options;
  }

  /**
   * Returns the value of password.
   * 
   * @return Returns password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the value of password.
   * 
   * @param password The value for password.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Returns the value of persons.
   * 
   * @return Returns persons.
   */
  public int getPersons() {
    return persons;
  }

  /**
   * Sets the value of persons.
   * 
   * @param persons The value for persons.
   */
  public void setPersons(int persons) {
    this.persons = persons;
  }

  /**
   * Returns the value of score.
   * 
   * @return Returns score.
   */
  public int getScore() {
    return score;
  }

  /**
   * Sets the value of score.
   * 
   * @param score The value for score.
   */
  public void setScore(int score) {
    this.score = score;
  }

  /**
   * Returns the value of spellSchool.
   * 
   * @return Returns spellSchool.
   */
  public String getSpellSchool() {
    return spellSchool;
  }

  /**
   * Sets the value of spellSchool.
   * 
   * @param spellSchool The value for spellSchool.
   */
  public void setSpellSchool(String spellSchool) {
    this.spellSchool = spellSchool;
  }

  /**
   * Returns the value of trustLevel.
   * 
   * @return Returns trustLevel.
   */
  public int getTrustLevel() {
    return trustLevel;
  }

  /**
   * Sets the value of trustLevel.
   * 
   * @param trustLevel The value for trustLevel.
   */
  public void setTrustLevel(int trustLevel) {
    this.trustLevel = trustLevel;
  }

  /**
   * Returns the value of trustLevelSetByUser.
   * 
   * @return Returns trustLevelSetByUser.
   */
  public boolean isTrustLevelSetByUser() {
    return trustLevelSetByUser;
  }

  /**
   * Sets the value of trustLevelSetByUser.
   * 
   * @param trustLevelSetByUser The value for trustLevelSetByUser.
   */
  public void setTrustLevelSetByUser(boolean trustLevelSetByUser) {
    this.trustLevelSetByUser = trustLevelSetByUser;
  }

  /**
   * @see magellan.library.Faction#getTreasury()
   */
  public int getTreasury() {
    return factionTreasury;
  }

  /**
   * @see magellan.library.Faction#setTreasury(int)
   */
  public void setTreasury(int silver) {
    factionTreasury = silver;
  }

  /**
   * Returns the id uniquely identifying this object.
   */
  @Override
  public EntityID getID() {
    return (EntityID) super.getID();
  }

}
