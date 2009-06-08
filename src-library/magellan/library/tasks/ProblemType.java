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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.library.tasks;

import java.util.regex.Pattern;


public class ProblemType {
  
  private String name;
  private String description;
  private String message;
  private Inspector inspector;

  public ProblemType(String name, String description, String message, Inspector inspector){
    if (name==null)
      throw new NullPointerException();
    if (!Pattern.matches("[A-Za-z][A-Za-z0-9-_]*", name))
      throw new IllegalArgumentException("invalid problem type name "+name);
    this.name = name;
    this.description = description;
    this.message = message;
    this.inspector = inspector;
  }

  public String getName(){
    return name;
  }
  
  public String getDescription(){
    return description;
  }
  
  public String getMessage(){
    return message;
  }
  
  public Inspector getInspector(){
    return inspector;
  }
  
  public String toString(){
    return getName();
  }
}
