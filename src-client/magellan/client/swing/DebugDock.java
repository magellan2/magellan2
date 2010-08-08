// class magellan.client.swing.DebugDock
// created on 14.07.2008
//
// Copyright 2003-2008 by magellan project team
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import magellan.client.desktop.MagellanDesktop;
import magellan.library.utils.logging.AbstractLogListener;
import magellan.library.utils.logging.LogListener;
import magellan.library.utils.logging.Logger;

/**
 * This is a small listener dock that prints all log events to a textarea.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 14.07.2008
 */
public class DebugDock extends JPanel implements LogListener {
  private static final Logger log = Logger.getInstance(DebugDock.class);

  /** @deprecated use {@link MagellanDesktop#DEBUG_IDENTIFIER} instead. */
  @Deprecated
  public static String IDENTIFIER = MagellanDesktop.DEBUG_IDENTIFIER;

  private static DebugDock INSTANCE = null;
  private static final int BUFFER_LENGTH = 50 * 1024;

  protected JTextArea logArea = null;
  protected Calendar calendar = Calendar.getInstance();
  protected AbstractLogListener defaultListener = new AbstractLogListener() {
    @Override
    public String getDate() {
      calendar.setTimeInMillis(System.currentTimeMillis());
      return super.getDate();
      // return DebugDock.toDay(calendar) + " " + toTime(calendar);
    }
  };

  protected DebugDock() {
    Logger.addLogListener(this);
    // Logger.activateDefaultLogListener(true);
    init();
  }

  public static DebugDock getInstance() {
    if (DebugDock.INSTANCE == null) {
      DebugDock.INSTANCE = new DebugDock();
    }
    return DebugDock.INSTANCE;
  }

  /**
   * Initializes the GUI
   */
  protected void init() {
    setLayout(new BorderLayout());

    logArea = new JTextArea();
    logArea.setEditable(false);

    JScrollPane scrollPane = new JScrollPane(logArea);
    scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    scrollPane.setBackground(logArea.getBackground());
    scrollPane.setWheelScrollingEnabled(true);
    scrollPane.setPreferredSize(new Dimension(1, 220));
    scrollPane.setMinimumSize(new Dimension(1, 220));

    add(scrollPane, BorderLayout.CENTER);
  }

  /**
   * 
   */
  public void debug(String str) {
    DebugDock.log.debug(str);
  }

  /**
   * 
   */
  public void info(String str) {
    DebugDock.log.info(str);
  }

  /**
   * 
   */
  public void warn(String str) {
    DebugDock.log.warn(str);
  }

  /**
   * 
   */
  public void error(String str) {
    DebugDock.log.error(str);
  }

  /**
   * 
   */
  public void fatal(String str) {
    DebugDock.log.fatal(str);
  }

  /**
   * @see magellan.library.utils.logging.LogListener#log(int, java.lang.Object, java.lang.Throwable)
   */
  public void log(int level, Object obj, Throwable throwable) {
    String str = defaultListener.getMessage(level, obj, null);
    setStatus(str, throwable);
  }

  /**
   * 
   */
  protected void setStatus(String message, Throwable throwable) {
    if (logArea != null) {
      StringBuffer buffer = new StringBuffer(logArea.getText().trim());

      buffer.append("\n").append(message);

      // Text-Area eingrenzen
      if (buffer.length() > DebugDock.BUFFER_LENGTH) {
        buffer.delete(0, buffer.length() - DebugDock.BUFFER_LENGTH);
      }

      logArea.setText(buffer.toString());
      logArea.setCaretPosition(buffer.length() - message.length() + 2);
    }
  }

  /**
   * Returns the string representation of a time
   */
  protected String toTime(Calendar calendar) {
    if (calendar == null)
      return "";
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    int min = calendar.get(Calendar.MINUTE);
    int sec = calendar.get(Calendar.SECOND);
    String r = "";
    if (hour < 10) {
      r += "0";
    }
    r += hour + ":";
    if (min < 10) {
      r += "0";
    }
    r += min + ":";
    if (sec < 10) {
      r += "0";
    }
    r += sec;
    return r;
  }

  /**
   * Returns the string representation of a day
   */
  protected static String toDay(Calendar calendar) {
    if (calendar == null)
      return "";
    String d = "";
    if (calendar.get(Calendar.DAY_OF_MONTH) < 10) {
      d += "0";
    }
    d += calendar.get(Calendar.DAY_OF_MONTH);
    d += ".";
    int month = calendar.get(Calendar.MONTH) + 1;
    if (month < 10) {
      d += "0";
    }
    d += month;
    d += ".";
    int year = calendar.get(Calendar.YEAR);
    if (year < 10) {
      d += "0";
    }
    d += year;
    return d;
  }

  /**
   * Returns the stack trace of a throwable
   */
  protected static String toString(Throwable exception) {
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
}
