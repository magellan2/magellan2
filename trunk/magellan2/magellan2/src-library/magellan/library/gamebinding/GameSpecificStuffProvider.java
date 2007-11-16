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

import magellan.library.utils.logging.Logger;


/**
 * This class maps game names to <code>GameSpecicifStuff</code> objects
 *
 * @author $Author: $
 * @version $Revision: 315 $
 */
public class GameSpecificStuffProvider {
	private static final Logger log = Logger.getInstance(GameSpecificStuffProvider.class);

	/**
	 * Returns the GameSpecificStuff object for the given class name
	 *
	 * @param className the classname of the game to load
	 *
	 * @return a GameSpecificStuff object based on the given game name
	 */
	public GameSpecificStuff getGameSpecificStuff(String className) {
		GameSpecificStuff gameSpecificStuff = loadGameSpecificStuff(className);

		if((className == null) || (gameSpecificStuff == null)) {
			gameSpecificStuff = new EresseaSpecificStuff();
			// if classname is not provided..no warning is needed
			if (className != null){
				log.warn("Unable to determine GameSpecificStuff (class: " + className + ") . Falling back to EresseaSpecificStuff.");
			}
		}

		return gameSpecificStuff;
	}

	private GameSpecificStuff loadGameSpecificStuff(String className) {
		if(className == null) {
			return null;
		}

		try {
			// TODO: perhaps use ResourcePathClassLoader instead?
			Class clazz = Class.forName(className);
			Object result = clazz.newInstance();

			if(result instanceof GameSpecificStuff) {
				return (GameSpecificStuff) result;
			}
		} catch(ClassNotFoundException e) {
			log.warn("Class '" + className + "' not found.", e);
		} catch(Exception e) {
			// IllegalAccessException - if the class or its nullary constructor is not accessible. 
			// InstantiationException - if this Class represents an abstract class, an interface, an array class, a primitive type, or void; or if the class has no nullary constructor; or if the instantiation fails for some other reason. 
			// ExceptionInInitializerError - if the initialization provoked by this method fails. 
			// SecurityException - if there is no permission to create a new
			log.warn("Class '" + className + "' cannot be instantiated.", e);
		}

		return null;
	}
}
