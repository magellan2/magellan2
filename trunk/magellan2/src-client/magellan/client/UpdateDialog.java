// class magellan.client.UpdateDialog
// created on Jan 21, 2010
//
// Copyright 2003-2010 by magellan project team
//
// $Author: stm$
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import magellan.client.swing.InternationalizedDialog;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.Resources;
import magellan.library.utils.VersionInfo;
import magellan.library.utils.logging.Logger;

/**
 * A Dialog for informing the user about new versions.
 * 
 * @author stm
 */
public class UpdateDialog extends InternationalizedDialog implements HyperlinkListener {
  private static final Logger log = Logger.getInstance(UpdateDialog.class);

  private final String lastVersion;
  private final String currentVersion;
  private boolean abort = false;
  @SuppressWarnings("unused")
  private final Client client;

  private JTextArea releaseText;

  private JEditorPane versionInfo;

  private JScrollPane releaseNotesPane;

  public UpdateDialog(Client c, String lastVersion, String currentVersion) {
    super(c, true);
    client = c;
    this.lastVersion = lastVersion;
    this.currentVersion = currentVersion;

    initGUI();

    setText();

    // center
    this.setLocation((getToolkit().getScreenSize().width - getWidth()) / 2, (getToolkit()
        .getScreenSize().height - getHeight()) / 2);
  }

  private void setText() {
    // Text area
    final StringBuilder text = new StringBuilder(Resources.get("updatedialog.htmlheader"));
    if (currentVersion == null || currentVersion.equals("null")) {
      // could not determine current version!
      text.append(Resources
          .get("updatedialog.noversionwarning", new Object[] { lastVersion, null }));
      showFile("RELEASENOTES.txt");
    } else if (lastVersion == null) {
      text.append(Resources.get("updatedialog.firstrunwarning", new Object[] { lastVersion,
          currentVersion }));
      showFile("README.txt");
    } else if (lastVersion.equals("null") || VersionInfo.isNewer(currentVersion, lastVersion)) {
      text.append(Resources.get("updatedialog.updatewarning", new Object[] { lastVersion,
          currentVersion }));
      showFile("RELEASENOTES.txt");
    } else if (VersionInfo.isNewer(lastVersion, currentVersion)) {
      text.append(Resources.get("updatedialog.downgradewarning", new Object[] { lastVersion,
          currentVersion }));
      showFile("RELEASENOTES.txt");
    } else
      throw new IllegalArgumentException("???");
    text.append(Resources.get("updatedialog.infotext"));
    text.append(Resources.get("updatedialog.htmlfooter"));

    versionInfo.setText(text.toString());
  }

  private void initGUI() {
    final JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

    setModal(true);
    setTitle(Resources.get("updatedialog.window.caption"));

    // Icon icon = MagellanImages.ABOUT_MAGELLAN;

    // JLabel magellanImage = new JLabel();
    // magellanImage.setIcon(icon);
    // magellanImage.setText("");
    // magellanImage.setAlignmentX(Component.CENTER_ALIGNMENT);
    // mainPanel.setBackground(MagellanImages.background);
    // mainPanel.setForeground(MagellanImages.foreground);

    versionInfo = new JEditorPane();
    versionInfo.setBackground(MagellanImages.background);
    versionInfo.setForeground(MagellanImages.foreground);
    versionInfo.setContentType("text/html");
    versionInfo.setEditable(false);
    versionInfo.setCaretPosition(0);
    versionInfo.addHyperlinkListener(this);
    final JScrollPane scrollPane = new JScrollPane(versionInfo);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setPreferredSize(new Dimension(600, 250));

    releaseText = new JTextArea();

    releaseText.setLineWrap(true);
    releaseText.setWrapStyleWord(true);

    releaseText.setBackground(MagellanImages.background);
    releaseText.setForeground(Color.BLACK);
    // releaseText.setFont(releaseText.getFont().deriveFont(releaseText.getFont().getSize()*1.2f));
    // Font font = releaseText.getFont();
    // releaseText.setContentType("text/html");
    releaseText.setEditable(false);
    releaseText.setCaretPosition(0);
    releaseNotesPane = new JScrollPane(releaseText);
    releaseNotesPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    releaseNotesPane.setPreferredSize(new Dimension(600, 300));

    final JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

    // OK Button
    final JButton btn_OK = new JButton(Resources.get("updatedialog.btn.ok.caption"));
    btn_OK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        abort = true;
        quit();
      }
    });

    // Quit Button
    final JButton btn_Quit = new JButton(Resources.get("updatedialog.btn.quit.caption"));
    btn_Quit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        abort = false;
        quit();
      }
    });

    final JPanel buttonPanel2 = new JPanel();
    buttonPanel2.setLayout(new BoxLayout(buttonPanel2, BoxLayout.X_AXIS));

    // README Button
    final JButton btn_README = new JButton(Resources.get("updatedialog.btn.README.caption"));
    btn_README.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showFile("README.txt");
      }
    });

    // CHANGELOG Button
    final JButton btn_CHANGELOG = new JButton(Resources.get("updatedialog.btn.CHANGELOG.caption"));
    btn_CHANGELOG.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showFile("CHANGELOG.txt");
      }
    });

    // RELEASENOTES Button
    final JButton btn_RELEASENOTES =
        new JButton(Resources.get("updatedialog.btn.RELEASENOTES.caption"));
    btn_RELEASENOTES.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showFile("RELEASENOTES.txt");
      }
    });

    buttonPanel.add(btn_OK);
    buttonPanel.add(btn_Quit);

    // buttonPanel.add(Box.createHorizontalGlue());
    // buttonPanel.add(Box.createHorizontalStrut(50));

    btn_README.setAlignmentX(Component.RIGHT_ALIGNMENT);
    btn_CHANGELOG.setAlignmentX(Component.RIGHT_ALIGNMENT);
    btn_RELEASENOTES.setAlignmentX(Component.RIGHT_ALIGNMENT);

    buttonPanel2.add(Box.createHorizontalGlue());
    buttonPanel2.add(btn_README);
    buttonPanel2.add(Box.createHorizontalGlue());
    buttonPanel2.add(btn_RELEASENOTES);
    buttonPanel2.add(Box.createHorizontalGlue());
    buttonPanel2.add(btn_CHANGELOG);
    buttonPanel2.add(Box.createHorizontalGlue());

    // mainPanel.add(magellanImage);
    mainPanel.add(scrollPane);
    mainPanel.add(buttonPanel2);
    mainPanel.add(releaseNotesPane);
    mainPanel.add(buttonPanel);

    getContentPane().add(mainPanel);

    pack();
  }

  private void showFile(String name) {
    releaseText.setText(loadFile(name));
    releaseText.setCaretPosition(0);

  }

  private String loadFile(String name) {
    File file = new File(Client.getBinaryDirectory(), name);
    if (!file.exists()) {
      file = new File(Client.getBinaryDirectory().getParent(), name);
      if (!file.exists()) {
        file = new File(Client.getResourceDirectory(), name);
        if (!file.exists()) {
          file = new File(new File(Client.getResourceDirectory(), "etc"), name);
        }
      }
    }
    try {
      final BufferedReader reader = new BufferedReader(new FileReader(file));
      final StringBuilder result = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        result.append(line).append("\n");
      }
      return result.toString();
    } catch (final FileNotFoundException e) {
      UpdateDialog.log.error(e);
      return Resources.get("updatedialog.fnfecxeption.message", file.toString());
    } catch (final Exception e) {
      UpdateDialog.log.error(e);
      return Resources.get("updatedialog.exception.message") + e.toString();
    }
  }

  public boolean getResult() {
    return abort;
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        // Loads the new page represented by link clicked
        final URI uri = e.getURL().toURI();

        // only in Java6 available, so we try to load it.
        // otherwise, we do nothing...
        final Class<?> c = Class.forName("java.awt.Desktop");
        if (c != null) {
          final Object desktop = c.getMethod("getDesktop").invoke(null);
          c.getMethod("browse", java.net.URI.class).invoke(desktop, uri);
        }
      } catch (final Exception exc) {
        // we do nothing here...
      }
    }
  }

}
