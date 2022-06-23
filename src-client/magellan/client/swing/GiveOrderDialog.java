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

package magellan.client.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import magellan.client.completion.AutoCompletion;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.swing.completion.MultiEditorOrderEditorList;
import magellan.client.utils.SwingUtils;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Item;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * A Dialog that asks the user for a string input (usually an eressea order). In Addition the user
 * is asked, if the order shall extend or replace existing orders.
 *
 * @author Ulrich Küster
 */
public class GiveOrderDialog extends InternationalizedDialog {
  private static final Logger log = Logger.getInstance(GiveOrderDialog.class);

  public static final String FIRST_POS = "first";
  public static final String LAST_POS = "last";

  private ButtonGroup position;
  private JCheckBox replaceOrders;
  private JCheckBox keepComments;
  private JButton ok;
  private JButton cancel;

  private Unit dummyUnit;

  private GameData world;

  private Collection<Unit> selectedUnits;

  private MultiEditorOrderEditorList editors;

  private Properties dummySettings;

  private EventDispatcher dummyDispatcher;

  private Properties settings;

  /**
   * Creates a new GiveOrderDialog object.
   *
   * @param dispatcher
   * @param settings
   * @param data
   */
  public GiveOrderDialog(Frame owner, Collection<Unit> selectedUnits, GameData data,
      Properties settings, EventDispatcher dispatcher) {
    super(owner, true);
    world = data;
    this.settings = settings;
    this.selectedUnits = selectedUnits;
    setTitle(Resources.get("giveorderdialog.window.title"));

    Container cp = getContentPane();
    cp.setLayout(new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
            new Insets(3, 3, 3, 3), 0, 0);

    c.gridwidth = 2;

    JLabel label = new JLabel(Resources.get("giveorderdialog.window.message"));
    cp.add(label, c);

    editors = getEditorList(getDummyDispatcher(), data, getDummySettings());
    editors.setHideButtons(true);
    editors.setListMode(MultiEditorOrderEditorList.LIST_UNIT);

    // build auto completion structure
    AutoCompletion completion = new AutoCompletion(getDummySettings(), getDummyDispatcher()) {
      @Override
      public boolean getLimitMakeCompletion() {
        return false;
      }

      @Override
      public boolean isLimitCompletions() {
        return false;
      }
    };
    editors.setCompleter(completion);
    editors.setEditAllFactions(true);
    completion.attachEditorManager(editors);
    GameDataEvent event = new GameDataEvent(this, data, false);
    completion.gameDataChanged(event);
    editors.gameDataChanged(event);
    dummyUnit = getDummyUnit();
    editors.selectionChanged(SelectionEvent.create(this, dummyUnit, SelectionEvent.ST_DEFAULT));
    SwingUtils.setPreferredSize(editors, 25, 8, true);
    editors.setMinimumSize(new Dimension(100, 100));

    JScrollPane helperPane = new JScrollPane(editors);

    c.gridx = 0;
    c.gridy++;
    c.weightx = 2;
    c.weighty = 1;
    c.gridwidth = 2;
    cp.add(helperPane, c);

    JRadioButton firstButton = new JRadioButton(Resources.get("giveorderdialog.radio.first.title"));
    firstButton.setActionCommand(GiveOrderDialog.FIRST_POS);
    JRadioButton lastButton = new JRadioButton(Resources.get("giveorderdialog.radio.last.title"));
    lastButton.setActionCommand(GiveOrderDialog.LAST_POS);
    position = new ButtonGroup();
    position.add(firstButton);
    position.add(lastButton);
    position.setSelected(firstButton.getModel(), true);

    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy++;
    c.weightx = 0;
    c.weighty = 0;
    c.anchor = GridBagConstraints.WEST;
    cp.add(firstButton, c);
    c.gridx = 1;
    c.weightx = 0;
    c.anchor = GridBagConstraints.EAST;
    cp.add(lastButton, c);

    replaceOrders = new JCheckBox(Resources.get("giveorderdialog.chkbox.replaceOrder.title"));
    c.gridx = 0;
    c.gridy++;
    c.weightx = 0;
    cp.add(replaceOrders, c);

    keepComments = new JCheckBox(Resources.get("giveorderdialog.chkbox.keepComments.title"));
    keepComments.setSelected(true);
    c.gridy++;
    cp.add(keepComments, c);

    ok = new JButton(Resources.get("giveorderdialog.btn.ok.caption"));
    ok.setMnemonic(Resources.get("giveorderdialog.btn.ok.mnemonic").charAt(0));

    // actionListener is added in the show() method
    c.gridy++;
    c.anchor = GridBagConstraints.EAST;
    cp.add(ok, c);

    cancel = new JButton(Resources.get("giveorderdialog.btn.cancel.caption"));
    cancel.setMnemonic(Resources.get("giveorderdialog.btn.cancel.mnemonic").charAt(0));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit();
      }
    });
    c.gridx = 1;
    c.anchor = GridBagConstraints.WEST;
    cp.add(cancel, c);
  }

  protected MultiEditorOrderEditorList getEditorList(EventDispatcher dispatcher, GameData data,
      Properties settings) {
    return new MultiEditorOrderEditorList(getDummyDispatcher(), data, getDummySettings(), null) {
      @Override
      public javax.swing.border.Border getBorder(Unit u, boolean active) {
        TitledBorder border = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), getCaption());
        return border;
      }

      ButtonPanel buttonPanel;

      @Override
      protected ButtonPanel getButtonPanel() {
        if (buttonPanel == null) {
          buttonPanel = new ButtonPanel() {
            @Override
            public ShortcutListener getShortCutListener() {
              return new ShortcutListener() {

                public void shortCut(KeyStroke shortcut) {
                  // do nothing
                }

                public String getShortcutDescription(KeyStroke stroke) {
                  return "invalid";
                }

                public Iterator<KeyStroke> getShortCuts() {
                  return Collections.<KeyStroke> emptyList().iterator();
                }

                public String getListenerDescription() {
                  return "GiveOrderDialog.ButtonPanel";
                }
              };
            }
          };
        }
        return buttonPanel;
      }
    };

  }

  private Properties getDummySettings() {
    if (dummySettings == null) {
      dummySettings = new Properties(getSettings());
    }
    return dummySettings;
  }

  private EventDispatcher getDummyDispatcher() {
    if (dummyDispatcher == null) {
      dummyDispatcher = new EventDispatcher() {
      };
    }
    return dummyDispatcher;
  }

  private Properties getSettings() {
    return settings;
  }

  private String getCaption() {
    StringBuilder builder = new StringBuilder();
    int MAX_LENGTH = 40;
    if (selectedUnits.size() > 1) {
      builder.append(selectedUnits.size()).append(" ").append(
          Resources.get("context.unitcontextmenu.units")).append(": ");
    }
    boolean first = true;
    for (Unit u : selectedUnits) {
      if (first) {
        first = false;
      } else {
        builder.append(", ");
      }
      builder.append(u.toString());
      if (builder.length() >= MAX_LENGTH) {
        builder.delete(MAX_LENGTH, builder.length()).append("...");
        break;
      }
    }
    return builder.toString();
  }

  private Unit getDummyUnit() {
    if (dummyUnit == null) {
      GameData data = getData();
      int id = 1;
      for (id = 1; id < Integer.MAX_VALUE; ++id)
        if (data.getUnit(UnitID.createUnitID(id, data.base)) == null) {
          break;
        }
      dummyUnit = MagellanFactory.createUnit(UnitID.createUnitID(id, data.base), data);
      for (Unit u : selectedUnits) {
        for (Skill s : u.getSkills()) {
          Skill oldSkill = dummyUnit.getSkill(s.getSkillType());
          if (oldSkill == null) {
            dummyUnit.addSkill(new Skill(s.getSkillType(), s.getPoints(), s.getLevel(), 1, s
                .noSkillPoints()));
          } else {
            oldSkill.setLevel(Math.max(s.getLevel(), oldSkill.getLevel()));
          }
        }
        for (Item i : u.getItems()) {
          Item oldItem = dummyUnit.getItem(i.getItemType());
          if (oldItem == null) {
            dummyUnit.addItem(new Item(i.getItemType(), i.getAmount()));
          } else {
            oldItem.setAmount(Math.max(oldItem.getAmount(), i.getAmount()));
          }
        }
        dummyUnit.setRace(u.getRace());
        if (dummyUnit.getRegion() == null) {
          dummyUnit.setRegion(u.getRegion());
          dummyUnit.setFaction(u.getFaction());
        }
      }
      if (dummyUnit.getRegion() == null) {
        Region dummyRegion = MagellanFactory.createRegion(CoordinateID.create(0, 0), getData());
        dummyUnit.setRegion(dummyRegion);
      }
    }
    dummyUnit.setName(getCaption());
    return dummyUnit;
  }

  private GameData getData() {
    return world;
  }

  /**
   * Shows the dialog.
   *
   * @return A string array with the following values: <br/>
   *         [0] : The order that was given <br/>
   *         [1] : A String representative of the boolean value for "Replace orders" <br/>
   *         [2] : A String representative of the boolean value for "Keep comments" <br/>
   *         [3] : One of {@link GiveOrderDialog#FIRST_POS}, {@link GiveOrderDialog#LAST_POS}
   */
  public String[] showGiveOrderDialog() {
    final String retVal[] = new String[4];
    ActionListener okButtonAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        retVal[0] = getOrderText();
        retVal[1] = String.valueOf(replaceOrders.isSelected());
        retVal[2] = String.valueOf(keepComments.isSelected());
        retVal[3] = String.valueOf(position.getSelection().getActionCommand());
        GiveOrderDialog.log.debug(position.getSelection() + " " + retVal[3]);
        quit();
      }
    };

    ok.addActionListener(okButtonAction);
    // order.addActionListener(okButtonAction);
    pack();
    setLocationRelativeTo(getOwner());
    setVisible(true);

    return retVal;
  }

  @Override
  protected void quit() {
    super.quit();
    dummyUnit.setRegion(null);
    dummyUnit.setFaction(null);
    editors.quit();
  }

  protected String getOrderText() {
    StringBuilder builder = new StringBuilder();
    for (Order o : dummyUnit.getOrders2()) {
      builder.append(o.getText()).append("\n");
    }
    return builder.toString();
  }
}
