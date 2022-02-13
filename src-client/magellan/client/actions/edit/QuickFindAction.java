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

package magellan.client.actions.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.swing.InternationalizedDialog;
import magellan.client.utils.ImageFactory;
import magellan.library.Building;
import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.Identifiable;
import magellan.library.Named;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.utils.Resources;

/**
 * An action for quickly finding and selecting a game object by ID.
 * 
 * @author stm
 * @version 1.0
 */
public class QuickFindAction extends MenuAction {

  public static interface Iconizer {
    Icon get(Identifiable i);
  }

  public static class MyRenderer implements ListCellRenderer<Identifiable> {

    private Iconizer iconizer;

    MyRenderer(Iconizer i) {
      iconizer = i;
    }

    public Component getListCellRendererComponent(JList<? extends Identifiable> list, Identifiable value, int index,
        boolean isSelected, boolean cellHasFocus) {
      JPanel p = new JPanel(new BorderLayout());

      JLabel l = new JLabel(value == null ? "---" : String.valueOf(value));
      l.setIcon(iconizer.get(value));
      p.add(l, BorderLayout.WEST);
      p.add(new JPanel(), BorderLayout.CENTER);
      if (value instanceof HasRegion) {
        p.add(new JLabel(String.valueOf(((HasRegion) value).getRegion())), BorderLayout.EAST);
      }
      return p;
    }
  }

  private ImageFactory iFactory;
  private Map<String, ImageIcon> iCache;
  private Iconizer iconizer;

  /**
   * Creates a new FindAction object.
   * 
   * @param client
   */
  public QuickFindAction(Client client) {
    super(client);
    iFactory = client.getMagellanContext().getImageFactory();
    iCache = new HashMap<String, ImageIcon>();

    iconizer = new Iconizer() {

      public Icon get(Identifiable i) {
        String name;
        if (i instanceof Unit) {
          name = ((Unit) i).getRace().getIcon();
        } else if (i instanceof Ship) {
          name = ((Ship) i).getShipType().getIcon();
        } else if (i instanceof Building) {
          name = ((Building) i).getBuildingType().getIcon();
        } else if (i instanceof Building) {
          name = ((Building) i).getBuildingType().getIcon();
        } else if (i instanceof Region) {
          name = ((Region) i).getRegionType().getIcon();
        } else {
          name = "spionage";
        }
        ImageIcon icon = iCache.get(name);
        if (icon == null) {
          icon = iFactory.loadImageIcon(name);
          iCache.put(name, icon);
        }
        return icon;
      }
    };
  }

  /**
   * Displays the dialog and selects the entity (if found).
   */
  @Override
  public void menuActionPerformed(ActionEvent e) {
    QuickFindDialog f =
        new QuickFindDialog(client, client.getDispatcher(), client.getData(), client
            .getProperties());
    f.setVisible(true);
    Identifiable selection = f.getInput();
    if (selection != null) {
      client.getDispatcher().fire(SelectionEvent.create(this, selection, SelectionEvent.ST_DEFAULT));
    } else {
      // we could optionally display a short error message here...
    }
  }

  static final int TYPES = 4, SIZE = 3;

  static class Cache extends HashMap<String, Identifiable[][]> implements Map<String, Identifiable[][]> {

    private GameData data;

    public void update(GameData data) {
      if (data != this.data || true) {
        this.data = data;
        clear();
        for (int i = 4; i >= 0; --i) {
          addAll(i, data.getUnits(), 0);
          addAll(i, data.getShips(), 1);
          addAll(i, data.getBuildings(), 2);
          addAll(i, data.getRegions(), 3);
        }
      }

    }

    private void addAll(int length, Collection<? extends Identifiable> nameds, int type) {
      for (Identifiable named : nameds) {
        String id;
        if (named instanceof Region) {
          String name = ((Region) named).getName();
          if (name != null) {
            id = ((Region) named).getCoordinate().toString(",", true);
            add(length, named, type, id);
            add(length, named, type, name);
          }
        } else {
          id = named.getID().toString();
          add(length, named, type, id);
          if (named instanceof Named) {
            add(length, named, type, ((Named) named).getName());
          }
        }
      }
    }

    private void add(int length, Identifiable named, int type, String id) {
      if (id.length() >= length && (length > 0 || id.length() > 4)) {
        String pre = length > 0 ? id.toLowerCase().substring(0, length) : id.toLowerCase();
        Identifiable[][] found = get(pre);
        if (found == null) {
          found = new Identifiable[TYPES][];
          put(pre, found);
        }
        if (found[type] == null) {
          found[type] = new Identifiable[SIZE];
        }
        Identifiable[] list = found[type];
        if (length == 0 || length == id.length()) {
          for (int i = SIZE - 1; i > 0; --i) {
            list[i] = list[i - 1];
          }
          list[0] = named;
        } else {
          int i;
          for (i = 0; i < SIZE; ++i) {
            if (list[i] == null) {
              list[i] = named;
              break;
            }
          }
        }
      }
    }
  }

  Cache cache;

  /**
   * Tries to find a unit, a ship, or a building (in this order) with the id given as string.
   * 
   * @param input
   * @return The found entity or <code>null</code> if none was found
   */
  public Identifiable findEntity(String input) {
    Identifiable[][] cand = findCandidates(input);
    if (cand == null)
      return null;
    for (Identifiable[] names : cand)
      if (names != null && names[0] != null)
        return names[0];
    return null;
  }

  public Identifiable[][] findCandidates(String input) {
    if (cache == null) {
      cache = new Cache();
    }
    cache.update(client.getData());
    return cache.get(input.trim().toLowerCase().substring(0, Math.min(input.trim().length(), 4)));
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.quickfindaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.quickfindaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.quickfindaction.name");
  }

  /**
   * @see magellan.client.actions.MenuAction#getTooltipTranslated()
   */
  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.quickfindaction.tooltip", false);
  }

  /**
   * A dialog with a text input field to input an ID.
   * 
   * @author stm
   * @version 1.0, May 31, 2009
   */
  public class QuickFindDialog extends InternationalizedDialog {

    /** id input box */
    private JTextComponent idInput;
    private Identifiable selected;
    private DefaultListModel<Identifiable> entityModel;
    private JList<Identifiable> entityList;

    /**
     * Create the dialog
     * 
     * @param client
     * @param dispatcher
     * @param data
     * @param properties
     */
    public QuickFindDialog(Client client, EventDispatcher dispatcher, GameData data,
        Properties properties) {
      super(client, true);
      initGUI();
      pack();
    }

    private void initGUI() {
      setTitle(Resources.get("quickfinddialog.window.title"));
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      setLocationRelativeTo(client);
      setResizable(false);

      idInput = new JTextField(8);
      entityModel = new DefaultListModel<Identifiable>();
      // regionModel = new DefaultListModel<NamedWrapper>();

      entityList = createList(entityModel);

      // close dialog if ESCAPE (cancel) or ENTER (find) is pressed
      idInput.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          switch (e.getKeyCode()) {
          case KeyEvent.VK_ESCAPE:
            setVisible(false);
            idInput.setText("");
            break;
          case KeyEvent.VK_ENTER:
            setVisible(false);
            break;
          }
        }
      });

      // update entityText
      idInput.getDocument().addDocumentListener(new DocumentListener() {

        public void removeUpdate(DocumentEvent e) {
          update();
        }

        public void insertUpdate(DocumentEvent e) {
          update();
        }

        public void changedUpdate(DocumentEvent e) {
          update();
        }

        private void update() {
          Identifiable[][] found = findCandidates(idInput.getText());
          entityModel.clear();
          if (found == null)
            return;
          for (int p = 0; p < SIZE; ++p) {
            for (int t = 0; t < TYPES; ++t) {
              if (entityModel.size() < 3) {
                if (found[t] != null && found[t][p] != null) {
                  entityModel.addElement(found[t][p]);
                }
              }
            }
          }

          QuickFindDialog.this.validate();
          QuickFindDialog.this.pack();
        }

      });

      JPanel panel = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
      // panel.setLayout(new SpringLayout());
      panel.setBorder(new EtchedBorder());

      panel.add(new JLabel(Resources.get("quickfinddialog.idinput.label")), c);
      c.gridx++;
      panel.add(idInput, c);
      // JPanel results = new JPanel();
      // panel.add(results);
      c.gridy++;
      c.gridx = 0;
      c.gridwidth = 2;
      panel.add(entityList, c);
      // c.gridx++;
      // panel.add(regionList, c);
      getContentPane().add(panel);

      setUndecorated(true);
      // SpringUtilities.makeCompactGrid(panel, 2, 2, 7, 7, 7, 7);
      pack();
    }

    private JList<Identifiable> createList(DefaultListModel<Identifiable> model) {
      model.addElement(null);

      JList<Identifiable> list = new JList<Identifiable>(model);
      list.setVisibleRowCount(3);
      list.setFocusable(false);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.addListSelectionListener((e) -> select(list));
      ListCellRenderer<Identifiable> myrenderer = new MyRenderer(iconizer);
      list.setCellRenderer(myrenderer);
      return list;
    }

    private void select(JList<Identifiable> list) {
      Identifiable x = list.getSelectedValue();
      if (x != null) {
        selected = x;
      }
      setVisible(false);
    }

    /**
     * @return the value of the ID input field
     */
    public Identifiable getInput() {
      return selected == null ? findEntity(idInput.getText()) : selected;
    }

  }

}
