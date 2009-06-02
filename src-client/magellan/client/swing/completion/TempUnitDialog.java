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

package magellan.client.swing.completion;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import magellan.client.swing.InternationalizedDialog;
import magellan.client.swing.MagellanFocusTraversalPolicy;
import magellan.client.utils.NameGenerator;
import magellan.library.utils.JVMUtilities;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class TempUnitDialog extends InternationalizedDialog implements ActionListener {
	private static final Logger log = Logger.getInstance(TempUnitDialog.class);
	protected JTextField id;
	protected JTextField name;
	protected JButton more;
	protected JButton ok;
	protected JButton cancel;
	protected JPanel bPanel;
	protected JPanel morePanel;
	protected JTextField recruit;
	protected JTextArea descript;
	protected JTextField order;
	protected JCheckBox giveRecruitCost;
	protected JCheckBox giveMaintainCost;
	protected GridBagConstraints con;
	protected GridBagLayout layout;
	protected Component posC;
	protected boolean approved = false;
	protected Properties settings;
	protected JButton nameGen;
	protected Container nameCon;

	/** DOCUMENT-ME */
	public static final String SETTINGS_KEY = "TempUnitDialog.ExtendedDialog";

	/**
	 * Creates new TempUnitDialog
	 *
	 * 
	 * 
	 * 
	 */
	public TempUnitDialog(Frame owner, Component parent, Properties settings) {
		super(owner, true);
		this.settings = settings;

		posC = parent;

		Container c = getContentPane();

		c.setLayout(layout = new GridBagLayout());

		con = new GridBagConstraints();
		con.gridwidth = 1;
		con.gridheight = 1;
		con.weightx = 0;
		con.anchor = GridBagConstraints.WEST;
		con.fill = GridBagConstraints.NONE;

		Insets i = new Insets(1, 2, 1, 2);

		con.insets = i;

		c.add(new JLabel(Resources.get("completion.tempunitdialog.id.label")), con);
		con.gridy = 1;
		c.add(new JLabel(Resources.get("completion.tempunitdialog.name.label")), con);

		con.gridy = 0;
		con.gridx = 1;
		con.weightx = 1;
		con.fill = GridBagConstraints.HORIZONTAL;
		c.add(id = new JTextField(5), con);
		id.addActionListener(this);
		con.gridy = 1;
		nameCon = new JPanel(new BorderLayout());
		((JPanel) nameCon).setPreferredSize(id.getPreferredSize());
		nameCon.add(name = new JTextField(20), BorderLayout.CENTER);
		c.add(nameCon, con);
		name.addActionListener(this);

		nameGen = new JButton("...");
		nameGen.addActionListener(this);
		nameGen.setEnabled(false);

		Insets insets = nameGen.getMargin();
		insets.left = 1;
		insets.right = 1;
		nameGen.setMargin(insets);
		nameGen.setFocusPainted(false);

		con.gridx = 0;
		con.gridy = 2;
		con.gridwidth = 3;

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		panel.add(more = new JButton(Resources.get("completion.tempunitdialog.more.more")));
		panel.setOpaque(false);
		more.addActionListener(this);
		bPanel = panel;
		c.add(panel, con);

		con.gridx = 2;
		con.gridy = 0;
		con.gridwidth = 1;
		con.gridheight = 2;
		panel = new JPanel(new GridLayout(2, 1));
		panel.setOpaque(false);
		panel.add(ok = new JButton(Resources.get("completion.tempunitdialog.ok")));
		ok.addActionListener(this);
		panel.add(cancel = new JButton(Resources.get("completion.tempunitdialog.cancel")));
		cancel.addActionListener(this);
		con.fill = GridBagConstraints.NONE;
		c.add(panel, con);

		morePanel = new JPanel(new GridBagLayout());
		morePanel.setOpaque(false);
		con.gridx = 0;
		con.gridy = 0;
		con.gridwidth = 1;
		con.gridheight = 1;
		morePanel.add(new JLabel(Resources.get("completion.tempunitdialog.recruit.label")), con);
		con.gridy = 2;
		morePanel.add(new JLabel(Resources.get("completion.tempunitdialog.order.label")), con);
		con.gridy = 3;
		con.anchor = GridBagConstraints.NORTHWEST;
		morePanel.add(new JLabel(Resources.get("completion.tempunitdialog.descript.label")), con);
		con.anchor = GridBagConstraints.CENTER;
		con.gridy = 1;
		con.gridx = 1;
		con.fill = GridBagConstraints.HORIZONTAL;
		morePanel.add(giveRecruitCost = new JCheckBox(Resources.get("completion.tempunitdialog.recruitCost.label")), con);
		con.gridx = 2;
		morePanel.add(giveMaintainCost = new JCheckBox(Resources.get("completion.tempunitdialog.maintainCost.label")), con);
		giveRecruitCost.setSelected(settings.getProperty("TempUnitDialog.AddRecruitCost", "false")
											.equals("true"));
		giveMaintainCost.setSelected(settings.getProperty("TempUnitDialog.AddMaintainCost", "false")
											 .equals("true"));

		con.gridx = 1;
		con.gridwidth = 2;
		con.gridy = 0;
		morePanel.add(recruit = new JTextField(5), con);
		con.gridy = 2;
		morePanel.add(order = new JTextField(10), con);
		con.gridy = 3;
		con.fill = GridBagConstraints.BOTH;
		morePanel.add(new JScrollPane(descript = new TablessTextArea(3, 10)), con);
		descript.setLineWrap(true);
		descript.setWrapStyleWord(true);

		// Focus management/Default opening state
		boolean open = false;

		if(settings.containsKey(TempUnitDialog.SETTINGS_KEY)) {
			String s = settings.getProperty(TempUnitDialog.SETTINGS_KEY);

			if(s.equalsIgnoreCase("true")) {
				open = true;
			}
		} else {
      open = true;  
    }
      

		if(open) {
			changeDialog();
		} else {
			setFocusList(open);
		}

		loadBounds();

		addWindowListener(new WindowAdapter() {
				@Override
        public void windowActivated(WindowEvent e) {
					// dont look too close on this method. It recalls itself until this.name is 
					// showing on screen and then it calls requestFocusInWindow on it (via
					// reflection api to stay compatible with jdk < 1.4
					SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if(TempUnitDialog.log.isDebugEnabled()) {
									TempUnitDialog.log.debug("TempUnitDialog.requestFocusInWindows: " +
											  TempUnitDialog.this.name.isShowing());
								}

								if(TempUnitDialog.this.name.isShowing()) {
									JVMUtilities.requestFocusInWindow(TempUnitDialog.this.name);
								} else {
									SwingUtilities.invokeLater(this);
								}
							}
						});
				}
			});
	}

	// overrides InternationalizedDialog.quit()
	// do not dispose
	@Override
  protected void quit() {
		settings.setProperty("TempUnitDialog.LastOrderEmpty",
							 (order.getText().length() == 0) ? "true" : "false");
		approved = false;
		saveBounds();
		setVisible(false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public void setVisible(boolean b) {
		super.setVisible(b);

		if(order != null) {
			settings.setProperty("TempUnitDialog.LastOrderEmpty",
								 (order.getText().length() == 0) ? "true" : "false");
		}
	}

	protected void loadBounds() {
		if(settings.containsKey("TempUnitDialog.bounds")) {
			int x = 0;
			int y = 0;
			int w = 0;
			int h = 0;
			StringTokenizer st = new StringTokenizer(settings.getProperty("TempUnitDialog.bounds"),
													 ",");
			try {
			  x = Integer.parseInt(st.nextToken());
			  y = Integer.parseInt(st.nextToken());
			  w = Integer.parseInt(st.nextToken());
			  h = Integer.parseInt(st.nextToken());
			  setBounds(x, y, w, h);
			} catch (Exception e){
			  pack();
			  setLocationRelativeTo(posC);
			}
		} else {
			pack();
			setLocationRelativeTo(posC);
		}
	}

	protected void saveBounds() {
		Rectangle r = getBounds();
		settings.setProperty("TempUnitDialog.bounds",
							 r.x + "," + r.y + "," + r.width + "," + r.height);
	}

	protected void setFocusList(boolean extended) {
    Vector<Component> components = new Vector<Component>();
    components.add(id);
    components.add(name);
    if (extended) {
      components.add(recruit);
      components.add(order);
      components.add(descript);
    }
    components.add(more);
    components.add(ok);
    components.add(cancel);
    
    FocusTraversalPolicy policy = new MagellanFocusTraversalPolicy(components);
    setFocusTraversalPolicy(policy);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void actionPerformed(java.awt.event.ActionEvent p1) {
		if(p1.getSource() == more) {
			changeDialog();

			return;
		} else if(p1.getSource() == nameGen) {
			NameGenerator gen = NameGenerator.getInstance();
			name.setText(gen.getName());
			nameGen.setEnabled(gen.isAvailable());

			return;
		}

		approved = (p1.getSource() instanceof JTextField) || (p1.getSource() == ok);
		saveBounds();
		setVisible(false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void show(String newID, String newName) {
		id.setText(newID);
		recruit.setText(null);

		if(settings.getProperty("TempUnitDialog.LastOrderEmpty", "false").equals("true")) {
			order.setText(null);
		} else {
			order.setText(Resources.get("completion.tempunitdialog.order.default") + " ");
		}

		descript.setText(null);
		show(newName);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void show(String newName) {
		approved = false;
		name.setText(newName);

		// mark whole name
		name.getCaret().setDot(0);
		name.getCaret().moveDot(name.getText().length());

		checkNameGen();
		super.setVisible(true);
		saveCostStates();
	}

	protected void saveCostStates() {
		if(giveRecruitCost.isSelected()) {
			settings.setProperty("TempUnitDialog.AddRecruitCost", "true");
		} else {
			settings.remove("TempUnitDialog.AddRecruitCost");
		}

		if(giveMaintainCost.isSelected()) {
			settings.setProperty("TempUnitDialog.AddMaintainCost", "true");
		} else {
			settings.remove("TempUnitDialog.AddMaintainCost");
		}
	}

	protected void checkNameGen() {
		NameGenerator gen = NameGenerator.getInstance();

		if(gen.isActive()) {
			nameCon.add(nameGen, BorderLayout.EAST);
		} else {
			nameCon.remove(nameGen);
		}

		nameGen.setEnabled(gen.isAvailable());
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isApproved() {
		return approved;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean wasExtendedDialog() {
		return getContentPane().getComponentCount() == 7;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getID() {
		return id.getText();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public String getName() {
		return name.getText();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getRecruit() {
		return recruit.getText();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getOrder() {
		return order.getText();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getDescript() {
		return descript.getText();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isGiveRecruitCost() {
		return giveRecruitCost.isSelected();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isGiveMaintainCost() {
		return giveMaintainCost.isSelected();
	}

	protected void changeDialog() {
		Container c = getContentPane();
		int count = c.getComponentCount();

		if(count == 6) { // add
			con.gridx = 0;
			con.gridy = 4;
			con.gridwidth = 3;
			con.gridheight = 1;
			con.fill = GridBagConstraints.HORIZONTAL;
			layout.setConstraints(bPanel, con);
			con.gridy = 2;
			con.gridheight = 2;
			con.gridwidth = 2;
			con.fill = GridBagConstraints.BOTH;
			c.add(morePanel, con);
			more.setText(Resources.get("completion.tempunitdialog.more.less"));
			setFocusList(true);
			settings.setProperty(TempUnitDialog.SETTINGS_KEY, "true");
		} else { // remove
			c.remove(morePanel);
			con.gridx = 0;
			con.gridy = 2;
			con.gridwidth = 3;
			con.gridheight = 1;
			con.fill = GridBagConstraints.HORIZONTAL;
			layout.setConstraints(bPanel, con);
			more.setText(Resources.get("completion.tempunitdialog.more.more"));
			setFocusList(false);
			settings.setProperty(TempUnitDialog.SETTINGS_KEY, "false");
		}

		pack();
		setLocationRelativeTo(posC);
	}


	protected class TablessTextArea extends JTextArea {
		/**
		 * Creates a new TablessTextArea object.
		 *
		 * 
		 * 
		 */
		public TablessTextArea(int rows, int columns) {
			super(rows, columns);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		@Override
    public boolean isManagingFocus() {
			return false;
		}
	}


}
