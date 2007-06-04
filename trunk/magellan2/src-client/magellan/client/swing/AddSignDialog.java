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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import magellan.client.event.EventDispatcher;
import magellan.client.swing.basics.SpringUtilities;
import magellan.library.Region;
import magellan.library.Sign;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * A dialog for adding a sign to a region
 * @author Fiete
 */
public class AddSignDialog extends InternationalizedDialog {
	private static final Logger log = Logger.getInstance(AddSignDialog.class);
	private Properties settings = null;
	private EventDispatcher dispatcher = null;
	private JTextField Line1 = null;
	private JTextField Line2 = null;
	private Region region = null;

	

	/**
	 * Create a new JVorlage object as a dialog with a parent window.
	 *
	 * 
	 * 
	 * 
	 */
	public AddSignDialog(Frame owner, boolean modal, Properties p,EventDispatcher dispatcher,Region r) {
		super(owner, modal);
		settings = p;
		region = r;
		this.dispatcher = dispatcher;
		init();
	}

	private void init() {
		setContentPane(getMainPane());
		setTitle(Resources.get("magellan.addsigndialog.window.title"));

		int width = Math.max(Integer.parseInt(settings.getProperty("AddSign.width", "350")), 350);
		int height = Math.max(Integer.parseInt(settings.getProperty("AddSign.height", "140")), 140);
		setSize(width, height);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = Integer.parseInt(settings.getProperty("AddSign.x",
													  ((screen.width - width) / 2) + ""));
		int y = Integer.parseInt(settings.getProperty("AddSign.y",
													  ((screen.height - height) / 2) + ""));
		setLocation(x, y);
	}

	private Container getMainPane() {
		SpringLayout layout = new SpringLayout();
		JPanel mainPanel = new JPanel(layout);		
		mainPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
		
		JLabel label1 = new JLabel(Resources.get("magellan.addsigndialog.label.line1"));
		Line1 = new JTextField(30);
		mainPanel.add(label1);
		mainPanel.add(Line1);
		
		JLabel label2 = new JLabel(Resources.get("magellan.addsigndialog.label.line2"));
		Line2 = new JTextField();
		mainPanel.add(label2);
		mainPanel.add(Line2);
		
		JButton okButton = new JButton(Resources.get("magellan.addsigndialog.btn.ok.caption"));
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addSign();
				}
			});

		JButton cancelButton = new JButton(Resources.get("magellan.addsigndialog.btn.close.caption"));
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					quit();
				}
			});

		mainPanel.add(okButton);
		mainPanel.add(cancelButton);
		
//		Lay out the panel.
		SpringUtilities.makeCompactGrid(mainPanel,
		                                3, 2, //rows, cols
		                                6, 6,        //initX, initY
		                                6, 6);       //xPad, yPad

		return mainPanel;
	}

	

	


	/**
	 * Stores all properties of AddSign that should be preserved to the global Properties object.
	 */
	private void storeSettings() {
		settings.setProperty("AddSign.width", getWidth() + "");
		settings.setProperty("AddSign.height", getHeight() + "");
		settings.setProperty("AddSign.x", getX() + "");
		settings.setProperty("AddSign.y", getY() + "");
	}

	

	/**
	 * going to make the change
	 *
	 */
	private void addSign(){
		String s1 = Line1.getText();
		String s2 = Line2.getText();
		
		if (s1!=null && s1.length()>0){
			region.addSign(new Sign(s1));
		} 
		if (s2!=null && s2.length()>0){
			region.addSign(new Sign(s2));
		} 
		dispatcher.fire(new GameDataEvent(this,region.getData()));
		quit();
	}
	
	
	/**
	 * stores position and exit
	 */
	protected void quit() {
		storeSettings();
		dispose();
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
			defaultTranslations.put("window.title", "Add a Sign");
			defaultTranslations.put("btn.ok.caption", "OK");
			defaultTranslations.put("btn.close.caption", "Close");
			defaultTranslations.put("label.line1", "Line 1");
			defaultTranslations.put("label.line2", "Line 2");
		}

		return defaultTranslations;
	}
}
