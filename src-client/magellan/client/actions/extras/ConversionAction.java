// class magellan.client.actions.extras.ConversionAction
// created on Sep 12, 2009
//
// Copyright 2003-2009 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.client.actions.extras;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.event.EventDispatcher;
import magellan.client.swing.InternationalizedDialog;
import magellan.client.swing.basics.SpringUtilities;
import magellan.library.GameData;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.Resources;

public class ConversionAction extends MenuAction {

  private ConversionDialog f;

  /**
   * Creates the menu action.
   * 
   * @param client
   */
  public ConversionAction(Client client) {
    super(client);
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.conversionaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.conversionaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.conversionaction.name");
  }

  /**
   * @see magellan.client.actions.MenuAction#getTooltipTranslated()
   */
  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.conversionaction.tooltip", false);
  }

  @Override
  public void menuActionPerformed(ActionEvent e) {
    if (f == null) {
      f =
          new ConversionDialog(client, client.getDispatcher(), client.getData(), client
              .getProperties());
      f.setVisible(true);
    } else {
      f.requestFocus();
    }
  }

  /**
   * A dialog with a text input field to input an ID.
   * 
   * @author stm
   * @version 1.0, May 31, 2009
   */
  public class ConversionDialog extends InternationalizedDialog {

    private JTextField id36;

    /**
     * Create the dialog
     * 
     * @param client
     * @param dispatcher
     * @param data
     * @param properties
     */
    public ConversionDialog(Client client, EventDispatcher dispatcher, GameData data,
        Properties properties) {
      super(client, false);
      initGUI();
      addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
          id36.requestFocus();
        }
      });
      pack();
    }

    private void initGUI() {
      setTitle(Resources.get("actions.conversionaction.window.title"));
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      setLocationRelativeTo(client);
      setResizable(false);

      id36 = new JTextField("ii", 8);
      id36.setSelectionStart(0);
      id36.setSelectionEnd(2);
      JLabel label36 = new JLabel(Resources.get("actions.conversionaction.label36.text"));
      final JTextField id10 = new JTextField("666", 8);
      id10.setSelectionStart(0);
      id10.setSelectionEnd(3);
      JLabel label10 = new JLabel(Resources.get("actions.conversionaction.label10.text"));

      // convert base 36 number to base 10
      id36.addKeyListener(new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
          try {
            id10.setText(convert(id36.getText(), 36, 10));
          } catch (Exception exc) {
            // conversion went wrong
            id10.setText("---");
          }
        }

      });

      id10.addKeyListener(new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
          try {
            id36.setText(convert(id10.getText(), 10, 36));
          } catch (Exception exc) {
            // conversion went wrong
            id36.setText("---");
          }
        }

      });

      JButton cancelButton = new JButton(Resources.get("actions.conversionaction.cancel.text"));

      JPanel panel = new JPanel();
      panel.setLayout(new SpringLayout());
      panel.setBorder(new EtchedBorder());

      panel.add(label36);
      panel.add(id36);
      panel.add(label10);
      panel.add(id10);
      panel.add(cancelButton);

      getContentPane().add(panel);

      SpringUtilities.makeCompactGrid(panel, 3, 2, 7, 7, 7, 7);

      setDefaultActions(cancelButton, cancelButton, cancelButton, id36, id10);
    }

    @Override
    public void setVisible(boolean b) {
      if (!b) {
        f = null;
      }
      super.setVisible(b);
    }

    @Override
    protected void quit() {
      setVisible(false);
    }

    /**
     * Convert <code>text</code> from <code>inBase</code> to <code>outBase</code>.
     * 
     * @param text
     * @param inBase
     * @param outBase
     * @return
     */
    private String convert(String text, int inBase, int outBase) {
      return IDBaseConverter.toString(IDBaseConverter.parse(text, inBase), outBase);
    }

  }
}
