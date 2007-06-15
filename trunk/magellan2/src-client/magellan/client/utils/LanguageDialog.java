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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class LanguageDialog {
	private static final Logger log = Logger.getInstance(LanguageDialog.class);

	// the settings needed for the resource loader
	protected Properties settings;

	// a list containing all installed languages as Lang objects
	protected List<Lang> languageList;

	// a Lang object defining the system default language
	protected Lang sysDefault;

	/**
	 * Creates new LanguageDialog
	 *
	 * 
	 * 
	 */
	public LanguageDialog(Properties settings) {
		this.settings = settings;

		Resources.getInstance();

		findLanguages();
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
    
    if (sysDefault == null) new Lang(Locale.ENGLISH);
    
    languageList = new LinkedList<Lang>();
    for (Locale locale : locales) {
      languageList.add(new Lang(locale));
    }
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Locale showDialog(Component parent) {
		if(languagesFound()) {
			Object ret = JOptionPane.showInputDialog(parent,
													 Resources.get("magellan.util.languagedialog.choose"),
                           Resources.get("magellan.util.languagedialog.title"),
													 JOptionPane.QUESTION_MESSAGE, null,
													 languageList.toArray(), sysDefault);

			if(ret != null) {
				return ((Lang) ret).locale;
			}
		}

		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean languagesFound() {
		return languageList.size() > 0;
	}

	protected class Lang implements Comparable<Lang> {
		protected Locale locale;

		/**
		 * Creates a new Lang object.
		 *
		 * 
		 */
		public Lang(String lang) {
			locale = new Locale(lang, "");
		}

		/**
		 * Creates a new Lang object.
		 *
		 * 
		 */
		public Lang(Locale l) {
			locale = l;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public String toString() {
			return locale.getDisplayLanguage();
		}

    public int compareTo(Lang o) {
      return toString().compareTo(o.toString());
    }
    
    public boolean equals(Object o) {
      if (o instanceof Lang) {
        Lang l = (Lang)o;
        return toString().equalsIgnoreCase(l.toString());
      }
      return false;
    }
	}

	// pavkovic 2003.01.28: this is a Map of the default Translations mapped to this class
	// it is called by reflection (we could force the implementation of an interface,
	// this way it is more flexible.)
	// Pls use this mechanism, so the translation files can be created automagically
	// by inspecting all classes.
	private static Map<String,String> defaultTranslations;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static synchronized Map<String,String> getDefaultTranslations() {
		if(defaultTranslations == null) {
			defaultTranslations = new Hashtable<String,String>();
			defaultTranslations.put("choose",
									"The following languages were found. Please choose one.");
			defaultTranslations.put("title", "Choose a language");
		}

		return defaultTranslations;
	}
}
