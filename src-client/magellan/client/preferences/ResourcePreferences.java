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

package magellan.client.preferences;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.layout.WrappableLabel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.SwingUtils;
import magellan.library.utils.Resources;

/**
 * Settings for all concerning resource paths
 * 
 * @author $Author: $
 * @version $Revision: 269 $
 */
public class ResourcePreferences extends AbstractPreferencesAdapter implements PreferencesAdapter {
  private JButton btnAdd = null;
  private JButton btnRemove = null;
  private JButton btnEdit = null;
  private JList lstPaths = null;
  private Properties settings = null;

  /**
   * Creates a new ResourceSettings object.
   */
  public ResourcePreferences(Collection<MagellanPlugIn> plugins, Properties settings) {
    this.settings = settings;

    initComponents();
  }

  /**
   * 
   */
  private void initComponents() {

    JComponent comment = WrappableLabel.getLabel(Resources.get("resource.resourcesettings.comment")).getComponent();

    addComponent(comment);

    getSpecialPathsPanel();

    getExternalPanel();

    getResourcePathsPanel();

  }

  protected JCheckBox chkSearchResources;
  protected JCheckBox chkSearchClassPath;

  /**
   * Miscellaneous external modules settings
   */
  protected Component getExternalPanel() {
    JPanel extPanel =
        addPanel(Resources.get("resource.externalmodulesettings.border.externalmodules"),
            new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();

    chkSearchResources =
        new JCheckBox(Resources.get("resource.externalmodulesettings.chk.searchResources"), Boolean
            .valueOf(
                settings.getProperty("ExternalModuleLoader.searchResourcePathClassLoader", "true"))
            .booleanValue());

    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 1.0;
    c.weighty = 1.0;
    extPanel.add(chkSearchResources, c);

    chkSearchClassPath =
        new JCheckBox(Resources.get("resource.externalmodulesettings.chk.searchClassPath"), Boolean
            .valueOf(settings.getProperty("ExternalModuleLoader.searchClassPath", "true"))
            .booleanValue());

    c.gridx = 0;
    c.gridy = 1;
    extPanel.add(chkSearchClassPath, c);

    return extPanel;
  }

  private List<JTextField> textFields;
  private List<String> keys;
  private JFileChooser fchooser;
  private File file;

  private JPanel spPanel;

  /**
   * ECheck and Vorlage paths (and the like...)
   */
  protected Component getSpecialPathsPanel() {
    spPanel =
        addPanel(Resources.get("resource.resourcesettings.special.title"), new GridBagLayout());

    GridBagConstraints con = new GridBagConstraints();
    con.anchor = GridBagConstraints.CENTER;
    con.fill = GridBagConstraints.HORIZONTAL;
    con.weighty = 0;
    con.gridy = 0;

    con.gridy++;
    con.gridwidth = 1;

    // list
    textFields = new LinkedList<JTextField>();
    keys = new LinkedList<String>();

    // files
    fchooser = new JFileChooser();

    try {
      file = new File(".");

      if (!file.isDirectory()) {
        file = file.getParentFile();
      }

      if (file == null) {
        file = new File(".");
      }
    } catch (Exception exc) {
    }

    addPath(con, "ECheck:", "JECheckPanel.echeckEXE");
    addPath(con, "Vorlage:", "JVorlage.vorlageFile");

    return spPanel;
  }

  protected void addPath(GridBagConstraints con, String label, String key) {
    con.gridx = 0;
    con.weightx = 0.25;
    JLabel l = new JLabel(label);
    l.setHorizontalAlignment(SwingConstants.RIGHT);
    spPanel.add(l, con);

    JTextField tf = new JTextField(settings.getProperty(key));
    con.gridx = 1;
    con.weightx = 0.5;
    spPanel.add(tf, con);

    textFields.add(tf);
    keys.add(key);

    con.gridx = 2;
    con.weightx = 0.25;
    spPanel.add(new DirButton(tf), con);

    con.gridy++;

    doLayout();
  }

  /**
   * All other resource paths...
   */
  protected Component getResourcePathsPanel() {
    JPanel rpPanel =
        addPanel(Resources.get("resource.resourcesettings.resourcepaths.title"),
            new java.awt.GridBagLayout());

    lstPaths = new JList(getWrappedURLs(Resources.getStaticPaths())); // later we need to assume
    // that this list's model is a
    // DefaultListModel!

    lstPaths.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane lstScroller = new JScrollPane(lstPaths, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    SwingUtils.setPreferredSize(lstScroller, 30, -1, true);

    GridBagConstraints c = new GridBagConstraints();
    c.gridheight = 3;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 0.1;
    c.weighty = 0.1;
    rpPanel.add(lstScroller, c);

    btnAdd = new JButton(Resources.get("resource.resourcesettings.btn.new.caption"));
    btnAdd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        btnAddActionPerformed(evt);
      }
    });

    c.gridheight = 1;
    c.gridx = 1;
    c.gridy = 0;
    c.fill = java.awt.GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 0, 5, 5);
    c.weightx = 0.0;
    c.weighty = 0.0;
    rpPanel.add(btnAdd, c);

    btnRemove = new JButton(Resources.get("resource.resourcesettings.btn.remove.caption"));
    btnRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        btnRemoveActionPerformed(evt);
      }
    });

    c.gridx = 1;
    c.gridy = 1;
    c.insets = new Insets(0, 0, 5, 5);
    rpPanel.add(btnRemove, c);

    btnEdit = new JButton(Resources.get("resource.resourcesettings.btn.edit.caption"));
    btnEdit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        btnEditActionPerformed(evt);
      }
    });

    c.gridx = 1;
    c.gridy = 2;
    c.insets = new Insets(0, 0, 5, 5);
    c.anchor = GridBagConstraints.NORTH;
    rpPanel.add(btnEdit, c);

    return rpPanel;
  }

  private DefaultListModel getWrappedURLs(Collection<URL> urls) {
    DefaultListModel wrappers = new DefaultListModel();

    for (URL url : urls) {
      wrappers.add(wrappers.getSize(), new URLWrapper(url));
    }

    return wrappers;
  }

  /**
   * Action for editing a resource path
   */
  private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {
    if (lstPaths.getSelectedValue() == null)
      return;

    Component parent = getTopLevelAncestor();
    URLWrapper w = (URLWrapper) lstPaths.getSelectedValue();

    if ((w != null) && (w.getUrl() != null)) {
      Object selectionValues[] = { w.toString() };
      String input =
          (String) JOptionPane.showInputDialog(parent, Resources
              .get("resource.resourcesettings.msg.edit.text"), Resources
                  .get("resource.resourcesettings.msg.edit.title"), JOptionPane.PLAIN_MESSAGE, null,
              null, selectionValues[0]);

      if (input != null) {
        if (input.startsWith("http")) {
          try {
            w.setUrl(new URL(input));
          } catch (MalformedURLException mue) {
            JOptionPane.showMessageDialog(parent, Resources
                .get("resource.resourcesettings.msg.invalidformat.text"));
          }
        } else {
          File f = new File(input);

          if (!f.exists()) {
            if (JOptionPane.showConfirmDialog(parent, Resources
                .get("resource.resourcesettings.msg.usenonexisting.text"), Resources
                    .get("resource.resourcesettings.msg.usenonexisting.title"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
              try {
                w.setUrl(new URL(input));
              } catch (MalformedURLException mue) {
                JOptionPane.showMessageDialog(parent, Resources
                    .get("resource.resourcesettings.msg.invalidformat.text"));
              }
            }
          } else {
            try {
              w.setUrl(f.toURI().toURL());
            } catch (MalformedURLException mue) {
              JOptionPane.showMessageDialog(parent, Resources
                  .get("resource.resourcesettings.msg.invalidformat.text"));
            }
          }
        }
      }
    }
  }

  private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {
    if (lstPaths.getSelectedValue() != null) {
      ((DefaultListModel) lstPaths.getModel()).removeElementAt(lstPaths.getSelectedIndex());
    }
  }

  /**
   * Button for adding a resource path.
   * 
   * @param evt
   */
  private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {
    URLWrapper urlWrapper = null;
    Component parent = getTopLevelAncestor();

    javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
    fc.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);

    if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
      java.io.File file = fc.getSelectedFile();

      try {
        if (file.exists()) {
          if (file.isDirectory()) {
            urlWrapper = new URLWrapper(file.toURI().toURL());
          } else {
            urlWrapper = new URLWrapper(Resources.file2URL(file));
          }
        } else {
          String name = file.getName();
          String parentName = "";

          if (file.getParentFile() != null) {
            parentName = file.getParentFile().getName();
          }

          if (!name.equals("") && name.equals(parentName)) {
            // in this case the user double clicked a directory instead of just selecting it
            urlWrapper = new URLWrapper(file.getParentFile().toURI().toURL());
          } else {
            JOptionPane.showMessageDialog(parent, Resources
                .get("resource.resourcesettings.msg.nonexistingfile.text"));
          }
        }
      } catch (MalformedURLException ex) {
        JOptionPane.showMessageDialog(parent, Resources
            .get("resource.resourcesettings.msg.urlexception.text")
            + " " + ex.toString());
      }
    }

    ((DefaultListModel) lstPaths.getModel()).insertElementAt(urlWrapper, 0);
  }

  public void initPreferences() {
    if (textFields.size() > 0) {
      for (int i = 0, max = textFields.size(); i < max; i++) {
        (textFields.get(i)).setText(settings.getProperty(keys.get(i)));
      }
    }
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    // resource paths
    Collection<URL> resourcePaths = new LinkedList<URL>();
    ListModel listModel = lstPaths.getModel();

    for (int j = 0; j < listModel.getSize(); j++) {
      URLWrapper wrapper = (URLWrapper) listModel.getElementAt(j);

      if ((wrapper != null) && (wrapper.getUrl() != null)) {
        resourcePaths.add(wrapper.getUrl());
      }
    }

    Resources.setStaticPaths(resourcePaths);
    Resources.storePaths(resourcePaths, settings);

    // special paths
    if (textFields.size() > 0) {
      for (int i = 0; i < textFields.size(); i++) {
        settings.setProperty(keys.get(i), (textFields.get(i)).getText());
      }
    }

    // external modules
    settings.setProperty("ExternalModuleLoader.searchResourcePathClassLoader", String
        .valueOf(chkSearchResources.isSelected()));
    settings.setProperty("ExternalModuleLoader.searchClassPath", String.valueOf(chkSearchClassPath
        .isSelected()));

  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
   */
  public Component getComponent() {
    return this;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
   */
  public String getTitle() {
    return Resources.get("resource.resourcesettings.title");
  }

  protected class DirButton extends JButton implements ActionListener {
    protected JTextField text;

    /**
     * Creates a new DirButton object.
     */
    public DirButton(JTextField jtf) {
      super(Resources.get("preferences.pathpreferencesadapter.prefs.btn"));
      text = jtf;
      addActionListener(this);
    }

    /**
     * DOCUMENT-ME
     */
    public void actionPerformed(ActionEvent e) {
      try {
        fchooser.setSelectedFile(new File(text.getText()));
      } catch (Exception exc) {
        fchooser.setCurrentDirectory(file);
      }

      if (fchooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        text.setText(fchooser.getSelectedFile().toString());
      }
    }
  }

  private static class URLWrapper {
    /** DOCUMENT-ME */
    private URL url;

    /**
     * Creates a new URLWrapper object.
     */
    public URLWrapper(URL url) {
      this.url = url;
    }

    public void setUrl(URL url) {
      this.url = url;
    }

    public URL getUrl() {
      return url;
    }

    /**
     * DOCUMENT-ME
     */
    @Override
    public String toString() {
      if (getUrl().getProtocol().equals("file")) {
        File f = new File(getUrl().getPath());

        if (f.exists())
          return f.toString();
        else
          return getUrl().toString();
      } else
        return getUrl().toString();
    }
  }
}
