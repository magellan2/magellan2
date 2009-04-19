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

import magellan.library.CompleteData;
import magellan.library.GameData;
import magellan.library.Rules;
import magellan.library.completion.Completer;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.OrderParser;
import magellan.library.io.GameDataIO;


/**
 * All the stuff needed for Eressea.
 *
 * @author $Author: $
 * @version $Revision: 242 $
 */
public class EresseaSpecificStuff implements GameSpecificStuff {
	/**
	 * This is a callback interface to let the  GameSpecificStuff create the GameData object.
	 */
	public GameData createGameData(Rules rules, String name) {
		return new CompleteData(rules, name);
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
		EresseaPostProcessor.getSingleton().postProcess(data);
	}

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#postProcessAfterTrustlevelChange(magellan.library.GameData)
   */
	public void postProcessAfterTrustlevelChange(GameData data) {
		EresseaPostProcessor.getSingleton().postProcessAfterTrustlevelChange(data);
	}

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getOrderChanger()
   */
	public OrderChanger getOrderChanger() {
		return EresseaOrderChanger.getSingleton();
	}

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getRelationFactory()
   */
	public RelationFactory getRelationFactory() {
		return EresseaRelationFactory.getSingleton();
	}

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getMovementEvaluator()
   */
	public MovementEvaluator getMovementEvaluator() {
		return EresseaMovementEvaluator.getSingleton();
	}

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getCompleter(magellan.library.GameData, magellan.library.completion.CompleterSettingsProvider)
   */
  public Completer getCompleter(GameData data, CompleterSettingsProvider csp) {
		return new EresseaOrderCompleter(data, csp);
	}

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getOrderParser(magellan.library.GameData)
   */
	public OrderParser getOrderParser(GameData data) {
		return new EresseaOrderParser(data);
	}
  
  /**
   * Delivers the Eressea specific Message Renderer (as of CR VERSION 41)
   * @param data - A GameData object to enrich the messages with names of units, regions ,...
   * @return the new EresseaMessageRenderer for rendering ONE message 
   * 
   * @see magellan.library.gamebinding.GameSpecificStuff#getMessageRenderer(magellan.library.GameData)
   */
  public MessageRenderer getMessageRenderer(GameData data) {
    return new EresseaMessageRenderer(data);
  }
  
  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getMapMergeEvaluator()
   */
  public MapMergeEvaluator getMapMergeEvaluator() {
    return EresseaMapMergeEvaluator.getSingleton();
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getOrderWriter()
   */
  public GameSpecificOrderWriter getOrderWriter() {
    return EresseaOrderWriter.getSingleton();
  }

  /**
   * @see magellan.library.gamebinding.GameSpecificStuff#getGameSpecificRules()
   */
  public GameSpecificRules getGameSpecificRules() {
    return EresseaGameSpecificRules.getInstance();
  }
}
