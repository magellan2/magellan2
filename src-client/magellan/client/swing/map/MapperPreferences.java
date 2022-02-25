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

package magellan.client.swing.map;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import magellan.client.Client;
import magellan.client.preferences.AbstractPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * The preferences panel providing a GUI to configure a Mapper object.
 */
public class MapperPreferences extends AbstractPreferencesAdapter implements PreferencesAdapter,
    ActionListener {
  // The source component to configure
  private Mapper source = null;

  // GUI elements
  private JCheckBox showTooltips;

  private JTabbedPane planes;

  // the map holding the associations between planes and renderers
  // maps RenderingPlane to MapCellRenderer
  private Map<RenderingPlane, MapCellRenderer> planeMap =
      new HashMap<RenderingPlane, MapCellRenderer>();
  private Collection<PreferencesAdapter> rendererAdapters = new LinkedList<PreferencesAdapter>();

  // for changing tooltips
  private ToolTipSwitcherDialog ttsDialog;
  private boolean dialogShown = false;

  private Map<RenderingPlane, JComboBox> renderBoxes = new HashMap<RenderingPlane, JComboBox>();

  /**
   * Creates a new MapperPreferences object.
   */
  public MapperPreferences(Mapper m, boolean showTips) {
    source = m;
    init(showTips);
  }

  private void init(boolean showTips) {
    GridBagConstraints gbc =
        new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL, new Insets(3, 3, 3, 3), 0, 0);

    if (showTips) {
      showTooltips =
          new JCheckBox(Resources.get("map.mapperpreferences.showtooltips.caption"), source
              .isShowingTooltip());

      JButton configureTooltips =
          new JButton(Resources.get("map.mapperpreferences.showtooltips.configure.caption"));
      configureTooltips.addActionListener(this);

      JPanel helpPanel = addPanel(null, new GridBagLayout());

      gbc.gridy++;
      gbc.gridwidth = 1;
      gbc.weightx = 0;
      helpPanel.add(showTooltips, gbc);
      gbc.gridx++;
      gbc.fill = GridBagConstraints.NONE;
      helpPanel.add(configureTooltips, gbc);
    }

    JPanel rendererPanel =
        addPanel(Resources.get("map.mapperpreferences.border.rendereroptions"), new BorderLayout());

    planes = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
    rendererPanel.add(planes, BorderLayout.CENTER);

    for (RenderingPlane plane : source.getPlanes()) {
      if (plane == null) {
        continue;
      }
      JPanel aRendererPanel = new JPanel(new GridBagLayout());
      gbc =
          new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
              GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0);

      JComboBox availableRenderers = new JComboBox();
      renderBoxes.put(plane, availableRenderers);
      availableRenderers.setEditable(false);
      availableRenderers.addItem(Resources.get("map.mapperpreferences.cmb.renderers.disabled"));

      final CardLayout cards = new CardLayout();
      final JPanel temp = new JPanel(cards);
      temp.add(new JPanel(), "NONE");

      for (MapCellRenderer r : source.getRenderers(plane.getIndex())) {
        availableRenderers.addItem(r);

        PreferencesAdapter adap = r.getPreferencesAdapter();
        Component adapterComponent = adap.getComponent();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(adapterComponent, BorderLayout.NORTH);
        temp.add(panel, r.getName());
        rendererAdapters.add(adap);
      }

      availableRenderers.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getItem() instanceof MapCellRenderer) {
              MapCellRenderer r = (MapCellRenderer) e.getItem();
              cards.show(temp, r.getName());

              // store that a renderer has been set for the selected plane
              RenderingPlane o = source.getPlanes().get(r.getPlaneIndex());
              planeMap.put(o, r);
            } else {
              // a mapper was deactivated
              int selectedIndex = planes.getSelectedIndex();

              if (selectedIndex > -1) {
                // store that a renderer has been set for the selected plane
                RenderingPlane o = source.getPlanes().get(selectedIndex);
                planeMap.put(o, null);
              }

              cards.first(temp);
            }

            temp.revalidate();
            temp.repaint();
          }
        }
      });

      JLabel lblRenderers = new JLabel(Resources.get("map.mapperpreferences.lbl.renderer.caption"));
      aRendererPanel.add(lblRenderers, gbc);
      gbc.gridx++;
      gbc.weightx = 1;
      aRendererPanel.add(availableRenderers, gbc);
      gbc.gridy++;
      gbc.gridx = 0;
      gbc.gridwidth = 2;
      gbc.weighty = 1;
      aRendererPanel.add(temp, gbc);

      MapCellRenderer r = plane.getRenderer();

      if (r != null) {
        availableRenderers.setSelectedItem(r);
      } else {
        availableRenderers.setSelectedIndex(0);
      }

      planes.addTab(plane.toString(), aRendererPanel);
    }

  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    for (PreferencesAdapter adap : rendererAdapters) {
      adap.initPreferences();
    }
    for (RenderingPlane plane : source.getPlanes()) {
      if (plane == null) {
        continue;
      }
      MapCellRenderer r = plane.getRenderer();
      JComboBox box = renderBoxes.get(plane);
      if (box != null)
        if (r != null) {
          box.setSelectedItem(r);
        } else {
          box.setSelectedIndex(0);
        }
    }
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    if (showTooltips != null) {
      source.setShowTooltip(showTooltips.isSelected());
    }

    if (dialogShown) {
      String[] tip = ttsDialog.getSelectedToolTip();
      if (tip != null) {
        source.setTooltipDefinition(tip[0], tip[1]);
      }

      dialogShown = false;
    }

    // set renderer for plane, taking those from the map is enough since only they can be changed
    for (RenderingPlane p : planeMap.keySet()) {
      source.setRenderer(planeMap.get(p), p.getIndex());
    }

    // apply changes on the renderers
    for (PreferencesAdapter adap : rendererAdapters) {
      adap.applyPreferences();
    }

    Mapper.setRenderContextChanged(true);
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
    return Resources.get("map.mapperpreferences.title");
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(java.awt.event.ActionEvent p1) {
    if (ttsDialog == null) {
      Component parent = getTopLevelAncestor();

      if (parent instanceof Frame) {
        ttsDialog =
            new ToolTipSwitcherDialog((Frame) parent, Resources
                .get("map.mapperpreferences.tooltipdialog.title"));
      } else {
        ttsDialog =
            new ToolTipSwitcherDialog((Dialog) parent, Resources
                .get("map.mapperpreferences.tooltipdialog.title"));
      }
    }

    ttsDialog.setVisible(true);
    dialogShown = true;
  }

  protected class ToolTipSwitcherDialog extends JDialog implements ActionListener,
      javax.swing.event.ListSelectionListener {
    /**
     * Imports/exports tooltips from/to text files. The text files are interpreted two-line-wise.
     * The first line acts as the name and the second one as the definition.
     */
    protected class ImExportDialog extends JDialog implements ActionListener {
      private final Logger log = Logger.getInstance(ImExportDialog.class);
      protected JFileChooser fileChooser;
      protected Dialog parent;
      protected JList list;
      protected java.util.List<String> data;
      protected JButton ok;
      protected boolean approved = false;

      /**
       * Creates a new ImExportDialog object.
       */
      public ImExportDialog(Dialog parent, String title) {
        super(parent, title, true);

        this.parent = parent;

        list = new JList();
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(100, 200));

        getContentPane().add(sp, BorderLayout.CENTER);

        JPanel p = new JPanel(new FlowLayout());
        ((FlowLayout) p.getLayout()).setAlignment(FlowLayout.CENTER);

        JButton b = new JButton(Resources.get("map.mapperpreferences.imexportdialog.OK"));
        ok = b;
        b.addActionListener(this);
        p.add(b);
        b = new JButton(Resources.get("map.mapperpreferences.imexportdialog.Cancel"));
        b.addActionListener(this);
        p.add(b);

        getContentPane().add(p, BorderLayout.SOUTH);
      }

      /**
       * Show dialog to export or import the tooltips.
       * 
       * @param doImport show import dialog if this is <code>true</code>, otherwise export dialog
       */
      public void showDialog(boolean doImport) {
        File file = null;
        JFileChooser jfc = new JFileChooser(Client.getResourceDirectory());
        int ret = 0;

        if (doImport) {
          ret = jfc.showOpenDialog(this);
        } else {
          ret = jfc.showSaveDialog(this);
        }

        if (ret == JFileChooser.APPROVE_OPTION) {
          try {
            file = jfc.getSelectedFile();
            data = new LinkedList<String>();

            if (doImport) {
              if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String s1 = null;
                String s2 = null;

                try {
                  do {
                    s1 = br.readLine();
                    s2 = br.readLine();

                    if ((s1 != null) && !s1.equals("") && (s2 != null) && !s2.equals("")) {
                      data.add(s1);
                      data.add(s2);
                    }
                  } while (s2 != null);
                } catch (Exception inner) {
                  // bad data
                  log.warn("bad data: " + s1 + "/" + s2);
                } finally {
                  br.close();
                }

                if (data.size() > 0) {
                  Object a[] = new Object[data.size() / 2];

                  for (int i = 0; i < a.length; i++) {
                    a[i] = data.get(i * 2);
                  }

                  list.setListData(a);
                  pack();
                  setLocationRelativeTo(parent);
                  approved = false;
                  setVisible(true);

                  if (approved) {
                    int indices[] = list.getSelectedIndices();

                    if ((indices != null) && (indices.length > 0)) {
                      for (int indice : indices) {
                        source.addTooltipDefinition(data.get(indice * 2), data
                            .get((indice * 2) + 1));
                      }
                    }
                  }
                } else { // no def found, show error
                  JOptionPane.showMessageDialog(this, Resources
                      .get("map.mapperpreferences.imexportdialog.nodeffound"));
                }
              } else { // file not found, show error
                JOptionPane.showMessageDialog(this, Resources
                    .get("map.mapperpreferences.imexportdialog.fnf"));
              }
            } else {
              data = source.getAllTooltipDefinitions();

              Object a[] = new Object[data.size() / 2];

              for (int i = 0; i < a.length; i++) {
                a[i] = data.get(i * 2);
              }

              list.setListData(a);
              pack();
              setLocationRelativeTo(parent);
              approved = false;
              setVisible(true);

              if (approved) {
                int indices[] = list.getSelectedIndices();

                if ((indices != null) && (indices.length > 0)) {
                  PrintWriter bw = new PrintWriter(new FileWriter(file));
                  ListModel model = list.getModel();

                  for (int indice : indices) {
                    bw.println(model.getElementAt(indice));
                    bw.println(data.get((indice * 2) + 1));
                  }

                  bw.close();
                }
              }
            }
          } catch (Exception exc) { // some I/O Error, show it
            JOptionPane.showMessageDialog(this, Resources
                .get("map.mapperpreferences.imexportdialog.ioerror")
                + exc.toString());
            log.error(exc);
          }
        }
      }

      /**
       * Reacts to clicks on Ok (approved=true) or Cancel (approved = false) and hides dialog.
       * 
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        if (actionEvent.getSource() == ok) {
          approved = true;
        }

        setVisible(false);
      }
    }

    /**
     * A dialog for adding/editing tooltips. After successful editing(name and definition != ""),
     * all line breaks are terminated from the value since they cause errors in the replacer
     * engine(keywords can't be recognized if divided by a line break character).
     */
    protected class AddTooltipDialog extends JDialog implements ActionListener {
      protected JButton cancel;
      protected JTextField name;
      protected JTextArea value;
      protected boolean existed = false;
      protected String origName = null;
      protected int origIndex = -1;

      /**
       * Creates a new AddTooltipDialog object.
       */
      public AddTooltipDialog(Dialog parent, String title, String name, String def, int index) {
        super(parent, title, true);

        if ((name != null) && (def != null)) {
          existed = true;
          origName = name;
          origIndex = index;
        }

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        center.add(new JLabel(Resources.get("map.mapperpreferences.addtooltipdialog.name")), gbc);
        gbc.gridy++;
        center.add(new JLabel(Resources.get("map.mapperpreferences.addtooltipdialog.value")), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        center.add(this.name = new JTextField(name, 20), gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        center.add(new JScrollPane(value = new JTextArea(def, 5, 20)), gbc);

        value.setLineWrap(true);

        getContentPane().add(center, BorderLayout.CENTER);

        JPanel s = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton b = new JButton(Resources.get("map.mapperpreferences.addtooltipdialog.OK"));
        b.addActionListener(this);
        s.add(b);

        b = new JButton(Resources.get("map.mapperpreferences.addtooltipdialog.Cancel"));
        b.addActionListener(this);
        cancel = b;
        s.add(b);

        getContentPane().add(s, BorderLayout.SOUTH);

        pack();

        // getMinimumSize doesn't seem to work properly, so a little bit more
        if (getWidth() < 300) {
          this.setSize(300, getHeight());
        }

        if (getHeight() < 100) {
          this.setSize(getWidth(), 100);
        }

        setLocationRelativeTo(parent);
      }

      /**
       * @see java.awt.Container#getMinimumSize()
       */
      @Override
      public Dimension getMinimumSize() {
        return new Dimension(300, 100);
      }

      /**
       * Reacts to click on Ok (by creating and adding the new tooltip) or Cancel (by adding
       * nothing) and hiding the dialog.
       * 
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        if (actionEvent.getSource() != cancel) {
          if ((name.getText() != null) && !name.getText().equals("") && (value.getText() != null)
              && !value.getText().equals("")) {
            StringBuffer buf = new StringBuffer(value.getText());

            // remove all '\n'
            int i = 0;

            while (i < buf.length()) {
              if (buf.charAt(i) == '\n') {
                buf.deleteCharAt(i);
                i = 0; // start again
              } else {
                i++;
              }
            }

            if (existed && origName.equals(name.getText())) { // need to overwrite

              java.util.List<String> l = source.getAllTooltipDefinitions();
              l.set(origIndex + 1, buf.toString());
              source.setAllTooltipDefinitions(l);
            } else {
              source.addTooltipDefinition(name.getText(), buf.toString());
            }
          }
        }

        setVisible(false);
      }
    }

    protected class AddByMaskDialog extends JDialog implements ActionListener {
      protected JButton ok;
      protected JButton cancel;
      protected JTextField title;
      protected JTextField padding;
      protected JTextField table[][];
      protected JCheckBox excludeOceans;
      protected String name;
      protected String leftPart = null;
      protected String midPart = null;
      protected String rightPart = null;
      protected String tableAttribs = null;
      protected int origIndex = -1;
      protected GridBagConstraints gbc = null;

      /**
       * Creates a new AddByMaskDialog object.
       */
      public AddByMaskDialog(Dialog parent, String title) {
        super(parent, title, true);
      }

      /**
       * Creates a dialog for adding a tooltip in table form. Asks for tooltip name, rows and cols.
       * 
       * @return <code>false</code> if input was aborted.
       */
      public boolean init() {
        // clear parts to flag that this is a new mask
        leftPart = null;
        midPart = null;
        rightPart = null;

        // get name and size
        name =
            JOptionPane.showInputDialog(this, Resources
                .get("map.mapperpreferences.tooltipdialog.addbymask.name"));

        if ((name == null) || (name.length() < 1))
          return false;

        String s =
            JOptionPane.showInputDialog(this, Resources
                .get("map.mapperpreferences.tooltipdialog.addbymask.rows"));
        int rows = 0;

        try {
          rows = Integer.parseInt(s);
        } catch (Exception exc) {
          return false;
        }

        if (rows <= 0)
          return false;

        s =
            JOptionPane.showInputDialog(this, Resources
                .get("map.mapperpreferences.tooltipdialog.addbymask.columns"));

        int columns = 0;

        try {
          columns = Integer.parseInt(s);
        } catch (Exception exc) {
          return false;
        }

        if (columns <= 0)
          return false;

        Container content = getContentPane();
        content.setLayout(new GridBagLayout());
        content.removeAll();

        initComponents(content);

        initUI(content, rows, columns, null, "0", null, true); // emtpy table

        pack();
        setLocationRelativeTo(getParent());
        return true;
      }

      protected String removePSigns(String s) {
        while (s.startsWith("§")) {
          s = s.substring(1);
        }

        while (s.endsWith("§")) {
          try {
            s = s.substring(0, s.length() - 1);
          } catch (Exception exc) {
            break;
          }
        }

        return s;
      }

      /**
       * Creates a dialog for editing a tooltip in table form. Asks for tooltip name, rows and cols.
       * 
       * @return <code>false</code> if input was aborted or old definition could not be parsed.
       */
      public boolean init(String tipName, String tipDef, int index) {
        /*
         * try to find the parts of the mask Assume: Start ...
         * "<b><center>"title"</center></b>"..."<table"table data"</table>"...End
         */
        name = tipName;
        origIndex = index;

        int i = tipDef.indexOf("<b><center>");

        if (i == -1) { // not correct
          i = tipDef.indexOf("<center><b>");

          if (i == -1)
            return false;
        }

        leftPart = tipDef.substring(0, i);

        String rest = tipDef.substring(i + 11);

        i = rest.indexOf("</center></b>");

        if (i == -1) {
          i = tipDef.indexOf("</b></center>");

          if (i == -1)
            return false;
        }

        String titleS = removePSigns(rest.substring(0, i));

        rest = rest.substring(i + 13);

        i = rest.indexOf("<table");

        int tagEnd = rest.indexOf("\">");
        String paddingS = rest.substring(i + 20, tagEnd);

        int j = rest.indexOf("</table>");

        if ((i == -1) || (j == -1))
          return false;

        String tData = rest.substring(i, j);

        rest = rest.substring(j + 8);

        rightPart = rest;

        // now that we have the parts try to parse the table data
        int rows = 0;

        // now that we have the parts try to parse the table data
        int columns = 0;
        rest = tData;

        // first get possible table attribs
        tableAttribs = rest.substring(6, rest.indexOf('>'));

        // now compute table size
        rest = rest.substring(rest.indexOf('>') + 1);

        int ccolumns = 0;

        while (rest.length() > 0) {
          if (rest.indexOf('<') >= 0) {
            rest = rest.substring(rest.indexOf('<') + 1);

            if (rest.startsWith("tr")) { // new row
              ccolumns = 0;
              rows++;
            } else if (rest.startsWith("td")) { // new element
              ccolumns++;

              if (ccolumns > columns) {
                columns = ccolumns;
              }
            }
          } else {
            rest = "";
          }
        }

        if ((rows == 0) || (columns == 0))
          return false;

        // now get the table elements
        String values[][] = new String[rows][columns];
        rest = tData.substring(tData.indexOf('>') + 1);
        i = -1;
        j = 0;

        while (rest.length() > 0) {
          if (rest.indexOf('<') >= 0) {
            rest = rest.substring(rest.indexOf('<') + 1);

            if (rest.startsWith("tr")) { // new row
              j = -1;
              i++;
            } else if (rest.startsWith("td")) { // new element
              j++;

              // extract data
              int k = rest.indexOf("</td>");

              if (k == -1) {
                k = Integer.MAX_VALUE;
              }

              int l = rest.indexOf("<td>");

              if (l == -1) {
                l = Integer.MAX_VALUE;
              }

              int m = rest.indexOf("</tr>");

              if (m == -1) {
                m = Integer.MAX_VALUE;
              }

              int n = rest.indexOf("<tr>");

              if (n == -1) {
                n = Integer.MAX_VALUE;
              }

              int o = rest.indexOf("</table>");

              if (o == -1) {
                o = Integer.MAX_VALUE;
              }

              int p = Math.min(k, l);
              int q = Math.min(m, n);
              p = Math.min(p, o);
              p = Math.min(p, q);

              if (p < 3) {
                values[i][j] = removePSigns(rest.substring(3));
              } else {
                values[i][j] = removePSigns(rest.substring(3, p));
              }

              rest = rest.substring(3 + ((values[i][j] == null) ? 0 : values[i][j].length()));
            }
          } else {
            rest = "";
          }
        }

        Container content = getContentPane();
        content.removeAll();

        initComponents(content);

        initUI(content, rows, columns, titleS, paddingS, values, false);

        pack();
        setLocationRelativeTo(getParent());
        return true;
      }

      protected void initComponents(Container content) {
        if (gbc == null) {
          content.setLayout(new GridBagLayout());
          gbc = new GridBagConstraints();
          gbc.fill = GridBagConstraints.HORIZONTAL;
          gbc.anchor = GridBagConstraints.CENTER;
          gbc.weightx = 1;
          gbc.insets = new Insets(2, 2, 2, 2);
        }

        if (title == null) {
          title = new JTextField(20);
        } else {
          title.setText(null);
        }

        if (padding == null) {
          padding = new JTextField("0", 3);
        }

        if (ok == null) {
          ok = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.addbymask.ok"));
          ok.addActionListener(this);
        }

        if (cancel == null) {
          cancel =
              new JButton(Resources.get("map.mapperpreferences.tooltipdialog.addbymask.cancel"));
          cancel.addActionListener(this);
        }

        if (excludeOceans == null) {
          excludeOceans =
              new JCheckBox(Resources
                  .get("map.mapperpreferences.tooltipdialog.addbymask.excludeocean"), true);
        }
      }

      protected void initUI(Container content, int rows, int cols, String titleS, String paddingS,
          String values[][], boolean showExclude) {
        // title
        if (title == null) {
          title = new JTextField(20);
        }
        title.setText(titleS);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        content.add(title, gbc);

        JLabel label = new JLabel("cellpadding");
        if (padding == null) {
          padding = new JTextField("0", 3);
        }
        padding.setText(paddingS);

        gbc.gridwidth = 1;
        gbc.gridx = 2;
        content.add(label);
        gbc.gridx++;
        content.add(padding, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;

        // table
        table = new JTextField[rows][cols];

        for (int i = 0; i < rows; i++) {
          gbc.gridy++;

          for (int j = 0; j < cols; j++) {
            table[i][j] = new JTextField(20);
            gbc.gridx = j;

            if (values != null) {
              table[i][j].setText(values[i][j]);
            }

            content.add(table[i][j], gbc);
          }
        }

        // buttons
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 2));

        if (showExclude) {
          south.add(excludeOceans);
        }

        south.add(ok);
        south.add(cancel);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        content.add(south, gbc);
      }

      /**
       * @see java.awt.Container#getMinimumSize()
       */
      @Override
      public Dimension getMinimumSize() {
        return new Dimension(300, 100);
      }

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        if (actionEvent.getSource() != cancel) {
          createTable();
        }

        setVisible(false);
      }

      /**
       * Creates the table out of the GUI mask. All text elements from the fields are packed inside
       * paragraph signs. Uses some default things like bold title.
       */
      protected void createTable() {
        if (leftPart == null) {
          createNewTable();
        } else {
          createEditedTable();
        }
      }

      protected void createEditedTable() {
        String rowStart = "<tr>";
        String columnStart = "<td>§";
        String columnEnd = "§</td>";
        String rowEnd = "</tr>";

        StringBuffer buf = new StringBuffer();

        if (leftPart != null) {
          buf.append(leftPart);
        }

        buf.append("<b><center>§");
        buf.append(title.getText());
        buf.append("§</center></b>");

        if (midPart != null) {
          buf.append(midPart);
        }

        buf.append("<table");

        if (tableAttribs != null) {
          Pattern p = Pattern.compile("cellpadding=\"[^\"]*\"");
          Matcher m = p.matcher(tableAttribs);
          while (m.find()) {
            m.appendReplacement(buf, "cellpadding=\"" + padding.getText() + "\"");
          }
          m.appendTail(buf);
        }
        buf.append(">");

        // table
        for (JTextField[] element : table) {
          buf.append(rowStart);

          for (JTextField element2 : element) {
            buf.append(columnStart);

            if (element2.getText() != null) {
              buf.append(removePSigns(element2.getText()));
            }

            buf.append(columnEnd);
          }

          buf.append(rowEnd);
        }

        buf.append("</table>");

        if (rightPart != null) {
          buf.append(rightPart);
        }

        java.util.List<String> l = source.getAllTooltipDefinitions();
        l.set(origIndex + 1, buf.toString());
        source.setAllTooltipDefinitions(l);
      }

      protected void createNewTable() {
        String rowStart = "<tr>";
        String columnStart = "<td>§";
        String columnEnd = "§</td>";
        String rowEnd = "</tr>";

        StringBuffer buf = new StringBuffer("<html>");

        // exclude oceans if selected
        if (excludeOceans.isSelected()) {
          buf.append("§if§isOzean§Ozean§else§");
        }

        buf.append("<b><center>§");

        // title
        if (title.getText() != null) {
          buf.append(title.getText());
        }

        buf.append("§</center></b><table cellpadding=\"" + padding.getText() + "\">");

        // table
        for (JTextField[] element : table) {
          buf.append(rowStart);

          for (JTextField element2 : element) {
            buf.append(columnStart);

            if (element2.getText() != null) {
              buf.append(removePSigns(element2.getText()));
            }

            buf.append(columnEnd);
          }

          buf.append(rowEnd);
        }

        buf.append("</table>");

        if (excludeOceans.isSelected()) {
          buf.append("§end§");
        }

        buf.append("</html>");

        source.addTooltipDefinition(name, buf.toString());
      }
    }

    protected JList<String> tooltipList;
    protected List<String> tooltips;
    protected JButton add;
    protected JButton edit;
    protected JButton info;
    protected JButton importT;
    protected JButton exportT;
    protected JButton delete;
    protected JButton mask;
    protected JButton editmask;
    protected JTextField text;
    protected Component listComp = null;
    protected Container listCont;
    protected ToolTipReplacersInfo infoDialog = null;
    protected AddByMaskDialog maskDialog = null;

    /**
     * Creates a new ToolTipSwitcherDialog object.
     */
    public ToolTipSwitcherDialog(Frame parent, String title) {
      super(parent, title, true);
      initDialog();
    }

    /**
     * Creates a new ToolTipSwitcherDialog object.
     */
    public ToolTipSwitcherDialog(Dialog parent, String title) {
      super(parent, title, true);
      initDialog();
    }

    private void initDialog() {
      Container content = getContentPane();
      content.setLayout(new BorderLayout());

      JButton b;

      JPanel main = new JPanel(new BorderLayout());
      javax.swing.border.Border border =
          new CompoundBorder(BorderFactory.createEtchedBorder(), new EmptyBorder(3, 3, 3, 3));

      main.setBorder(border);

      text = new JTextField(30);
      text.setEditable(false);
      main.add(text, BorderLayout.NORTH);

      JPanel mInner = new JPanel(new BorderLayout());

      Container iButtons = new JPanel(new GridBagLayout());

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridwidth = 1;
      gbc.gridheight = 2;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 0.5;
      gbc.insets = new Insets(0, 1, 10, 1);

      JPanel normal = new JPanel(new GridLayout(0, 1, 2, 3));
      normal.setBorder(border);

      b = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.add"));
      b.addActionListener(this);
      add = b;
      normal.add(b);
      b = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.edit"));
      b.addActionListener(this);
      edit = b;
      edit.setEnabled(false);
      normal.add(b);

      iButtons.add(normal, gbc);

      gbc.gridy = 2;

      normal = new JPanel(new GridLayout(0, 1, 2, 3));
      normal.setBorder(border);

      b = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.addbymask"));
      b.addActionListener(this);
      mask = b;
      normal.add(b);

      b = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.editbymask"));
      b.addActionListener(this);
      editmask = b;
      editmask.setEnabled(false);
      normal.add(b);

      iButtons.add(normal, gbc);

      gbc.gridheight = 1;
      gbc.gridy = 4;
      gbc.insets.left += 5;
      gbc.insets.right += 5;
      gbc.insets.bottom = 0;

      b = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.delete"));
      b.addActionListener(this);
      delete = b;
      delete.setEnabled(false);
      iButtons.add(b, gbc);

      JPanel iDummy = new JPanel();
      iDummy.add(iButtons);

      mInner.add(iDummy, BorderLayout.EAST);

      listCont = mInner;
      recreate();

      main.add(mInner, BorderLayout.CENTER);

      Container east = new JPanel(new GridLayout(0, 1, 2, 3));

      b = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.ok"));
      b.addActionListener(this);
      east.add(b);

      b = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.info"));
      info = b;
      b.addActionListener(this);
      east.add(b);

      b = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.import"));
      b.addActionListener(this);
      importT = b;
      east.add(b);

      b = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.export"));
      b.addActionListener(this);
      exportT = b;
      east.add(b);

      JPanel dummy = new JPanel();
      dummy.add(east);
      content.add(dummy, BorderLayout.EAST);
      content.add(main, BorderLayout.CENTER);

      pack();
      setLocationRelativeTo(getParent());
    }

    /**
     * @see java.awt.Container#getMinimumSize()
     */
    @Override
    public Dimension getMinimumSize() {
      return new Dimension(100, 100);
    }

    protected void recreate() {
      if (listCont == null)
        return;

      java.util.List<String> list = source.getAllTooltipDefinitions();
      List<String> v = new LinkedList<String>();
      tooltips = new LinkedList<String>();

      Iterator<String> it = list.iterator();

      while (it.hasNext()) {
        String name = it.next();
        String def = it.next();
        v.add(name);
        tooltips.add(def);
      }

      if (tooltipList == null) {
        tooltipList = new JList<String>(v.toArray(new String[] {}));
        tooltipList.setBorder(BorderFactory.createEtchedBorder());
        tooltipList.addListSelectionListener(this);
        tooltipList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane jsp = new JScrollPane(tooltipList);
        String[] currentTT = source.getTooltipDefinition();
        if (currentTT != null && currentTT[0] != null) {
          tooltipList.setSelectedValue(currentTT[0], true);
        }
        listComp = jsp;
        listCont.add(jsp, BorderLayout.CENTER);
      } else {
        int oldIndex = tooltipList.getSelectedIndex();
        tooltipList.setListData(v.toArray(new String[] {}));

        if (oldIndex >= v.size()) {
          oldIndex = -1;
        }

        tooltipList.setSelectedIndex(oldIndex);

        if (oldIndex != -1) {
          text.setText(tooltips.get(oldIndex));
        }
      }

      pack();
      setLocationRelativeTo(getParent());
      getContentPane().repaint();
      this.repaint();
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == importT) {
        new ImExportDialog(this, Resources
            .get("map.mapperpreferences.tooltipdialog.importdialog.title")).showDialog(true);
        recreate();
      } else if (e.getSource() == exportT) {
        new ImExportDialog(this, Resources
            .get("map.mapperpreferences.tooltipdialog.exportdialog.title")).showDialog(false);
      } else if (e.getSource() == add) {
        new AddTooltipDialog(this, Resources
            .get("map.mapperpreferences.tooltipdialog.addtooltipdialog.title"), null, null, -1)
                .setVisible(true);
        recreate();
      } else if (e.getSource() == mask) {
        if (maskDialog == null) {
          maskDialog =
              new AddByMaskDialog(this, Resources
                  .get("map.mapperpreferences.tooltipdialog.addbymask.title"));
        }
        if (maskDialog.init()) {
          maskDialog.setVisible(true);
          recreate();
        }
      } else if (e.getSource() == editmask) {
        if ((tooltipList != null) && (tooltipList.getSelectedIndex() > -1)) {
          int index = tooltipList.getSelectedIndex();

          if (maskDialog == null) {
            maskDialog =
                new AddByMaskDialog(this, Resources
                    .get("map.mapperpreferences.tooltipdialog.addbymask.title"));
          }

          if (maskDialog.init(tooltipList.getSelectedValue().toString(), tooltips.get(index),
              index * 2)) {
            maskDialog.setVisible(true);
            recreate();
          }
        }
      } else if (e.getSource() == edit) {
        if ((tooltipList != null) && (tooltipList.getSelectedIndex() > -1)) {
          int index = tooltipList.getSelectedIndex();
          AddTooltipDialog dialog =
              new AddTooltipDialog(this, Resources
                  .get("map.mapperpreferences.tooltipdialog.addtooltipdialog.title2"), tooltipList
                      .getSelectedValue().toString(), tooltips.get(index), index * 2);
          dialog.setVisible(true);
          recreate();
        }
      } else if (e.getSource() == delete) {
        try {
          java.util.List<String> src = source.getAllTooltipDefinitions();
          src.remove(tooltipList.getSelectedIndex() * 2);
          src.remove(tooltipList.getSelectedIndex() * 2);
          source.setAllTooltipDefinitions(src);
          recreate();
        } catch (Exception exc) {
          //
        }
      } else if (e.getSource() == info) {
        ToolTipReplacersInfo.showInfoDialog(this, Resources.get(
            "map.mapperpreferences.tooltipdialog.help"));
      } else {
        setVisible(false);

        if (infoDialog != null) {
          infoDialog.setVisible(false);
        }
      }
    }

    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
      if (tooltipList.getSelectedIndex() >= 0) {
        text.setText(tooltips.get(tooltipList.getSelectedIndex()));
        edit.setEnabled(true);
        editmask.setEnabled(true);
        delete.setEnabled(true);
      } else {
        edit.setEnabled(false);
        editmask.setEnabled(false);
        delete.setEnabled(false);
      }
    }

    /**
     * Return the currently selected tooltip in the list (the first one if none are selected).
     */
    public String[] getSelectedToolTip() {
      if (tooltipList != null) {
        int i = tooltipList.getSelectedIndex();
        i = Math.max(i, 0);

        return new String[] { tooltipList.getModel().getElementAt(i), tooltips.get(i) };
      }

      return null;
    }
  }

}
