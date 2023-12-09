package magellan.client.swing.map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import magellan.client.utils.SwingUtils;
import magellan.library.utils.Resources;
import magellan.library.utils.replacers.ReplacerFactory;

/**
 * A simple info dialog consisting of a list on the left side, where all currently registered
 * Replacer are shown, and a text area on the right that displays the description of the selected
 * replacer.
 */
class ToolTipReplacersInfo extends JDialog implements javax.swing.event.ListSelectionListener,
    ActionListener {
  private static Map<Component, ToolTipReplacersInfo> instances =
      new HashMap<Component, ToolTipReplacersInfo>();
  protected JList<String> list;
  protected JTextArea text;
  protected List<String> rList;
  protected ReplacerFactory replacerMap;
  private String info;

  /**
   * Creates a new ToolTipReplacersInfo object without owner.
   * 
   * @param title
   * @param info
   */
  public ToolTipReplacersInfo(String title, String info) {
    super();
    setTitle(title);
    setInfo(info);
    init();
  }

  /**
   * Creates a new ToolTipReplacersInfo with the specified dialog as owner
   * 
   * @param parent The non-<code>null</code> dialog owner.
   * @param title
   * @param info
   */
  public ToolTipReplacersInfo(Dialog parent, String title, String info) {
    super(parent, title);
    setInfo(info);
    init();
  }

  /**
   * Creates a new ToolTipReplacersInfo object.
   * <p>
   * NOTE: This constructor does not allow you to create an unowned JDialog. To create an unowned
   * JDialog you must use the TooltipReplacersInfo(JDialog, String) constructor with an argument of
   * null.
   * </p>
   */
  /**
   * @param parent The non-<code>null</code> dialog owner.
   * @param title
   * @param info
   */
  public ToolTipReplacersInfo(Frame parent, String title, String info) {
    super(parent, title);
    setInfo(info);
    init();
  }

  private void setInfo(String info) {
    this.info = info;
  }

  protected void init() {

    list = new JList<String>();
    list.setFixedCellWidth(150);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(this);

    text = new JTextArea(50, 25);
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);

    JScrollPane p = new JScrollPane(list);
    p.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    Container c = getContentPane();
    text.setBackground(c.getBackground());
    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, p, new JScrollPane(text));

    JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton exit = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.tooltipinfo.ok"));
    exit.addActionListener(this);
    south.add(exit);

    c.add(sp, BorderLayout.CENTER);
    c.add(south, BorderLayout.SOUTH);

    SwingUtils.setPreferredSize(this, 40, -1, true);
    pack();
    setLocationRelativeTo(getParent());
  }

  /**
   * Initializes the dialog from
   * magellan.library.utils.replacers.ReplacerHelp.getDefaultReplacerFactory() and displays it.
   */
  public void showDialog() {
    replacerMap = magellan.library.utils.replacers.ReplacerHelp.getDefaultReplacerFactory();

    if (rList == null) {
      rList = new ArrayList<String>();
    }

    rList.clear();
    rList.addAll(replacerMap.getReplacers());
    Collections.sort(rList);

    list.setListData(rList.toArray(new String[] {}));
    text.setText(info);

    super.setVisible(true);
  }

  /**
   * Hides the dialog.
   */
  public void actionPerformed(ActionEvent e) {
    setVisible(false);
  }

  /**
   * Displays the appropriate description.
   */
  public void valueChanged(javax.swing.event.ListSelectionEvent lse) {
    if (list.getSelectedIndex() >= 0) {
      String label = rList.get(list.getSelectedIndex());
      magellan.library.utils.replacers.Replacer rep = (replacerMap.createReplacer(label));
      if (rep == null) {
        text.setText("Internal error - please report.");
      } else {
        text.setText(rep.getDescription());
      }
    }
  }

  public static void showInfoDialog(Component source, String info) {
    Dialog parent = null;

    ToolTipReplacersInfo infoDialog = instances.get(source);
    if (infoDialog == null) {
      Frame frame = null;
      if (source instanceof Dialog) {
        parent = (Dialog) source;
      } else {
        if (source instanceof Frame) {
          frame = (Frame) source;
        }
        if (frame == null) {
          Window window = SwingUtilities.getWindowAncestor(source);
          if (window instanceof Frame) {
            frame = (Frame) window;
          }
          if (window instanceof Dialog) {
            parent = (Dialog) window;
          }
        }
      }
      if (parent != null) {
        infoDialog =
            new ToolTipReplacersInfo(parent, Resources
                .get("map.mapperpreferences.tooltipdialog.tooltipinfo.title"), info);
      } else if (frame != null) {
        infoDialog =
            new ToolTipReplacersInfo(frame, Resources
                .get("map.mapperpreferences.tooltipdialog.tooltipinfo.title"), info);
      } else {
        infoDialog =
            new ToolTipReplacersInfo(Resources
                .get("map.mapperpreferences.tooltipdialog.tooltipinfo.title"), info);
      }
      instances.put(source, infoDialog);
    }
    if (!infoDialog.isVisible()) {
      infoDialog.showDialog();
    }
  }
}