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

import java.io.IOException;
import java.util.Map;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.completion.Completer;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.OrderParser;
import magellan.library.io.GameDataIO;
import magellan.library.io.ReportParser;
import magellan.library.io.file.FileType;
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
   * Returns a pair of transformers that transform coordinates from the global data and the added
   * data to coordinates in the merged report.
   * 
   * @param globalData
   * @param addedData
   * @param ui
   * @param interactive if <code>true</code>, the user may be asked question about the selection of
   *          the correct transformer. Otherwise it is tried to determine the correct transformer
   *          automatically.
   * @return A pair of transformers, the first one for the original report, the second one for the
   *         added report. Or <code>null</code> if no good transformer can be found.
   */
  public ReportTransformer[] getTransformers(GameData globalData, GameData addedData,
      UserInterface ui, boolean interactive);

  /**
   * Returns a map of possible combat states.
   * 
   * @return A map whose keys are internal values used in {@link Unit#getCombatStatus()} and values
   *         are resource keys.
   */
  public Map<Integer, String> getCombatStates();

  /**
   * Return the mapper to adjust map grid cells.
   */
  public CoordMapper getCoordMapper();

  /**
   * Mapper to adjust map grid cells.
   */
  public interface CoordMapper {

    /** Returns the x axis offset for a cell with given x coordinate. */
    float getXX(int x);

    /** Returns the y axis offset for a cell with given x coordinate. */
    float getXY(int x);

    /** Returns the y axis offset for a cell with given y coordinate. */
    float getYY(int y);

    /** Returns the x axis offset for a cell with given y coordinate. */
    float getYX(int y);
  }

  /** The mapper that works for Eressea's coordinate system. */
  CoordMapper ERESSEA_MAPPER = new CoordMapper() {

    public float getYY(int y) {
      return (-1.0f * (y + 1.0f));
    }

    public float getXY(int x) {
      return .5f * x;
    }

    public float getXX(int x) {
      return x;
    }

    public float getYX(int y) {
      return 0;
    }
  };

  /**
   * Returns a reader for parsing reports of the given FileType.
   * 
   * @return The parser or <code>null</code> if no parser applies to the file type
   * @throws IOException if an I/O exception occurs
   */
  public ReportParser getParser(FileType aFileType) throws IOException;

  /**
   * Returns a reader for parsing order files
   * 
   * @return The parser or <code>null</code> if order reading is not supported.
   */
  public GameSpecificOrderReader getOrderReader(GameData data);

  public MapMetric getMapMetric();

}
