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
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import magellan.client.utils.SwingUtils;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.utils.Resources;
import magellan.library.utils.RoutePlanner;
import magellan.library.utils.guiwrapper.RoutingDialogData;
import magellan.library.utils.guiwrapper.RoutingDialogDataPicker;

/**
 * A dialog to fetch a destination region (usually for a ship). Out of the knowledge of this
 * destination region magellan can create respective orders that move the ship to that region. It is
 * also possible, to determine, if ship range shall be considered, whether Vorlage-meta-orders shall
 * be created if the destination region can not be reached in one week and if the captain's orders
 * shall be replaced.
 * 
 * @author Ulrich Küster
 */
public class RoutingDialog extends InternationalizedDialog implements RoutingDialogDataPicker {
  private JButton ok;
  private JButton cancel;
  private JCheckBox createRoute;
  private JCheckBox createSingleTrip;
  private JCheckBox createStop;
  private JCheckBox considerShipRange;
  private JCheckBox createVorlageOrders;
  private JCheckBox replaceOrdersBox;

  private JComboBox<Region> regions;
  private JTextField regionName;
  private JTextField xCor;
  private JTextField yCor;

  /**
   * Creates a new RoutingDialog object.
   */
  public RoutingDialog(Frame owner, GameData data, boolean initializeRegions) {
    this(owner, data, data.getRegions(), true, initializeRegions);
  }

  /**
   * Creates a new RoutingDialog object.
   */
  public RoutingDialog(Frame owner, GameData data, Collection<Region> destRegions) {
    this(owner, data, destRegions, true, true);
  }

  /**
   * Creates a new RoutingDialog object.
   * 
   * @param owner
   * @param data
   * @param destRegions
   * @param excludeUnnamed
   * @param initializeRegions If this is false, {@link #initialize(GameData, Collection, boolean)}
   *          <em>must</em> be called manually!
   */
  public RoutingDialog(Frame owner, GameData data, Collection<Region> destRegions,
      boolean excludeUnnamed, boolean initializeRegions) {
    super(owner, true);
    setTitle(Resources.get("routingdialog.window.title"));

    if (initializeRegions) {
      initialize(data, destRegions, excludeUnnamed);
    }
  }

  public void initialize(GameData data, Collection<Region> destRegions, boolean excludeUnnamed) {
    Container cp = getContentPane();
    cp.setLayout(new GridBagLayout());

    GridBagConstraints c =
        new GridBagConstraints(0, 0, 4, 1, 0, 0.2, GridBagConstraints.CENTER,
            GridBagConstraints.BOTH, new Insets(1, 3, 1, 3), 0, 0);

    JPanel destSelect = new JPanel();
    destSelect.setLayout(new GridBagLayout());
    destSelect.setBorder(BorderFactory.createTitledBorder(Resources
        .get("routingdialog.window.message"))); // BorderFactory.createBevelBorder(BevelBorder.LOWERED));

    // destSelect.add(new JLabel(Resources.get("routingdialog.window.message")), c);
    c.gridwidth = 1;
    destSelect.add(new JLabel(Resources.get("routingdialog.xcoor")), c);

    ++c.gridx;
    c.weightx = 0.2;
    xCor = new JTextField(3);
    // xCor.setPreferredSize(new Dimension(40, 20));
    destSelect.add(xCor, c);

    ++c.gridx;
    c.weightx = 0;
    destSelect.add(new JLabel(Resources.get("routingdialog.ycoor")), c);

    ++c.gridx;
    c.weightx = 0.2;
    yCor = new JTextField(3);
    // yCor.setPreferredSize(new Dimension(40, 20));
    destSelect.add(yCor, c);

    ++c.gridy;
    c.gridx = 0;
    c.gridwidth = 4;
    destSelect.add(new JSeparator(SwingConstants.HORIZONTAL), c);

    if (destRegions != null) {
      ++c.gridy;
      c.gridx = 0;
      c.gridwidth = 1;
      c.weightx = 0;

      destSelect.add(new JLabel(Resources.get("routingdialog.regionname")), c);

      regionName = new JTextField();
      ++c.gridx;
      c.weightx = 0.2;
      c.gridwidth = 3;
      final Vector<Region> regionList = new Vector<Region>();
      destSelect.add(regionName, c);
      regionName.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          int i =
              Collections
                  .binarySearch(regionList, regionName.getText(), new RegionNameComparator());

          if (i < 0) {
            i = -i - 1;
          }

          regions.setSelectedIndex(i);
        }

        public void removeUpdate(DocumentEvent e) {
          int i =
              Collections
                  .binarySearch(regionList, regionName.getText(), new RegionNameComparator());

          if (i < 0) {
            i = -i - 1;
          }

          regions.setSelectedIndex(i);
        }

        public void changedUpdate(DocumentEvent e) {
          int i =
              Collections
                  .binarySearch(regionList, regionName.getText(), new RegionNameComparator());

          if (i < 0) {
            i = -i - 1;
          }

          regions.setSelectedIndex(i);
        }
      });

      for (Region r : destRegions) {
        if (!excludeUnnamed || ((r.getName() != null) && (!"".equals(r.getName())))) {
          regionList.add(r);
        }
      }

      Collections.sort(regionList, new RegionNameComparator());
      regions = new JComboBox<Region>(regionList);
      Dimension dim = SwingUtils.getDimension(20, 2, false);
      dim.height = regions.getPreferredSize().height;
      regions.setPreferredSize(dim);
      regions.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Region r = (Region) regions.getSelectedItem();
          CoordinateID co = r.getCoordinate();
          xCor.setText(co.getX() + "");
          yCor.setText(co.getY() + "");
        }
      });
      c.gridx = 0;
      c.gridwidth = 4;
      ++c.gridy;
      destSelect.add(regions, c);
    }

    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    cp.add(destSelect, c);

    createSingleTrip =
        new JCheckBox(Resources.get("routingdialog.radiobtn.createsingletrip.title"));
    createRoute = new JCheckBox(Resources.get("routingdialog.radiobtn.createroute.title"));

    createRoute.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        createVorlageOrders.setEnabled(!createRoute.isSelected());
        createStop.setEnabled(createRoute.isSelected());
      }
    });

    createStop = new JCheckBox(Resources.get("routingdialog.radiobtn.createstop.title"));

    createSingleTrip.setSelected(true);
    createRoute.setSelected(true);

    c.gridy++;
    cp.add(createRoute, c);

    c.gridy++;
    cp.add(createStop, c);

    c.gridy++;
    cp.add(createSingleTrip, c);

    // c.insets.left = 30;
    c.gridy++;

    considerShipRange = new JCheckBox(Resources.get("routingdialog.chkbox.considerrange.title"));
    considerShipRange.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (considerShipRange.isSelected()) {
          createVorlageOrders.setEnabled(true);
        } else {
          createVorlageOrders.setEnabled(false);
        }
      }
    });
    cp.add(considerShipRange, c);

    c.gridy++;
    createVorlageOrders =
        new JCheckBox(Resources.get("routingdialog.chkbox.createvorlageorders.title"));
    cp.add(createVorlageOrders, c);

    c.gridy++;
    c.insets.left = 3;
    replaceOrdersBox = new JCheckBox(Resources.get("routingdialog.chkbox.replaceorders.title"));
    cp.add(replaceOrdersBox, c);

    c.gridy++;
    c.gridwidth = 1;
    ok = new JButton(Resources.get("routingdialog.okbutton.text"));
    ok.setMnemonic(Resources.get("routingdialog.okbutton.mnemonic").charAt(0));
    cp.add(ok, c);

    c.gridx = 1;
    cancel = new JButton(Resources.get("routingdialog.cancelbutton.text"));
    cancel.setMnemonic(Resources.get("routingdialog.cancelbutton.mnemonic").charAt(0));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit();
      }
    });
    cp.add(cancel, c);
  }

  /**
   * Shows the dialog and returns the above explained values
   * 
   * @return A RetValue or <code>null</code> if no destination has been selected
   */
  public RetValue showRoutingDialog() {
    final RetValue retVal = new RetValue(null, 0, false, false, false);
    ActionListener okButtonAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int x = 0;
        int y = 0;

        try {
          x = Integer.parseInt(xCor.getText());
          y = Integer.parseInt(yCor.getText());
          retVal.dest = CoordinateID.create(x, y);

          retVal.mode |= createRoute.isSelected() ? RoutePlanner.MODE_CONTINUOUS : 0;
          retVal.mode |= createSingleTrip.isSelected() ? 0 : RoutePlanner.MODE_RETURN;
          retVal.mode |= createStop.isSelected() ? RoutePlanner.MODE_STOP : 0;

          retVal.useRange = considerShipRange.isSelected();
          retVal.useVorlage = createVorlageOrders.isSelected();
          retVal.replaceOrders = replaceOrdersBox.isSelected();
          quit();
        } catch (NumberFormatException exc) {
          // do not accept;
        }

      }
    };

    ok.addActionListener(okButtonAction);
    pack();
    setLocationRelativeTo(getOwner());
    setVisible(true);

    if (retVal.dest == null)
      return null;
    else
      return retVal;
  }

  /**
   * Just to order regions by their names.
   */
  private static class RegionNameComparator implements Comparator<Object> {
    /**
     * Compares two regions by name.
     */
    public int compare(Object o1, Object o2) {
      String n1 = "";
      String n2 = "";

      if (o1 instanceof Region) {
        Region r1 = (Region) o1;
        n1 = r1.getName();

        if (n1 == null) {
          n1 = r1.getCoordinate().toString();
        }
      } else {
        n1 = o1.toString();
      }

      if (o2 instanceof Region) {
        Region r2 = (Region) o2;
        n2 = r2.getName();

        if (n2 == null) {
          n2 = r2.getCoordinate().toString();
        }
      } else {
        n2 = o2.toString();
      }

      return n1.compareToIgnoreCase(n2);
    }
  }

  /**
   * Represents the result of the dialog. This is basically a tuple consisting of <br/>
   * <code>dest</code> - the destination coordinate<br/>
   * <code>makeRoute</code> - whether to construct a route rather than a simple path<br/>
   * <code>useRange</code> - whether to consider the ship range<br/>
   * <code>useVorlage</code> - whether to create Vorlage orders<br/>
   * <code>replaceOrders</code> - whether to replace the unit's orders
   * 
   * @author $Author: $
   * @version $Revision: 389 $
   */
  public static class RetValue implements RoutingDialogData {
    /** The coordinates of the destination */
    public CoordinateID dest;

    /** whether to consider the ship's range */
    public boolean useRange;

    /** whether to create Vorlage orders */
    public boolean useVorlage;

    /** whether to replace the unit's orders */
    public boolean replaceOrders;

    /** The route mode, a combination of {@link RoutePlanner}.MODE_... constants */
    public int mode;

    /**
     * Creates a new RetValue object.
     * 
     * @param d The destination
     * @param mode The mode, a combination of {@link RoutePlanner}.MODE_... constants
     * @param range whether to consider the ship's range
     * @param vorlage whether to create Vorlage orders
     * @param replace whether to replace the unit's orders
     */
    public RetValue(CoordinateID d, int mode, boolean range, boolean vorlage, boolean replace) {
      dest = d;
      this.mode = mode;
      useRange = range;
      useVorlage = vorlage;
      replaceOrders = replace;
    }

    public CoordinateID getDestination() {
      return dest;
    }

    public int getMode() {
      return mode;
    }

    public boolean useRange() {
      return useRange;
    }

    public boolean replaceOrders() {
      return replaceOrders;
    }

    public boolean useVorlage() {
      return useVorlage;
    }

  }
}
