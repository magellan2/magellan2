/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.library.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import magellan.library.utils.logging.Logger;

/**
 * I'm not really sure why this class is here. It implements Map&lt;String, String&gt;. It's probably just
 * a pre-java-1.5 typesafe variant plus the <code>getTag</code> and <code>putTag</code> methods.
 * <code>entrySet()</code> returns <code>null</code> and <code>equals()</code> always returns
 * <code>false</code>.
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 * @deprecated (stm) Nobody seems to really need this class. A simple Map&lt;String, String&gt; would do.
 *             The implementation is very inefficient.
 */
@Deprecated
public class TagMap implements Map<String, String> {
  protected static class Tag {
    /** DOCUMENT-ME */
    public String key;

    /** DOCUMENT-ME */
    public String value;

    /**
     * Creates a new Tag object.
     */
    public Tag() {
    }

    /**
     * Creates a new Tag object.
     */
    public Tag(String k, String v) {
      key = k;
      value = v;
    }
  }

  public TagMap() {
    Logger.getInstance(TagMap.class).warn("using deprecated TagMap");
  }

  protected Tag tags[] = null;

  /**
   * @see java.util.Map#clear()
   */
  public void clear() {
    tags = null;
  }

  /**
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object obj) {
    if (!(obj instanceof String) || (tags == null))
      return false;

    String key = (String) obj;

    for (Tag tag : tags) {
      if (tag.key.equals(key))
        return true;
    }

    return false;
  }

  /**
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object obj) {
    if (!(obj instanceof String) || (tags == null))
      return false;

    String value = (String) obj;

    for (Tag tag : tags) {
      if (tag.value.equals(value))
        return true;
    }

    return false;
  }

  /**
   * Always return <code>null</code>!
   * 
   * @see java.util.Map#entrySet()
   */
  public Set<Map.Entry<String, String>> entrySet() {
    return null;
  }

  /**
   * Always returns <code>false</code>!
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    // FIXME
    return false;
  }

  /**
   * @see java.util.Map#get(java.lang.Object)
   */
  public String get(Object obj) {
    if ((obj == null) || !(obj instanceof String) || (tags == null))
      return null;

    String key = (String) obj;

    for (Tag tag : tags) {
      if (tag.key.equals(key))
        return tag.value;
    }

    return null;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (tags == null)
      return super.hashCode();

    int j = 0;

    for (Tag tag : tags) {
      j += tag.key.hashCode();
    }

    return j;
  }

  /**
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty() {
    return tags == null;
  }

  /**
   * @see java.util.Map#keySet()
   */
  public Set<String> keySet() {
    Set<String> s = new HashSet<String>();

    if (tags != null) {
      for (Tag tag : tags) {
        s.add(tag.key);
      }
    }

    return s;
  }

  /**
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public String put(String key, String value) {
    if (key == null)
      return null;

    if (containsKey(key)) {
      for (int i = 0; i < tags.length; i++) {
        if (tags[i].key.equals(key)) {
          String old = tags[i].value;
          tags[i].value = value;

          return old;
        }
      }
    } else {
      int curSize = 0;

      if (tags != null) {
        curSize = tags.length;
      }

      Tag temp[] = new Tag[curSize + 1];

      for (int i = 0; i < curSize; i++) {
        temp[i + 1] = tags[i];
      }

      temp[0] = new Tag(key, value);
      tags = temp;
    }

    return null;
  }

  /**
   * @see java.util.Map#putAll(java.util.Map)
   */
  public void putAll(Map<? extends String, ? extends String> map) {
    if (map.size() > 0) {
      Set<? extends String> s = map.keySet();
      Iterator<? extends String> it = s.iterator();

      while (it.hasNext()) {
        String key = it.next();
        put(key, map.get(key));
      }
    }
  }

  /**
   * @see java.util.Map#remove(java.lang.Object)
   */
  public String remove(Object obj) {
    if ((obj == null) || !containsKey(obj))
      return null;

    if (tags.length == 1) {
      String old = tags[0].value;
      tags = null;

      return old;
    }

    Tag temp[] = new Tag[tags.length - 1];
    int j = 0;
    String old = null;

    for (Tag tag : tags) {
      if (!tag.key.equals(obj)) {
        temp[j] = tag;
        j++;
      } else {
        old = tag.value;
      }
    }

    tags = temp;

    return old;
  }

  /**
   * @see java.util.Map#size()
   */
  public int size() {
    if (tags == null)
      return 0;

    return tags.length;
  }

  /**
   * @see java.util.Map#values()
   */
  public Collection<String> values() {
    int s = 0;

    if (tags != null) {
      s = tags.length;
    }

    List<String> l = new ArrayList<String>(s);

    if (tags != null) {
      for (Tag tag : tags) {
        l.add(tag.value);
      }
    }

    return l;
  }

  /**
   * Equivalent to <code>get(tag)</code>.
   * 
   * @param tag
   */
  public String getTag(String tag) {
    return get(tag);
  }

  /**
   * Equivalent to <code>put(tag, value)</code>.
   * 
   * @param tag
   * @param value
   */
  public String putTag(String tag, String value) {
    return put(tag, value);
  }
}
