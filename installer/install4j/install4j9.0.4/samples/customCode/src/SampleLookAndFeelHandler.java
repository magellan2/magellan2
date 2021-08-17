import com.install4j.api.laf.LookAndFeelHandler;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 * Shows how to implement a custom look and feel. In this case,
 * The Nimbus look and feel that is included with the JDK is
 * applied. The LookAndFeelEnhancer super-interface has a number
 * of methods that can be overridden to help the installer application
 * with certain aspects of the UI creation.
 * <p>
 * This class is selected on the Installer->Screens & Actions->Look & Feel step
 * in the install4j IDE.
 * </p>
 */
public class SampleLookAndFeelHandler implements LookAndFeelHandler {
    @Override
    public void applyLookAndFeel(boolean darkDesktop) throws Exception {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
    }
}
