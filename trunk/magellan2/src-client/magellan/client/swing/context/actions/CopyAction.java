package magellan.client.swing.context.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import magellan.client.event.EventDispatcher;
import magellan.library.ID;
import magellan.library.utils.Resources;

public class CopyAction extends ContextAction {

  public CopyAction(ID selected, EventDispatcher dispatcher) {
    super(selected, dispatcher);
  }

  public CopyAction(ID selected, List<?> selectedObjects, EventDispatcher dispatcher) {
    super(selected, selectedObjects, dispatcher);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
  }

  /**
   * @see magellan.client.swing.context.actions.ContextAction#getNameTranslated()
   */
  @Override
  protected String getNameTranslated() {
    return Resources.get("context.action.copyaction.name");
  }

}
