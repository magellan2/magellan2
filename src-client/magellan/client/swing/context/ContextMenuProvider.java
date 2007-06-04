package magellan.client.swing.context;

import java.util.Collection;

import javax.swing.JMenuItem;

import magellan.client.event.EventDispatcher;
import magellan.library.GameData;


public interface ContextMenuProvider {
    
    /*
     * creates a JMenuItem. Will be called on right-clicking units.
     */
    public JMenuItem createContextMenu(
            EventDispatcher dispatcher, 
            GameData data, 
            Object argument,
            Collection selectedObjects);
}
