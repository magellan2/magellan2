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

package magellan.client.swing.tree;

import java.util.Properties;

import javax.swing.JPanel;

import magellan.client.swing.preferences.DetailedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class DetailsNodeWrapperDrawPolicy extends AbstractNodeWrapperDrawPolicy {
  // data for pref adapter
  protected int count;

  // data for pref adapter
  protected int rows;

  // data for pref adapter
  protected int subcount[];
  protected Properties settings;
  protected String prefix;
  protected String sK[][];
  protected String lK[];
  protected String resourcePrefix;
  boolean properties[];

  /**
   * Creates new NodeWrapperPreferencesDialog with subcategories
   * 
   * @param count Number of options (top level check boxes)
   * @param subcount Number of sub-options (check boxes in details entry)
   * @param p Properties to store options
   * @param prefix Prefix for properties keys 
   * @param sK suffixes of properties keys
   * @param lK suffixes of resource keys
   * @param rows number of rows of boxes
   * @param resourcePrefix Prefix for resource keys
   */
  public DetailsNodeWrapperDrawPolicy(int count, int subcount[], Properties p, String prefix, String sK[][], String lK[], int rows, String resourcePrefix) {
    this.count = count;
    this.subcount = subcount;
    this.settings = p;
    this.prefix = prefix;
    this.sK = sK;
    this.lK = lK;
    this.rows = rows;
    this.resourcePrefix = resourcePrefix;

    loadSettings();
  }

  protected void loadSettings() {
    int sum = 0;

    if (subcount != null) {
      for (int i = 0; i < count; i++) {
        sum += Math.max(0, subcount[i]);
        sum++;
      }
    } else {
      sum = count;
    }

    properties = new boolean[sum];

    for (int i = 0; i < properties.length; i++) {
      properties[i] = settings.getProperty(prefix + "." + sK[i][0], sK[i][1]).equals("true");
    }
  }

  // pavkovic 2003.06.23: back to version 1.3
  // public void applyPreferences() {
  public void applyPreferences(DetailedPreferencesAdapter adapter) {
    System.arraycopy(adapter.properties, 0, properties, 0, properties.length);
    applyPreferences();
  }

  /**
   * DOCUMENT-ME
   * 
   * @see magellan.client.swing.preferences.PreferencesFactory#createPreferencesAdapter()
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new DetailPolicyPreferencesAdapter(count, subcount, settings, prefix, sK, lK, rows, this);
  }

  /**
   * DOCUMENT-ME
   */
  public String getString(String key) {
    return Resources.get(resourcePrefix+key);
  }

  protected JPanel getExternalDetailContainer(int index) {
    return null;
  }

  class DetailPolicyPreferencesAdapter extends DetailedPreferencesAdapter {
    DetailsNodeWrapperDrawPolicy parent;

    /**
     * Creates a new DetailPolicyPreferencesAdapter object.
     * 
     * @param count Number of options (top level check boxes)
     * @param subcount Number of sub-options (check boxes in details entry)
     * @param p Properties to store options
     * @param prefix Prefix for properties keys 
     * @param sK suffixes of properties keys
     * @param lK suffixes of resource keys
     * @param rows number of rows of boxes
     * @param resourcePrefix Prefix for resource keys
     */
    public DetailPolicyPreferencesAdapter(int count, int subcount[], Properties p, String prefix,
        String sK[][], String lK[], int rows, DetailsNodeWrapperDrawPolicy parent) {
      super(count, subcount, p, prefix, sK, lK, rows, true);
      this.parent = parent;
      init();
    }

    @Override
    protected void applyChanges(int indices[]) {
      parent.applyPreferences(this);
    }

    @Override
    protected String getString(String key) {
      return parent.getString(key);
    }
  }

  /**
   * DOCUMENT-ME
   */
  public String getTitle() {
    return getString("pref.title");
  }
}
