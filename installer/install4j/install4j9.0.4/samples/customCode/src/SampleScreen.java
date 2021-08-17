import com.install4j.api.Util;
import com.install4j.api.beans.ExternalFile;
import com.install4j.api.screens.AbstractInstallerScreen;

import javax.swing.*;
import java.awt.*;
import java.io.File;


/**
 * Simple sample screen to show how to integrate custom screens into your installer.
 *
 * The property buttonTitle can be changed in the install4j IDE.
 *
 * This sample screen has no BeanInfo. The SampleAction, ManyFeaturesAction and the SampleFormComponents used
 * in this project show how to use a BeanInfo.
 */
public class SampleScreen extends AbstractInstallerScreen {

    private JButton button;

    private String buttonTitle = "Action!";

    private ExternalFile buttonImage;

    public ExternalFile getButtonImage() {
        return buttonImage;
    }

    public void setButtonImage(ExternalFile buttonImage) {
        this.buttonImage = buttonImage;
    }

    public String getButtonTitle() {
        // call replaceVariables to resolve installer variables
        return replaceVariables(buttonTitle);
    }

    public void setButtonTitle(String buttonTitle) {
        this.buttonTitle = buttonTitle;
    }

    @Override
    public JComponent createComponent() {
        JPanel panel = new JPanel(new FlowLayout());

        button = new JButton();
        File iconFile = getInstallerContext().getExternalFile(buttonImage, false);
        if (iconFile != null) {
            button.setIcon(new ImageIcon(iconFile.getAbsolutePath()));
        }
        panel.add(button);
        button.addActionListener(e -> {
            Util.showMessage("I will go back to the previous screen now.");
            getInstallerContext().goBackInHistory(1);
        });

        return panel;
    }

    @Override
    public void willActivate() {
        button.setText(getButtonTitle());
    }

    @Override
    public String getTitle() {
        return "Sample Screen";
    }

    @Override
    public String getSubTitle() {
        return "This is a sample screen";
    }

    @Override
    public boolean isFillVertical() {
        return false;
    }

    @Override
    public boolean isFillHorizontal() {
        return false;
    }
}
