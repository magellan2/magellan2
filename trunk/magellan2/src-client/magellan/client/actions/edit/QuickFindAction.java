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

package magellan.client.actions.edit;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.swing.InternationalizedDialog;
import magellan.client.swing.basics.SpringUtilities;
import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.Named;
import magellan.library.utils.Resources;


/**
 * An action for quickly finding and selecting a game object by ID.
 *
 * @author stm
 * @version 1.0
 */
public class QuickFindAction extends MenuAction {
  /**
	 * Creates a new FindAction object.
	 *
	 * @param client
	 */
	public QuickFindAction(Client client) {
    super(client);
	}

	/**
	 * Displays the dialog and selects the entity (if found). 
	 */
	@Override
  public void menuActionPerformed(ActionEvent e) {
		QuickFindDialog f = new QuickFindDialog(client, client.getDispatcher(), client.getData(), client.getProperties());
		f.setVisible(true);
		String input = f.getInput();
		  Named o = findEntity(input.trim());
		  if (o!=null)
	      client.getDispatcher().fire(SelectionEvent.create(this, o, SelectionEvent.ST_DEFAULT));
		  else if (false){
		    // we could optionally display a short error message here...
		    JDialog err = new JDialog(client) {
		      boolean started = false;
		      @Override
          public void setVisible(boolean vis){
		        if (vis)
		          // hide dialog after 500 ms
		          new Thread(new Runnable() {

		            public void run() {
		              try {
		                while (!started)
		                  Thread.sleep(500);
		              } catch (InterruptedException e) {
		              }
		              setVisible(false);
		            }
		          }).start();
            super.setVisible(vis);
            started = true;
		      }
        };
        JPanel content = new JPanel();
        content.add(new JLabel(Resources.get("quickfindaction.notfound.label", new Object[] { input.trim()})));
        err.setLocationRelativeTo(client);
        err.setUndecorated(true);
        err.add(content);
        err.pack();
        err.setVisible(true);
		  }
	}

  /**
   * Tries to find a unit, a ship, or a building (in this order) with the id given as string.
   * 
   * @param input
   * @return The found entity or <code>null</code> if none was found
   */
	public Named findEntity(String input) {
    try {
    EntityID id = EntityID.createEntityID(input.trim(), client.getData().base);
    Named  o = client.getData().getUnit(id);
    if (o==null)
      o = client.getData().getShip(id);
    if (o==null)
      o = client.getData().getBuilding(id);
    return o;
    } catch (NumberFormatException exc){
      
    }
    return null;
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.quickfindaction.accelerator",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.quickfindaction.mnemonic",false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.quickfindaction.name");
  }

  /**
   * @see magellan.client.actions.MenuAction#getTooltipTranslated()
   */
  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.quickfindaction.tooltip",false);
  }

  /**
   * A dialog with a text input field to input an ID.
   *
   * @author stm
   * @version 1.0, May 31, 2009
   */
  public class QuickFindDialog extends InternationalizedDialog {

    /** id input box */
    private JTextComponent idInput;
    
    /** text for displaying the found units name */
    private JLabel entityText;
    private JLabel regionText;

    /**
     * Create the dialog
     * 
     * @param client
     * @param dispatcher
     * @param data
     * @param properties
     */
    public QuickFindDialog(Client client, EventDispatcher dispatcher, GameData data,
        Properties properties) {
      super(client, true);
      initGUI();
      pack();
    }

    private void initGUI() {
      setTitle(Resources.get("quickfinddialog.window.title"));
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      setLocationRelativeTo(client);
      setResizable(false);
      
      idInput = new JTextField(8);
      entityText = new JLabel("---");
      regionText = new JLabel("---");
      
      // close dialog if ESCAPE (cancel) or ENTER (find) is pressed
      idInput.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          switch (e.getKeyCode()) {
          case KeyEvent.VK_ESCAPE:
            setVisible(false);
            idInput.setText("");
            break;
          case KeyEvent.VK_ENTER:
            setVisible(false);
            break;
          }
        }
      });
      
      // update entityText
      idInput.getDocument().addDocumentListener(new DocumentListener() {
      
        public void removeUpdate(DocumentEvent e) {
          update(idInput.getText());
        }
      
        public void insertUpdate(DocumentEvent e) {
          update(idInput.getText());
        }
      
        public void changedUpdate(DocumentEvent e) {
          update(idInput.getText());
        }
        public void update(String text) {
          Named o = findEntity(text);
          if (o==null){
            entityText.setText("---");
            regionText.setText("");
          }else{
            entityText.setText(o.getName());
            if (o instanceof HasRegion)
              regionText.setText(((HasRegion) o).getRegion().toString());
          }
          QuickFindDialog.this.validate();
          QuickFindDialog.this.pack();
        }
      
      });
      
      JPanel panel = new JPanel();
      panel.setLayout(new SpringLayout());
      panel.setBorder(new EtchedBorder());
      
      panel.add(new JLabel(Resources.get("quickfinddialog.idinput.label")));
      panel.add(idInput);
      panel.add(entityText);
      panel.add(regionText);
      getContentPane().add(panel);
      
      this.setUndecorated(true);
      SpringUtilities.makeCompactGrid(panel, 2, 2, 7, 7, 7, 7);
    }
    
    /**
     * @return the value of the ID input field
     */
    public String getInput(){
      return idInput.getText();
    }

  }

}
