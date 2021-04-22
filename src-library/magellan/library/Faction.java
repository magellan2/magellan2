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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import magellan.library.rules.Options;
import magellan.library.rules.Race;
import magellan.library.utils.TrustLevels;

/**
 * A class representing a faction in Eressea.
 */
public interface Faction extends UnitContainer {

  /**
   * @deprecated Use {@link TrustLevel#TL_DEFAULT}.
   */
  @Deprecated
  public static final int TL_DEFAULT = TrustLevel.TL_DEFAULT;

  /**
   * @deprecated Use {@link TrustLevel#TL_PRIVILEGED}.
   */
  @Deprecated
  public static final int TL_PRIVILEGED = 100;

  /**
   * Returns <code>true</code> iff this faction has trustlevel at least{@link TrustLevel#PRIVILEGED}
   * 
   * @deprecated Use {@link TrustLevels#isPrivileged(Faction)}
   */
  @Deprecated
  public boolean isPrivileged();

  /**
   * Assigns this faction a locale indicating the language of its report and the orders.
   */
  public void setLocale(Locale l);

  /**
   * Returns the locale of this faction indicating the language of its report and orders.
   * 
   * @return My return <code>null</code>.
   */
  public Locale getLocale();

  /**
   * Sets the faction dependent prefix for the race name.
   */
  public void setRaceNamePrefix(String prefix);

  /**
   * Returns the faction dependent prefix for the race name.
   * 
   * @return May return <code>null</code>
   */
  public String getRaceNamePrefix();

  /**
   * Returns the race of this faction. This method is an alias for the getType() method.
   */
  public Race getRace();

  /**
   * Returns a string representation of this faction.
   */
  public String toString();

  /**
   * Returns the value of age.
   * 
   * @return Returns age.
   */
  public int getAge();

  /**
   * Sets the value of age.
   * 
   * @param age The value for age.
   */
  public void setAge(int age);

  /**
   * Returns the value of allies (HELP statuses).
   * 
   * @return Returns allies. May return <code>null</code>
   */
  public Map<EntityID, Alliance> getAllies();

  /**
   * Sets the value of allies (HELP statuses).
   * 
   * @param allies The value for allies. May be <code>null</code>.
   */
  public void setAllies(Map<EntityID, Alliance> allies);

  /**
   * Returns the alliance this faction is a member of or <code>null</code> (E3 ALLIANCE order).
   */
  public AllianceGroup getAlliance();

  /**
   * Sets the alliance this faction is a member of (E3 ALLIANCE order).
   */
  public void setAlliance(AllianceGroup alliance);

  /**
   * Returns the value of averageScore.
   * 
   * @return Returns averageScore.
   */
  public int getAverageScore();

  /**
   * Sets the value of averageScore.
   * 
   * @param averageScore The value for averageScore.
   */
  public void setAverageScore(int averageScore);

  /**
   * Returns the value of battles.
   * 
   * @return Returns battles. May return <code>null</code>.
   */
  public List<Battle> getBattles();

  /**
   * Sets the value of battles.
   * 
   * @param battles The value for battles.
   */
  public void setBattles(List<Battle> battles);

  /**
   * Returns the value of email.
   * 
   * @return Returns email.
   */
  public String getEmail();

  /**
   * Sets the value of email.
   * 
   * @param email The value for email.
   */
  public void setEmail(String email);

  /**
   * Returns the value of errors.
   * 
   * @return Returns errors. May return <code>null</code>.
   */
  public List<String> getErrors();

  /**
   * Sets the value of errors.
   * 
   * @param errors The value for errors.
   */
  public void setErrors(List<String> errors);

  /**
   * Returns the value of groups.
   * 
   * @return Returns groups. May return <code>null</code>.
   */
  public Map<IntegerID, Group> getGroups();

  /**
   * Sets the value of groups.
   * 
   * @param groups The value for groups.
   */
  public void setGroups(Map<IntegerID, Group> groups);

  /**
   * Returns the value of hasGiveAlliance.
   * 
   * @return Returns hasGiveAlliance.
   */
  @Deprecated
  public boolean isHasGiveAlliance();

  /**
   * Sets the value of hasGiveAlliance.
   * 
   * @param hasGiveAlliance The value for hasGiveAlliance.
   */
  @Deprecated
  public void setHasGiveAlliance(boolean hasGiveAlliance);

  /**
   * Returns the value of heroes.
   * 
   * @return Returns heroes.
   */
  public int getHeroes();

  /**
   * Sets the value of heroes.
   * 
   * @param heroes The value for heroes.
   */
  public void setHeroes(int heroes);

  /**
   * Returns the value of maxHeroes.
   * 
   * @return Returns maxHeroes.
   */
  public int getMaxHeroes();

  /**
   * Sets the value of maxHeroes.
   * 
   * @param maxHeroes The value for maxHeroes.
   */
  public void setMaxHeroes(int maxHeroes);

  /**
   * Returns the value of maxMigrants.
   * 
   * @return Returns maxMigrants.
   */
  public int getMaxMigrants();

  /**
   * Sets the value of maxMigrants.
   * 
   * @param maxMigrants The value for maxMigrants.
   */
  public void setMaxMigrants(int maxMigrants);

  /**
   * Returns the value of messages.
   * 
   * @return Returns messages. May return <code>null</code>.
   */
  public List<Message> getMessages();

  /**
   * Sets the value of messages.
   * 
   * @param messages The value for messages.
   */
  public void setMessages(List<Message> messages);

  /**
   * Returns the value of migrants.
   * 
   * @return Returns migrants.
   */
  public int getMigrants();

  /**
   * Sets the value of migrants.
   * 
   * @param migrants The value for migrants.
   */
  public void setMigrants(int migrants);

  /**
   * Returns the value of options.
   * 
   * @return Returns options. May return <code>null</code>.
   */
  public Options getOptions();

  /**
   * Sets the value of options.
   * 
   * @param options The value for options.
   */
  public void setOptions(Options options);

  /**
   * Returns the value of password.
   * 
   * @return Returns password. Returns <code>null</code> if no password is known.
   */
  public String getPassword();

  /**
   * Sets the value of password.
   * 
   * @param password The value for password.
   */
  public void setPassword(String password);

  /**
   * Returns the value of persons.
   * 
   * @return Returns persons.
   */
  public int getPersons();

  /**
   * Sets the value of persons.
   * 
   * @param persons The value for persons.
   */
  public void setPersons(int persons);

  /**
   * Returns the value of score.
   * 
   * @return Returns score.
   */
  public int getScore();

  /**
   * Sets the value of score.
   * 
   * @param score The value for score.
   */
  public void setScore(int score);

  /**
   * Returns the value of spellSchool.
   * 
   * @return Returns spellSchool. May return <code>null</code>.
   */
  public String getSpellSchool();

  /**
   * Sets the value of spellSchool.
   * 
   * @param spellSchool The value for spellSchool.
   */
  public void setSpellSchool(String spellSchool);

  /**
   * Returns the value of trustLevel.
   * 
   * @return Returns trustLevel.
   */
  public int getTrustLevel();

  /**
   * Sets the value of trustLevel.
   * 
   * @param trustLevel The value for trustLevel.
   */
  public void setTrustLevel(int trustLevel);

  /**
   * Returns the value of trustLevelSetByUser.
   * 
   * @return Returns trustLevelSetByUser.
   */
  public boolean isTrustLevelSetByUser();

  /**
   * Sets the value of trustLevelSetByUser.
   * 
   * @param trustLevelSetByUser The value for trustLevelSetByUser.
   */
  public void setTrustLevelSetByUser(boolean trustLevelSetByUser);

  /**
   * Indicates, if one priviliged faction has set the "GIVE" right to this faction. used for showing
   * unit capacity only for items of such factions
   */
  public boolean hasGiveAlliance();

  /**
   * Sets the faction treasury for this faction. If the value is negative or zero, this attribute is
   * disabled (default).
   */
  public void setTreasury(int silver);

  /**
   * Returns the faction treasury for this faction. This value can be negative - that means this
   * attribute is disabled (default).
   */
  public int getTreasury();

  /**
   * Returns the id uniquely identifying this object.
   */
  public EntityID getID();

}
