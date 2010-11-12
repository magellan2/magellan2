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

import magellan.library.GameData;
import magellan.library.completion.Completer;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.OrderParser;
import magellan.library.io.GameDataIO;
import magellan.library.utils.UserInterface;
import magellan.library.utils.transformation.ReportTransformer;

/**
 * This utility class provides game specific methods for certain properties of the game.
 */
public interface GameSpecificStuff {
  /**
   * Return a GameData object for the specified game
   * 
   * @param name The game name (like "Eressea", "E3", ...)
   * @return A new, empty GameData object
   */
  public GameData createGameData(String name);

  /**
   * DOCUMENT-ME Warning: Not implemented
   * 
   * @return A game data reader for this game
   * @deprecated this somehow never got used (or implemented)
   */
  @Deprecated
  public GameDataIO getGameDataIO();

  /**
   * Processes a GameData object augmenting objects with additional information from messages,
   * simplifying the GameData structure, creating temp units as distinct objects etc. Note that this
   * method requires the classes Locales and Translations to be set up properly so the order
   * translations can be found.
   * 
   * @param data the GameData object to process.
   */
  public void postProcess(GameData data);

  /**
   * For TrustLevels. TODO DOCUMENT ME! Long description.
   * 
   * @param data
   */
  public void postProcessAfterTrustlevelChange(GameData data);

  // for Unit

  /**
   * Delivers a game specific RelationFactory
   */
  public RelationFactory getRelationFactory();

  /**
   * Delivers a game specific MovementEvaluator
   */
  public MovementEvaluator getMovementEvaluator();

  /**
   * Delivers a game specific OrderChanger
   */
  public OrderChanger getOrderChanger();

  /**
   * Delivers a game specific OrderParser
   */
  public OrderParser getOrderParser(GameData data);

  /**
   * Delivers a game specific Completer
   */
  public Completer getCompleter(GameData data, CompleterSettingsProvider csp);

  /**
   * Delivers a game specific Message Renderer
   * 
   * @param data - A GameData object to enrich the messages with names of units, regions ,...
   * @return the MessageRenderer for rendering ONE Message
   */
  public MessageRenderer getMessageRenderer(GameData data);

  /**
   * 
   */
  public MapMergeEvaluator getMapMergeEvaluator();

  /**
   * Returns a game specific order writer. This writer provides some additional informations for a
   * game specific order.
   */
  public GameSpecificOrderWriter getOrderWriter();

  /**
   * Returns a game specific rule manager.
   */
  public GameSpecificRules getGameSpecificRules();

  /**
   * Returns the name of the game this stuff belongs to.
   */
  public String getName();

  /**
   * DOCUMENT-ME
   * 
   * @param globalData
   * @param addedData
   * @param ui
   * @param interactive
   * @return
   */
  public ReportTransformer[] getTransformers(GameData globalData, GameData addedData,
      UserInterface ui, boolean interactive);
}
