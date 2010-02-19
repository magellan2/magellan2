// class magellan.library.utils.logging.AbstractLogListener
// created on Feb 19, 2010
//
// Copyright 2003-2010 by magellan project team
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
package magellan.library.utils.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Calendar;

import magellan.library.utils.Utils;

/**
 * Use this class to create your own log listeners.
 * 
 * @author stm
 */
public class AbstractLogListener {
  protected Calendar calendar = Calendar.getInstance();

  /**
   * Returns a standard message that includes a time stamp and, if <code>aThrowable!=null</code>, a
   * stack trace.
   */
  public String getMessage(int aLevel, Object aObj, Throwable aThrowable) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);

    calendar.setTimeInMillis(System.currentTimeMillis());

    String prefix = getMarker(aLevel);
    ps.print(prefix + " ");
    ps.print(getDate());
    ps.print(": ");

    if (aObj != null) {
      if (aObj instanceof Throwable) {
        ((Throwable) aObj).printStackTrace(ps);
      } else {
        ps.println(aObj);
      }
    }

    if (aThrowable != null) {
      aThrowable.printStackTrace(ps);
    } else {
      if ((aObj != null) && !(aObj instanceof Throwable) && aObj.toString().endsWith("Error")) {
        new Exception("SELF GENERATED STACK TRACE").printStackTrace(ps);
      }
    }

    ps.close();

    return baos.toString();
  }

  /**
   * Returns the current date and time with milliseconds.
   */
  public String getDate() {
    return Utils.toDayAndTime(calendar.getTime());
  }

  /**
   * Returns a marker string like "(WW)".
   */
  public String getMarker(int aLevel) {
    String prefix = "(--)";
    switch (aLevel) {
    case Logger.FATAL:
      prefix = "(FF)";
      break;
    case Logger.ERROR:
      prefix = "(EE)";
      break;
    case Logger.WARN:
      prefix = "(WW)";
      break;
    case Logger.INFO:
      prefix = "(II)";
      break;
    case Logger.FINE:
      prefix = "(F1)";
      break;
    case Logger.FINER:
      prefix = "(F2)";
      break;
    case Logger.FINEST:
      prefix = "(F3)";
      break;
    case Logger.DEBUG:
      prefix = "(DD)";
      break;
    case Logger.AWT:
      prefix = "(AA)";
      break;
    default:
      prefix = "(--)";
      break;
    }
    return prefix;
  }

}