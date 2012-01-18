package magellan.library.merge;

import magellan.library.GameData;

public abstract class BaseMerger {
  protected final GameData olderGD, newerGD, resultGD;

  protected BaseMerger(GameData olderGD, GameData newerGD, GameData resultGD) {
    this.resultGD = resultGD;
    this.newerGD = newerGD;
    this.olderGD = olderGD;
  }
}
