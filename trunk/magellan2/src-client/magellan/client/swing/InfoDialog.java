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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import magellan.client.Client;
import magellan.client.utils.SwingUtils;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.MagellanUrl;
import magellan.library.utils.Resources;
import magellan.library.utils.VersionInfo;

/**
 *
 */
public class InfoDialog extends InternationalizedDialog implements HyperlinkListener {
  private JPanel jPanel;
  private JButton btn_OK;
  private JLabel magellanImage;
  private JEditorPane jTextArea1;

  /**
   * Creates a new InfoDlg object.
   *
   * @param parent modally stucked frame.
   */
  public InfoDialog(JFrame parent) {
    super(parent, true);
    initComponents();

    SwingUtils.center(this);
  }

  private void initComponents() {
    jPanel = new JPanel();
    jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

    setModal(true);
    setTitle(Resources.get("infodlg.window.title"));
    Icon icon = MagellanImages.ABOUT_MAGELLAN;

    magellanImage = new JLabel();
    magellanImage.setIcon(icon);
    magellanImage.setText("");
    magellanImage.setAlignmentX(Component.CENTER_ALIGNMENT);
    JPanel filler = new JPanel();
    filler.add(magellanImage);
    filler.setBackground(MagellanImages.BACKGROUND);
    jPanel.add(filler);
    String text =
        Resources.get("infodlg.infotext", getVersionString(), MagellanUrl.getRootUrl(), MagellanUrl
            .getMagellanUrl("www.fernando"), MagellanUrl.getMagellanUrl(MagellanUrl.WWW_BUGS),
            Client.getLogFile().toURI().toASCIIString(), Client.getLogFile().getName());

    jTextArea1 = new JEditorPane();
    jTextArea1.setContentType("text/html");
    jTextArea1.setEditable(false);
    // jTextArea1 = new JTextArea();
    // jTextArea1.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
    // jTextArea1.setWrapStyleWord(true);
    // jTextArea1.setLineWrap(true);
    // jTextArea1.setEditable(false);
    jTextArea1.setText(text);
    jTextArea1.setCaretPosition(0);
    jTextArea1.setPreferredSize(new Dimension(400, 400));
    jTextArea1.addHyperlinkListener(this);

    JScrollPane scrollPane = new JScrollPane(jTextArea1);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    // scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
    scrollPane.setPreferredSize(new Dimension(400, 400));
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

  }

  private String getVersionString() {
    String versionInfo = VersionInfo.getVersion(null);
    if (versionInfo == null) {
      versionInfo = "not available";
    }
    return Resources.get("infodlg.infotext.version", versionInfo) + "\n";
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        // Loads the new page represented by link clicked
        URI uri = e.getURL().toURI();

        // only in Java6 available, so we try to load it.
        // otherwise, we do nothing...
        Class<?> c = Class.forName("java.awt.Desktop");
        if (c != null) {
          Object desktop = c.getMethod("getDesktop").invoke(null);
          c.getMethod("browse", java.net.URI.class).invoke(desktop, uri);
        }
      } catch (Exception exc) {
        // we do nothing here...
      }
    }

  }

}
