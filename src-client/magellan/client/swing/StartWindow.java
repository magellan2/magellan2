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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import magellan.client.Client;
import magellan.client.utils.SwingUtils;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.Resources;
import magellan.library.utils.VersionInfo;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class StartWindow extends JFrame {
  protected Collection<Icon> images;
  protected int steps;
  protected int currentStep = 0;
  protected JLabel imageLabel;
  protected JProgressBar progress;
  protected JTextPane text;
  protected JTextPane versionText;

  private File magellanDir = null;

  /**
   * Creates new StartWindow
   */
  public StartWindow(Icon icon, int steps, File magellanDirectory) {
    super("Magellan");
    magellanDir = magellanDirectory;
    init(icon, steps);
  }

  /**
   * Creates a new StartWindow object.
   */
  public StartWindow(Collection<Icon> icons, int steps, File magellanDirectory) {
    super("Magellan");
    magellanDir = magellanDirectory;
    init(icons, steps);
  }

  protected void init(Icon icon, int numberOfSteps) {
    Collection<Icon> icons = new ArrayList<Icon>(1);

    if (icon != null) {
      icons.add(icon);
    }

    init(icons, numberOfSteps);
  }

  protected void init(Collection<Icon> icons, int numberOfSteps) {
    setUndecorated(true);
    setResizable(false);

    images = icons;
    steps = numberOfSteps;

    Image iconImage = Client.getApplicationIcon();

    // set the application icon
    if (iconImage != null) {
      setIconImage(iconImage);
    }

    Container cont = getContentPane();
    cont.setLayout(new SimpleLayout());

    // use the colors from the default file
    Color foreground = MagellanImages.FOREGROUND;
    Color background = MagellanImages.BACKGROUND;

    cont.setBackground(background);
    ((JComponent) cont).setBorder(new LineBorder(background, 2));

    int prefwidth = 0;

    if ((images != null) && !images.isEmpty()) {
      Icon icon = images.iterator().next();
      prefwidth = icon.getIconWidth();
      imageLabel = new JLabel(icon);
      imageLabel.setBackground(background);
      cont.add(imageLabel);
    } else {
      prefwidth = 400;
    }

    if (steps > 0) {
      progress = new JProgressBar(SwingConstants.HORIZONTAL, 0, steps);
      progress.setStringPainted(true);
      progress.setBorderPainted(false);
      progress.setForeground(foreground);
      progress.setBackground(background);
      cont.add(progress);
    }

    String names = null;
    String descr = "\n" + Resources.get("startwindow.infotext");

    names = "Roger Butenuth, Enno Rehling, Stefan Götz, Klaas Prause, Sebastian Tusk, ";
    names += "Andreas Gampe, Roland Behme, Michael Schmidt, Henning Zahn, Oliver Hertel, ";
    names += "Guenter Grossberger, Sören Bendig, Marc Geerligs, Matthias Müller, ";
    names += "Ulrich Küster, Jake Hofer, Ilja Pavkovic, Fiete Fietz, Steffen Mecke, ";
    names += "Steve Wagner, Thoralf Rickert, Ralf Duckstein, Mark Gerritsen\n";

    StyledDocument styled = new DefaultStyledDocument();

    MutableAttributeSet set = new SimpleAttributeSet();
    StyleConstants.setBold(set, true);

    MutableAttributeSet set2 = new SimpleAttributeSet();
    // StyleConstants.setFontSize(set2, 10);

    try {
      styled.insertString(0, names, set);
      styled.insertString(styled.getLength(), descr, set2);
    } catch (Exception exc) {
      // ignore this
    }

    text = new JTextPane(styled);
    text.setEditable(false);

    text.setForeground(foreground);
    text.setBackground(background);

    // cont.add(text, BorderLayout.SOUTH);
    cont.add(text);

    // Fiete 20060911: trying to add Version info to start screen (bottom)
    String version = VersionInfo.getVersion(magellanDir);

    if (version == null) {
      version = "version not available";
    }

    StyledDocument styledVersion = new DefaultStyledDocument();

    MutableAttributeSet setVersion = new SimpleAttributeSet();
    StyleConstants.setFontSize(setVersion, 12);
    StyleConstants.setBold(setVersion, true);

    try {
      styledVersion.insertString(0, version, setVersion);
    } catch (Exception exc) {
      // ignore this error
    }
    versionText = new JTextPane(styledVersion);

    versionText.setEditable(false);

    versionText.setForeground(foreground);
    versionText.setBackground(background);

    cont.add(versionText, BorderLayout.SOUTH);

    // make all same length
    Dimension prefDim;

    if (progress != null) {
      prefDim = progress.getPreferredSize();

      if (prefDim.width != prefwidth) {
        prefDim.width = prefwidth;
        progress.setPreferredSize(prefDim);
      }
    }

    prefDim = text.getPreferredSize();

    if (prefDim.width != prefwidth) {
      prefDim.width = prefwidth;
      text.setPreferredSize(prefDim);
      text.setSize(prefDim);

      // try to change height
      try {
        Rectangle rect = text.modelToView(styled.getLength());
        prefDim.height = rect.y + rect.height;
        text.setPreferredSize(prefDim);
      } catch (Exception exc) {
        // ignore this error
      }
    }

    prefDim = versionText.getPreferredSize();

    if (prefDim.width != prefwidth) {
      prefDim.width = prefwidth;
      versionText.setPreferredSize(prefDim);
      versionText.setSize(prefDim);

      // try to change height
      try {
        Rectangle rect = versionText.modelToView(styledVersion.getLength());
        prefDim.height = rect.y + rect.height;
        versionText.setPreferredSize(prefDim);
      } catch (Exception exc) {
        // ignore this error
      }
    }

    pack();

    SwingUtils.center(this);

  }

  /**
   * DOCUMENT-ME
   */
  public void progress(int step, String message) {
    if ((progress != null) && (step <= steps)) {
      progress.setValue(step);
      progress.setString(message);

      if ((images != null) && (step < images.size())) {
        Icon icon = null;
        Iterator<Icon> it = images.iterator();

        for (int i = 0; i <= step; i++) {
          icon = it.next();
        }

        imageLabel.setIcon(icon);
      }
    }
  }

  protected static class SimpleLayout implements LayoutManager {
    /**
     * DOCUMENT-ME
     */
    public void layoutContainer(java.awt.Container container) {
      int width = 0;
      int height = 0;
      Component c[] = container.getComponents();

      if ((c != null) && (c.length > 0)) {
        int i;
        Dimension d;

        for (i = 0; i < c.length; i++) {
          d = c[i].getPreferredSize();

          if (d.width > width) {
            width = d.width;
          }

          height += d.height;
        }

        Insets insets = container.getInsets();
        int x = 0;
        int y = 0;

        if (insets != null) {
          x += insets.left;
          y += insets.top;
        }

        for (i = 0; i < c.length; i++) {
          int h = c[i].getPreferredSize().height;
          c[i].setBounds(x, y, width, h);
          y += h;
        }
      }
    }

    /**
     * DOCUMENT-ME
     */
    public java.awt.Dimension preferredLayoutSize(java.awt.Container container) {
      int width = 0;
      int height = 0;
      Component c[] = container.getComponents();

      if ((c != null) && (c.length > 0)) {
        int i;
        Dimension d;

        for (i = 0; i < c.length; i++) {
          d = c[i].getPreferredSize();

          if (d.width > width) {
            width = d.width;
          }

          height += d.height;
        }
      }

      Insets insets = container.getInsets();

      if (insets != null) {
        width += (insets.left + insets.right);
        height += (insets.top + insets.bottom);
      }

      return new Dimension(width, height);
    }

    /**
     * DOCUMENT-ME
     */
    public void addLayoutComponent(java.lang.String str, java.awt.Component component) {
      // nothing to do
    }

    /**
     * DOCUMENT-ME
     */
    public java.awt.Dimension minimumLayoutSize(java.awt.Container container) {
      return preferredLayoutSize(container);
    }

    /**
     * DOCUMENT-ME
     */
    public void removeLayoutComponent(java.awt.Component component) {
      // nothing to do
    }
  }
}
