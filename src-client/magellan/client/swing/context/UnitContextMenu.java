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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import magellan.client.EMapDetailsPanel;
import magellan.client.event.EventDispatcher;
import magellan.client.event.OrderConfirmEvent;
import magellan.client.event.SelectionEvent;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.extern.MagellanPlugIn;
import magellan.client.swing.GiveOrderDialog;
import magellan.client.swing.RemoveOrderDialog;
import magellan.client.swing.RoutingDialog;
import magellan.client.swing.context.actions.ContextAction;
import magellan.client.utils.UnitRoutePlanner;
import magellan.client.utils.Units;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaOrderParser;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;
import magellan.library.utils.ShipRoutePlanner;


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

  /**
   * The selected Units (that are a subset of the selected objects in the overview tree). Notice:
   * this.unit does not need to be element of this collection!
   */
  private Collection<Unit> selectedUnits;

  /**
   * Creates new UnitContextMenu
   * 
   * @param unit            last selected unit - is not required to be in selected objects
   * @param selectedObjects null or Collection of selected objects
   * @param dispatcher      EventDispatcher
   * @param data            the actual GameData or World
   */
  public UnitContextMenu(Unit unit, Collection<Unit> selectedObjects, EventDispatcher dispatcher, GameData data) {
    super(unit.toString());
    this.unit = unit;
    this.data = data;
    this.dispatcher = dispatcher;

    if (selectedObjects !=null && selectedObjects.contains(unit)) {
      init(selectedObjects);
    } else {
      init(Collections.singletonList(unit));
    }
  }

  /**
   * Initialize this component.
   */
  private void init(Collection<Unit> selectedObjects) {
    selectedUnits = ContextAction.filterObjects(selectedObjects, Unit.class);

    JMenuItem unitString = new JMenuItem(getCaption());
    unitString.setEnabled(false);
    add(unitString);

    if (selectedUnits.size() <= 1) {
      initSingle();
    } else {
      initMultiple();
    }

    initBoth(selectedObjects);
  }

  private String getCaption() {
    if (selectedUnits.size()==1)
      return selectedUnits.iterator().next().toString();
    else
      return selectedUnits.size() + " " + Resources.get("context.unitcontextmenu.units");
  }

  /**
   * Sets some menu entries that can be used for one or multiple selected units.
   */
  private void initBoth(Collection<Unit> selectedObjects) {
    // this part for both (but only for selectedUnits)

    if (getComponentCount() > 0) {
      addSeparator();
    }

    // change confirmation status
    if (containsPrivilegedUnit()) {
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
      JMenuItem addOrder = new JMenuItem(Resources.get("context.unitcontextmenu.menu.addorder.caption"));
      addOrder.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_addOrder();
        }
      });
      add(addOrder);

      // remove orders
      JMenuItem removeOrders = new JMenuItem(Resources.get("context.unitcontextmenu.menu.removeorder.caption"));
      removeOrders.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_removeOrders();
        }
      });
      add(removeOrders);

    }

    // select selected units in overview
    if (this.selectedUnits.size() > 0) {
      JMenuItem selectUnits = null;
      if (this.selectedUnits.size() == 1) {
        selectUnits = new JMenuItem(Resources.get("context.unitcontextmenu.setasunitselection_singular.caption"));
      } else {
        selectUnits = new JMenuItem(Resources.get("context.unitcontextmenu.setasunitselection_plural.caption"));
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
    for (String tag : tags) {
      JMenuItem removeTag = new JMenuItem(Resources.get("context.unitcontextmenu.removetag.caption") + ": " + tag);
      removeTag.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_removeTag(e);
        }
      });
      add(removeTag);
    }

    // route planning menus
    
    // test route planning capability
    boolean canPlan = UnitRoutePlanner.canPlan(unit);
    Region reg = unit.getRegion();

    if (canPlan && (selectedUnits != null)) {
      Iterator it = selectedUnits.iterator();

      while (canPlan && it.hasNext()) {
        Unit u = (Unit) it.next();
        canPlan = UnitRoutePlanner.canPlan(u);

        if ((u.getRegion() == null) || !reg.equals(u.getRegion())) {
          canPlan = false;
        }
      }
    }

    if (canPlan) {
      if (getComponentCount() > 0) {
        addSeparator();
      }

      JMenuItem planRoute = new JMenuItem(Resources.get("context.unitcontextmenu.menu.planroute"));

      planRoute.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          planRoute(e);
        }
      });
      add(planRoute);
    }

    initContextMenuProviders(selectedObjects);
  }

  private void initContextMenuProviders(Collection selectedObjects) {
    Collection<UnitContextMenuProvider> cmpList = getContextMenuProviders();
    if (!cmpList.isEmpty()) {
      addSeparator();
    }
    
    for (UnitContextMenuProvider cmp : cmpList) {
      add(cmp.createContextMenu(dispatcher, data, unit, selectedObjects));
    }

  }

  /**
   * Searchs for Context Menu Providers in the plugins and adds them to the menu.
   */
  private Collection<UnitContextMenuProvider> getContextMenuProviders() {
    Collection<UnitContextMenuProvider> cmpList = new ArrayList<UnitContextMenuProvider>();
    for (MagellanPlugIn plugIn : Client.INSTANCE.getPlugIns()) {
      if (plugIn instanceof UnitContextMenuProvider) {
        cmpList.add((UnitContextMenuProvider)plugIn);
      }
    }
    return cmpList;
  }

  private void initMultiple() {
    // this part for multiple unit-selections

    // copy IDs to clipboard
    JMenuItem copyMultipleID = new JMenuItem(Resources.get("context.unitcontextmenu.menu.copyids.caption"));
    copyMultipleID.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_copyMultipleID(e);
      }
    });
    add(copyMultipleID);

    // copy IDs+names to clipboard
    JMenuItem copyMultipleNameID = new JMenuItem(Resources.get("context.unitcontextmenu.menu.copyidsandnames.caption"));
    copyMultipleNameID.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_copyMultipleNameID(e);
      }
    });
    add(copyMultipleNameID);
    
    // copy IDs+names+numPersons to clipboard
    JMenuItem copyMultipleNameIDPersonCount = new JMenuItem(Resources.get("context.unitcontextmenu.menu.copyidsandnamesandcounts.caption"));
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
    JMenuItem copyUnitID = new JMenuItem(Resources.get("context.unitcontextmenu.menu.copyid.caption"));
    copyUnitID.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_copyID(e);
      }
    });
    add(copyUnitID);

    // copy ID+name to clipboard
    JMenuItem copyUnitNameID = new JMenuItem(Resources.get("context.unitcontextmenu.menu.copyidandname.caption"));
    copyUnitNameID.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        event_copyNameID(e);
      }
    });
    add(copyUnitNameID);
    
    // add ID+name+numPersons to clipboard
    JMenuItem copyUnitNameIDPersonCount = new JMenuItem(Resources.get("context.unitcontextmenu.menu.copyidandnameandcount.caption"));
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
    if (EMapDetailsPanel.isPrivilegedAndNoSpy(unit)) {
      JMenuItem hideID = new JMenuItem(Resources.get("context.unitcontextmenu.menu.disguise.caption"));

      hideID.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          event_hideID(e);
        }
      });
      add(hideID);
    }

    // remove unit from list of a teacher
    // is student of someone?
    for (Unit teacher : unit.getTeachers()){
      JMenuItem removeFromTeachersList = new JMenuItem(
          Resources.get("context.unitcontextmenu.menu.removeFromTeachersList") + ": " + teacher.toString());
      add(removeFromTeachersList);
      removeFromTeachersList.addActionListener(new RemoveUnitFromTeachersListAction(unit,
          teacher, this.data));
    }
    

    // plan ship route
    if ((unit.getShip() != null) && unit.equals(unit.getShip().getOwnerUnit())) {
      JMenuItem planShipRoute = new JMenuItem(Resources.get("context.unitcontextmenu.menu.planshiproute.caption"));
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
   * Sets the selected Units as selected Units in Overview FeatureRequest
   * 
   * @author Fiete
   */
  private void event_selectUnits() {
    if (this.selectedUnits.size() > 1) {
      dispatcher.fire(new SelectionEvent<Unit>(this, this.selectedUnits, null));
    }
    if (this.selectedUnits.size() == 1) {
      dispatcher.fire(new SelectionEvent<Unit>(this, this.selectedUnits, (Unit) this.selectedUnits
          .toArray()[0]));
    }
  }

  /**
   * Gives an order (optional replacing the existing ones) to the selected units.
   */
  private void event_addOrder() {
    GiveOrderDialog giveOderDialog = new GiveOrderDialog(JOptionPane.getFrameForComponent(this), getCaption());
    String s[] = giveOderDialog.showGiveOrderDialog();
    if (s[0] != null) {
      for (Unit u : selectedUnits) {

        if (EMapDetailsPanel.isPrivilegedAndNoSpy(u)) {
          Units.addOrders(u, s);
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
    RemoveOrderDialog removeOderDialog = new RemoveOrderDialog(JOptionPane.getFrameForComponent(this), getCaption());
    String s[] = removeOderDialog.showDialog();
    if (s[0] != null) {
      for (Unit u : selectedUnits) {

        if (EMapDetailsPanel.isPrivilegedAndNoSpy(u)) {
          Units.removeOrders(u, s);
          dispatcher.fire(new UnitOrdersEvent(this, u));
        }
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
    Collection<String> values = new HashSet<String>();
    Collection<Unit> keyUnits = new HashSet<Unit>();

    {
      Collection<Region> regions = new HashSet<Region>();
      for (Unit unit : selectedUnits) {
        regions.add(unit.getRegion());
      }
      for (Region r : regions) {
        for (Unit u : r.units()) {
          keyUnits.add(u);
          keys.addAll(u.getTagMap().keySet());
          values.addAll(u.getTagMap().values());
        }
      }
    }

    List<String> sortedKeys = new ArrayList<String>(keys);
    Collections.sort(sortedKeys);
    key = showInputDialog(Resources.get("context.unitcontextmenu.addtag.tagname.message"), sortedKeys);

    if ((key != null) && (key.length() > 0)) {
      String value = null;

      Collection<String> keyValues = new HashSet<String>();
      for (Unit u : keyUnits) {
        String v = u.getTag(key);
        if (v != null) {
          keyValues.add(v);
        }
      }

      List<String> sortedKeyValues = new ArrayList<String>(keyValues);
      Collections.sort(sortedKeyValues);

      // values.removeAll(keyValues);
      // List sortedValues = CollectionFactory.createArrayList(values);
      // Collections.sort(sortedValues);
      // sortedKeyValues.addAll(sortedValues);

      value = showInputDialog(Resources.get("context.unitcontextmenu.addtag.tagvalue.message"), sortedKeyValues);

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

  private String showInputDialog(String message, List values) {
    if (1 == 2) {
      return JOptionPane.showInputDialog(message);
    } else {

      // the combo box (add/modify items if you like to)
      JComboBox comboBox = new JComboBox(values.toArray());
      // has to be editable
      comboBox.setEditable(true);
      comboBox.getEditor().selectAll();
      // change the editor's document
      // new JComboBoxCompletion(comboBox,true);

      // create and show a window containing the combo box
      Frame parent = dispatcher.getMagellanContext().getClient();
      JDialog frame = new JDialog(parent, message, true);
      frame.setLocationRelativeTo(parent);
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

      comboBox.getEditor().getEditorComponent().addKeyListener(
          new MyKeyAdapter(frame, comboBox));

      frame.setVisible(true);
      frame.dispose();

      return (String) comboBox.getSelectedItem();
    }
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
          // TODO: Coalesce unitordersevent
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
    if (unit.getModifiedPersons() != unit.getPersons()){
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

    for (Iterator iter = selectedUnits.iterator(); iter.hasNext();) {
      Unit u = (Unit) iter.next();

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
    String s = "";

    for (Unit u : selectedUnits) {
      s += (u.toString() + "\n");
    }

    StringSelection strSel = new StringSelection(s);
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);

    unit = null;
    selectedUnits.clear();
  }

  private void event_copyMultipleNameIDPersonCount(ActionEvent e) {
    String s = "";
    int cntUnits=0;
    int cntPersons = 0;
    int cntModifiedPersons=0;
    for (Unit u : selectedUnits) {
      s += (u.toString() + ":" + u.getPersons());
      if (u.getModifiedPersons()!=u.getPersons()){
        s+="(" + u.getModifiedPersons() + ")";
      }
      s += "\n";
      cntUnits++;
      cntPersons+=u.getPersons();
      cntModifiedPersons+=u.getModifiedPersons();
    }
    if (cntUnits>0){
      s+=cntUnits + " units with " + cntPersons + "(" + cntModifiedPersons + ") individuals.";
    } else {
      s="no units";
    }
    
    
    StringSelection strSel = new StringSelection(s);
    Clipboard cb = getToolkit().getSystemClipboard();
    cb.setContents(strSel, null);

    unit = null;
    selectedUnits.clear();
  }
  
  private void planRoute(ActionEvent e) {
    if (UnitRoutePlanner.planUnitRoute(unit, data, this, selectedUnits)) {
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
    Unit unit = ShipRoutePlanner.planShipRoute(this.unit.getShip(), data, this, new RoutingDialog(JOptionPane.getFrameForComponent(this),data,false));

    if (unit != null) {
      dispatcher.fire(new UnitOrdersEvent(this, unit));
    }
  }

  /**
   * Checks whether the selectedUnits contain at least one Unit-object, that belongs to a
   * privileged faction.
   * 
   * 
   */
  private boolean containsPrivilegedUnit() {
    for (Unit u : selectedUnits) {
      if (EMapDetailsPanel.isPrivileged(u.getFaction())) {
        return true;
      }
    }

    return false;
  }

  private class RemoveUnitFromTeachersListAction implements ActionListener {
    private Unit student;

    private Unit teacher;

    private GameData gameData;

    /**
     * Creates a new RemoveUnitFromTeachersListAction object, which shall remove the student ID
     * from the teacher's TEACHING orders.
     * 
     * @param student
     *            The affected student Unit
     * 
     * @param teacher
     *            The affected teacher Unit
     * 
     */
    public RemoveUnitFromTeachersListAction(Unit student, Unit teacher, GameData data) {
      this.student = student;
      this.teacher = teacher;
      this.gameData = data;
    }

    /**
     * Removes student's ID from teacher's teaching orders.
     * 
     * 
     * @param e
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      String id = student.getID().toString();
      Collection<String> orders = teacher.getOrders();
      int i = 0;
      String order = null;

      // look for teaching orders
      for (Iterator<String> iter = orders.iterator(); iter.hasNext(); i++) {
        order = iter.next();
        EresseaOrderParser parser = new EresseaOrderParser(gameData);
        if (!parser.read(new StringReader(order))) {
          continue;
        }

        List tokens = parser.getTokens();
        if (((OrderToken) tokens.get(0)).equalsToken(Resources.getOrderTranslation(EresseaConstants.O_TEACH))) {
          if (order.indexOf(id) > -1) {
            teacher.removeOrderAt(i, false);
            // FIXME The meaning of tokens.size() is undefined
            if (tokens.size() > 3) { // teacher teaches more than one unit
              // remove unit's ID from order
              String newOrder = order.substring(0, order.indexOf(id))
                  + order.substring(java.lang.Math.min(order.indexOf(id) + 1
                      + id.length(), order.length()), order.length());
              teacher.addOrderAt(i, newOrder);
            }
            // we wouldn't need this, but we get a ConcurrentModificationException
            // without it
            break;
          }
        }

      }
      dispatcher.fire(new UnitOrdersEvent(this, teacher));
      dispatcher.fire(new UnitOrdersEvent(this, student));
      unit.getRegion().refreshUnitRelations(true);
//      dispatcher.fire(new GameDataEvent(this, gameData));
    }
  }
}
