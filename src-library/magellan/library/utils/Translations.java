// class magellan.library.utils.Translations
// created on 20.11.2007
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
package magellan.library.utils;

import magellan.library.Rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * 
 * class contains and handels translations from the CR or 
 * default Magellan translations
 * Is part of GameData
 * 
 * thinking about to make it "Localized" 
 *
 * @author ...
 * @version 1.0, 20.11.2007
 */

public class Translations {

  // central structure to hold the translations
  private HashMap<String,TranslationType> translationMap = null;
  
  /**
   * Adds a new Translation 
   * @param original
   * @param translated
   * @param source
   */
   

  public void addTranslation(String original, String translated, int source){
    // do we have to check if source is well defined?
    if (original==null){
      return;
    }
    
    // check if we have already a Translation to that string
    if (this.translationMap!=null){
      TranslationType translationType = this.translationMap.get(original);
      if (translationType!=null){
        // yes, we have already an Translation..setting new values
        translationType.setSource(source);
        translationType.setTranslation(translated);
        return;
      } 
    } else {
      // translationMap create
      this.translationMap = new HashMap<String, TranslationType>();
    }
    // adding
    this.translationMap.put(original, new TranslationType(translated,source));
  }
  
  
  /**
   * returns the translated string
   * source is not importand
   * @param original
   * @return
   */
  public String getTranslation(String original){
    return getTranslation(original,TranslationType.sourceUnknown);
  }
  
  
  /**
   * returns the translated string, if it is from the specified
   * source. No filtering is done, when sourceUnknown is choosen
   * @param original
   * @param source
   * @return
   */
  public String getTranslation(String original, int source){
    if (original==null){
      return null;
    }
    if (this.translationMap==null || this.translationMap.size()==0){
      return original; 
    }
    
    TranslationType translationType = this.translationMap.get(original);
    if (translationType!=null && translationType.getTranslation()!=null){
      if (source==TranslationType.sourceUnknown || source==translationType.getSource()){
        return translationType.getTranslation();
      } 
    }
    if (source==TranslationType.sourceUnknown){
      // if we don´t searched for a specific source, we return original
      return original;
    } else {
      // we searched for an specific source and found nothing..returning null
      return null;
    }
  }
  
  /**
   * returns the translated string
   * @param original
   * @return
   */
  public TranslationType getTranslationType(String original){
    if (original==null){
      return null;
    }
    if (this.translationMap==null || this.translationMap.size()==0){
      return null; 
    }
    
    return this.translationMap.get(original);
    
  }
  
  
  /**
   * clear complete contents of the Translations
   */
  public void clear(){
    if (this.translationMap!=null){
      this.translationMap.clear();
    }
  }
  
  /**
   * adds the complete translations to this translations
   * the actual contents is not cleared!
   * @param translations
   */
  public void addAll(Translations translations, Rules rules){
    if (translations!=null && translations.iteratorKeys()!=null){
      if (this.translationMap==null){
        this.translationMap = new HashMap<String, TranslationType>();
      }
      for (Iterator<String> iter = translations.iteratorKeys();iter.hasNext();){
        String original = (String)iter.next();
        TranslationType translationType = translations.getTranslationType(original);
        if (translationType!=null){
          this.translationMap.put(original, translationType);
          rules.changeName(original, translationType.getTranslation());
        }
      }
    }
  }
  
  /**
   * provides an Iterator over the keys = original strings
   * @return
   */
  public Iterator<String> iteratorKeys(){
    if (this.translationMap==null){
      return null;
    }
    return this.translationMap.keySet().iterator();
  }
  
  /**
   * returns the size of the translations object (number of translations)
   * @return
   */
  public int size(){
    if (this.translationMap==null){
      return 0;
    }
    
    return this.translationMap.size();
  }
  
  
   /**
    * provides a sorted set of the keys
    * @return
    */
   public TreeSet<String> getKeyTreeSet(){
     if (this.translationMap==null){
       return null;
     }
     return new TreeSet<String>(this.translationMap.keySet());
   }
   
   /**
    * removes an Translation
    * @param o
    */
   public void remove(String o){
     if (this.translationMap==null || this.translationMap.size()==0){
       return;
     }
     // we do nothing return...
     this.translationMap.remove(o);
   }
   
   /**
    * returns true, if given String is already in this translations
    * @param s
    * @return
    */
   public boolean contains(String s){
     if (this.translationMap==null){
       return false;
     }
     return this.translationMap.containsKey(s);
   }
   
   
}
