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

/*
 * LanguageDialog.java
 *
 * Created on 28. M?rz 2002, 10:38
 */
package magellan.client.utils;

import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class LanguageDialog {
  // the settings needed for the resource loader
  protected Properties settings;

  // a list containing all installed languages as Lang objects
  protected List<Lang> languageList;

  // a Lang object defining the system default language
  protected Lang sysDefault;

  private JOptionPane pane;

  private JDialog dialog;

  private String message = Resources.get("util.languagedialog.choose");

  private String title = Resources.get("util.languagedialog.title");

  /**
   * Creates new LanguageDialog
   * 
   * @param parent The parent component
   * @param settings The settings needed for the resource loader
   */
  public LanguageDialog(Component parent, Properties settings) {
    this.settings = settings;

    Resources.getInstance();

    findLanguages();

    initDialog(parent);
  }

  protected void findLanguages() {
    List<Locale> locales = Resources.getAvailableLocales();

    final Locale defaultLocale = Locale.getDefault();

    for (Locale locale : locales) {
      if (locale.getLanguage().equalsIgnoreCase(defaultLocale.getLanguage())) {
        sysDefault = new Lang(defaultLocale);
        break;
      }
    }

    if (sysDefault == null) {
      new Lang(Locale.ENGLISH);
    }

    languageList = new LinkedList<Lang>();
    for (Locale locale : locales) {
      languageList.add(new Lang(locale));
    }
  }

  private void initDialog(Component parent) {
    if (languagesFound()) {
      pane =
          new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
              null, null, null);

      pane.setWantsInput(true);
      pane.setSelectionValues(languageList.toArray());
      pane.setInitialSelectionValue(sysDefault);
      pane.setComponentOrientation(((parent == null) ? JOptionPane.getRootFrame() : parent)
          .getComponentOrientation());

      dialog = pane.createDialog(parent, title);
    } else {
      pane =
          new JOptionPane("No languages found!", JOptionPane.WARNING_MESSAGE,
              JOptionPane.OK_CANCEL_OPTION, null, null, null);
      dialog = pane.createDialog(parent, title);
    }

  }

  /**
   * Moves this component to a new location. The top-left corner of the new location is specified by
   * the <code>x</code> and <code>y</code> parameters in the coordinate space of this component's
   * parent.
   * 
   * @param x the <i>x</i>-coordinate of the new location's top-left corner in the parent's
   *          coordinate space
   * @param y the <i>y</i>-coordinate of the new location's top-left corner in the parent's
   *          coordinate space
   */
  public void setLocation(int x, int y) {
    dialog.setLocation(x, y);
  }

  /**
   * @return Width of the dialog
   */
  public int getWidth() {
    return dialog.getWidth();
  }

  /**
   * @return Height of the dialog
   */
  public int getHeight() {
    return dialog.getHeight();
  }

  /**
   * Display the dialog and return user's choice.
   */
  public Locale show() {
    if (languagesFound()) {
      pane.selectInitialValue();

      dialog.setAlwaysOnTop(true);
      dialog.setVisible(true);
      dialog.dispose();

      Object value = pane.getInputValue();
      if (value == JOptionPane.UNINITIALIZED_VALUE)
        return null;
      return ((Lang) value).locale;
    } else {
      dialog.setVisible(true);
      dialog.dispose();
      return null;
    }
  }

  /**
   * @return <code>true</code> iff at least one language has been found
   */
  public boolean languagesFound() {
    return languageList.size() > 0;
  }

  protected class Lang implements Comparable<Lang> {
    protected Locale locale;

    /**
     * Creates a new Lang object.
     */
    public Lang(String lang) {
      locale = new Locale(lang, "");
    }

    /**
     * Creates a new Lang object.
     */
    public Lang(Locale l) {
      locale = l;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return locale.getDisplayLanguage(locale);
    }

    /**
     * Compares the String representations.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Lang o) {
      return toString().compareTo(o.toString());
    }

    /**
     * Compares the languages
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
      if (o instanceof Lang) {
        Lang l = (Lang) o;
        return toString().equalsIgnoreCase(l.toString());
      }
      return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return toString().hashCode();
    }
  }
}
