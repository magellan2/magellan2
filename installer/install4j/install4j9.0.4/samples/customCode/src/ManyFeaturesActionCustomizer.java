import javax.swing.*;
import java.awt.*;
import java.beans.Customizer;

public class ManyFeaturesActionCustomizer extends JPanel implements Customizer {
    
    private ManyFeaturesAction manyFeaturesAction;
    private JButton button;

    public ManyFeaturesActionCustomizer() {
        setName("My Customizer");

        setLayout(new FlowLayout());
        button = new JButton("Show the value of some property");
        button.addActionListener(e -> JOptionPane.showMessageDialog(
            ManyFeaturesActionCustomizer.this,
            "The value of the \"Hidden property\" is \"" +  manyFeaturesAction.getHiddenProperty() + "\""
        ));
        add(button);
    }

    @Override
    public void setObject(Object bean) {
        manyFeaturesAction = (ManyFeaturesAction)bean;
    }
}
