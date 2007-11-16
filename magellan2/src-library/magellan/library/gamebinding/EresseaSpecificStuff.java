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
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 242 $
 */
public class EresseaSpecificStuff implements GameSpecificStuff {
	/**
	 * This is a callback interface to let the  GameSpecificStuff create the GameData object.
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public GameData createGameData(Rules rules, String name) {
		return new CompleteData(rules, name);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public GameDataIO getGameDataIO() {
		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void postProcess(GameData data) {
		EresseaPostProcessor.getSingleton().postProcess(data);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void postProcessAfterTrustlevelChange(GameData data) {
		EresseaPostProcessor.getSingleton().postProcessAfterTrustlevelChange(data);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public OrderChanger getOrderChanger() {
		return EresseaOrderChanger.getSingleton();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public RelationFactory getRelationFactory() {
		return EresseaRelationFactory.getSingleton();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public MovementEvaluator getMovementEvaluator() {
		return EresseaMovementEvaluator.getSingleton();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public Completer getCompleter(GameData data, CompleterSettingsProvider csp) {
		return new EresseaOrderCompleter(data, csp);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public OrderParser getOrderParser(GameData data) {
		return new EresseaOrderParser(data);
	}
}
