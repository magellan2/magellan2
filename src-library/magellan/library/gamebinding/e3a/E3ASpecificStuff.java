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

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import magellan.client.swing.map.CellGeometry;
import magellan.library.CompleteData;
import magellan.library.GameData;
import magellan.library.Rules;
import magellan.library.completion.Completer;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.GameSpecificOrderWriter;
import magellan.library.gamebinding.GameSpecificRules;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.gamebinding.MapMergeEvaluator;
import magellan.library.gamebinding.MessageRenderer;
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.gamebinding.OrderChanger;
import magellan.library.gamebinding.RelationFactory;
import magellan.library.io.GameDataIO;
import magellan.library.io.RulesReader;
import magellan.library.utils.UserInterface;
import magellan.library.utils.transformation.ReportTransformer;
import magellan.library.utils.transformation.TransformerFinder;

/**
 * All the stuff needed for E2K9.
 * 
 * @author $Author: $
 * @version $Revision: 242 $
 */
public class E3ASpecificStuff implements GameSpecificStuff {
  private static final String name = "E3";

  private Rules rules;
  private MovementEvaluator movementEvaluator;
  private GameSpecificRules gameSpecificRules;
  private MapMergeEvaluator mapMergeEvaluator;

  private RelationFactory relationFactory;

  private E3AOrderChanger orderChanger;

  private static final SortedMap<Integer, String> combatStates = new TreeMap<Integer, String>();

  static {
    combatStates.put(0, "unit.combatstatus.aggressive");
    combatStates.put(1, "unit.combatstatus.front");
    combatStates.put(2, "unit.combatstatus.back");
    combatStates.put(3, "unit.combatstatus.defensive");
    combatStates.put(4, "unit.combatstatus.passive");
    combatStates.put(5, "unit.combatstatus.escape");
  }

  /**
   * Returns the value of rules.
   * 
   * @return Returns rules.
   */
  public Rules getRules() {
    return rules;
  }

  public E3ASpecificStuff() {
    rules = new RulesReader().readRules(getName());
  }

  /**
   * This is a callback interface to let the GameSpecificStuff create the GameData object.
   * 
   * @param name The game name (like "Eressea", "E3", ...)
   */
  public GameData createGameData(String name) {
    return new CompleteData(getRules(), name);
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getGameDataIO()
   */
  public GameDataIO getGameDataIO() {
    return null;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#postProcess(magellan.library.GameData)
   */
  public void postProcess(GameData data) {
    E3APostProcessor.getSingleton().postProcess(data);
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#postProcessAfterTrustlevelChange(magellan.library.GameData)
   */
  public void postProcessAfterTrustlevelChange(GameData data) {
    E3APostProcessor.getSingleton().postProcessAfterTrustlevelChange(data);
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getOrderChanger()
   */
  public OrderChanger getOrderChanger() {
    if (orderChanger == null) {
      orderChanger = new E3AOrderChanger(getRules());
    }
    return orderChanger;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getRelationFactory()
   */
  public RelationFactory getRelationFactory() {
    if (relationFactory == null) {
      relationFactory = new E3ARelationFactory(getRules());
    }

    return relationFactory;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getMovementEvaluator()
   */
  public MovementEvaluator getMovementEvaluator() {
    if (movementEvaluator == null) {
      movementEvaluator = new E3AMovementEvaluator(rules);
    }
    return movementEvaluator;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getCompleter(magellan.library.GameData,
   *      magellan.library.completion.CompleterSettingsProvider)
   */
  public Completer getCompleter(GameData data, CompleterSettingsProvider csp) {
    return new E3AOrderCompleter(data, csp);
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getOrderParser(magellan.library.GameData)
   */
  public OrderParser getOrderParser(GameData data) {
    return new E3AOrderParser(data);
  }

  /**
   * Delivers the Eressea specific Message Renderer (as of CR VERSION 41)
   * 
   * @param data - A GameData object to enrich the messages with names of units, regions ,...
   * @return the new EresseaMessageRenderer for rendering ONE message
   * @see magellan.library.gamebinding.GameSpecificStuff#getMessageRenderer(magellan.library.GameData)
   */
  public MessageRenderer getMessageRenderer(GameData data) {
    return new E3AMessageRenderer(data);
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getMapMergeEvaluator()
   */
  public MapMergeEvaluator getMapMergeEvaluator() {
    if (mapMergeEvaluator == null) {
      mapMergeEvaluator = new E3AMapMergeEvaluator(rules);
    }

    return mapMergeEvaluator;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getOrderWriter()
   */
  public GameSpecificOrderWriter getOrderWriter() {
    return E3AOrderWriter.getSingleton();
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getGameSpecificRules()
   */
  public GameSpecificRules getGameSpecificRules() {
    if (gameSpecificRules == null) {
      gameSpecificRules = new E3AGameSpecificRules(rules);
    }
    return gameSpecificRules;
  }

  public String getName() {
    return E3ASpecificStuff.name;
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getTransformers(magellan.library.GameData,
   *      magellan.library.GameData, magellan.library.utils.UserInterface, boolean)
   */
  public ReportTransformer[] getTransformers(GameData globalData, GameData addedData,
      UserInterface ui, boolean interactive) {

    return (new TransformerFinder(globalData, addedData, ui, interactive, true)).getTransformers();
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getCombatStates()
   */
  public Map<Integer, String> getCombatStates() {
    return Collections.unmodifiableSortedMap(combatStates);
  }

  public CoordMapper getCoordMapper(CellGeometry cellGeometry) {
    return GameSpecificStuff.ERESSEA_MAPPER;
  }
}
