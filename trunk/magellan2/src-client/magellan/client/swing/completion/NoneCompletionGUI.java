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
 * NoneCompletionGUI.java
 *
 * Created on 16. Oktober 2001, 12:53
 */
package magellan.client.swing.completion;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.swing.text.JTextComponent;

import magellan.client.completion.AutoCompletion;
import magellan.library.completion.Completion;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class NoneCompletionGUI extends AbstractCompletionGUI {
	protected Completion last = null;
	protected boolean offering = false;

	/**
	 * Creates new NoneCompletionGUI
	 */
	public NoneCompletionGUI() {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean editorMayLoseFocus() {
		return false;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean editorMayUpdateCaret() {
		return false;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Completion getSelectedCompletion() {
		return last;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int[] getSpecialKeys() {
		return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void init(AutoCompletion autoCompletion) {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isOfferingCompletion() {
		return offering;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 */
	public void offerCompletion(javax.swing.text.JTextComponent jTextComponent,
								java.util.Collection collection, java.lang.String str) {
		last = (Completion) collection.iterator().next();
		offering = true;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 * 
	 */
	public void cycleCompletion(JTextComponent editor, Collection completions, String stub,
								int index) {
		Iterator it = completions.iterator();

		for(int i = 0; i <= index; i++) {
			last = (Completion) it.next();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void specialKeyPressed(int param) {
	}

	/**
	 * DOCUMENT-ME
	 */
	public void stopOffer() {
		offering = false;
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
			defaultTranslations = new Hashtable<String, String>();
			defaultTranslations.put("gui.title", "No display");
		}

		return defaultTranslations;
	}
  
  

  /**
   */
  @Override
  public String getTitle() {
    return Resources.get("completion.nonecompletiongui.gui.title");
  }
}
