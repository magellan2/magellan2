// class magellan.library.utils.MapRadixTreeImpl
// created on Aug 3, 2010
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
package magellan.library.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for Radix tree {@link RadixTree} !!WARNING!! This class violates the general
 * contract of {@link Map} in that the results of {@link #keySet()}, {@link #values()} and
 * {@link #entrySet()} are not backed by the map. Changes to the result sets are not reflected in
 * this map!
 * 
 * @author Tahseen Ur Rehman (tahseen.ur.rehman {at.spam.me.not} gmail.com)
 * @author Javid Jamae
 * @author Dennis Heidsiek
 * @author stm
 */
public class RadixTreeImpl<T> implements MapRadixTree<T> {

  /**
   * The visitor interface that is used by {@link RadixTreeImpl} for performing a task on a searched
   * node.
   * 
   * @author Tahseen Ur Rehman (tahseen.ur.rehman {at.spam.me.not} gmail.com)
   * @author Javid Jamae
   * @author Dennis Heidsiek
   * @author stm
   * @param <T> The content type
   * @param <R> The result type
   */
  public interface Visitor<T, R> {
    /**
     * This method gets called by {@link RadixTreeImpl#visit(String, Visitor) visit} when it finds a
     * node matching the key given to it.
     * 
     * @param key The key that matched the node
     * @param parent The parent of the node being visited
     * @param node The node that is being visited
     */
    public void visit(String key, int alreadyMatched, StringBuffer postfix,
        RadixTreeNode<T> parent, RadixTreeNode<T> node);

    public boolean isReady();

    /**
     * The visitor can store any type of result object, depending on the context of what it is being
     * used for.
     * 
     * @return The result captured by the visitor.
     */
    public R getResult();
  }

  /**
   * A simple standard implementation for a {@link magellan.library.utils.RadixTreeImpl.Visitor}
   * 
   * @author Dennis Heidsiek
   * @author stm
   * @param <T> The RadixTree value type
   * @param <R> The result type of this visitor
   */
  public static abstract class VisitorImpl<T, R> implements Visitor<T, R> {

    protected R result;
    protected boolean ready;

    public VisitorImpl() {
      this.result = null;
    }

    public VisitorImpl(R initialValue) {
      this.result = initialValue;
    }

    public R getResult() {
      return result;
    }

    public boolean isReady() {
      return ready;
    }

    public abstract void visit(String key, int alreadyMatched, StringBuffer postfix,
        RadixTreeNode<T> parent, RadixTreeNode<T> node);
  }

  /**
   * Represents a node of a Radix tree {@link RadixTreeImpl}
   * 
   * @author Tahseen Ur Rehman
   * @email tahseen.ur.rehman {at.spam.me.not} gmail.com
   * @param <T> The content type
   */
  static class RadixTreeNode<T> {
    private String key;

    private List<RadixTreeNode<T>> children;

    private boolean real;

    private T value;

    /**
     * intialize the fields with default values to avoid null reference checks all over the places
     */
    public RadixTreeNode() {
      key = "";
      children = new ArrayList<RadixTreeNode<T>>();
      real = false;
    }

    public T getValue() {
      return value;
    }

    public void setValue(T data) {
      this.value = data;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String value) {
      this.key = value;
    }

    public boolean isReal() {
      return real;
    }

    public void setReal(boolean datanode) {
      this.real = datanode;
    }

    public List<RadixTreeNode<T>> getChildren() {
      return children;
    }

    public void setChildren(List<RadixTreeNode<T>> childern) {
      this.children = childern;
    }

    public int getNumberOfMatchingCharacters(String key, int matchedAlready) {
      int numberOfMatchingCharacters = 0;
      while (matchedAlready + numberOfMatchingCharacters < key.length()
          && numberOfMatchingCharacters < this.getKey().length()) {
        if (key.charAt(matchedAlready + numberOfMatchingCharacters) != this.getKey().charAt(
            numberOfMatchingCharacters)) {
          break;
        }
        numberOfMatchingCharacters++;
      }
      return numberOfMatchingCharacters;
    }

    @Override
    public String toString() {
      return key;

    }

  }

  protected RadixTreeNode<T> root;

  protected long size;

  /**
   * @see java.util.Map#size()
   */
  public int size() {
    return (int) size;
  }

  /**
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key) {
    if (key instanceof String)
      return contains((String) key);
    else
      return false;
  }

  /**
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object value) {
    return values().contains(value);
  }

  /**
   * @see java.util.Map#get(java.lang.Object)
   */
  public T get(Object key) {
    if (key instanceof String)
      return find((String) key);
    else
      return null;
  }

  /**
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public T put(String key, T value) {
    T old = find(key);
    insert(key, value);
    return old;
  }

  /**
   * @see java.util.Map#remove(java.lang.Object)
   */
  public T remove(Object key) {
    if (key instanceof String) {
      T result = find((String) key);
      delete((String) key);
      return result;
    } else
      return null;
  }

  /**
   * @see java.util.Map#putAll(java.util.Map)
   */
  public void putAll(Map<? extends String, ? extends T> m) {
    for (java.util.Map.Entry<? extends String, ? extends T> entry : m.entrySet()) {
      insert(entry.getKey(), entry.getValue());
    }
  }

  /**
   * @see java.util.Map#clear()
   */
  public void clear() {
    root = new RadixTreeNode<T>();
    root.setKey("");
    size = 0;
  }

  /**
   * !!WARNING!! This violates the general contract of {@link Map} in that it isn't backed by the
   * map. Changes to the result set are not reflected in this map!
   * 
   * @see java.util.Map#keySet()
   */
  public Set<String> keySet() {
    return searchPrefixMap("", Integer.MAX_VALUE).keySet();
  }

  /**
   * !!WARNING!! This violates the general contract of {@link Map} in that it isn't backed by the
   * map. Changes to the result set are not reflected in this map!
   * 
   * @see java.util.Map#values()
   */
  public Collection<T> values() {
    return searchPrefixMap("", Integer.MAX_VALUE).values();
  }

  /**
   * !!WARNING!! This violates the general contract of {@link Map} in that it isn't backed by the
   * map. Changes to the result set are not reflected in this map!
   * 
   * @see java.util.Map#entrySet()
   */
  public Set<java.util.Map.Entry<String, T>> entrySet() {
    return searchPrefixMap("", Integer.MAX_VALUE).entrySet();
  }

  /**
   * @see magellan.library.utils.RadixTree#searchPrefix(java.lang.String, int)
   */
  public ArrayList<T> searchPrefix(String prefix, int recordLimit) {
    return new ArrayList<T>(searchPrefixMap(prefix, recordLimit).values());
  }

  /**
   * Returns a map containing all entries whose keys are have the specified key as a prefix. The
   * returned result is <i>not</i> backed by this maps. Changes made to it are independent of this
   * map.
   * 
   * @param key
   * @param recordLimit
   */
  public Map<String, T> searchPrefixMap(String key, int recordLimit) {
    VisitorImpl<T, Map<String, T>> visitor;
    visit(key, visitor = new VisitorImpl<T, Map<String, T>>(new LinkedHashMap<String, T>()) {

      @Override
      public void visit(String key, int alreadyMatched, StringBuffer postfix,
          RadixTreeNode<T> parent, RadixTreeNode<T> node) {
        if (alreadyMatched >= key.length()) {
          if (node.isReal()) {
            result.put(key + postfix.toString(), node.getValue());
          }
        } else {
          ready = result.size() > 0 && alreadyMatched < key.length();
        }
      }

    });
    return visitor.getResult();
  }

  /**
   * Create a Radix Tree with only the default node root.
   */
  public RadixTreeImpl() {
    clear();
  }

  public T find(String key) {
    Visitor<T, T> visitor = new VisitorImpl<T, T>() {

      @Override
      public void visit(String key, int alreadyMatched, StringBuffer postfix,
          RadixTreeNode<T> parent, RadixTreeNode<T> node) {
        if (node.isReal() && key.length() == alreadyMatched && postfix.length() == 0) {
          ready = true;
          result = node.getValue();
        }
      }
    };

    visit(key, visitor);

    return visitor.getResult();
  }

  public boolean delete(String key) {
    Visitor<T, Boolean> visitor = new VisitorImpl<T, Boolean>(Boolean.FALSE) {
      @Override
      public void visit(String key, int alreadyMatched, StringBuffer postfix,
          RadixTreeNode<T> parent, RadixTreeNode<T> node) {
        result =
            Boolean.valueOf(node.isReal() && key.length() == alreadyMatched
                && postfix.length() == 0);
        ready = key.length() <= alreadyMatched || postfix.length() > 0;

        // if it is a real node
        if (result.booleanValue()) {
          // If there no children of the node we need to
          // delete it from the its parent children list
          if (node.getChildren().size() == 0) {
            Iterator<RadixTreeNode<T>> it = parent.getChildren().iterator();
            while (it.hasNext()) {
              if (it.next().getKey().equals(node.getKey())) {
                it.remove();
                break;
              }
            }

            // if parent is not real node and has only one child
            // then they need to be merged.
            if (parent.getChildren().size() == 1 && parent.isReal() == false) {
              mergeNodes(parent, parent.getChildren().get(0));
            }
          } else if (node.getChildren().size() == 1) {
            // we need to merge the only child of this node with
            // itself
            mergeNodes(node, node.getChildren().get(0));
          } else { // we jus need to mark the node as non real.
            node.setReal(false);
          }
        }
      }

      /**
       * Merge a child into its parent node. Operation only valid if it is only child of the parent
       * node and parent node is not a real node.
       * 
       * @param parent The parent Node
       * @param child The child Node
       */
      private void mergeNodes(RadixTreeNode<T> parent, RadixTreeNode<T> child) {
        parent.setKey(parent.getKey() + child.getKey());
        parent.setReal(child.isReal());
        parent.setValue(child.getValue());
        parent.setChildren(child.getChildren());
      }

    };

    visit(key, visitor);

    if (visitor.getResult().booleanValue()) {
      size--;
    }
    return visitor.getResult().booleanValue();
  }

  /*
   * (non-Javadoc)
   * @see ds.tree.RadixTree#insert(java.lang.String, java.lang.Object)
   */
  public void insert(String key, T value) throws DuplicateKeyException {
    try {
      insert(key, 0, root, value);
    } catch (DuplicateKeyException e) {
      // re-throw the exception with 'key' in the message
      throw new DuplicateKeyException("Duplicate key: '" + key + "'");
    }
    size++;
  }

  /**
   * Recursively insert the key in the radix tree.
   * 
   * @param key The key to be inserted
   * @param node The current node
   * @param value The value associated with the key
   * @throws DuplicateKeyException If the key already exists in the database.
   */
  private void insert(String key, int alreadyMatched, RadixTreeNode<T> node, T value)
      throws DuplicateKeyException {

    int numberOfMatchingCharacters = node.getNumberOfMatchingCharacters(key, alreadyMatched);

    if (node.getKey().equals("") == true
        || numberOfMatchingCharacters == 0
        || (numberOfMatchingCharacters < key.length() && numberOfMatchingCharacters >= node
            .getKey().length())) {
      // we are either at the root node
      // or we need to go down the tree
      boolean foundChild = false;
      // String newText = key.substring(numberOfMatchingCharacters, key.length());
      for (RadixTreeNode<T> child : node.getChildren()) {
        if (child.getKey().charAt(0) == key.charAt(alreadyMatched + numberOfMatchingCharacters)) {
          foundChild = true;
          insert(key, alreadyMatched + numberOfMatchingCharacters, child, value);
          break;
        }
      }

      if (foundChild == false) {
        // just add the node as the child of the current node
        RadixTreeNode<T> n = new RadixTreeNode<T>();
        n.setKey(key.substring(alreadyMatched + numberOfMatchingCharacters));
        n.setReal(true);
        n.setValue(value);

        node.getChildren().add(n);
      }
    } else if (alreadyMatched + numberOfMatchingCharacters == key.length()
        && numberOfMatchingCharacters == node.getKey().length()) {
      // there is an exact match-- just make the current node a "real" node
      if (node.isReal() == true)
        throw new DuplicateKeyException("Duplicate key");

      node.setReal(true);
      node.setValue(value);
    } else if (numberOfMatchingCharacters > 0
        && numberOfMatchingCharacters < node.getKey().length()) {
      // This node needs to be split as the key to be inserted
      // is a prefix of the current node key
      RadixTreeNode<T> n1 = new RadixTreeNode<T>();
      n1.setKey(node.getKey().substring(numberOfMatchingCharacters, node.getKey().length()));
      n1.setReal(node.isReal());
      n1.setValue(node.getValue());
      n1.setChildren(node.getChildren());

      node.setKey(key.substring(alreadyMatched, alreadyMatched + numberOfMatchingCharacters));
      node.setChildren(new ArrayList<RadixTreeNode<T>>());
      node.getChildren().add(n1);

      if (alreadyMatched + numberOfMatchingCharacters < key.length()) {
        RadixTreeNode<T> n2 = new RadixTreeNode<T>();
        n2.setKey(key.substring(alreadyMatched + numberOfMatchingCharacters, key.length()));
        n2.setReal(true);
        n2.setValue(value);

        node.getChildren().add(n2);
        node.setValue(null);
        node.setReal(false);
      } else {
        node.setValue(value);
        node.setReal(true);
      }
    } else {
      // this key needs to be added as the child of the current node
      RadixTreeNode<T> n = new RadixTreeNode<T>();
      n.setKey(node.getKey().substring(numberOfMatchingCharacters, node.getKey().length()));
      n.setChildren(node.getChildren());
      n.setReal(node.isReal());
      n.setValue(node.getValue());

      node.setKey(key.substring(alreadyMatched));
      node.setReal(true);
      node.setValue(value);

      node.getChildren().add(n);
    }
  }

  public boolean contains(String key) {
    Visitor<T, Boolean> visitor = new VisitorImpl<T, Boolean>(Boolean.FALSE) {
      @Override
      public void visit(String key, int alreadyMatched, StringBuffer postfix,
          RadixTreeNode<T> parent, RadixTreeNode<T> node) {
        if (node.isReal() && key.length() == alreadyMatched && postfix.length() == 0) {
          result = Boolean.valueOf(node.isReal());
          ready = true;
        }
      }
    };

    visit(key, visitor);

    return visitor.getResult().booleanValue();
  }

  /**
   * visit the node those key matches the given key
   * 
   * @param key The key that need to be visited
   * @param visitor The visitor object
   */
  public <R> void visit(String key, Visitor<T, R> visitor) {
    if (root != null) {
      visit(key, 0, new StringBuffer(), visitor, null, root);
    }
  }

  public long getSize() {
    return size;
  }

  /**
   * Display the Trie on console. WARNING! Do not use this for a large Trie, it's for testing
   * purpose only.
   */
  @Deprecated
  public void display() {
    formatNodeTo(new Formatter(System.out), 0, root);
  }

  @Deprecated
  protected void display(int level, RadixTreeNode<T> node) {
    formatNodeTo(new Formatter(System.out), level, node);
  }

  /**
   * WARNING! Do not use this for a large Trie, it's for testing purpose only.
   */
  private void formatNodeTo(Formatter f, int level, RadixTreeNode<T> node) {
    for (int i = 0; i < level; i++) {
      f.format(" ");
    }
    f.format("|");
    for (int i = 0; i < level; i++) {
      f.format("-");
    }

    if (node.isReal() == true) {
      f.format("%s[%s]*%n", node.getKey(), node.getValue());
    } else {
      f.format("%s%n", node.getKey());
    }

    for (RadixTreeNode<T> child : node.getChildren()) {
      formatNodeTo(f, level + 1, child);
    }
  }

  /**
   * Writes a textual representation of this tree to the given formatter. Currently, all options are
   * simply ignored. WARNING! Do not use this for a large Trie, it's for testing purpose only.
   */
  public void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatNodeTo(formatter, 0, root);
  }

  /**
   * Complete the a prefix to the point where ambiguity starts. Example: If a tree contain "blah1",
   * "blah2" complete("b") &rarr; return "blah"
   * 
   * @param prefix The prefix we want to complete
   * @return The unambiguous completion of the string.
   */
  public String complete(String prefix) {
    return complete(prefix, root, "");
  }

  private String complete(String key, RadixTreeNode<T> node, String base) {
    int i = 0;
    int keylen = key.length();
    int nodelen = node.getKey().length();

    while (i < keylen && i < nodelen) {
      if (key.charAt(i) != node.getKey().charAt(i)) {
        break;
      }
      i++;
    }

    if (i == keylen && i <= nodelen)
      return base + node.getKey();
    else if (nodelen == 0 || (i < keylen && i >= nodelen)) {
      String beginning = key.substring(0, i);
      String ending = key.substring(i, keylen);
      for (RadixTreeNode<T> child : node.getChildren()) {
        if (child.getKey().startsWith(ending.charAt(0) + ""))
          return complete(ending, child, base + beginning);
      }
    }

    return "";
  }

  /**
   * recursively visit the tree based on the supplied "key". calls the Visitor for the node those
   * key matches the given prefix
   * 
   * @param prefix The key o prefix to search in the tree
   * @param visitor The Visitor that will be called if a node with "key" as its key is found
   * @param node The Node from where onward to search
   */
  private <R> void visit(String prefix, int matchedAlready, StringBuffer postfix,
      Visitor<T, R> visitor, RadixTreeNode<T> parent, RadixTreeNode<T> node) {

    int numberOfMatchingCharacters = 0;
    if (postfix.length() == 0) {
      numberOfMatchingCharacters =
          node.getNumberOfMatchingCharacters(prefix, matchedAlready + postfix.length());
    }

    int postfixLength = postfix.length();
    if (numberOfMatchingCharacters < node.getKey().length()) {
      postfix.append(node.getKey().substring(numberOfMatchingCharacters));
    }
    visitor.visit(prefix, matchedAlready + numberOfMatchingCharacters, postfix, parent, node);

    if (!visitor.isReady()) {
      for (RadixTreeNode<T> child : node.getChildren()) {
        if ((matchedAlready + numberOfMatchingCharacters >= prefix.length() || child.getKey()
            .charAt(0) == prefix.charAt(matchedAlready + numberOfMatchingCharacters))) {
          visit(prefix, matchedAlready + numberOfMatchingCharacters, postfix, visitor, node, child);
        }
        if (visitor.isReady()) {
          break;
        }
      }
    }
    if (numberOfMatchingCharacters < node.getKey().length()) {
      postfix.delete(postfixLength, postfix.length());
    }
  }

  @Override
  public String toString() {
    StringBuffer s;
    formatNodeTo(new Formatter(s = new StringBuffer()), 0, root);
    return s.toString();
    // return searchPrefixMap("", Integer.MAX_VALUE).toString();
  }

}
