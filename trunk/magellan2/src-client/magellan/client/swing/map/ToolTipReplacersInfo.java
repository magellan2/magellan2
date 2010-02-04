package magellan.client.swing.map;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import magellan.library.utils.Resources;
import magellan.library.utils.replacers.ReplacerFactory;

/**
 * A simple info dialog consisting of a list on the left side, where all currently registered
 * Replacer are shown, and a text area on the right that displays the description of the selected
 * replacer.
 */
class ToolTipReplacersInfo extends JDialog implements javax.swing.event.ListSelectionListener,
    ActionListener {
  protected JList list;
  protected JTextArea text;
  protected List<String> rList;
  protected ReplacerFactory replacerMap;

  /**
   * Creates a new ToolTipReplacersInfo object without owner.
   * 
   * @param title
   */
  public ToolTipReplacersInfo(String title) {
    super();
    setTitle(title);
    init();
  }

  /**
   * Creates a new ToolTipReplacersInfo with the specified dialog as owner
   * 
   * @param parent The non-<code>null</code> dialog owner.
   * @param title
   */
  public ToolTipReplacersInfo(Dialog parent, String title) {
    super(parent, title);
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
   */
  public ToolTipReplacersInfo(Frame parent, String title) {
    super(parent, title);
    init();
  }

  protected void init() {

    list = new JList();
    list.setFixedCellWidth(150);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(this);

    text = new JTextArea(50, 25);
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);

    Container c = getContentPane();
    c.setLayout(new BorderLayout());

    JScrollPane p = new JScrollPane(list);
    p.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    c.add(p, BorderLayout.WEST);
    text.setBackground(c.getBackground());
    c.add(new JScrollPane(text), BorderLayout.CENTER);

    JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton exit = new JButton(Resources.get("map.mapperpreferences.tooltipdialog.tooltipinfo.ok"));
    exit.addActionListener(this);
    south.add(exit);
    c.add(south, BorderLayout.SOUTH);

    this.setSize(600, 400); // because of some pack mysteries
    setLocationRelativeTo(getParent());
  }

  /**
   * Initializes the dialog from
   * magellan.library.utils.replacers.ReplacerHelp.getDefaultReplacerFactory() and displays it.
   */
  public void showDialog() {
    replacerMap = magellan.library.utils.replacers.ReplacerHelp.getDefaultReplacerFactory();

    if (rList == null) {
      rList = new LinkedList<String>();
    }

    rList.clear();
    rList.addAll(replacerMap.getReplacers());
    Collections.sort(rList);

    list.setListData(rList.toArray());

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
      magellan.library.utils.replacers.Replacer rep =
          (replacerMap.createReplacer(rList.get(list.getSelectedIndex())));

      if (rep == null) {
        text.setText("Internal error - please report.");
      } else {
        text.setText(rep.getDescription()); // Debug: +"\n\n--\n"+rep.getClass().getName());
      }
    }
  }
}