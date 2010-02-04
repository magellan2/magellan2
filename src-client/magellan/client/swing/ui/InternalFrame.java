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

package magellan.client.swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import magellan.client.utils.SimpleInternalFrame;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class InternalFrame extends JPanel {
  private SimpleInternalFrame sif;
  private Component content;

  /**
   * Creates a new InternalFrame object.
   */
  public InternalFrame() {
    this("");
  }

  /**
   * Creates a new InternalFrame object.
   */
  public InternalFrame(String title) {
    this(title, null);
  }

  /**
   * Creates a new InternalFrame object.
   */
  public InternalFrame(String title, Component content) {
    super(new BorderLayout());
    // sif = new SimpleInternalFrame(title);
    // add(sif);

    if (content != null) {
      setContent(content);
    }
  }

  /**
   * DOCUMENT-ME
   */
  public void setContent(Component content) {
    if (content != null) {
      if (sif == null) {
        if (this.content != null) {
          remove(this.content);
        }
        this.content = content;
        add(content);
        setBorder(BorderFactory.createLineBorder(Color.gray));
        // setBorder(BorderFactory.createEtchedBorder());
      } else {
        setBorder(BorderFactory.createEmptyBorder());
        sif.setContent(content);
      }
    }
  }

  /**
   * DOCUMENT-ME
   */
  public Component getContent() {
    return sif.getContent();
  }

  /**
   * DOCUMENT-ME
   */
  public void setTitle(String title) {
    sif.setTitle(title);
  }
}
