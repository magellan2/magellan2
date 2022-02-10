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

package magellan.client.utils;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import magellan.client.Macifier;
import magellan.client.swing.InternationalizedDialog;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 *
 */
public class TextAreaDialog extends InternationalizedDialog implements HyperlinkListener {
  private static final Logger log = Logger.getInstance(TextAreaDialog.class);
  private JPanel jPanel;
  private JButton btn_OK;
  private JEditorPane jTextArea1;
  private JScrollPane scrollPane;

  /**
   * Creates a new InfoDlg object.
   * 
   * @param parent modally stucked frame.
   * @param title
   * @param text
   */
  public TextAreaDialog(Dialog parent, String title, String text) {
    super(parent, true);
    initComponents(title, text);

    SwingUtils.center(this);
  }

  /**
   * Creates a new InfoDlg object.
   * 
   * @param parent modally stucked frame.
   * @param title
   * @param text
   */
  public TextAreaDialog(JFrame parent, String title, String text) {
    super(parent, true);
    initComponents(title, text);

    // SwingUtils.center(this);
    GraphicsConfiguration gc = getGraphicsConfiguration();
    Rectangle b = gc.getBounds();
    Toolkit tk = getToolkit();
    Dimension ss = getToolkit().getScreenSize();

    // center

    SwingUtils.center(this);
  }

  @Override
  public void setPreferredSize(Dimension preferredSize) {
    jTextArea1.setPreferredSize(preferredSize);
    // scrollPane.setPreferredSize(preferredSize);
    super.setPreferredSize(preferredSize);
  }

  /**
   * Sets the type of content that this editor handles.
   * 
   * @param type the non-<code>null</code> mime type for the content editing support, for example
   *          "text/html" or "text/plain" (the default).
   * @see JEditorPane#setContentType(String)
   * @throws NullPointerException if the <code>type</code> parameter is <code>null</code>
   */
  public void setContentType(String type) {
    jTextArea1.setContentType(type);
  }

  private void initComponents(String title, String text) {
    jPanel = new JPanel();
    jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

    setTitle(title);

    jTextArea1 = new JEditorPane();
    jTextArea1.setContentType("text/plain");
    jTextArea1.setEditable(false);
    // jTextArea1 = new JTextArea();
    // jTextArea1.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
    // jTextArea1.setWrapStyleWord(true);
    // jTextArea1.setLineWrap(true);
    // jTextArea1.setEditable(false);
    jTextArea1.setText(text);
    jTextArea1.setCaretPosition(0);
    jTextArea1.setPreferredSize(new Dimension(600, 400));
    jTextArea1.addHyperlinkListener(this);
    scrollPane = new JScrollPane(jTextArea1);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    // scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
    scrollPane.setPreferredSize(new Dimension(600, 400));
    jPanel.add(scrollPane);

    // OK Button
    btn_OK = new JButton(Resources.get("infodlg.btn.close.caption"));
    btn_OK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        quit();
      }
    });
    btn_OK.setAlignmentX(Component.CENTER_ALIGNMENT);
    jPanel.add(btn_OK);

    getContentPane().add(jPanel);

    pack();

    // close dialog on Enter or ESC
    KeyAdapter closeAdapter = new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
        case KeyEvent.VK_ENTER:
          quit();
          break;
        }
        super.keyTyped(e);
      }
    };
    jPanel.addKeyListener(closeAdapter);
    jTextArea1.addKeyListener(closeAdapter);
    scrollPane.addKeyListener(closeAdapter);
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        // Loads the new page represented by link clicked
        if (e.getURL() != null) {
          Macifier.browse(e.getURL().toURI());
        }
      } catch (Exception e1) {
        log.error(e1);
      }
    }
  }
}
