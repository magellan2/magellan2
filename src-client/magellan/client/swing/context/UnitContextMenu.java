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

package magellan.client.swing.context;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;

import magellan.client.Client;
import magellan.client.event.EventDispatcher;
import magellan.client.event.OrderConfirmEvent;
import magellan.client.event.SelectionEvent;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.GiveOrderDialog;
import magellan.client.swing.RemoveOrderDialog;
import magellan.client.swing.RoutingDialog;
import magellan.client.swing.context.actions.ContextAction;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.utils.OrderToken;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.ShipRoutePlanner;
import magellan.library.utils.UnitRoutePlanner;

/**
 * This is a contextmenu provider for one or more selected units.
 *
 * @author $Author: $
 * @version $Revision: 389 $
 */
public class UnitContextMenu extends JPopupMenu {
  private Unit unit;

  private GameData data;

  private EventDispatcher dispatcher;

  private Properties settings;

  /**
   * The selected Units (that are a subset of the selected objects in the overview tree). Notice:
   * this.unit does not need to be element of this collection!
   */
  private Collection<Unit> selectedUnits;

  private GameSpecificStuff gameSpecStuff;

  /**
   * Creates new UnitContextMenu
   *
   * @param unit last selected unit - is not required to be in selected objects
   * @param selectedObjects null or Collection of selected objects
   * @param dispatcher EventDispatcher
   * @param data the actual GameData or World
   */
  public UnitContextMenu(Unit unit, Collection<?> selectedObjects, EventDispatcher dispatcher,
      GameData data) {
    super(unit.toString());
    this.unit = unit;
    this.data = data;
    gameSpecStuff = data.getGameSpecificStuff();
    this.dispatcher = dispatcher;
    settings = dispatcher.getMagellanContext().getProperties();

    if (selectedObjects != null) {
      // tree selection
      init(selectedObjects);
    } else {
      // order editor selection
      init(Collections.singletonList(unit));
    }
  }

  /**
   * Initialize this component.
   */
  private void init(Collection<?> selectedObjects) {
    selectedUnits = ContextAction.filterObjects(selectedObjects, Unit.class);

    JMenuItem unitString = new JMenuItem(getCaption());
    // if (selectedObjects.contains(unit))
    // unitString.setEnabled(false);
    unitString.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispatcher.fire(SelectionEvent.create(UnitContextMenu.this, unit));
      }
    });

    add(unitString);

    if (!selectedObjects.contains(unit))
      return;

    initBoth();
  }

  private String getCaption() {
    if (selectedUnits.size() <= 1)
      return unit.toString();
    else
      return selectedUnits.size() + " " + Resources.get("context.unitcontextmenu.units");
  }

  /**
   * Sets some menu entries that can be used for one or multiple selected units.
   */
  private void initBoth() {
    if (selectedUnits.size() <= 1) {
      initSingle();
    } else {
      initMultiple();
      addSeparator();
    }

    // route planning menus
    JMenuItem planRoute = getPlanRoute();
    if (planRoute != null) {
      add(planRoute);
    }

    // this part for both (but only for selectedUnits)

    if (getComponentCount() > 0) {
      addSeparator();
    }

    // change confirmation status
    if (isEditAll() || containsPrivilegedUnit()) {
      JMenuItem validateOrders;
      if (shouldConfirm(selectedUnits)) {
        validateOrders =
            new JMenuItem(Resources.get("context.unitcontextmenu.menu.confirm.caption"));
      } else {
        validateOrders =
            new JMenuItem(Resources.get("context.unitcontextmenu.menu.unconfirm.caption"));
      }
      validateOrders.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_confirmOrders();
        }
      });
      add(validateOrders);

      // add order
      JMenuItem addOrder =
          new JMenuItem(Resources.get("context.unitcontextmenu.menu.addorder.caption"));
      addOrder.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_addOrder();
        }
      });
      add(addOrder);

      // remove orders
      JMenuItem removeOrders =
          new JMenuItem(Resources.get("context.unitcontextmenu.menu.removeorder.caption"));
      removeOrders.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_removeOrders();
        }
      });
      add(removeOrders);

    }

    // select selected units in overview
    if (selectedUnits.size() > 0) {
      JMenuItem selectUnits = null;
      if (selectedUnits.size() == 1) {
        selectUnits =
            new JMenuItem(Resources
                .get("context.unitcontextmenu.setasunitselection_singular.caption"));
      } else {
        selectUnits =
            new JMenuItem(Resources
                .get("context.unitcontextmenu.setasunitselection_plural.caption"));
      }
      selectUnits.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_selectUnits();
        }
      });
      add(selectUnits);
    }

    // tag stuff
    if (getComponentCount() > 0) {
      addSeparator();
    }

    // add tag menu
    JMenuItem addTag = new JMenuItem(Resources.get("context.unitcontextmenu.addtag.caption"));
    addTag.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_addTag();
      }
    });
    add(addTag);

    // remove tag menu
    Collection<String> tags = new TreeSet<String>();
    for (Unit u : selectedUnits) {
      tags.addAll(u.getTagMap().keySet());
    }
    int count = 0;
    for (String tag : tags) {
      if (count++ > 40) {
        add(new JMenuItem(Resources.get("context.unitcontextmenu.toomanytags.message")));
        break;
      }
      JMenuItem removeTag =
          new JMenuItem(Resources.get("context.unitcontextmenu.removetag.caption") + ": " + tag);
      removeTag.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_removeTag(e);
        }
      });
      add(removeTag);
    }

    initContextMenuProviders();
  }

  private JMenuItem getPlanRoute() {
    // test route planning capability
    boolean canPlan = UnitRoutePlanner.canPlan(unit);
    Region reg = unit.getRegion();

    if (canPlan && (selectedUnits != null)) {
      Iterator<Unit> it = selectedUnits.iterator();

      while (canPlan && it.hasNext()) {
        Unit u = it.next();
        canPlan = UnitRoutePlanner.canPlan(unit);

        if ((u.getRegion() == null) || !reg.equals(u.getRegion())) {
          canPlan = false;
        }
      }
    }

    if (canPlan) {
      JMenuItem planRoute = new JMenuItem(Resources.get("context.unitcontextmenu.menu.planroute"));

      planRoute.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          planRoute(e);
        }
      });
      return planRoute;
    }

    return null;
  }

  private void initContextMenuProviders() {
    Collection<UnitContextMenuProvider> cmpList = getContextMenuProviders();
    if (!cmpList.isEmpty()) {
      addSeparator();
    }

    for (UnitContextMenuProvider cmp : cmpList) {
      add(cmp.createContextMenu(dispatcher, data, unit, selectedUnits));
    }

  }

  /**
   * Searchs for Context Menu Providers in the plugins and adds them to the menu.
   */
  private Collection<UnitContextMenuProvider> getContextMenuProviders() {
    Collection<UnitContextMenuProvider> cmpList = new ArrayList<UnitContextMenuProvider>();
    for (MagellanPlugIn plugIn : Client.INSTANCE.getPlugIns()) {
      if (plugIn instanceof UnitContextMenuProvider) {
        cmpList.add((UnitContextMenuProvider) plugIn);
      }
    }
    return cmpList;
  }

  private void initMultiple() {
    // this part for multiple unit-selections

    // copy IDs to clipboard
    JMenuItem copyMultipleID =
        new JMenuItem(Resources.get("context.unitcontextmenu.menu.copyids.caption"));
    copyMultipleID.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_copyMultipleID(e);
      }
    });
    add(copyMultipleID);

    // copy IDs+names to clipboard
    JMenuItem copyMultipleNameID =
        new JMenuItem(Resources.get("context.unitcontextmenu.menu.copyidsandnames.caption"));
    copyMultipleNameID.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_copyMultipleNameID(e);
      }
    });
    add(copyMultipleNameID);

    // copy IDs+names+numPersons to clipboard
    JMenuItem copyMultipleNameIDPersonCount =
        new JMenuItem(Resources
            .get("context.unitcontextmenu.menu.copyidsandnamesandcounts.caption"));
    copyMultipleNameIDPersonCount.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_copyMultipleNameIDPersonCount(e);
      }
    });
    add(copyMultipleNameIDPersonCount);

  }

  private void initSingle() {
    // This part for single-unit-selections

    // copy ID to clipboard
    JMenuItem copyUnitID =
        new JMenuItem(Resources.get("context.unitcontextmenu.menu.copyid.caption"));
    copyUnitID.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_copyID(e);
      }
    });
    add(copyUnitID);

    // copy ID+name to clipboard
    JMenuItem copyUnitNameID =
        new JMenuItem(Resources.get("context.unitcontextmenu.menu.copyidandname.caption"));
    copyUnitNameID.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_copyNameID(e);
      }
    });
    add(copyUnitNameID);

    // add ID+name+numPersons to clipboard
    JMenuItem copyUnitNameIDPersonCount =
        new JMenuItem(Resources.get("context.unitcontextmenu.menu.copyidandnameandcount.caption"));
    copyUnitNameIDPersonCount.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_copyNameIDPersonCount(e);
      }
    });
    add(copyUnitNameIDPersonCount);

    if (getComponentCount() > 0) {
      addSeparator();
    }

    // add orders to disguise unit
    if (isEditAll() || magellan.library.utils.Units.isPrivilegedAndNoSpy(unit)) {
      JMenuItem hideID =
          new JMenuItem(Resources.get("context.unitcontextmenu.menu.disguise.caption"));

      hideID.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_hideID(e);
        }
      });
      add(hideID);
    }

    // remove unit from list of a teacher
    // is student of someone?
    for (Unit teacher : unit.getTeachers()) {
      JMenuItem removeFromTeachersList =
          new JMenuItem(Resources.get("context.unitcontextmenu.menu.removeFromTeachersList") + ": "
              + teacher.toString());
      add(removeFromTeachersList);
      removeFromTeachersList.addActionListener(new RemoveUnitFromTeachersListAction(unit, teacher,
          data));
    }

    // plan ship route
    if ((unit.getShip() != null) && unit.equals(unit.getShip().getModifiedOwnerUnit())) {
      JMenuItem planShipRoute =
          new JMenuItem(Resources.get("context.unitcontextmenu.menu.planshiproute.caption"));
      planShipRoute.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          planShipRoute();
        }
      });
      planShipRoute.setEnabled(ShipRoutePlanner.canPlan(unit.getShip()));
      add(planShipRoute);
    }

  }

  /**
   * Sets the selected Units as selected Units in Overview
   *
   * @author Fiete
   */
  private void event_selectUnits() {
    if (selectedUnits.size() > 1) {
      dispatcher.fire(SelectionEvent.create(this, selectedUnits.iterator().next(), selectedUnits));
    }
    if (selectedUnits.size() == 1) {
      dispatcher.fire(SelectionEvent.create(this, selectedUnits.iterator().next()));
    }
  }

  /**
   * Gives an order (optionally replacing the existing ones) to the selected units.
   */
  private void event_addOrder() {
    GiveOrderDialog giveOderDialog =
        new GiveOrderDialog(JOptionPane.getFrameForComponent(this), selectedUnits.isEmpty()
            ? Collections.singletonList(unit) : selectedUnits, data, settings, dispatcher);

    String s[] = giveOderDialog.showGiveOrderDialog();
    if (s[0] != null) {
      for (Unit u : selectedUnits) {
        if (isEditAll() || magellan.library.utils.Units.isPrivilegedAndNoSpy(u)) {
          magellan.client.utils.Units.addOrders(u, s, false);
          dispatcher.fire(new UnitOrdersEvent(this, u));
        }
      }
    }

    unit = null;
    selectedUnits.clear();
  }

  /**
   * Removes order containing certain strings of the selected units.
   */
  private void event_removeOrders() {
    RemoveOrderDialog removeOderDialog =
        new RemoveOrderDialog(JOptionPane.getFrameForComponent(this), getCaption());
    String s[] = removeOderDialog.showDialog();
    if (s[0] != null) {
      Set<Region> regions = new HashSet<Region>();
      for (Unit u : selectedUnits) {

        if (isEditAll() || magellan.library.utils.Units.isPrivilegedAndNoSpy(u)) {
          magellan.client.utils.Units.removeOrders(u, s);
          dispatcher.fire(new UnitOrdersEvent(this, u));
          regions.add(u.getRegion());
        }
      }
      for (Region r : regions) {
        r.refreshUnitRelations(true);
      }
    }

    unit = null;
    selectedUnits.clear();
  }

  /**
   * Changes the confirmation state of the selected units.
   */
  private void event_confirmOrders() {
    boolean status = shouldConfirm(selectedUnits);

    for (Unit u : selectedUnits) {
      u.setOrdersConfirmed(status);
    }

    dispatcher.fire(new OrderConfirmEvent(this, selectedUnits));

    unit = null;
    selectedUnits.clear();
  }

  private boolean shouldConfirm(Collection<Unit> selectedUnits) {
    return !(selectedUnits.iterator().next()).isOrdersConfirmed();
  }

  private void event_addTag() {
    String key = null;
    Collection<String> keys = new HashSet<String>();
    Collection<Unit> keyUnits = new HashSet<Unit>();

    {
      // collect all tag keys for all units in region
      Collection<Region> regions = new HashSet<Region>();
      for (Unit unit : selectedUnits) {
        regions.add(unit.getRegion());
      }
      for (Region r : regions) {
        for (Unit u : r.units()) {
          keyUnits.add(u);
          keys.addAll(u.getTagMap().keySet());
        }
      }
    }

    // present key selection
    key =
        showInputDialog(dispatcher.getMagellanContext().getClient(),
            Resources.get("context.unitcontextmenu.addtag.tagname.message"), sort(keys));

    if ((key != null) && (key.length() > 0)) {
      String value = null;

      Collection<String> keyValues = new HashSet<String>();
      for (Unit u : keyUnits) {
        String v = u.getTag(key);
        if (v != null) {
          keyValues.add(v);
        }
      }

      value =
          showInputDialog(dispatcher.getMagellanContext().getClient(), Resources.get(
              "context.unitcontextmenu.addtag.tagvalue.message"), sort(keyValues));

      if (value != null) {
        for (Unit u : selectedUnits) {
          u.putTag(key, value);
          // TODO: Coalesce unitordersevent
          dispatcher.fire(new UnitOrdersEvent(this, u));
        }
      }
    }

    unit = null;
    selectedUnits.clear();
  }

  static String[] sort(Collection<String> keys) {
    if (keys == null)
      return new String[] {};
    String[] sortedKeys = keys.toArray(new String[] {});
    Arrays.sort(sortedKeys);
    return sortedKeys;
  }

  /**
   * Lets user select one of the given values or enter a new one.
   */
  static String showInputDialog(Frame owner, String message, String[] values) {
    // the combo box (add/modify items if you like to)
    JComboBox<String> comboBox = new JComboBox<String>(values);
    // has to be editable
    comboBox.setEditable(true);
    comboBox.getEditor().selectAll();
    // change the editor's document
    // new JComboBoxCompletion(comboBox,true);

    // create and show a window containing the combo box
    JDialog frame = new JDialog(owner, message, true);
    frame.setLocationRelativeTo(owner);
    frame.getContentPane().setLayout(new BorderLayout());
    frame.setResizable(false);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    JPanel pane = new JPanel(new BorderLayout());
    pane.add(new JLabel(message), BorderLayout.NORTH);
    pane.add(comboBox, BorderLayout.CENTER);

    frame.getContentPane().add(new JLabel("  "), BorderLayout.NORTH);
    frame.getContentPane().add(new JLabel("  "), BorderLayout.SOUTH);
    frame.getContentPane().add(pane, BorderLayout.CENTER);
    frame.getContentPane().add(new JLabel("  "), BorderLayout.WEST);
    frame.getContentPane().add(new JLabel("  "), BorderLayout.EAST);
    frame.pack();

    comboBox.getEditor().getEditorComponent().addKeyListener(new MyKeyAdapter(frame, comboBox));

    frame.setVisible(true);
    frame.dispose();

    return (String) comboBox.getSelectedItem();
  }

  private static class MyOkAction extends AbstractAction {
    private JDialog frame;

    // private JComboBox comboBox;

    private MyOkAction(JDialog frame, JComboBox comboBox) {
      this.frame = frame;
      // this.comboBox = comboBox;
    }

    public void actionPerformed(ActionEvent e) {
      frame.setVisible(false);
    }
  }

  private static class MyCancelAction extends AbstractAction {
    private JDialog frame;

    private JComboBox comboBox;

    private MyCancelAction(JDialog frame, JComboBox comboBox) {
      this.frame = frame;
      this.comboBox = comboBox;
    }

    public void actionPerformed(ActionEvent e) {
      frame.setVisible(false);
      comboBox.setSelectedItem(null);
    }
  }

  private static class MyKeyAdapter extends KeyAdapter {
    private JDialog frame;

    private JComboBox comboBox;

    public MyKeyAdapter(JDialog frame, JComboBox comboBox) {
      this.frame = frame;
      this.comboBox = comboBox;
    }

    @Override
    public void keyPressed(KeyEvent e) {
      switch (e.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
        new MyCancelAction(frame, comboBox).actionPerformed(null);
        break;
      case KeyEvent.VK_ENTER:
        new MyOkAction(frame, comboBox).actionPerformed(null);
        break;
      }
    }
  }

  private void event_removeTag(ActionEvent e) {
    String command = e.getActionCommand();
    int index = command.indexOf(": ");
    if (index > 0) {
      String key = command.substring(index + 2, command.length());
      if (key != null) {
        for (Unit u : selectedUnits) {
          u.removeTag(key);
          dispatcher.fire(new UnitOrdersEvent(this, u));
        }
      }
    }

    unit = null;
    selectedUnits.clear();

  }

  private void event_copyID(ActionEvent e) {
    StringSelection strSel = new StringSelection(unit.toString(false));
    Clipboard cb = getToolkit().getSystemClipboard();

    cb.setContents(strSel, null);

    unit = null;
    selectedUnits.clear();
  }

  private void event_copyNameID(ActionEvent e) {

    StringSelection strSel = new StringSelection(unit.toString());
    Clipboard cb = getToolkit().getSystemClipboard();

    cb.setContents(strSel, null);

    unit = null;
    selectedUnits.clear();
  }

  private void event_copyNameIDPersonCount(ActionEvent e) {
    String s = unit.toString();
    s += ":" + unit.getPersons();
    if (unit.getModifiedPersons() != unit.getPersons()) {
      s += "(" + unit.getModifiedPersons() + ")";
    }
    StringSelection strSel = new StringSelection(s);
    Clipboard cb = getToolkit().getSystemClipboard();

    cb.setContents(strSel, null);

    unit = null;
    selectedUnits.clear();
  }

  private void event_hideID(ActionEvent e) {
    data.getGameSpecificStuff().getOrderChanger().addMultipleHideOrder(unit);
    dispatcher.fire(new UnitOrdersEvent(this, unit));

    unit = null;
    selectedUnits.clear();
  }

  private void event_copyMultipleID(ActionEvent e) {
    StringBuffer idString = new StringBuffer("");

    for (Iterator<Unit> iter = selectedUnits.iterator(); iter.hasNext();) {
      Unit u = iter.next();

      idString.append(u.toString(false));
      if (iter.hasNext()) {
        idString.append(" ");
      }
    }

    StringSelection strSel = new StringSelection(idString.toString());
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);

    unit = null;
    selectedUnits.clear();
  }

  private void event_copyMultipleNameID(ActionEvent e) {
    StringBuilder s = new StringBuilder();

    for (Unit u : selectedUnits) {
      s.append(u.toString()).append("\n");
    }

    StringSelection strSel = new StringSelection(s.toString());
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);

    unit = null;
    selectedUnits.clear();
  }

  private void event_copyMultipleNameIDPersonCount(ActionEvent e) {
    StringBuilder s = new StringBuilder();
    int cntUnits = 0;
    int cntPersons = 0;
    int cntModifiedPersons = 0;
    for (Unit u : selectedUnits) {
      s.append(u.toString()).append(":").append(u.getPersons());
      if (u.getModifiedPersons() != u.getPersons()) {
        s.append("(").append(u.getModifiedPersons()).append(")");
      }
      s.append("\n");
      cntUnits++;
      cntPersons += u.getPersons();
      cntModifiedPersons += u.getModifiedPersons();
    }
    if (cntUnits > 0) {
      s.append(cntUnits).append(" units with ").append(cntPersons).append("(").append(
          cntModifiedPersons).append(") individuals.");
    } else {
      s = new StringBuilder("no units");
    }

    StringSelection strSel = new StringSelection(s.toString());
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);

    unit = null;
    selectedUnits.clear();
  }

  private void planRoute(ActionEvent e) {
    if ((new UnitRoutePlanner()).planUnitRoute(unit, data, this, selectedUnits, new RoutingDialog(
        JOptionPane.getFrameForComponent(this), data, false))) {
      if (selectedUnits != null) {
        for (Unit u : selectedUnits) {
          if (!u.equals(unit)) {
            dispatcher.fire(new UnitOrdersEvent(this, u));
          }
        }
      }

      dispatcher.fire(new UnitOrdersEvent(this, unit));
    }

    unit = null;
    selectedUnits.clear();
  }

  private void planShipRoute() {
    Unit unit =
        (new ShipRoutePlanner()).planShipRoute(this.unit.getShip(), data, this, new RoutingDialog(
            JOptionPane.getFrameForComponent(this), data, false));

    if (unit != null) {
      dispatcher.fire(new UnitOrdersEvent(this, unit));
    }
  }

  /**
   * Checks whether the selectedUnits contain at least one Unit-object, that belongs to a privileged
   * faction.
   */
  private boolean containsPrivilegedUnit() {
    for (Unit u : selectedUnits) {
      if (magellan.library.utils.Units.isPrivilegedAndNoSpy(u))
        return true;
    }

    return false;
  }

  private boolean isEditAll() {
    return settings.getProperty(PropertiesHelper.ORDEREDITOR_EDITALLFACTIONS,
        Boolean.FALSE.toString()).equals("true");
  }

  private GameSpecificStuff getGameSpecifiStuff() {
    return gameSpecStuff;
  }

  private class RemoveUnitFromTeachersListAction implements ActionListener {
    private Unit student;

    private Unit teacher;

    private GameData gameData;

    /**
     * Creates a new RemoveUnitFromTeachersListAction object, which shall remove the student ID from
     * the teacher's TEACHING orders.
     *
     * @param student The affected student Unit
     * @param teacher The affected teacher Unit
     */
    public RemoveUnitFromTeachersListAction(Unit student, Unit teacher, GameData data) {
      this.student = student;
      this.teacher = teacher;
      gameData = data;
    }

    /**
     * Removes student's ID from teacher's teaching orders.
     *
     * @param e
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      String id = student.getID().toString();
      Orders orders = teacher.getOrders2();
      int i = 0;

      // look for teaching orders
      for (Order order : orders) {
        if (order.getProblem() != null || order.isEmpty()) {
          continue;
        }

        if (orders.isToken(order, 0, EresseaConstants.OC_TEACH)) {
          String newTeachOrder = pruneOrder(order, id);
          teacher.removeOrderAt(i);
          if (newTeachOrder != null) {
            teacher.addOrderAt(i, newTeachOrder, true);
          }
          // we wouldn't need this, but we get a
          // ConcurrentModificationException
          // without it
          break;
        }
        i++;
      }
      dispatcher.fire(new UnitOrdersEvent(this, teacher));
      dispatcher.fire(new UnitOrdersEvent(this, student));
    }

    private String pruneOrder(Order order, String id) {
      StringBuilder result = new StringBuilder();
      for (OrderToken token : order.getTokens())
        if (!token.getText().equals(id)) {
          if (result.length() > 0) {
            result.append(" ");
          }
          result.append(token.getText());
        }
      return result.toString();
    }
  }
}
