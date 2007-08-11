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

package magellan.client.desktop;

import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.KeyStroke;

import magellan.library.utils.Pair;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class DesktopEnvironment extends Object {
	/** */
	private static MagellanDesktop desktop;

	// init state
	private static boolean initialized = false;
	private static List<ShortcutListener> pendingSCListeners;
	private static List<Pair<KeyStroke,ShortcutListener>> pendingSingleSCListeners;
	private static List<Pair<KeyStroke,ActionListener>> pendingAEListeners;

	/**
	 * 
	 */
	static void init(MagellanDesktop md) {
		desktop = md;
		initialized = true;

		// somebody registered before init
		if(pendingSCListeners != null) {
			Iterator it = pendingSCListeners.iterator();

			while(it.hasNext()) {
				try {
					registerShortcutListener((ShortcutListener) it.next());
				} catch(Exception exc) {
				}
			}

			pendingSCListeners = null;
		}

		if(pendingSingleSCListeners != null) {
			Iterator<Pair<KeyStroke,ShortcutListener>> it = pendingSingleSCListeners.iterator();

			while(it.hasNext()) {
				try {
          Pair<KeyStroke,ShortcutListener> pair = it.next();
					KeyStroke ks = pair.getKey();
					ShortcutListener sl = pair.getValue();
					registerShortcutListener(ks, sl);
				} catch(Exception exc) {
				}
			}

			pendingSCListeners = null;
		}

		if(pendingAEListeners != null) {
			Iterator<Pair<KeyStroke,ActionListener>> it = pendingAEListeners.iterator();

			while(it.hasNext()) {
				try {
          Pair<KeyStroke,ActionListener> pair = it.next();
					KeyStroke ks = pair.getKey();
					ActionListener al = pair.getValue();
					registerActionListener(ks, al);
				} catch(Exception exc) {
				}
			}

			pendingAEListeners = null;
		}
	}

	/**
   * 
	 */
	public static void registerShortcutListener(ShortcutListener sl) {
		if(initialized) {
			desktop.registerShortcut(sl);
		} else {
			if(pendingSCListeners == null) {
				pendingSCListeners = new LinkedList<ShortcutListener>();
			}

			pendingSCListeners.add(sl);
		}
	}

	/**
   * 
	 */
	public static void registerShortcutListener(KeyStroke stroke, ShortcutListener sl) {
		if(initialized) {
			desktop.registerShortcut(stroke, sl);
		} else {
			if(pendingSingleSCListeners == null) {
				pendingSingleSCListeners = new LinkedList<Pair<KeyStroke,ShortcutListener>>();
			}

			pendingSingleSCListeners.add(new Pair<KeyStroke, ShortcutListener>(stroke,sl));
		}
	}

	/**
   * 
	 */
	public static void registerActionListener(KeyStroke stroke, ActionListener al) {
		if(initialized) {
			desktop.registerShortcut(stroke, al);
		} else {
			if(pendingAEListeners == null) {
				pendingAEListeners = new LinkedList<Pair<KeyStroke,ActionListener>>();
			}
      
			pendingAEListeners.add(new Pair<KeyStroke, ActionListener>(stroke,al));
		}
	}

	/**
   * 
	 */
	public static MagellanDesktop getDesktop() {
		return desktop;
	}

	/**
   * 
	 */
	public static void requestFocus(String component) {
		if(initialized) {
			getDesktop().componentRequestFocus(component);
		}
	}

	/**
   * 
	 */
	public static void repaintComponent(String component) {
		if(initialized) {
			getDesktop().repaint(component);
		}
	}

	/**
   * 
	 */
	public static void repaintAll() {
		if(initialized) {
			getDesktop().repaintAllComponents();
		}
	}

	/**
   * 
	 */
	public static void updateLaF() {
		if(initialized) {
			getDesktop().updateLaF();
		}
	}
}
