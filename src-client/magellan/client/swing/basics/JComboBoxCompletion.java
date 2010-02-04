package magellan.client.swing.basics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/* 
 * from http://www.orbital-computer.de/JComboBox/
 * 
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication, visit
 * http://creativecommons.org/licenses/publicdomain/
 */
public class JComboBoxCompletion extends PlainDocument {
  JComboBox comboBox;
  ComboBoxModel model;
  JTextComponent editor;
  // flag to indicate if setSelectedItem has been called
  // subsequent calls to remove/insertString should be ignored
  boolean selecting = false;
  boolean hidePopupOnFocusLoss;
  boolean hitBackspace = false;
  boolean hitBackspaceOnSelection;

  KeyListener editorKeyListener;
  FocusListener editorFocusListener;

  private boolean mayEditUnknownItems = false;

  public JComboBoxCompletion(final JComboBox comboBox) {
    this(comboBox, false);
  }

  public JComboBoxCompletion(final JComboBox comboBox, boolean mayEditUnknownItems) {
    this.mayEditUnknownItems = mayEditUnknownItems;
    this.comboBox = comboBox;
    model = comboBox.getModel();
    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!selecting) {
          highlightCompletedText(0);
        }
      }
    });
    comboBox.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("editor")) {
          configureEditor((ComboBoxEditor) e.getNewValue());
        }
        if (e.getPropertyName().equals("model")) {
          model = (ComboBoxModel) e.getNewValue();
        }
      }
    });
    editorKeyListener = new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (comboBox.isDisplayable()) {
          comboBox.setPopupVisible(true);
        }
        hitBackspace = false;
        switch (e.getKeyCode()) {
        // determine if the pressed key is backspace (needed by the remove method)
        case KeyEvent.VK_BACK_SPACE:
          hitBackspace = true;
          hitBackspaceOnSelection = editor.getSelectionStart() != editor.getSelectionEnd();
          break;
        // ignore delete key
        case KeyEvent.VK_DELETE:
          e.consume();
          comboBox.getToolkit().beep();
          break;
        }
      }
    };
    // Bug 5100422 on Java 1.5: Editable JComboBox won't hide popup when tabbing out
    hidePopupOnFocusLoss = System.getProperty("java.version").startsWith("1.5");
    // Highlight whole text when gaining focus
    editorFocusListener = new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        highlightCompletedText(0);
      }

      @Override
      public void focusLost(FocusEvent e) {
        // Workaround for Bug 5100422 - Hide Popup on focus loss
        if (hidePopupOnFocusLoss) {
          comboBox.setPopupVisible(false);
        }
      }
    };
    configureEditor(comboBox.getEditor());
    // Handle initially selected object
    Object selected = comboBox.getSelectedItem();
    if (selected != null) {
      setText(selected.toString());
    }
    highlightCompletedText(0);
  }

  public static void enable(JComboBox comboBox) {
    // has to be editable
    comboBox.setEditable(true);
    // change the editor's document
    new JComboBoxCompletion(comboBox);
  }

  void configureEditor(ComboBoxEditor newEditor) {
    if (editor != null) {
      editor.removeKeyListener(editorKeyListener);
      editor.removeFocusListener(editorFocusListener);
    }

    if (newEditor != null) {
      editor = (JTextComponent) newEditor.getEditorComponent();
      editor.addKeyListener(editorKeyListener);
      editor.addFocusListener(editorFocusListener);
      editor.setDocument(this);
    }
  }

  @Override
  public void remove(int offs, int len) throws BadLocationException {
    // return immediately when selecting an item
    if (selecting)
      return;
    if (hitBackspace) {
      // user hit backspace => move the selection backwards
      // old item keeps being selected
      if (offs > 0) {
        if (hitBackspaceOnSelection) {
          offs--;
        }
      } else {
        // User hit backspace with the cursor positioned on the start => beep
        comboBox.getToolkit().beep(); // when available use:
        // UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
      }
      highlightCompletedText(offs);
    } else {
      super.remove(offs, len);
    }
  }

  @Override
  public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
    // return immediately when selecting an item
    if (selecting)
      return;
    // insert the string into the document
    super.insertString(offs, str, a);
    // lookup and select a matching item
    Object item = lookupItem(getText(0, getLength()));

    boolean listContainsSelectedItem = true;
    if (item == null) {
      if (mayEditUnknownItems) {
        // no item matches => use the current input as selected item
        item = getText(0, getLength());
        listContainsSelectedItem = false;
      } else {
        // keep old item selected if there is no match
        item = comboBox.getSelectedItem();
        // imitate no insert (later on offs will be incremented by str.length(): selection won't
        // move forward)
        offs = offs - str.length();
        // provide feedback to the user that his input has been received but can not be accepted
        comboBox.getToolkit().beep(); // when available use:
        // UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
      }
    }
    setSelectedItem(item);
    setText(item.toString());
    // select the completed part
    if (listContainsSelectedItem) {
      highlightCompletedText(offs + str.length());
    }
  }

  private void setText(String text) {
    try {
      // remove all text and insert the completed string
      super.remove(0, getLength());
      super.insertString(0, text, null);
    } catch (BadLocationException e) {
      throw new RuntimeException(e.toString());
    }
  }

  private void highlightCompletedText(int start) {
    editor.setCaretPosition(getLength());
    editor.moveCaretPosition(start);
  }

  private void setSelectedItem(Object item) {
    selecting = true;
    model.setSelectedItem(item);
    selecting = false;
  }

  private Object lookupItem(String pattern) {
    Object selectedItem = model.getSelectedItem();
    // only search for a different item if the currently selected does not match
    if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern))
      return selectedItem;
    else {
      // iterate over all items
      for (int i = 0, n = model.getSize(); i < n; i++) {
        Object currentItem = model.getElementAt(i);
        // current item starts with the pattern?
        if (currentItem != null && startsWithIgnoreCase(currentItem.toString(), pattern))
          return currentItem;
      }
    }
    // no item starts with the pattern => return null
    return null;
  }

  // checks if str1 starts with str2 - ignores case
  private boolean startsWithIgnoreCase(String str1, String str2) {
    return str1.toUpperCase().startsWith(str2.toUpperCase());
  }

  private static void createAndShowGUI() {
    // the combo box (add/modify items if you like to)
    final JComboBox comboBox =
        new JComboBox(new Object[] { "Ester", "Jordi", "Jordina", "Jorge", "Sergi" });
    comboBox.setEditable(true);
    // enable(comboBox);

    // create and show a window containing the combo box
    final JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(comboBox);
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JComboBoxCompletion.createAndShowGUI();
      }
    });
  }
}
