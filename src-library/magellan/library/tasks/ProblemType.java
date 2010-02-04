// class magellan.library.tasks.ProblemType
// created on Jun 7, 2009
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
package magellan.library.tasks;

public class ProblemType {

  private String name;
  private String group;
  private String description;
  private String message;

  // private Inspector inspector;

  /**
   * name, group and description describe the problem and are used in preferences for selection.
   * message is displayed in the user. If the message depends on the problem (not just on the type)
   * it should be <code>null</code> here. A name must start with a letter and contain only letters,
   * numbers, '-', '_', and ' '.
   * 
   * @param name The (localized) name of the ProblemType.
   * @param group The (localized) group this problem belongs to or <code>null</code>
   * @param description The localized description of the problem or <code>null</code>
   * @param message The message to display or <code>null</code>
   * @param inspector The inspector that causes this ProblemType. Never <code>null</code>
   * @throws NullPointerException if <code>name == null</code>
   * @throws IllegalArgumentException if name contains illegal characters or does not start with a
   *           letter
   */
  public ProblemType(String name, String group, String description, String message) {
    if (name == null)
      throw new NullPointerException();
    // if (!Pattern.matches("[A-Za-z‹¸÷ˆƒ‰][A-Za-z‹¸÷ˆƒ‰ﬂ0-9-_ .]*", name))
    // throw new IllegalArgumentException("invalid problem type name " + name);
    this.name = name;
    this.group = group;
    this.description = description;
    this.message = message;
    // this.inspector = inspector;
  }

  /**
   * Returns a localized, human-readable name of this problem.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns an identifier for a group of ProblemTypes this ProblemType belongs to or
   * <code>null</code>.
   */
  public String getGroup() {
    return group;
  }

  /**
   * Returns a localized, human-readable description of the type or <code>null</code>
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the message for this type or <code>null</code> (if the message depends on more than
   * just the type.
   * 
   * @return
   */
  public String getMessage() {
    return message;
  }

  /**
   * An inspector creating this ProblemType.
   * 
   * @return
   */
  // public Inspector getInspector() {
  // return inspector;
  // }

  @Override
  public String toString() {
    return getName();
  }
}
