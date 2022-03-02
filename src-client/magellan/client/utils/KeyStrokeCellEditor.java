package magellan.client.utils;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;

/**
 * A CellEditor component for editing key strokes in JTable cells.
 */
public class KeyStrokeCellEditor extends AbstractCellEditor implements TableCellEditor {

  private KeyTextField textField;
  private Object oldValue;
  private JTextField dummy = new JTextField();

  public KeyStrokeCellEditor() {
    textField = new KeyTextField(20);
    textField.setIgnoreEnter(true);
  }

  @Override
  public void cancelCellEditing() {
    if (oldValue instanceof KeyStroke) {
      textField.init((KeyStroke) oldValue);
    }
    super.cancelCellEditing();
  }

  // @Override
  // public boolean stopCellEditing() {
  // if (oldValue instanceof KeyStroke)
  // if (!changeStroke((KeyStroke) getCellEditorValue(), (KeyStroke) oldValue, -1)) {
  // textField.init((KeyStroke) oldValue);
  // }
  //
  // return super.stopCellEditing();
  // }

  @Override
  public boolean isCellEditable(EventObject anEvent) {
    return super.isCellEditable(anEvent);
  }

  /**
   * @see javax.swing.DefaultCellEditor#getCellEditorValue()
   */
  public Object getCellEditorValue() {
    if (oldValue instanceof KeyStroke)
      return textField.getKeyStroke();
    else
      return oldValue;
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
      int row, int column) {
    if (value instanceof KeyStroke) {
      oldValue = value;
      textField.init((KeyStroke) value);
      return textField;
    } else {
      oldValue = value;
      dummy.setEditable(false);
      dummy.setText(oldValue.toString());
      return dummy;
    }
  }

}
