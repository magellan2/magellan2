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

package magellan.client.swing;

import java.awt.Frame;

import magellan.library.gamebinding.MapMetric;
import magellan.library.utils.transformation.BoxTransformer.BBox;

/**
 * Dialog for asking about world dimensions.
 * 
 * @author stm
 * @deprecated moved to magellan.library.utils.SetGirthDialog (temporarily?)
 */
@Deprecated
public class SetGirthDialog extends magellan.library.utils.SetGirthDialog {

  public SetGirthDialog(Frame parent, BBox idBox, Integer level, MapMetric metric) {
    super(parent, idBox, level, metric);
  }
}
