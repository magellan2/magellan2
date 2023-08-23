package magellan.client;

import java.io.File;

import magellan.client.Client.Parameters;
import magellan.client.swing.StartWindow;
import magellan.client.utils.MagellanFinder;
import magellan.client.utils.ProfileManager;
import magellan.library.GameData;
import magellan.library.utils.MagellanImages;

/**
 * Helper class to get a Client. Needed for testing only!
 *
 * @author stm
 */
public class ClientProvider {
  /**
   * Creates a Client
   *
   * @param data
   * @return a new Client object
   */
  public static Client getClient(GameData data, File dir) {
    Parameters parameters = new Client.Parameters();
    parameters.binDir = MagellanFinder.findMagellanDirectory();
    parameters.resourceDir = dir;
    parameters.settingsDir = parameters.resourceDir;
    parameters.profile = "de";
    ProfileManager.init(parameters.settingsDir);
    ProfileManager.setProfile("de");
    parameters.settingsDir = ProfileManager.getProfileDirectory();
    Client.startWindow = new StartWindow(MagellanImages.ABOUT_MAGELLAN, 5, new File("."));
    return new Client(data, parameters.binDir, parameters.resourceDir, parameters.settingsDir,
        false, new File(parameters.settingsDir, "errors.txt"));
  }

  /**
   * Creates a mock client.
   */
  public static Client getClient() {
    return new Client() {
    };
  }
}
