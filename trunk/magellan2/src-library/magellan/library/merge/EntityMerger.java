package magellan.library.merge;

import static magellan.library.merge.PropertyMerger.mergeBeans;

import java.util.Map;

import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Identifiable;

public abstract class EntityMerger<T extends Identifiable, I extends ID> extends BaseMerger implements TwoPassMerger {
  public EntityMerger(GameData olderGD, GameData newerGD, GameData resultGD) {
    super(olderGD, newerGD, resultGD);
  }

  @SuppressWarnings("unchecked")
  protected void mergeEntityViews(Map<I, T> sourceView, Map<I, T> resultView) {
    if (sourceView != null) {
      for (T sourceEntity : sourceView.values()) {
        I id = (I) sourceEntity.getID();
        T targetEntity = resultView.get(id);
        if (targetEntity == null) {
          targetEntity = createEntity(id, resultGD);
          resultView.put(id, targetEntity);
        }
        mergeEntity(sourceEntity, targetEntity, mergeBeans(sourceEntity, targetEntity));
      }
    }
  }

  protected abstract T createEntity(I id, GameData gameData);

  protected abstract void mergeEntity(T source, T target, PropertyMerger<T> propertyMerger);
}
