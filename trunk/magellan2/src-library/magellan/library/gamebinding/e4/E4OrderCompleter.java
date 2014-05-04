/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding.e4;

import magellan.library.GameData;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.gamebinding.e3a.E3AOrderCompleter;

/**
 * 
 */
public class E4OrderCompleter extends E3AOrderCompleter {

  /**
   * Creates a new <tt>EresseaOrderCompleter</tt> taking context information from the specified
   * <tt>GameData</tt> object.
   * 
   * @param gd The <tt>GameData</tt> this completer uses as context.
   */
  public E4OrderCompleter(GameData gd, CompleterSettingsProvider ac) {
    super(gd, ac);
  }

}
