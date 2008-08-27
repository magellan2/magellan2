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

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import magellan.library.utils.Resources;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class OpenOrdersAccessory extends HistoryAccessory {
	private JCheckBox chkAutoConfirm = null;
	private JCheckBox chkIgnoreSemicolonComments = null;
	private JCheckBox chkDoNotOverwriteConfirmedOrders = null;

	/**
	 * Creates a new OpenOrdersAccessory object.
	 *
	 * 
	 * 
	 */
	public OpenOrdersAccessory(Properties setting, JFileChooser fileChooser) {
		super(setting, fileChooser);

		GridBagConstraints c = new GridBagConstraints();

		chkAutoConfirm = new JCheckBox(Resources.get("openordersaccessory.chk.autoconfirmation.caption"));
		chkAutoConfirm.setToolTipText(Resources.get("openordersaccessory.chk.autoconfirmation.tooltip"));

		chkIgnoreSemicolonComments = new JCheckBox(Resources.get("openordersaccessory.chk.ignoresemicoloncomments.caption"));
		chkIgnoreSemicolonComments.setToolTipText(Resources.get("openordersaccessory.chk.ignoresemicoloncomments.tooltip"));

		chkDoNotOverwriteConfirmedOrders = new JCheckBox(Resources.get("openordersaccessory.chk.donotoverwriteconfirmedorders.caption"));
		chkDoNotOverwriteConfirmedOrders.setToolTipText(Resources.get("openordersaccessory.chk.donotoverwriteconfirmedorders.tooltip"));
		
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		this.add(chkAutoConfirm, c);

		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		this.add(chkIgnoreSemicolonComments, c);
		
		c.anchor = GridBagConstraints.NORTHWEST;
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.weighty = 0.0;
    this.add(chkDoNotOverwriteConfirmedOrders, c);
    
    
		
		
		
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean getAutoConfirm() {
		return chkAutoConfirm.isSelected();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setAutoConfirm(boolean bool) {
		chkAutoConfirm.setSelected(bool);
	}

	/**
	 * 
	 * @return true, if we must not overwrite existing confirmed orders
	 */
	public boolean getDoNotOverwriteConfirmedOrders(){
	  return chkDoNotOverwriteConfirmedOrders.isSelected();
	}
	
	/**
	 * sets the value of DoNotOverwriteConfirmedOrders
	 * @param bool
	 */
	public void setDoNotOverwriteConfirmedOrders(boolean bool){
	  chkDoNotOverwriteConfirmedOrders.setSelected(bool);
	}
	
	
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean getIgnoreSemicolonComments() {
		return chkIgnoreSemicolonComments.isSelected();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setIgnoreSemicolonComments(boolean bool) {
		chkIgnoreSemicolonComments.setSelected(bool);
	}
}
