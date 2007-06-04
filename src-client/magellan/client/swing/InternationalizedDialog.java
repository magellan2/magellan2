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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import magellan.library.utils.JVMUtilities;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 310 $
 */
public abstract class InternationalizedDialog extends JDialog {
	private static final Logger log = Logger.getInstance(InternationalizedDialog.class);

	/**
	 * Creates a new InternationalizedDialog object.
	 *
	 * @param owner the <code>Frame</code> from which the dialog is displayed
	 * @param modal <code>true</code> for a modal dialog, false for one that allows others windows to be 
	 * active at the same time
	 */
	public InternationalizedDialog(Frame owner, boolean modal) {
		super(owner, modal);
		initDialog();
	}

	/**
	 * Creates a new InternationalizedDialog object.
	 *
	 * 
	 * 
	 */
	public InternationalizedDialog(Dialog owner, boolean modal) {
		super(owner, modal);
		initDialog();
	}

	protected void processKeyEvent(KeyEvent e) {
		super.processKeyEvent(e);

		if((e.getID() == KeyEvent.KEY_PRESSED) && (e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
			quit();
		}
	}

	protected void initDialog() {
		// call setFocusableWindowState (true) on java 1.4 while staying compatible with Java 1.3
		JVMUtilities.setFocusableWindowState(this, true);

		addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(log.isDebugEnabled()) {
						log.debug("InternationalizedDialog.KeyEvent :" + e);
					}

					if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						quit();
					}
				}
			});
		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					if(log.isDebugEnabled()) {
						log.debug("InternationalizedDialog.WindowEvent :" + e);
					}

					quit();
				}
			});
	}

	protected void quit() {
		if(log.isDebugEnabled()) {
			log.debug("InternationalizedDialog.quit called. (" + this + ")");
		}

		dispose();
	}
}
