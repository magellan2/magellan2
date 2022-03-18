// class magellan.client.utils.ErrorWindow
// created on 19.05.2007
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
package magellan.client.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This class provides an error window for presenting any error message on the screen. It is very
 * important in this class to prevent a method to throw any error or exception (maximum error
 * handling and error robustness).<br>
 * <br>
 * <b>ToDo:</b> Replace the extends to JDialog or something like that to enable modal behavior. Also
 * make it possible to set the window on top of any other window...and so on...
 * 
 * @author Thoralf Rickert
 * @version 0.1
 */
public class ErrorWindow extends JDialog implements ActionListener, WindowCloseable {
  private static final Logger log = Logger.getInstance(ErrorWindow.class);

  private static final String SHOW_DETAILS_BUTTON = Resources.get("buttons.details.more");

  private static final String HIDE_DETAILS_BUTTON = Resources.get("buttons.details.less");

  private static final String UNKNOWN_ERROR_MESSAGE = Resources.get("errorwindow.unknown");

  protected JTextArea errorMessage = null;

  protected JTextArea errorDescription = null;

  protected JScrollPane scrollPane = null;

  protected boolean shutdownOnCancel = true;

  protected boolean cleanShutdown = true;

  protected JButton okButton = null;

  protected JButton cancelButton = null;

  protected JButton detailsButton = null;

  protected int xSize = 0;

  protected int ySize = 0;

  protected boolean actionPerformed = false;

  private static enum ActionCommand {
    CANCEL, DETAILS, OK;
  }

  // **********************************************************************
  /**
   */

  public ErrorWindow() {
    this((JFrame) null, null, null, null);
  }

  // **********************************************************************
  /**
   * @param message is a user friendly message with a question that the user can answer with "OK" or
   *          "Cancel".
   */

  public ErrorWindow(String message) {
    this((JFrame) null, message, null, null);
  }

  // **********************************************************************
  /**
   * @param message is a user friendly message with a question that the user can answer with "OK" or
   *          "Cancel".
   * @param throwable is an occurred exception or error, that couldn't be served or caught.
   */

  public ErrorWindow(String message, Throwable throwable) {
    this((JFrame) null, message, null, throwable);
  }

  // **********************************************************************
  /**
   * @param throwable is an occurred exception or error, that couldn't be served or caught.
   */

  public ErrorWindow(Throwable throwable) {
    this((JFrame) null, ErrorWindow.UNKNOWN_ERROR_MESSAGE, null, throwable);
    if (throwable != null && throwable.getMessage() != null) {
      setErrorMessage(throwable.getMessage(), null, throwable);
    }
  }

  // **********************************************************************
  /**
   * @param message is a user friendly message with a question that the user can answer with "OK" or
   *          "Cancel".
   * @param description is a longer description of the error for experts.
   * @param throwable is an occured exception or error, that couldn't be served or catched.
   */

  public ErrorWindow(String message, String description, Throwable throwable) {
    this((Frame) null, message, description, throwable);
  }

  public ErrorWindow(Frame owner, Throwable throwable) {
    this(owner, null, null, throwable);
  }

  // **********************************************************************
  /**
   * @param owner is the parent window where the error occured.
   * @param message is a user friendly message with a question that the user can answer with "OK" or
   *          "Cancel".
   * @param description is a longer description of the error for experts.
   * @param throwable is an occured exception or error, that couldn't be served or catched.
   */

  public ErrorWindow(Frame owner, String message, String description, Throwable throwable) {
    super(owner, true);
    setup();
    pack();
    setErrorMessage(message, description, throwable);
  }

  // **********************************************************************
  /**
   * @param owner is the parent window where the error occured.
   * @param message is a user friendly message with a question that the user can answer with "OK" or
   *          "Cancel".
   * @param description is a longer description of the error for experts.
   * @param throwable is an occured exception or error, that couldn't be served or catched.
   */

  public ErrorWindow(Dialog owner, String message, String description, Throwable throwable) {
    super(owner, true);
    setup();
    pack();
    setErrorMessage(message, description, throwable);
  }

  // **********************************************************************
  /**
   * Sets the error message of an error inside this dialog.
   * 
   * @param message is a user friendly message with a question that the user can answer with "OK" or
   *          "Cancel".
   */

  public void setErrorMessage(String message) {
    setErrorMessage(message, null, null);
  }

  // **********************************************************************
  /**
   * Sets the error message and description of an error inside this dialog.
   * 
   * @param message is a user friendly message with a question that the user can answer with "OK" or
   *          "Cancel".
   * @param throwable is an occured exception or error, that couldn't be served or catched.
   */

  public void setErrorMessage(String message, Throwable throwable) {
    setErrorMessage(message, null, throwable);
  }

  // **********************************************************************
  /**
   * Sets the error message and description of an error inside this dialog.
   * 
   * @param message is a user friendly message with a question that the user can answer with "OK" or
   *          "Cancel".
   * @param description is a longer description of the error for experts.
   */

  public void setErrorMessage(String message, String description) {
    setErrorMessage(message, description, null);
  }

  // **********************************************************************
  /**
   * Sets the error message and description of an error inside this dialog.
   * 
   * @param message is a user friendly message with a question that the user can answer with "OK" or
   *          "Cancel".
   * @param description is a longer description of the error for experts.
   * @param throwable is an occured exception or error, that couldn't be served or catched.
   */

  public void setErrorMessage(String message, String description, Throwable throwable) {
    if (message == null) {
      // replace the message by a default message
      message = ErrorWindow.UNKNOWN_ERROR_MESSAGE;
    }
    errorMessage.setText(message);
    pack();
    validate();

    StringBuffer sb = new StringBuffer();
    sb.append(message);
    sb.append("\n\n");
    if (description != null) {
      sb.append(description).append("\n\n");
    }

    if (throwable != null) {
      sb.append(getExceptionAsString(throwable));
    }

    errorDescription.setText(sb.toString());
    errorDescription.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
    detailsButton.setEnabled(description != null || throwable != null);
  }

  /**
   * Liefert den StackTrace als String zurück.
   */
  private String getExceptionAsString(Throwable exception) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream stream = new PrintStream(baos);
      exception.printStackTrace(stream);
      stream.close();
      baos.close();
      return baos.toString();
    } catch (Exception e) {
      return "";
    }
  }

  // **********************************************************************
  /**
   */

  protected void setup() {
    ErrorWindow.log.debug("ErrorWindow setup...");

    addWindowListener(new WindowClosingDispatcher(this));

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(0, 1, 5, 5));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    okButton = new JButton(Resources.get("button.continue"));
    okButton.setRequestFocusEnabled(false);
    okButton.setActionCommand(ActionCommand.OK.toString());
    okButton.addActionListener(this);
    // okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonPanel.add(okButton);

    cancelButton = new JButton(Resources.get("button.quit"));
    cancelButton.setRequestFocusEnabled(false);
    cancelButton.setActionCommand(ActionCommand.CANCEL.toString());
    cancelButton.addActionListener(this);
    // cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonPanel.add(cancelButton);

    buttonPanel.add(Box.createRigidArea(new Dimension(1, 10)));
    buttonPanel.add(Box.createGlue());

    detailsButton = new JButton(ErrorWindow.SHOW_DETAILS_BUTTON);
    detailsButton.setRequestFocusEnabled(false);
    detailsButton.setActionCommand(ActionCommand.DETAILS.toString());
    detailsButton.addActionListener(this);
    // detailsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonPanel.add(detailsButton);
    buttonPanel.add(Box.createGlue());

    errorMessage = new TextArea();
    errorMessage.setText("An error occurs");
    errorMessage.setRequestFocusEnabled(false);
    errorMessage.setEditable(false);
    errorMessage.setBackground(getContentPane().getBackground());
    errorMessage.setSelectionColor(getContentPane().getBackground());
    errorMessage.setSelectedTextColor(getContentPane().getForeground());
    errorMessage.setFont(okButton.getFont());
    errorMessage.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    errorMessage.setWrapStyleWord(true);
    errorMessage.setLineWrap(true);

    JScrollPane scrollPane2 = new JScrollPane(errorMessage);
    scrollPane2.setViewportBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    scrollPane2.setWheelScrollingEnabled(true);
    scrollPane2.setPreferredSize(new Dimension(200, 220));
    scrollPane2.setBorder(BorderFactory.createEmptyBorder());

    errorDescription = new TextArea();
    // errorDescription.setContentType("text/plain");
    errorDescription.setText("If you can see this, something went wrong.");
    errorDescription.setBackground(getContentPane().getBackground());
    errorDescription.setFont(new Font("Courier New", Font.PLAIN, 12));
    errorDescription.setEditable(false);
    errorDescription.setMinimumSize(new Dimension(500, 1));
    errorDescription.setLineWrap(false);

    scrollPane = new JScrollPane(errorDescription);
    // scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    scrollPane.setWheelScrollingEnabled(true);
    scrollPane.setPreferredSize(new Dimension(10, 220));
    scrollPane.setMinimumSize(new Dimension(10, 50));
    scrollPane.setVisible(false);

    getContentPane().setLayout(new BorderLayout());
    // GridBagConstraints gc =
    // new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.FIRST_LINE_START,
    // GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 2, 2);
    // gc.fill = GridBagConstraints.HORIZONTAL;
    // gc.weightx = 1;
    // gc.weighty = 0.1;
    // getContentPane().add(scrollPane2, gc);
    JPanel p = new JPanel(new BorderLayout());
    p.add(scrollPane2, BorderLayout.CENTER);
    p.add(buttonPanel, BorderLayout.EAST);
    getContentPane().add(p, BorderLayout.NORTH);

    scrollPane2.setPreferredSize(new Dimension(550, 100));

    // gc.gridx++;
    // gc.fill = GridBagConstraints.NONE;
    // gc.weightx = 0;
    // getContentPane().add(buttonPanel, gc);
    // getContentPane().add(buttonPanel, BorderLayout.EAST);
    // gc.gridx = 0;
    // gc.gridy++;
    // gc.gridwidth = 2;
    // gc.fill = GridBagConstraints.BOTH;
    // gc.weighty = 1;
    // gc.weightx = 1;
    //
    // getContentPane().add(scrollPane, gc);
    getContentPane().add(scrollPane, BorderLayout.CENTER);

    setTitle("Fehler");
  }

  // **********************************************************************
  /**
   */

  public void actionPerformed(ActionEvent event) {
    ActionCommand cmd = ActionCommand.valueOf(event.getActionCommand());

    ErrorWindow.log.debug("User selected action '" + cmd + "'");

    switch (cmd) {
    case OK: {
      // close window
      close();
      actionPerformed = true;
      break;
    }
    case CANCEL: {
      // close window
      if (JOptionPane.showConfirmDialog(this, Resources.get(
          "errorwindow.confirmquit.message"), null,
          JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        close();
        // and exit???
        if (shutdownOnCancel) {
          if (cleanShutdown) {
            System.exit(0);
          } else {
            System.exit(1);
          }
        }
        actionPerformed = true;
      }
      break;
    }
    case DETAILS: {
      // check details state
      if (isDetailed()) {
        hideDetails();
      } else {
        showDetails();
      }
      repaint();
      break;
    }
    }
  }

  // **********************************************************************
  /**
   */

  public void windowClosing() {
    ErrorWindow.log.debug("Closing MainWindow...");
    actionPerformed = true;
  }

  static Object lock = null;
  static int i;

  // **********************************************************************
  /**
   */
  public void open() {
    if (lock != null) {
      synchronized (lock) {
        ErrorWindow.log.warn("Error Window instance " + ++i);
        pack();
        validate();

        setVisible(true);
        --i;
      }
    } else {
      lock = this;
      ++i;
      ErrorWindow.log.debug("Open Window");
      pack();
      validate();

      setVisible(true);
      --i;
      lock = null;
    }

  }

  // **********************************************************************
  /**
   */

  public void close() {
    ErrorWindow.log.debug("Close Window");
    setVisible(false);
  }

  /**
   * @see java.awt.Dialog#setVisible(boolean)
   */
  @Override
  public void setVisible(boolean b) {
    if (b) {
      log.error("Error window: " + (errorMessage != null ? errorMessage.getText() : "") + " : "
          + (errorDescription != null ? errorDescription.getText() : ""));
    }
    super.setVisible(b);
  }

  // **********************************************************************
  /**
   */

  protected boolean isDetailed() {
    String currentDetailsButtonText = detailsButton.getText();
    if (currentDetailsButtonText.equals(ErrorWindow.SHOW_DETAILS_BUTTON))
      return false;
    else if (currentDetailsButtonText.equals(ErrorWindow.HIDE_DETAILS_BUTTON))
      return true;
    else
      return false;
  }

  // **********************************************************************
  /**
   */

  protected void showDetails() {
    ErrorWindow.log.debug("Open details panel...");
    detailsButton.setText(ErrorWindow.HIDE_DETAILS_BUTTON);
    scrollPane.setVisible(true);
    scrollPane.scrollRectToVisible(new Rectangle(0, 0, 0, 0));

    pack();
    validate();
  }

  // **********************************************************************
  /**
   */

  protected void hideDetails() {
    ErrorWindow.log.debug("Close details panel...");
    detailsButton.setText(ErrorWindow.SHOW_DETAILS_BUTTON);
    scrollPane.setVisible(false);
    pack();
    validate();
  }

  // **********************************************************************
  /**
   * Whether to stop the application with a System.exit(0) or not.
   */

  public void setShutdownOnCancel(boolean shutdownOnCancel) {
    this.shutdownOnCancel = shutdownOnCancel;
    cancelButton.setEnabled(shutdownOnCancel);
  }

  // **********************************************************************
  /**
   * This method sets the window dimension and positions the window to the center of the screen.
   */

  public void setWindowSize(int xSize, int ySize) {
    this.xSize = xSize;
    this.ySize = ySize;
    if (xSize > 0 && ySize > 0) {
      int x = getToolkit().getScreenSize().width;
      int y = getToolkit().getScreenSize().height;
      setSize(xSize, ySize);
      setLocation(new Point((x / 2 - xSize / 2), (y / 2 - ySize / 2)));
    }
  }

  /**
   * @param cleanShutdown Wenn der Wert wahr ist, dann wird ein sauberer Shutdown durchgeführt und
   *          ErrorWindow.shutdown aufgerufen. Wenn der Wert falsch ist, dann wird direkt
   *          System.exit aufgerufen und nur noch der Start-ShutdownHook Prozess wird gestartet.
   */
  public void setCleanShutdown(boolean cleanShutdown) {
    this.cleanShutdown = cleanShutdown;
  }
}

/**
 * Dies ist ein Bugfix für JTextArea in Verbindung mit JScrollPane. Nach dem Einfügen von Text wird
 * die CaretPosition auf 0 gesetzt, um den Textinhalt von oben nach unten lesen zu können. Außerdem
 * wird beim Disablen die Farbe der Komponente geändert und nicht wirklich disabled (weil sieht
 * scheiße aus...)
 * 
 * @author Thoralf Rickert
 * @version 29.10.2003
 */
class TextArea extends JTextArea {
  @Override
  public void setText(String text) {
    super.setText(text);
    setCaretPosition(0);
  }

  @Override
  public void setEnabled(boolean value) {
    // super.setEnabled(value);
    if (value) {
      setBackground(Color.WHITE);
      setForeground(Color.BLACK);
      setSelectionColor(Color.BLUE);
      setSelectedTextColor(Color.WHITE);
    } else {
      setBackground(Color.LIGHT_GRAY);
      setForeground(Color.BLACK);
      setSelectionColor(getBackground());
      setSelectedTextColor(getForeground());
      /*
       * setRequestFocusEnabled(false); setEditable(false);
       */
    }
  }
}

/**
 * This class can be used from any Frame or Window. It is useful to implement an external
 * WindowAdapter to close a window. The using window class should add an instance of this class to
 * the the list of listener.
 * 
 * @author Thoralf Rickert
 * @version 0.1
 */

class WindowClosingDispatcher extends WindowAdapter {

  private WindowCloseable listener = null;

  // **********************************************************************
  /**
   * This constructor creates an dispatcher for the window closing event. You should call
   * {@link #setListener(WindowCloseable)} to set the instance of the window that should be used.
   */

  public WindowClosingDispatcher() {
    listener = null;
  }

  // **********************************************************************
  /**
   */

  public WindowClosingDispatcher(WindowCloseable listener) {
    setListener(listener);
  }

  // **********************************************************************
  /**
   */

  public void setListener(WindowCloseable listener) {
    this.listener = listener;
  }

  // **********************************************************************
  /**
   */

  public WindowCloseable getListener() {
    return listener;
  }

  // **********************************************************************
  /**
   */

  @Override
  public void windowClosing(WindowEvent event) {
    if (listener != null) {
      listener.windowClosing();
    } else {
      Logger.getInstance(this.getClass().getName()).error(
          "WindowClosingDispatcher.windowClosing() failed (listener=null)");
    }
  }
}

/**
 * This interface represents a Java AWT or Swing window that can be closed. A class that implements
 * this interface has the method {@link #windowClosing()} that will be used if the window must be
 * closed.
 * 
 * @author Thoralf Rickert
 * @version 0.1
 */

interface WindowCloseable {

  // **********************************************************************
  /**
   * This method will be called from the {@link WindowClosingDispatcher} if the user wants to close
   * the window (f.e. by using the system independent close function (the X in the window title) or
   * ALT+F4 on MS Windows and some X-Windows WindowManagers).
   */

  public void windowClosing();
}

/**
 * Dies ist ein Bugfix für JEditorPane in Verbindung mit JScrollPane. Nach dem Einfügen von Text
 * wird die CaretPosition auf 0 gesetzt, um den Textinhalt von oben nach unten lesen zu können.
 * Außerdem wird beim Disablen die Farbe der Komponente geändert und nicht wirklich disabled (weil
 * sieht scheiße aus...)
 * 
 * @author Thoralf Rickert
 * @version 29.10.2003
 */
class EditorPane extends JEditorPane {
  @Override
  public void setText(String text) {
    super.setText(text);
    setCaretPosition(0);
  }

  @Override
  public void setEnabled(boolean value) {
    // super.setEnabled(value);
    if (value) {
      setBackground(Color.WHITE);
      setForeground(Color.BLACK);
      setSelectionColor(Color.BLUE);
      setSelectedTextColor(Color.WHITE);
    } else {
      setBackground(Color.LIGHT_GRAY);
      setForeground(Color.BLACK);
      setSelectionColor(getBackground());
      setSelectedTextColor(getForeground());
      /*
       * setRequestFocusEnabled(false); setEditable(false);
       */
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Errors");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.add(new JLabel("a label"));
    frame.setLocationByPlatform(true);
    frame.pack();
    frame.setVisible(true);
    ErrorWindow e;

    int i[] = { 0 };
    e = new ErrorWindow(new Random().ints()
        .takeWhile((x) -> i[0]++ < 100)
        .map((x) -> x < 0 ? -x : x)
        .mapToObj((x) -> String.valueOf(x))
        .reduce("\n", (x, y) -> i[0] % 10 == 0 ? x + y + "  ---- " : x + y),
        "detail", new NumberFormatException("what is a number?"));
    e.open();

    e = new ErrorWindow();
    e.open();

    e = new ErrorWindow("Hello, Message!");
    e.open();

    e = new ErrorWindow(new RuntimeException("Hello, Exception"));
    e.open();

    e = new ErrorWindow("Hello, an Exception!", new NullPointerException("an exception"));
    e.open();

    e = new ErrorWindow("Message", "Description", new Exception("exception"));
    e.open();

    e = new ErrorWindow(new JDialog(frame.getOwner()), "Message", "Description", new Exception("exception"));
    e.open();

    e = new ErrorWindow(frame, "Message", "Description", new Exception("exception"));
    e.open();

    System.exit(0);
  }
}
