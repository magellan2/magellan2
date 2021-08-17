import com.install4j.api.formcomponents.AbstractFormComponent;

import javax.swing.*;

/**
 * A sample form component that can be added on form screens. It displays a label and an uneditable text field.
 * Both the text of the label and the text field can be customized by the user.
 */
public class SampleFormComponent extends AbstractFormComponent {

    private String labelText;
    private String text;

    private JLabel label;
    private JTextField textField;


    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public JComponent createLeftComponent() {
        label = new JLabel(labelText);

        return label;
    }

    @Override
    public JComponent createCenterComponent() {

        textField = new JTextField();
        textField.setEditable(false);

        return textField;
    }

    @Override
    public boolean isFillCenterHorizontal() {
        // the text field uses all available horizontal space
        return true;
    }

    @Override
    public void initialize() {
        super.initialize();

        // Texts are applied to the components here rather than in the creation methods, so that variables can be processed
        // each time the screen is shown.
        label.setText(replaceVariables(labelText));
        textField.setText(replaceVariables(text));
    }
}
