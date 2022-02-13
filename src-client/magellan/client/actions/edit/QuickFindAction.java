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
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.Identifiable;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitID;
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
      String name = String.valueOf(value);
      if (name.length() > 40) {
        name = name.substring(0, 40).concat("...");
      }
      JLabel l = new JLabel(value == null ? "---" : name);
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

  static final int TYPES = 4;

  /**
   * Tries to find a unit, a ship, or a building (in this order) with the id given as string.
   * 
   * @param input
   * @return The found entity or <code>null</code> if none was found
   */
  public Identifiable findEntity(String input) {
    Identifiable[] cand = findCandidates(input);
    for (Identifiable name : cand)
      if (name != null)
        return name;
    return null;
  }

  public Identifiable[] findCandidates(String input) {
    Identifiable[] cand = new Identifiable[TYPES];
    String id = input.trim().toLowerCase();
    GameData data = client.getData();
    try {
      cand[0] = data.getUnit(UnitID.createUnitID(id, data.base));
    } catch (Exception e) {
      //
    }
    try {
      cand[1] = data.getShip(EntityID.createEntityID(id, data.base));
    } catch (Exception e) {
      //
    }
    try {
      cand[2] = data.getBuilding(EntityID.createEntityID(id, data.base));
    } catch (Exception e) {
      //
    }
    try {
      cand[3] = data.getRegion(CoordinateID.parse(id, ","));
    } catch (Exception e) {
      //
    }
    if (cand[3] == null) {
      try {
        int c = id.indexOf(',');
        if (c > 0) {
          id = id.substring(0, c);
        }
        int x = Integer.parseInt(id);
        for (int y = 0; y < 10; ++y) {
          for (int sgn = -1; sgn <= 1; sgn += 2) {
            cand[3] = data.getRegion(CoordinateID.create(x, sgn * y));
          }
          if (cand[3] != null) {
            break;
          }
        }
      } catch (NumberFormatException e) {
        // ignore
      }
    }
    return cand;
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
          Identifiable[] cand = findCandidates(idInput.getText());
          entityModel.clear();
          for (Identifiable found : cand) {
            if (found != null) {
              entityModel.addElement(found);
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
