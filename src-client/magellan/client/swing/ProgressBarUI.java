// class magellan.client.swing.SwingUserInterface
// created on 07.11.2007

// Copyright 2003-2007 by magellan project team

// Author : $Author: $
// $Id: $

// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package magellan.client.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import magellan.client.utils.ErrorWindow;
import magellan.library.gamebinding.MapMetric;
import magellan.library.utils.Resources;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.BoxTransformer.BBox;

public class ProgressBarUI implements UserInterface, ActionListener {
  private static final Logger log = Logger.getInstance(ProgressBarUI.class);
  // user interface
  protected ProgressDlg dlg = null;
  protected boolean showing = false;
  protected boolean ready = false;
  private javax.swing.Timer timer;
  private int delay;
  private Progress progress;

  /**
   * Creates a modal progressbar with standard closing listener.
   * 
   * @param parent
   */
  public ProgressBarUI(JFrame parent) {
    this(parent, true, 0, null);
  }

  /**
   * Creates a modal progressbar with standard closing listener.
   * 
   * @param parent
   */
  public ProgressBarUI(JDialog parent) {
    this(parent, true, 0, null);
  }

  /**
   * Creates a ProgressBar Dialog which can be modal or non-modal. listener.proceed() is called if
   * the dialog receives a WINDOW_CLOSING event(e.g., if the user clicks the dialog's close button.
   * If <code>listener</code> is <code>null</code>, a standard listener shall be used which asks the
   * user for confirmation. If listener.proceed() returns <code>true</code>, the ProgressBarUI is
   * closed.
   * 
   * @param parent The JFrame acting as the parent frame of the dialog.
   * @param modal Whether the dialog should be modal or not
   * @param delay The time after which the dialog shall be shown after show() has been called.
   * @param listener The listener reacting to WINDOW_CLOSING events. If <code>null</code>, a
   *          standard listener shall be used.
   */
  public ProgressBarUI(JFrame parent, boolean modal, int delay, ClosingListener listener) {
    dlg = new ProgressDlg(parent, modal, listener);
    this.delay = delay;
    init();
  }

  /**
   * Creates a ProgressBar Dialog which can be modal or non-modal. listener.proceed() is called if
   * the dialog receives a WINDOW_CLOSING event(e.g., if the user clicks the dialog's close button.
   * If <code>listener</code> is <code>null</code>, a standard listener shall be used which asks the
   * user for confirmation. If listener.proceed() returns <code>true</code>, the ProgressBarUI is
   * closed.
   * 
   * @param parent The JD acting as the parent frame of the dialog.
   * @param modal Whether the dialog should be modal or not
   * @param delay The time after which the dialog shall be shown after show() has been called.
   * @param listener The listener reacting to WINDOW_CLOSING events.
   */
  public ProgressBarUI(JDialog parent, boolean modal, int delay, ClosingListener listener) {
    dlg = new ProgressDlg(parent, modal, listener);
    this.delay = delay;
    init();
  }

  public void addClosingListener(ClosingListener listener) {
    dlg.addClosingListener(listener);
  }

  /**
   * Initialize the user interface.
   */
  private void init() {
    dlg.progressBar.setMinimum(0);
    dlg.progressBar.setMaximum(100);
    setTitle(Resources.get("progressbarui.title.default"));
    setProgress(Resources.get("progressbarui.label.default"), 0);

    timer = new Timer(delay, this);
  }

  /**
   * @see magellan.library.utils.UserInterface#setTitle(java.lang.String)
   */
  public void setTitle(String title) {
    dlg.setTitle(title);
  }

  /**
   * @see magellan.library.utils.UserInterface#setMaximum(int)
   */
  public void setMaximum(int progressmaximum) {
    dlg.progressBar.setIndeterminate(progressmaximum <= 0);
    if (progressmaximum > 0) {
      dlg.progressBar.setMaximum(progressmaximum);
    }
  }

  /**
   * @see magellan.library.utils.UserInterface#setProgress(java.lang.String, int)
   */
  public void setProgress(String strMessage, int iProgress) {
    progress = new Progress();
    progress.strMessage = strMessage;
    progress.iProgress = iProgress;
    SwingUtilities.invokeLater(progress);
  }

  public int getProgress() {
    return progress == null ? -1 : progress.iProgress;
  }

  /**
   * @see magellan.library.utils.UserInterface#show()
   */
  public synchronized void show() {
    if (delay > 0) {
      timer.restart();
    } else if (!showing) {
      doShow();
    }
  }

  /**
   * Called if the timer fires. Shows the dialog (in the AWTEvent thread).
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public synchronized void actionPerformed(ActionEvent e) {
    if (e.getSource() == timer && !showing) {
      doShow();
    }
  }

  protected synchronized void doShow() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (!ready) {
          showing = true;
          try {
            dlg.setVisible(true);
          } catch (RuntimeException t) {
            throw t;
          }
        }
      }
    });
  }

  /**
   * Hide window.
   * 
   * @see magellan.library.utils.UserInterface#ready()
   */
  public synchronized void ready() {
    timer.stop();
    ready = true;
    if (!showing) {
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (!showing) {
          log.finer("sleep disabled");
        }
        if (dlg.isVisible()) {
          dlg.setVisible(false);
        }
        // if the progress dialog hasn't been set visible, because invokeLater is
        // waiting for an event, we cannot dispose the dialog, because this would
        // wait, too and could cause a deadlock. Therefore we only dispose if it
        // has already been shown.
        if (showing) {
          dlg.dispose();
        }
      }
    });
  }

  public boolean isVisible() {
    return dlg.isVisible();
  }

  /**
   * @see magellan.library.utils.UserInterface#confirm(java.lang.String, java.lang.String)
   */
  public boolean confirm(String strMessage, String strTitle) {
    Confirm conf = new Confirm(strTitle, strMessage);

    try {
      SwingUtilities.invokeAndWait(conf);
    } catch (Throwable e) {
      ProgressBarUI.log.error(e);
    }

    return conf.bResult;
  }

  /**
   * @see magellan.library.utils.UserInterface#input(java.lang.String, java.lang.String,
   *      java.lang.Object[], java.lang.Object)
   */
  public Object input(String strMessage, String strTitle, Object[] values, Object initialSelection) {
    Input input = new Input(strMessage, strTitle, values, initialSelection);
    try {
      SwingUtilities.invokeAndWait(input);
    } catch (Throwable e) {
      ProgressBarUI.log.error(e);
    }
    return input.sResult;
  }

  public void showException(String message, String description, Throwable exception) {
    ErrorWindow ew =
        message == null ? new ErrorWindow(Resources.get("progressbarui.message.unknownerror"),
            exception) : new ErrorWindow(message, description, exception);
    ew.setShutdownOnCancel(false);
    ew.setVisible(true);

    // throw new RuntimeException(exception);
  }

  public void showMessageDialog(String message) {
    JOptionPane.showMessageDialog(dlg, message);
  }

  public static ClosingListener getDefaultClosingListener(final Component parent) {
    return new ClosingListener() {

      public boolean close(WindowEvent e) {
        return (JOptionPane.showConfirmDialog(parent, Resources.get("progressbarui.abort.message"),
            Resources.get("progressbarui.abort.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
      }

    };
  }

  /**
   *
   */
  private static class ProgressDlg extends JDialog {

    private List<ClosingListener> closingListeners;

    /**
     * @see java.awt.Window#processEvent(java.awt.AWTEvent)
     */
    @Override
    protected void processEvent(AWTEvent e) {
      if (e instanceof WindowEvent) {
        WindowEvent we = (WindowEvent) e;
        if (we.getID() != WindowEvent.WINDOW_CLOSING) {
          super.processEvent(e);
        } else {
          for (ClosingListener l : closingListeners) {
            if (!l.close(we)) {
              ProgressBarUI.log.info("abort aborted");
              return;
            }
          }
          ProgressBarUI.log.info("aborted");
          super.processEvent(e);
        }
      } else {
        super.processEvent(e);
      }

    }

    public JLabel labelText;
    public JProgressBar progressBar;

    /**
     * Creates new form ProgressDlg
     */
    public ProgressDlg(Dialog parent, boolean modal, ClosingListener listener) {
      super(parent, modal);
      // setUndecorated(true);
      init(listener);
    }

    /**
     * Creates new form ProgressDlg
     */
    public ProgressDlg(Frame parent, boolean modal, ClosingListener listener) {
      super(parent, modal);
      // setUndecorated(true);
      init(listener);
    }

    protected void init(ClosingListener listener) {
      closingListeners = new LinkedList<ClosingListener>();
      if (listener == null) {
        closingListeners.add(ProgressBarUI.getDefaultClosingListener(ProgressDlg.this));
      } else {
        closingListeners.add(listener);
      }

      initComponents();
      super.setFocusableWindowState(false);
      setLocationRelativeTo(getParent());
      pack();
    }

    /**
     * Adds a listener that is called on WINDOW_CLOSING events.
     * 
     * @param listener
     */
    public void addClosingListener(ClosingListener listener) {
      closingListeners.add(listener);
    }

    /**
     *
     */
    private void initComponents() {
      labelText = new JLabel();
      progressBar = new JProgressBar();
      getContentPane().setLayout(new GridBagLayout());

      GridBagConstraints gridBagConstraints1;
      setTitle(Resources.get("progressbarui.title.default"));

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

    public Confirm(String t, String m) {
      strTitle = t;
      strMessage = m;
    }

    /**
     * 
     */
    public void run() {
      if (JOptionPane.showConfirmDialog(dlg, strMessage, strTitle, JOptionPane.YES_NO_OPTION,
          JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
        bResult = true;
      } else {
        bResult = false;
      }
    }
  }

  private class Input implements Runnable {
    String strMessage;
    String strTitle;

    Object[] values;
    Object initialSelection;

    Object sResult = null;

    public Input(String m, String t, Object[] val, Object initial) {
      strMessage = m;
      strTitle = t;
      values = val;
      initialSelection = initial;
    }

    /**
     * 
     */
    public void run() {
      sResult =
          JOptionPane.showInputDialog(dlg, strMessage, strTitle, JOptionPane.QUESTION_MESSAGE,
              null, values, initialSelection);
    }
  }

  public void showDialog(String title, String message, int messageType, int options) {
    final JDialog dialog =
        (new JOptionPane(message, messageType, options)).createDialog(dlg, title);
    try {
      SwingUtilities.invokeAndWait(new Runnable() {

        public void run() {
          dialog.setVisible(true);
        }
      });
    } catch (Exception e) {
      ProgressBarUI.log.error(e);
    }
  }

  public BBox askForGirth(BBox best, int layer, MapMetric metric) {
    final SetGirthDialog dialog = new SetGirthDialog(null, best, layer, metric);
    try {
      SwingUtilities.invokeAndWait(new Runnable() {

        public void run() {
          dialog.setVisible(true);
        }
      });
    } catch (Exception e) {
      ProgressBarUI.log.error(e);
    }
    if (dialog.approved())
      return dialog.getNewBorders().getBox(layer);
    else
      return null;
  }

}
