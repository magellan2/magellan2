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
 * @version 1.0
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
		DesktopEnvironment.desktop = md;
		DesktopEnvironment.initialized = true;

		// somebody registered before init
		if(DesktopEnvironment.pendingSCListeners != null) {
			Iterator<ShortcutListener> it = DesktopEnvironment.pendingSCListeners.iterator();

			while(it.hasNext()) {
				try {
					DesktopEnvironment.registerShortcutListener(it.next());
				} catch(Exception exc) {
				}
			}

			DesktopEnvironment.pendingSCListeners = null;
		}

		if(DesktopEnvironment.pendingSingleSCListeners != null) {
			Iterator<Pair<KeyStroke,ShortcutListener>> it = DesktopEnvironment.pendingSingleSCListeners.iterator();

			while(it.hasNext()) {
				try {
          Pair<KeyStroke,ShortcutListener> pair = it.next();
					KeyStroke ks = pair.getKey();
					ShortcutListener sl = pair.getValue();
					DesktopEnvironment.registerShortcutListener(ks, sl);
				} catch(Exception exc) {
				}
			}

			DesktopEnvironment.pendingSCListeners = null;
		}

		if(DesktopEnvironment.pendingAEListeners != null) {
			Iterator<Pair<KeyStroke,ActionListener>> it = DesktopEnvironment.pendingAEListeners.iterator();

			while(it.hasNext()) {
				try {
          Pair<KeyStroke,ActionListener> pair = it.next();
					KeyStroke ks = pair.getKey();
					ActionListener al = pair.getValue();
					DesktopEnvironment.registerActionListener(ks, al);
				} catch(Exception exc) {
				}
			}

			DesktopEnvironment.pendingAEListeners = null;
		}
	}

	/**
   * 
	 */
	public static void registerShortcutListener(ShortcutListener sl) {
		if(DesktopEnvironment.initialized) {
			DesktopEnvironment.desktop.registerShortcut(sl);
		} else {
			if(DesktopEnvironment.pendingSCListeners == null) {
				DesktopEnvironment.pendingSCListeners = new LinkedList<ShortcutListener>();
			}

			DesktopEnvironment.pendingSCListeners.add(sl);
		}
	}

	/**
   * 
	 */
	public static void registerShortcutListener(KeyStroke stroke, ShortcutListener sl) {
		if(DesktopEnvironment.initialized) {
			DesktopEnvironment.desktop.registerShortcut(stroke, sl);
		} else {
			if(DesktopEnvironment.pendingSingleSCListeners == null) {
				DesktopEnvironment.pendingSingleSCListeners = new LinkedList<Pair<KeyStroke,ShortcutListener>>();
			}

			DesktopEnvironment.pendingSingleSCListeners.add(new Pair<KeyStroke, ShortcutListener>(stroke,sl));
		}
	}

	/**
   * 
	 */
	public static void registerActionListener(KeyStroke stroke, ActionListener al) {
		if(DesktopEnvironment.initialized) {
			DesktopEnvironment.desktop.registerShortcut(stroke, al);
		} else {
			if(DesktopEnvironment.pendingAEListeners == null) {
				DesktopEnvironment.pendingAEListeners = new LinkedList<Pair<KeyStroke,ActionListener>>();
			}
      
			DesktopEnvironment.pendingAEListeners.add(new Pair<KeyStroke, ActionListener>(stroke,al));
		}
	}

	/**
   * 
	 */
	public static MagellanDesktop getDesktop() {
		return DesktopEnvironment.desktop;
	}

	/**
   * 
	 */
	public static void requestFocus(String component) {
		if(DesktopEnvironment.initialized) {
			DesktopEnvironment.getDesktop().componentRequestFocus(component);
		}
	}

	/**
   * 
	 */
	public static void repaintComponent(String component) {
		if(DesktopEnvironment.initialized) {
			DesktopEnvironment.getDesktop().repaint(component);
		}
	}

	/**
   * 
	 */
	public static void repaintAll() {
		if(DesktopEnvironment.initialized) {
			DesktopEnvironment.getDesktop().repaintAllComponents();
		}
	}

	/**
   * 
	 */
	public static void updateLaF() {
		if(DesktopEnvironment.initialized) {
			DesktopEnvironment.getDesktop().updateLaF();
		}
	}
}
