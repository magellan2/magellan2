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

package magellan.client;

import java.util.Properties;

import magellan.client.event.EventDispatcher;
import magellan.client.utils.ImageFactory;
import magellan.client.utils.NameGenerator;
import magellan.library.GameData;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.Resources;
import magellan.library.utils.replacers.ReplacerHelp;

/**
 * This class keeps all anchors to global resources e.g. EventDispatcher, Properties...<br>
 */
public class MagellanContext implements MagellanEnvironment {
  private Properties settings;
  private Properties completionSettings;
  private EventDispatcher dispatcher;
  private GameData data;
  private Client client;

  public MagellanContext(Client client) {
    this.client = client;
  }

  public Client getClient() {
    return client;
  }

  /**
   * Returns the properties of Magellan.
   */
  public Properties getProperties() {
    return settings;
  }

  /**
   * DOCUMENT-ME
   */
  public void setProperties(Properties p) {
    settings = p;
  }

  /**
   * Returns the EventDispatcher of Magellan.
   */
  public EventDispatcher getEventDispatcher() {
    return dispatcher;
  }

  public void setEventDispatcher(EventDispatcher d) {
    dispatcher = d;
    dispatcher.setMagellanContext(this);
  }

  /**
   * Returns the current GameData.
   */
  public GameData getGameData() {
    return data;
  }

  public void setGameData(GameData d) {
    data = d;
  }

  ImageFactory imageFactory = null;

  public ImageFactory getImageFactory() {
    return imageFactory;
  }

  private ReplacerHelp replacerHelp;

  public ReplacerHelp getReplacerHelp() {
    return replacerHelp;
  }

  /**
   * Initializes global resources.
   */
  public synchronized void init() {
    // ResourcePathClassLoader.init(settings); // init resource class with new settings

    // init the translations with the loaded settings
    Resources.getInstance();

    // init the static resource paths
    Resources.initStaticPaths(settings);

    // init the idbaseconverter
    IDBaseConverter.init();

    NameGenerator.init(settings);

    // inits ImageFactory
    imageFactory = new ImageFactory(getEventDispatcher());

    // inits ReplacerHelp
    replacerHelp = new ReplacerHelp(getEventDispatcher(), getGameData());
  }

  public Properties getCompletionProperties() {
    return completionSettings;
  }

  public void setCompletionProperties(Properties completionSettings2) {
    completionSettings = completionSettings2;
  }
}
