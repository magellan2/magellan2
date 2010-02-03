/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.utils;

import java.awt.Component;
import java.util.Collection;

import javax.swing.JOptionPane;

import magellan.client.swing.RoutingDialog;
import magellan.library.GameData;
import magellan.library.Unit;

/**
 * @author Andreas
 * @version 1.0
 * @deprecated Use {@link magellan.library.utils.UnitRoutePlanner}.
 */
@Deprecated
public class UnitRoutePlanner {

  /**
   * @deprecated {@link magellan.library.utils.UnitRoutePlanner#planUnitRoute(Unit, GameData, Component, Collection)}
   */
  @Deprecated
  public boolean planUnitRoute(Unit unit, GameData data, Component ui, Collection<Unit> otherUnits) {
    return (new magellan.library.utils.UnitRoutePlanner()).planUnitRoute(unit, data, ui,
        otherUnits, new RoutingDialog(JOptionPane.getFrameForComponent(ui), data, false));
  }

}
