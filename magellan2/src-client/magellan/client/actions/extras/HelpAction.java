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

package magellan.client.actions.extras;

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.JOptionPane;

import magellan.client.Client;
import magellan.client.actions.MenuAction;
import magellan.library.utils.ResourcePathClassLoader;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * FIXME This class is currenty not working because of reconstruction
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class HelpAction extends MenuAction {
  private static final Logger log = Logger.getInstance(HelpAction.class);
  private Object helpBroker = null;

  /**
   * Creates a new HelpAction object.
   */
  public HelpAction(Client client) {
    super(client);
  }

  /**
   * DOCUMENT-ME
   */
  public void menuActionPerformed(ActionEvent e) {
    // SG: had a lot of fun when I implemented this :-)
    try {
      ClassLoader loader = new ResourcePathClassLoader(client.getProperties());
      URL hsURL = loader.getResource("help/magellan.hs");

      if (hsURL == null) hsURL = loader.getResource("magellan.hs");
      if (hsURL == null) {
        JOptionPane.showMessageDialog(client, Resources.get("actions.helpaction.msg.helpsetnotfound.text"));
        return;
      }

      Class helpSetClass = null;
      Class helpBrokerClass = null;

      if (this.helpBroker == null) {
        try {
          helpSetClass = Class.forName("javax.help.HelpSet", true, ClassLoader.getSystemClassLoader());
          Class.forName("javax.help.CSH$DisplayHelpFromSource", true, ClassLoader.getSystemClassLoader());
          helpBrokerClass = Class.forName("javax.help.HelpBroker", true, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException ex) {
          JOptionPane.showMessageDialog(client, Resources.get("actions.helpaction.msg.javahelpnotfound.text"));

          return;
        }

        Class helpSetConstructorSignature[] = { Class.forName("java.lang.ClassLoader"), hsURL.getClass() };
        Constructor helpSetConstructor = helpSetClass.getConstructor(helpSetConstructorSignature);
        Object helpSetConstructorArgs[] = { loader, hsURL };

        // this calls new javax.help.Helpset(ClassLoader, URL)
        Object helpSet = helpSetConstructor.newInstance(helpSetConstructorArgs);

        Method helpSetCreateHelpBrokerMethod = helpSetClass.getMethod("createHelpBroker", (Class[])null);

        // this calls new javax.help.Helpset.createHelpBroker()
        this.helpBroker = helpSetCreateHelpBrokerMethod.invoke(helpSet, (Object[])null);

        Method initPresentationMethod = helpBrokerClass.getMethod("initPresentation", (Class[])null);
        // this calls new javax.help.HelpBroker.initPresentation()
        initPresentationMethod.invoke(this.helpBroker, (Object[])null);

      }

      Class setDisplayedMethodSignature[] = { boolean.class };
      Method setDisplayedMethod = this.helpBroker.getClass().getMethod("setDisplayed", setDisplayedMethodSignature);
      Object setDisplayedMethodArgs[] = { Boolean.TRUE };

      // this calls new javax.help.HelpBroker.setDisplayed(true)
      setDisplayedMethod.invoke(this.helpBroker, setDisplayedMethodArgs);
    } catch (Exception ex) {
      log.error(ex);
    }
  }

  /**
   * @see magellan.client.actions.MenuAction#getAcceleratorTranslated()
   */
  @Override
  protected String getAcceleratorTranslated() {
    return Resources.get("actions.helpaction.accelerator", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getMnemonicTranslated()
   */
  @Override
  protected String getMnemonicTranslated() {
    return Resources.get("actions.helpaction.mnemonic", false);
  }

  /**
   * @see magellan.client.actions.MenuAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("actions.helpaction.name");
  }

  @Override
  protected String getTooltipTranslated() {
    return Resources.get("actions.helpaction.tooltip", false);
  }

}
