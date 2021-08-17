import com.install4j.api.Util;
import com.install4j.api.context.UserCanceledException;
import com.install4j.api.launcher.ApplicationLauncher;
import com.install4j.api.launcher.SplashScreen;
import com.install4j.api.launcher.StartupNotification;
import com.install4j.api.launcher.Variables;
import com.install4j.api.update.ApplicationDisplayMode;
import com.install4j.api.update.UpdateChecker;
import com.install4j.api.update.UpdateDescriptor;
import com.install4j.api.update.UpdateDescriptorEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSeparatorUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class HelloGui extends JFrame {

    private JLabel helloLabel;
    private JPanel updateNoticePanel;

    private HelloGui() {

        setSize(600, 400);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - 600) / 2, (screenSize.height - 400) / 2);

        setTitle("Hello World GUI " + getVersion());

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem updateItem = new JMenuItem("Check For Update");
        updateItem.addActionListener(event -> checkForUpdate());
        menu.add(updateItem);

        JMenuItem changeNameMenu = new JMenu("Change Greeting Name");

        JMenuItem changeNameItem = new JMenuItem("With A Custom Installer Application");
        changeNameItem.addActionListener(event -> changeName(false));
        changeNameMenu.add(changeNameItem);

        JMenuItem changeNameInProcessItem = new JMenuItem("With An In-Process Custom Installer Application)");
        changeNameInProcessItem.addActionListener(event -> changeName(true));
        changeNameMenu.add(changeNameInProcessItem);

        menu.add(changeNameMenu);
        menu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(event -> System.exit(0));

        menu.add(exitItem);

        menuBar.add(menu);
        setJMenuBar(menuBar);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        helloLabel = new JLabel(getGreetingText());
        helloLabel.setFont(helloLabel.getFont().deriveFont(50f));
        helloLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Container contentPane = getContentPane();
        contentPane.add(helloLabel, BorderLayout.CENTER);


        Box box = Box.createVerticalBox();
        box.setBorder(createEmptyBorder());
        box.add(new JLabel(" * See the \"File\" menu for examples of an interactive auto-updater and a custom installer application"));
        box.add(new JLabel(" * Quit and pass \"fail\" as an argument to hello_gui to see what happens for a startup failure"));
        box.add(new JLabel(" * Start hello_gui.exe again to see how startup notification works"));
        contentPane.add(box, BorderLayout.SOUTH);

        checkForUpdateWithApi();
    }

    private String getVersion() {
        try {
            return Variables.getCompilerVariable("sys.version");
        } catch (IOException e) {
            return "";
        }
    }

    private void checkForUpdateWithApi() {
        if (isUpdatable()) {
            // Here we check for updates in the background with the API.
            new SwingWorker<UpdateDescriptorEntry, Object>() {
                @Override
                protected UpdateDescriptorEntry doInBackground() throws Exception {
                    // The compiler variable sys.updatesUrl holds the URL where the updates.xml file is hosted.
                    // That URL is defined on the "Installer->Auto Update Options" step.
                    // The same compiler variable is used by the "Check for update" actions that are contained in the update
                    // downloaders.
                    String updateUrl = Variables.getCompilerVariable("sys.updatesUrl");
                    UpdateDescriptor updateDescriptor = UpdateChecker.getUpdateDescriptor(updateUrl, ApplicationDisplayMode.GUI);
                    // If getPossibleUpdateEntry returns a non-null value, the version number in the updates.xml file
                    // is greater than the version number of the local installation.
                    return updateDescriptor.getPossibleUpdateEntry();
                }

                @Override
                protected void done() {
                    try {
                        UpdateDescriptorEntry updateDescriptorEntry = get();
                        // only installers and single bundle archives on macOS are supported for background updates
                        if (updateDescriptorEntry != null && (!updateDescriptorEntry.isArchive() || updateDescriptorEntry.isSingleBundle())) {
                            if (!updateDescriptorEntry.isDownloaded()) {
                                // An update is available for download, so we add an update notice panel at the top of the window
                                addUpdateNotice(updateDescriptorEntry, e -> {
                                    JButton button = (JButton)e.getSource();
                                    setProgressText(button, -1);
                                    button.setEnabled(false);
                                    downloadAndUpdate(button);
                                });
                            } else if (UpdateChecker.isUpdateScheduled()) {
                                // The update has been downloaded, but installation did not succeed yet.
                                // When the user clicks the button we will execute the update directly
                                addUpdateNotice(updateDescriptorEntry, e -> executeUpdate());
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        // UserCanceledException means that the user has cancelled the proxy dialog
                        if (!(cause instanceof UserCanceledException)) {
                            e.printStackTrace();
                        }
                    }
                }
            }.execute();
        }
    }

    private boolean isUpdatable() {
        try {
            Path installationDirectory = Paths.get(String.valueOf(Variables.getInstallerVariable("sys.installationDir")));
            return !Files.getFileStore(installationDirectory).isReadOnly() && (Util.isWindows() || Util.isMacOS() || (Util.isLinux() && !Util.isArchive()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addUpdateNotice(final UpdateDescriptorEntry updateDescriptorEntry, ActionListener actionListener) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        panel.add(new JLabel("Hello World GUI " + updateDescriptorEntry.getNewVersion() + " is available."));
        JButton button = new JButton("Update Now");
        button.addActionListener(actionListener);
        panel.add(button);
        panel.setOpaque(false);
        panel.setBorder(createEmptyBorder());

        updateNoticePanel = new JPanel(new BorderLayout());
        updateNoticePanel.setBackground(new Color(255, 255, 200));
        updateNoticePanel.add(panel, BorderLayout.CENTER);
        updateNoticePanel.add(createSeparator(), BorderLayout.SOUTH);

        getContentPane().add(updateNoticePanel, BorderLayout.NORTH);
        getContentPane().revalidate();
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        if (Util.isMacOS()) {
            separator.setUI(new BasicSeparatorUI());
        }
        Dimension size = separator.getPreferredSize();
        size.height = 1;
        separator.setPreferredSize(size);
        return separator;
    }

    private EmptyBorder createEmptyBorder() {
        return new EmptyBorder(5, 5, 5, 5);
    }

    private void downloadAndUpdate(JButton button) {
        // Here the background update downloader is launched in the background
        // See checkForUpdate(), where the interactive updater is launched for comments on launching an update downloader.
        new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {
                // Note the third argument which makes the call to the background updater blocking.
                // The callback receives progress information from the update downloader and changes the text on the button
                ApplicationLauncher.launchApplication("2297", null, true, new ApplicationLauncher.Callback() {
                    @Override
                    public void exited(int exitValue) {
                    }

                    @Override
                    public void prepareShutdown() {
                    }

                    @Override
                    public ApplicationLauncher.ProgressListener createProgressListener() {
                        return new ApplicationLauncher.ProgressListenerAdapter() {
                            boolean downloading;
                            @Override
                            public void actionStarted(String id) {
                                downloading = Objects.equals(id, "downloadFile");
                            }

                            @Override
                            public void percentCompleted(int value) {
                                if (downloading) {
                                    setProgressText(button, value);
                                }
                            }

                            @Override
                            public void indeterminateProgress(boolean indeterminateProgress) {
                                setProgressText(button, -1);
                            }
                        };
                    }
                });
                // At this point the update downloader has returned and we can check if the "Schedule update installation"
                // action has registered an update installer for execution
                // We now switch to the EDT in done() for terminating the application
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // rethrow exceptions that occurred in doInBackground() wrapped in an ExecutionException
                    if (UpdateChecker.isUpdateScheduled()) {
                        JOptionPane.showMessageDialog(HelloGui.this, "Download is complete, the new version will now be installed.", "Hello", JOptionPane.INFORMATION_MESSAGE);
                        // We execute the update immediately, but you could ask the user whether the update should be
                        // installed now. The scheduling of update installers is persistent, so this will also work
                        // after a restart of the launcher.
                        executeUpdate();
                    } else {
                        JOptionPane.showMessageDialog(HelloGui.this, "Update could not be downloaded", "Hello", JOptionPane.ERROR_MESSAGE);
                        removeNoticePane();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    removeNoticePane();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(HelloGui.this, "An error has occurred:" + e.getCause().getMessage(), "Hello", JOptionPane.ERROR_MESSAGE);
                    removeNoticePane();
                }
            }

            private void removeNoticePane() {
                JPanel noticePanel = updateNoticePanel;
                if (noticePanel != null) {
                    getContentPane().remove(noticePanel);
                    updateNoticePanel = null;
                    getContentPane().revalidate();
                }
            }
        }.execute();
    }

    private void setProgressText(JButton button, int percent) {
        EventQueue.invokeLater(() -> {
            if (percent < 0) {
                button.setText("Download in progress ...");
            } else {
                button.setText("Download in progress (" + percent + "% complete)");
            }
        });
    }

    private void executeUpdate() {
        // The arguments that are passed to the installer switch the default GUI mode to an unattended
        // mode with a progress bar. "-q" activates unattended mode, and "-splash Updating hello world ..."
        // shows a progress bar with the specified title.
        UpdateChecker.executeScheduledUpdate(Arrays.asList("-q", "-splash", "Updating Hello World GUI ...", "-alerts"), true, null);
    }

    private void checkForUpdate() {
        // Here, the "Standalone updater application" is launched in a new process.
        // The ID of the installer application is shown in the install4j IDE on the screens & actions tab
        // when the "Show IDs" toggle button is selected.
        // Use the "Integration wizard" button on the "Launcher integration" tab in the configuration
        // panel of the installer application, to get such a code snippet.
        try {
            ApplicationLauncher.launchApplication("535", null, false, null);
            // This call returns immediately, because the "blocking" argument is set to false
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not launch updater.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changeName(boolean inProcess) {
        // Here, the "Configure greeting application" is launched in the same process or in a new process.
        // The ID of the installer application is shown in the install4j IDE on the screens & actions tab
        // when the "Show IDs" toggle button is selected.
        // Use the "Integration wizard" button on the "Launcher integration" tab in the configuration
        // panel of the installer application, to get such a code snippet.
        if (inProcess) {
            ApplicationLauncher.launchApplicationInProcess("594", null, new NameChangeCallback(), ApplicationLauncher.WindowMode.DIALOG, this);
        } else {
            try {
                ApplicationLauncher.launchApplication("594", null, false, new NameChangeCallback());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "There was an error starting the installer application: " + e.getMessage(),
                        "Hello World",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private String getGreetingText() {
        return "Hello " + getGreetingName() + "!";
    }

    private String getGreetingName() {
        try {
            Map variables = Variables.loadFromPreferenceStore(true);
            if (variables != null) {
                return (String)variables.get("greeting");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "world";
    }

    private void printToConsole() {
        if (Util.isWindows()) {
            // In order to see the following output, please start the launcher from a console window
            // with the parameter -console
            //
            // On Windows, GUI applications usually cannot access the console if they were started from a console window
            // Since the "Allow -console parameter" option was selected in the launcher configuration of the
            // hello_gui executable, the console is acquired by the launcher and stdout will be printed to it
            System.out.println(getGreetingText());
            String additionalMessage = System.getProperty("additional.message");
            if (additionalMessage != null) {
                // Print the VM property that is contained in the hello.vmoptions file
                System.out.println(additionalMessage);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        try {
            SplashScreen.writeMessage("Initializing giant application ...");
            Thread.sleep(1000);
            SplashScreen.writeMessage("Opening complex main window ...");
            Thread.sleep(1000);
        } catch (SplashScreen.ConnectionException ex) {
        }
        if (args.length == 1 && args[0].equals("fail")) {
            throw new RuntimeException("I was asked to fail");
        } else {
            final HelloGui helloGui = new HelloGui();

            // startup notification on Microsoft Windows
            StartupNotification.registerStartupListener(parameters ->
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(
                                    helloGui,
                                    "I've been started again, with parameters \"" + parameters + "\".",
                                    "Hello World",
                                    JOptionPane.INFORMATION_MESSAGE
                            )
                    )
            );

            helloGui.printToConsole();
            helloGui.setVisible(true);
        }
    }

    private class NameChangeCallback implements ApplicationLauncher.Callback {
        @Override
        public void exited(int exitValue) {
            SwingUtilities.invokeLater(() -> {
                helloLabel.setText(getGreetingText());
                printToConsole();
            });
        }

        @Override
        public void prepareShutdown() {
            // will not be invoked in this case
        }
    }
}