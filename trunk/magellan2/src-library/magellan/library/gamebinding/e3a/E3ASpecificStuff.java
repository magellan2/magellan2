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

import java.io.IOException;
import java.util.Map;

import magellan.library.GameData;
import magellan.library.completion.Completer;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.GameSpecificOrderReader;
import magellan.library.gamebinding.GameSpecificOrderWriter;
import magellan.library.gamebinding.GameSpecificRules;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.gamebinding.MapMergeEvaluator;
import magellan.library.gamebinding.MapMetric;
import magellan.library.gamebinding.MessageRenderer;
import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.gamebinding.OrderChanger;
import magellan.library.gamebinding.RelationFactory;
import magellan.library.io.GameDataIO;
import magellan.library.io.ReportParser;
import magellan.library.io.file.FileType;
import magellan.library.utils.UserInterface;
import magellan.library.utils.transformation.ReportTransformer;

/**
 * All the stuff needed for E3.
 */
public class E3ASpecificStuff implements GameSpecificStuff {

  E3ASpecificStuffProvider e3Provider;

  public E3ASpecificStuff() {
    this("E3");
  }

  public E3ASpecificStuff(String name) {
    e3Provider = new E3ASpecificStuffProvider(name);
  }

  public GameData createGameData(String name) {
    return e3Provider.createGameData(name);
  }

  public GameDataIO getGameDataIO() {
    return e3Provider.getGameDataIO();
  }

  public void postProcess(GameData data) {
    e3Provider.postProcess(data);
  }

  public void postProcessAfterTrustlevelChange(GameData data) {
    e3Provider.postProcessAfterTrustlevelChange(data);
  }

  public RelationFactory getRelationFactory() {
    return e3Provider.getRelationFactory();
  }

  public MovementEvaluator getMovementEvaluator() {
    return e3Provider.getMovementEvaluator();
  }

  public OrderChanger getOrderChanger() {
    return e3Provider.getOrderChanger();
  }

  public OrderParser getOrderParser(GameData data) {
    return e3Provider.getOrderParser(data);
  }

  public Completer getCompleter(GameData data, CompleterSettingsProvider csp) {
    return e3Provider.getCompleter(data, csp);
  }

  public MessageRenderer getMessageRenderer(GameData data) {
    return e3Provider.getMessageRenderer(data);
  }

  public MapMergeEvaluator getMapMergeEvaluator() {
    return e3Provider.getMapMergeEvaluator();
  }

  public GameSpecificOrderWriter getOrderWriter() {
    return e3Provider.getOrderWriter();
  }

  public GameSpecificRules getGameSpecificRules() {
    return e3Provider.getGameSpecificRules();
  }

  public String getName() {
    return e3Provider.getName();
  }

  public ReportTransformer[] getTransformers(GameData globalData, GameData addedData, UserInterface ui,
      boolean interactive) {
    return e3Provider.getTransformers(globalData, addedData, ui, interactive);
  }

  public Map<Integer, String> getCombatStates() {
    return e3Provider.getCombatStates();
  }

  public CoordMapper getCoordMapper() {
    return e3Provider.getCoordMapper();
  }

  public ReportParser getParser(FileType aFileType) throws IOException {
    return e3Provider.getParser(aFileType);
  }

  public GameSpecificOrderReader getOrderReader(GameData data) {
    return e3Provider.getOrderReader(data);
  }

  public MapMetric getMapMetric() {
    return e3Provider.getMapMetric();
  }
}
