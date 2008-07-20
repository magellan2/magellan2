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

package magellan.client.swing.tasks;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.util.Properties;

import magellan.client.event.EventDispatcher;
import magellan.client.swing.InternationalizedDataDialog;
import magellan.library.GameData;
import magellan.library.utils.Resources;


/**
 * A dialog wrapper for the TaskTable panel.
 */
public class TaskTableDialog extends InternationalizedDataDialog {
	private TaskTablePanel panel = null;

	/**
	 * Create a new TaskTableDialog object as a dialog with a parent window.
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public TaskTableDialog(Frame owner, boolean modal, EventDispatcher ed, GameData initData,
						   Properties p) {
		super(owner, modal, ed, initData, p);
		init();

		//pack();
	}

	private void init() {
		setContentPane(getMainPane());
		setTitle(Resources.get("tasks.tasktabledialog.window.title"));

		int width = Integer.parseInt(settings.getProperty("TaskTableDialog.width", "500"));
		int height = Integer.parseInt(settings.getProperty("TaskTableDialog.height", "300"));
		this.setSize(width, height);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = Integer.parseInt(settings.getProperty("TaskTableDialog.x",
													  ((screen.width - getWidth()) / 2) + ""));
		int y = Integer.parseInt(settings.getProperty("TaskTableDialog.y",
													  ((screen.height - getHeight()) / 2) + ""));
		this.setLocation(x, y);
	}

	private Container getMainPane() {
		if(panel == null) {
			panel = new TaskTablePanel(dispatcher, data, settings);
		}

		return panel;

		/*
		JPanel mainPanel = new JPanel();
		mainPanel.add(panel);
		return mainPanel;
		*/
	}

	private void storeSettings() {
		settings.setProperty("TaskTableDialog.x", getX() + "");
		settings.setProperty("TaskTableDialog.y", getY() + "");
		settings.setProperty("TaskTableDialog.width", getWidth() + "");
		settings.setProperty("TaskTableDialog.height", getHeight() + "");
	}

	@Override
  protected void quit() {
		storeSettings();
		panel.quit();
		super.quit();
	}
}
