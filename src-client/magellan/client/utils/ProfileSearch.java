// class magellan.client.utils.ProfileSearch
// created on Aug 4, 2022
//
// Copyright 2003-2022 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.client.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

import magellan.client.Client;
import magellan.client.swing.InternationalizedDialog;
import magellan.client.swing.basics.SpringUtilities;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Responsible for searching profiles in the file system.
 *
 * @author stm
 * @version 1.0, Aug 5, 2022
 */
public class ProfileSearch extends InternationalizedDialog {
  private static Logger log = Logger.getInstance(ProfileDialog.class);

  protected enum SearchState {
    Searching, Searched, Imported, Interrupted
  }

  private static class PathInfo {
    static DateFormat year = DateFormat.getDateInstance(); // new SimpleDateFormat("yyyy-MM-DD");
    static DateFormat day = DateFormat.getTimeInstance(); // new SimpleDateFormat("");

    private Path path;
    private String time;
    private boolean imported;
    private boolean failed;
    private int p;

    public PathInfo(Path p) {
      path = p;
    }

    @Override
    public String toString() {

      return path.getParent().toString();
    }

    public String getDetails() {
      if (time == null) {
        try {
          FileTime date = Files.getLastModifiedTime(path);
          if (date.toInstant().isAfter(Instant.now().minus(Duration.ofDays(1)))) {
            time = day.format(date.toMillis());
          } else {
            time = year.format(date.toMillis());
          }
        } catch (Exception e) {
          time = "???";
        }
        try {
          Collection<String> ps = ProfileManager.getProfiles(path.getParent());
          p = ps == null ? 0 : ps.size();
        } catch (Exception e) {
          log.fine(e);
          p = 0;
        }
      }
      String extra = "";
      if (imported) {
        extra = Resources.get("profiledialog.search.pathdetails.imported");
      }
      if (failed) {
        extra = Resources.get("profiledialog.search.pathdetails.failed");
      }
      return Resources.get("profiledialog.search.pathdetails", time, p, extra);
    }

    public Path getPath() {
      return path;
    }

    public void setImported() {
      imported = true;
    }

    public void setFailed() {
      failed = true;
    }

  }

  protected static class PathCellRenderer extends JLabel implements ListCellRenderer<PathInfo> {
    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public Component getListCellRendererComponent(JList<? extends PathInfo> list, PathInfo value, int index,
        boolean isSelected, boolean cellHasFocus) {
      Component r = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      // JLabel cell = new JLabel(value.toString());
      Color color = null;
      if (value.failed) {
        color = Color.RED;
      } else if (value.imported) {
        color = Color.GREEN;
      }
      if (color != null) {
        if (isSelected) {
          color = color.darker();
        }
        r.setBackground(color);
      }
      if (r instanceof JComponent) {
        ((JComponent) r).setToolTipText(value.getDetails());
        ((JLabel) r).setText(
            "<html><div style='font-weight:bold'>" + value.toString()
                + "</div>  <div style='font-size:x-small;font-style:italic;font-weight:lighter'>"
                + value.getDetails()
                + "</div></html>");
      }
      return r;
    }
  }

  private static class ProfileSearchWorker extends SwingWorker<Collection<Path>, Path> {

    private Collection<Path> searchRoots;
    private java.util.function.Consumer<String> status;
    private java.util.function.Consumer<Path> results;
    private Consumer<SearchState> finish;

    public ProfileSearchWorker(Collection<Path> searchRoots, Consumer<String> status, Consumer<Path> results,
        Consumer<SearchState> finish) {
      this.searchRoots = searchRoots;
      this.status = status;
      this.results = results;
      this.finish = finish;
    }

    @Override
    protected Collection<Path> doInBackground() throws Exception {
      Set<Path> pCandidates = new HashSet<Path>();
      List<Path> sCandidates = new ArrayList<Path>();

      searchRoots.stream()
          .forEachOrdered(top -> {
            try {
              log.fine("walking " + top);
              Files.walkFileTree(top, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                  if (isCancelled())
                    return FileVisitResult.TERMINATE;
                  if (dir != top && searchRoots.contains(dir)) {
                    log.fine("already searched " + dir);
                    return FileVisitResult.SKIP_SUBTREE;
                  }
                  status.accept(dir.toString());
                  return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                  if (isCancelled())
                    return FileVisitResult.TERMINATE;
                  if (file.getFileName().toString().equals(ProfileManager.INIFILE)) {
                    if (ProfileManager.getProfiles(file.getParent()) != null) {
                      pCandidates.add(file);
                      publish(file);
                    }
                  } else if (file.getFileName().toString().equals(Client.SETTINGS_FILENAME)) {
                    sCandidates.add(file);
                  }
                  return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)
                    throws IOException {
                  log.fine("could not visit " + file + " " + exc.getClass() + " " + exc.getMessage());
                  return FileVisitResult.CONTINUE;
                }

              });
            } catch (Exception e) {
              log.fine("error on " + top);
            }
          });

      Collection<Path> rest = new ArrayList<Path>();
      for (Path p : sCandidates) {
        Path parent = p.toAbsolutePath().getParent().getParent().resolve(ProfileManager.INIFILE);
        if (!pCandidates.contains(parent)) {
          // paths.addElement(new PathInfo(p));
          rest.add(p);
        }
      }
      return rest;
    }

    @Override
    protected void process(List<Path> chunks) {
      for (Path p : chunks) {
        results.accept(p);
      }
    }

    @Override
    protected void done() {
      log.fine("done");
      try {
        if (!isCancelled()) {
          for (Path p : get()) {
            results.accept(p);
          }
          finish.accept(SearchState.Searched);
        } else {
          finish.accept(SearchState.Interrupted);
        }
      } catch (CancellationException | InterruptedException e) {
        log.fine(e.getClass());
        finish.accept(SearchState.Interrupted);
      } catch (ExecutionException e) {
        log.fine(e.getClass() + " " + e.getCause());
        finish.accept(SearchState.Interrupted);
      }
    }

  }

  private JList<PathInfo> pathList;
  private DefaultListModel<PathInfo> paths;

  private JButton btnImport;
  private JButton btnClose;
  private JButton[] actionButtons;

  private JProgressBar status;

  private SwingWorker<Collection<Path>, Path> worker;
  private long t0 = System.currentTimeMillis();

  /**
   * Initializes the UI
   * 
   * @param owner
   */
  public ProfileSearch(Dialog owner) {
    super(owner, true);
    initGUI();
    setResizable(true);
    SwingUtils.center(this);
  }

  private void initGUI() {
    JPanel mainPanel;
    add(mainPanel = new JPanel());
    mainPanel.setLayout(new GridBagLayout());
    GridBagConstraints gc = new GridBagConstraints();

    JLabel searchLabel = new JLabel(Resources.get("profiledialog.search.label"));
    setTitle(searchLabel.getText());

    status = new JProgressBar();
    status.setEnabled(false);
    status.setStringPainted(true);
    status.setString("...");
    SwingUtils.setPreferredSize(status, 40, 2, true);

    paths = new DefaultListModel<PathInfo>();
    pathList = new JList<PathInfo>(paths);
    pathList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    pathList.setCellRenderer(new PathCellRenderer());

    btnImport = new JButton(Resources.get("profiledialog.search.import"));
    btnClose = new JButton(Resources.get("profiledialog.search.close"));

    btnImport.addActionListener(this::importProfiles);
    btnClose.addActionListener(this::cancel);

    actionButtons = new JButton[] {
        new JButton(new AbstractAction(Resources.get("profiledialog.search.action.homedir")) {
          public void actionPerformed(ActionEvent e) {
            search(Path.of(System.getProperty("user.home")));
          }
        }),
        new JButton(new AbstractAction(Resources.get("profiledialog.search.action.datadir")) {
          public void actionPerformed(ActionEvent e) {
            search(MagellanFinder.getAppDataDirectory().toPath());
          }
        }),
        new JButton(new AbstractAction(Resources.get("profiledialog.search.action.programdir")) {
          public void actionPerformed(ActionEvent e) {
            search(Path.of(".").toAbsolutePath().getParent());
          }
        }),
        new JButton(new AbstractAction(Resources.get("profiledialog.search.action.alldir")) {
          public void actionPerformed(ActionEvent e) {
            search(StreamSupport.stream(Path.of(".").getFileSystem().getRootDirectories().spliterator(), false)
                .collect(Collectors.toList()));
          }

        }),
        new JButton(new AbstractAction(Resources.get("profiledialog.search.action.selectdir")) {
          public void actionPerformed(ActionEvent e) {
            search((Collection<Path>) null);
          }

        }) };

    JPanel operations = new JPanel(new SpringLayout());
    int width = 0;
    for (JButton b : actionButtons) {
      operations.add(b);
      if (width < b.getPreferredSize().width) {
        width = b.getPreferredSize().width;
      }
    }
    for (JButton b : actionButtons) {
      Dimension d = b.getPreferredSize();
      d.width = width;
      b.setMinimumSize(d);
      b.setPreferredSize(d);
    }
    SpringUtilities.makeCompactGrid(operations, 1, actionButtons.length, 0, 0, 5, 5);

    JPanel actions = new JPanel(new SpringLayout());
    actions.add(btnImport);
    actions.add(btnClose);
    SpringUtilities.makeCompactGrid(actions, 1, 2, 0, 0, 5, 0);

    gc.gridx = 0;
    gc.anchor = GridBagConstraints.NORTHWEST;
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.weightx = 0;
    gc.insets = new Insets(2, 2, 2, 2);

    mainPanel.add(searchLabel, gc);
    mainPanel.add(operations, gc);

    gc.anchor = GridBagConstraints.NORTHWEST;
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.weightx = 1;
    mainPanel.add(status, gc);

    pathList.setVisibleRowCount(5);
    gc.fill = GridBagConstraints.BOTH;
    gc.weightx = .1;
    gc.weighty = .1;
    mainPanel.add(new JScrollPane(pathList), gc);

    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.weightx = 0;
    gc.weighty = 0;
    mainPanel.add(actions, gc);
    add(mainPanel);
    pack();

    JComponent[] cancelComponents = new JComponent[actionButtons.length + 3];
    System.arraycopy(actionButtons, 0, cancelComponents, 0, actionButtons.length);
    int c = actionButtons.length;
    cancelComponents[c++] = btnImport;
    cancelComponents[c++] = btnClose;
    cancelComponents[c++] = pathList;
    setDefaultActions(null, null, cancelComponents);
  }

  private void search(Path searchRoot) {
    search(Collections.singleton(searchRoot));
  }

  private void search(Collection<Path> searchRoots) {
    if (searchRoots == null) {
      JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.setDialogType(JFileChooser.OPEN_DIALOG);

      if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        searchRoots = Collections.singleton(fc.getSelectedFile().toPath());
      }
    }

    if (searchRoots != null) {
      paths.clear();
      worker = new ProfileSearchWorker(searchRoots, this::setStatus, this::addResult, this::setState);
      setState(SearchState.Searching);
      worker.execute();
    }
  }

  private void addResult(Path p) {
    PathInfo pi = new PathInfo(p);
    if (ProfileManager.getSettingsDirectory().toPath().equals(p.toAbsolutePath().getParent())) {
      pi.imported = true;
    }
    paths.addElement(pi);
  }

  private void setState(SearchState state) {
    status.setString(Resources.get("profiledialog.search.status." + state.name().toLowerCase()));
    switch (state) {
    case Searching:
      status.setIndeterminate(true);
      status.setEnabled(true);
      btnClose.setText(Resources.get("profiledialog.search.stop"));
      btnImport.setEnabled(false);
      for (JButton b : actionButtons) {
        b.setEnabled(false);
      }
      break;
    case Searched:
    case Interrupted:
      status.setIndeterminate(false);
      status.setValue(0);
      status.setEnabled(false);
      btnClose.setText(Resources.get("profiledialog.search.close"));
      btnImport.setEnabled(true);
      for (JButton b : actionButtons) {
        b.setEnabled(true);
      }
      break;
    case Imported:
      // btnImport.setEnabled(false);
      status.setIndeterminate(false);
      status.setEnabled(false);
      pathList.setSelectedIndex(-1);
      btnClose.setText(Resources.get("profiledialog.search.close"));
      for (JButton b : actionButtons) {
        b.setEnabled(true);
      }
      break;
    }
    pathList.repaint();
  }

  private void setStatus(String string) {
    if (System.currentTimeMillis() - t0 > 52) {
      if (string.length() > 70) {
        string = string.substring(0, 25) + " ... " + string.substring(string.length() - 35);
      }
      status.setString(string);
      t0 = System.currentTimeMillis();
    }
  }

  protected void importProfiles(ActionEvent evt) {
    List<String> imported = new ArrayList<String>();
    List<PathInfo> failed = new ArrayList<PathInfo>();
    for (PathInfo p : pathList.getSelectedValuesList()) {
      try {
        imported.addAll(ProfileManager.importProfilesFromDir(p.getPath().getParent()));
        p.setImported();
      } catch (Throwable e) {
        log.info(e);
        failed.add(p);
        p.setFailed();
      }
    }

    if (failed.size() > 0) {
      JOptionPane.showMessageDialog(this, Resources.get("profiledialog.search.message.imported.failed",
          imported.size(), failed.size()));
    } else {
      JOptionPane.showMessageDialog(this, Resources.get("profiledialog.search.message.imported", imported.size()));
    }
    setState(SearchState.Imported);
  }

  @Override
  protected void quit() {
    setVisible(false);
    super.quit();
  }

  protected void cancel(ActionEvent evt) {
    if (worker != null && !worker.isDone()) {
      worker.cancel(true);
      worker = null;
      setState(SearchState.Interrupted);
    } else {
      quit();
    }
  }

  @Override
  public void setVisible(boolean b) {
    if (!b) {
      if (worker != null) {
        worker.cancel(true);
      }
    }
    super.setVisible(b);
  }

}
