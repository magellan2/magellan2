package magellan.client.desktop;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenu;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.StringViewMap;

public interface DockingFrameworkBuilder {

  // internal?
  void setInActive(View view);

  void setActive(View view);

  void setActiveLayout(DockingLayout layout);

  ////

  public JMenu createDesktopMenu(Map<String, Component> components, Properties settings,
      ActionListener listener);

  RootWindow buildDesktop(Map<String, Component> components, File file);

  Collection<Component> getComponentsUsed();

  void setProperties(Properties settings);

  void updateLayoutMenu();

  StringViewMap getViewMap();

  void write(File file) throws IOException;

  void setVisible(RootWindow splitRoot, String viewName, boolean setVisible);

  void setTabVisibility(boolean b);

  void deleteCurrentLayout();

  void addLayouts(File selectedFile);

  Object getLayout(String newLayoutName);

  void createNewLayout(String newLayoutName);

}