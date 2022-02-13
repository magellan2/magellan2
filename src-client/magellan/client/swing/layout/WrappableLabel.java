// class magellan.client.preferences.WrappableLable
// created on Jul 13, 2021
//
// Copyright 2003-2021 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.client.swing.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIDefaults;

/**
 * Provides a component that looks like a label, but behaves like a TextPane, so that it can wrap lines.
 *
 * @author stm
 * @version 1.0, Jul 14, 2021
 */
public class WrappableLabel extends JPanel {

  /**
   * Factory method.
   * 
   * @param text
   * @return A label that represents the given text
   */
  public static WrappableLabel getLabel(String text) {
    return new WrappableLabel(text);
  }

  private JTextArea label;

  // private WrappableLabel(String text) {
  // super(new BorderLayout());
  // JComponent comment = new JLabel(
  // String.format("<html><body style=\"text-align: left; text-justify: inter-word;\">%s</body></html>",
  // text));
  //
  // comment.setPreferredSize(new Dimension(300, 100));
  // add(comment, BorderLayout.CENTER);
  // // setMinimumSize(new Dimension(300, 100));
  // }

  private WrappableLabel(String text) {
    super();
    setLayout(new BorderLayout());
    label = new JTextArea(text);
    JScrollPane sPane = new JScrollPane(label);
    // label.setMinimumSize(new Dimension(200, 50));
    add(sPane, BorderLayout.CENTER);
    sPane.setViewportView(label);
    sPane.setBorder(null);
    label.setBorder(null);
    label.setMargin(new Insets(0, 0, 0, 0));

    // label.setPreferredSize(new Dimension(300, 30));
    // label.setRows(3);
    label.setColumns(3);

    label.setLineWrap(true);
    label.setWrapStyleWord(true);
    label.setEditable(false);
    label.setFocusable(false);
    JLabel parent = new JLabel();
    Color c = parent.getBackground();
    UIDefaults defaults = new UIDefaults();
    defaults.put("TextArea[Enabled].backgroundPainter", c);
    defaults.put("TextArea.contentMargins", new Insets(0, 0, 0, 0));
    defaults.put("ScrollPane.contentMargins", new Insets(0, 0, 0, 0));

    // defaults.put("TextArea[Enabled].borderPainter", null);
    // defaults.put("ScrollPane[Enabled].borderPainter", null);
    label.putClientProperty("Nimbus.Overrides", defaults);
    label.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
    label.setBackground(c);
    label.setSelectionColor(c);
    label.setSelectedTextColor(parent.getForeground());
    label.setFont(parent.getFont());
    // label.setBorder(null);
    putClientProperty("Nimbus.Overrides", defaults);
    putClientProperty("Nimbus.Overrides.InheritDefaults", true);
    setBackground(c);
    setFont(parent.getFont());
  }

  /**
   * Changes the represented text
   * 
   * @param t
   */
  public void setText(String t) {
    label.setText(t);
  }

}

//
// // public WrappableLabel(String string) {
// // super(String.format(
// // "<html><body style=\"text-align: justify; text-justify: inter-word;\">%s</body></html>",
// // string));
// // setPreferredSize(new Dimension(300, 30));
// //
// // }
//
// }
