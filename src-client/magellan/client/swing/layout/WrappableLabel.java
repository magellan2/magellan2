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
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIDefaults;

import io.github.parubok.text.multiline.MultilineLabel;
import magellan.client.utils.SwingUtils;

/**
 * Provides a component that looks like a label, but behaves like a TextPane, so that it can wrap lines.
 *
 * @author stm
 * @version 1.0, Jul 14, 2021
 */
public class WrappableLabel {
  // so we have three implementations here and I'm not quite happy with any of them. They are all kind of imperfect in
  // some way and sometimes ugly.

  private static final int TEXTAREA_IMPL = 0;
  private static final int MULTILINE_IMPL = 1;
  private static final int HTML_IMPL = 2;
  private static int IMPL = TEXTAREA_IMPL;

  /**
   * Factory method.
   * 
   * @param text
   * @return A label that represents the given text
   */
  public static WrappableLabel getLabel(String text) {
    return new WrappableLabel(text, IMPL);
  }

  private WrappableLabel(String text, int mode) {
    if (mode == HTML_IMPL) {
      initHtml(text);
    } else if (mode == TEXTAREA_IMPL) {
      initTextArea(text);
    } else if (mode == MULTILINE_IMPL) {
      initMultiline(text);
    } else
      throw new Error("implementation error");// initTextarea(text);
  }

  private JTextArea label;

  private MultilineLabel textComponent;

  private JLabel comment;
  private JComponent panel;

  private void initHtml(String text) {
    JPanel pp = new JPanel(new BorderLayout());
    panel = pp;
    comment = new JLabel(
        String.format("<html><body style=\"text-align: left; text-justify: inter-word;\">%s</body></html>",
            text));
    SwingUtils.setPreferredSize(comment, 24, 8, false);
    pp.add(comment, BorderLayout.CENTER);
    // setMinimumSize(new Dimension(300, 100));
  }

  private void initMultiline(String text) {
    textComponent = new MultilineLabel(text);
    textComponent.setPreferredWidthLimit(300);
  }

  void initTextArea(String text) {
    label = new JTextArea(text);

    JScrollPane sPane = new JScrollPane(label) {
      @Override
      public Dimension getMinimumSize() {
        Dimension dim = label.getMinimumSize();
        dim.height += 10;
        return dim;
      }
    };
    // label.setMinimumSize(new Dimension(200, 50));
    panel = sPane;
    // panel.add(sPane, BorderLayout.CENTER);
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
    panel.putClientProperty("Nimbus.Overrides", defaults);
    panel.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
    panel.setBackground(c);
    panel.setFont(parent.getFont());
  }

  /**
   * Changes the represented text
   * 
   * @param t
   */
  public void setText(String t) {
    if (t == null) {
      t = "";
    }
    if (label != null) {
      label.setText(t);
    } else if (textComponent != null) {
      textComponent.setText(t);
    } else if (comment != null) {
      comment.setText(String.format(
          "<html><body style=\"text-align: left; text-justify: inter-word;\">%s</body></html>", t));
    }
  }

  public JComponent getComponent() {
    if (textComponent != null)
      return textComponent;
    else
      return panel;
  }

  public void setPreferredWidth(int w) {
    if (textComponent != null) {
      textComponent.setPreferredWidthLimit(w);
    }
    // TODO
  }

  public void setPreferredLines(int c) {
    if (textComponent != null) {
      textComponent.setPreferredViewportLineCount(c);
    }
    // TODO
  }

}
