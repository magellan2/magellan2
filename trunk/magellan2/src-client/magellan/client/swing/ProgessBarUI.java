// class magellan.client.swing.SwingUserInterface
// created on 07.11.2007
//
// Copyright 2003-2007 by magellan project team
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.client.swing;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import magellan.library.utils.Resources;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;

public class ProgessBarUI implements UserInterface {
  private static final Logger log = Logger.getInstance(ProgessBarUI.class);
  // user interface
  protected ProgressDlg dlg = null;
  protected boolean showing;

  public ProgessBarUI(JFrame parent) {
    dlg = new ProgressDlg(parent, true);
    init();
  }

  public ProgessBarUI(JDialog parent) {
    dlg = new ProgressDlg(parent, true);
    init();
  }

  /**
   * Initialize the user interface.
   */
  private void init() {
    dlg.labelText.setText(Resources.get("util.reportmerger.status.merge"));
    dlg.progressBar.setMinimum(0);
    dlg.progressBar.setMaximum(100);
  }
  
  /**
   * @see magellan.library.utils.UserInterface#setMaximum(int)
   */
  public void setMaximum(int progressmaximum) {
    //dlg.progressBar.setMaximum(reports.length * 4);
    dlg.progressBar.setMaximum(progressmaximum);
  }
  
  /**
   * @see magellan.library.utils.UserInterface#show()
   */
  public void show() {
    showing=true;
    SwingUtilities.invokeLater((new Runnable() {public void run() {
      ProgessBarUI.this.dlg.setVisible(true);
    }})); 
  }

  /**
   * @see magellan.library.utils.UserInterface#confirm(java.lang.String, java.lang.String)
   */
  public boolean confirm(String strMessage, String strTitle) {
    Confirm conf = new Confirm();
    conf.strMessage = strMessage;
    conf.strTitle = strTitle;

    try {
      SwingUtilities.invokeAndWait(conf);
    } catch(Exception e) {
      log.error(e);
    }

    return conf.bResult;
  }

  /**
   * @see magellan.library.utils.UserInterface#setProgress(java.lang.String, int)
   */
  public void setProgress(String strMessage, int iProgress) {
    Progress progress = new Progress();
    progress.strMessage = strMessage;
    progress.iProgress = iProgress;

    SwingUtilities.invokeLater(progress);
  }

  /**
   * @see magellan.library.utils.UserInterface#ready()
   */
  public void ready() {
    if (showing) {
      // wait for dialog to come up in the first place
      while (!dlg.isShowing()) {
        log.debug("ready " + dlg.isShowing());
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    showing = false;
    log.debug("ready " + dlg.isShowing());
    dlg.setVisible(false);
    dlg.dispose();
  }
  
  public boolean isVisible() {
    return dlg.isVisible();
  }

  /**
   * @see magellan.library.utils.UserInterface#setTitle(java.lang.String)
   */
  public void setTitle(String title) {
    dlg.setTitle(title);
  }

  
  /**
   *
   */
  private class ProgressDlg extends JDialog {

    public JLabel labelText;
    public JProgressBar progressBar;
    
    /**
     * Creates new form ProgressDlg
     */
    public ProgressDlg(Dialog parent, boolean modal) {
      super(parent, modal);
      init();
    }
    
    /**
     * Creates new form ProgressDlg
     */
    public ProgressDlg(Frame parent, boolean modal) {
      super(parent, modal);
      init();
    }
    
    protected void init() {
      initComponents();
      pack();
      
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);

    }

    /**
     *
     */
    private void initComponents() {
      labelText = new JLabel();
      progressBar = new JProgressBar();
      getContentPane().setLayout(new GridBagLayout());

      GridBagConstraints gridBagConstraints1;
      setTitle(Resources.get("util.reportmerger.window.title"));

      labelText.setPreferredSize(new Dimension(250, 16));
      labelText.setMinimumSize(new Dimension(250, 16));
      labelText.setText("jLabel1");
      labelText.setHorizontalAlignment(SwingConstants.LEFT);
      labelText.setMaximumSize(new Dimension(32767, 16));

      gridBagConstraints1 = new GridBagConstraints();
      gridBagConstraints1.gridx = 0;
      gridBagConstraints1.gridy = 1;
      gridBagConstraints1.fill = GridBagConstraints.BOTH;
      gridBagConstraints1.insets = new Insets(0, 5, 5, 5);
      gridBagConstraints1.weightx = 1.0;
      gridBagConstraints1.weighty = 0.5;
      getContentPane().add(labelText, gridBagConstraints1);

      progressBar.setPreferredSize(new Dimension(250, 14));
      progressBar.setMinimumSize(new Dimension(250, 14));

      gridBagConstraints1 = new GridBagConstraints();
      gridBagConstraints1.fill = GridBagConstraints.BOTH;
      gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
      gridBagConstraints1.weightx = 1.0;
      gridBagConstraints1.weighty = 0.5;
      getContentPane().add(progressBar, gridBagConstraints1);
    }
  }


  /**
   *
   */
  private class Progress implements Runnable {
    String strMessage;
    int iProgress;

    /**
     *
     */
    public void run() {
      dlg.labelText.setText(strMessage);
      dlg.progressBar.setValue(iProgress);
    }
  }

  /**
   *
   */
 private class Confirm implements Runnable {
   String strMessage;
   String strTitle;
   boolean bResult = false;

   /**
    * 
    */
   public void run() {
     if(JOptionPane.showConfirmDialog(dlg, strMessage,
         Resources.get("util.reportmerger.msg.confirmmerge.title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
       bResult = true;
     } else {
       bResult = false;
     }
   }
 }

}

