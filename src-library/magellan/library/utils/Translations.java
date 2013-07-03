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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import magellan.library.Rules;

/**
 * class contains and handles translations from the CR or default Magellan translations. Is part of
 * GameData thinking about to make it "Localized"
 * 
 * @version 1.0, 20.11.2007
 */

public class Translations {

  // central structure to hold the translations
  private final Map<String, TranslationType> translationMap = CollectionFactory
      .createSyncOrderedMap();

  /**
   * Adds a new Translation
   * 
   * @param original
   * @param translated
   * @param source One of {@link TranslationType#SOURCE_UNKNWON},{@link TranslationType#SOURCE_CR},
   *          or {@link TranslationType#SOURCE_MAGELLAN}
   */

  public void addTranslation(String original, String translated, int source) {
    // do we have to check if source is well defined?
    if (original == null)
      return;

    // adding
    translationMap.put(original, new TranslationType(translated, source));
  }

  /**
   * returns the translated string source is not important
   * 
   * @param original
   * @return the translated string; if the original was not found, the original is returned.
   */
  public String getTranslation(String original) {
    return getTranslation(original, TranslationType.SOURCE_UNKNWON);
  }

  /**
   * returns the translated string, if it is from the specified source. No filtering is done, when
   * sourceUnknown is chosen
   * 
   * @param original
   * @param source One of {@link TranslationType#SOURCE_UNKNWON},{@link TranslationType#SOURCE_CR},
   *          or {@link TranslationType#SOURCE_MAGELLAN}
   * @return the translated string; if the original was not found, the original is returned unless
   *         <code>source!= {@link TranslationType#SOURCE_UNKNWON}</code>.
   */
  public String getTranslation(String original, int source) {
    if (original == null)
      return null;
    if (translationMap == null || translationMap.size() == 0)
      return original;

    TranslationType translationType = translationMap.get(original);
    if (translationType != null && translationType.getTranslation() != null) {
      if (source == TranslationType.SOURCE_UNKNWON || source == translationType.getSource())
        return translationType.getTranslation();
    }
    if (source == TranslationType.SOURCE_UNKNWON)
      // if we don´t searched for a specific source, we return original
      return original;
    else
      // we searched for an specific source and found nothing..returning null
      return null;
  }

  /**
   * returns the translated string
   * 
   * @param original
   */
  public TranslationType getTranslationType(String original) {
    if (original == null)
      return null;

    return translationMap.get(original);

  }

  /**
   * clear complete contents of the Translations
   */
  public void clear() {
    translationMap.clear();
  }

  /**
   * adds the complete translations to this translations the actual contents is not cleared!
   * 
   * @param translations
   */
  public void addAll(Translations translations, Rules rules) {
    if (translations != null && translations.iteratorKeys() != null) {
      for (Iterator<String> iter = translations.iteratorKeys(); iter.hasNext();) {
        String original = iter.next();
        TranslationType translationType = translations.getTranslationType(original);
        if (translationType != null) {
          translationMap.put(original, translationType);
          rules.changeName(original, translationType.getTranslation());
        }
      }
    }
  }

  /**
   * provides an Iterator over the keys = original strings
   */
  public Iterator<String> iteratorKeys() {
    return translationMap.keySet().iterator();
  }

  /**
   * returns the size of the translations object (number of translations)
   */
  public int size() {
    return translationMap.size();
  }

  /**
   * provides a sorted set of the keys
   */
  public TreeSet<String> getKeyTreeSet() {
    return new TreeSet<String>(translationMap.keySet());
  }

  /**
   * removes an Translation
   * 
   * @param o
   */
  public void remove(String o) {
    translationMap.remove(o);
  }

  /**
   * Returns true if given String is already in this translations.
   * 
   * @param s
   */
  public boolean contains(String s) {
    return translationMap.containsKey(s);
  }

}
