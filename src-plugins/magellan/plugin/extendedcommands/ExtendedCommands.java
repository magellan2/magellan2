// class magellan.plugin.extendedcommands.ExtendedCommands
// created on 02.02.2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.plugin.extendedcommands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jdk.jshell.Diag;
import jdk.jshell.EvalException;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Kind;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis.Completeness;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import magellan.client.Client;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.swing.DebugDock;
import magellan.client.swing.ProgressBarUI;
import magellan.client.utils.ErrorWindow;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.Encoding;
import magellan.library.utils.Locales;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.UserInterface;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;

/**
 * This class holds the commands for all units.
 * 
 * @author Thoralf Rickert
 */
public class ExtendedCommands {

  /**
   * Exception to indicate errors during script evaluation.
   * 
   * @author stm
   */
  public static class JShellException extends Exception {

    private Snippet snippet;
    private SnippetEvent event;
    private String message;
    private Stream<Diag> diagnostics;
    private StringBuilder source;

    /**
     * @param snippet
     * @param diagnostics
     */
    public JShellException(Snippet snippet, Stream<Diag> diagnostics) {
      super("Error in snippet: " + snippet.kind());
      message = "Error in snippet: " + snippet.kind();
      this.snippet = snippet;
      this.diagnostics = diagnostics;
    }

    /**
     * @param event
     * @param diagnostics
     */
    public JShellException(SnippetEvent event, Stream<Diag> diagnostics) {
      super();
      this.event = event;
      this.diagnostics = diagnostics;

      switch (event.status()) {
      case REJECTED:
        message = "Snippet rejected";
        break;
      default:
        if (event.exception() != null) {
          if (event.exception() instanceof EvalException) {
            message = "Exception in snippet caused by " + ((EvalException) event.exception())
                .getExceptionClassName();
          } else {
            message = "Exception in snippet: " + event.exception().getMessage();
          }
        } else {
          message = "Snippet problem: " + event.toString();
        }
      }
    }

    /**
     * @param message
     * @param source
     */
    public JShellException(String message, StringBuilder source) {
      this.message = message;
      this.source = source;
    }

    @Override
    public String getMessage() {
      return message;
    }

    /**
     * Returns a more verbose description. The result depends on the Shell implementation.
     */
    public String getDescription() {
      final StringBuilder sb = new StringBuilder();
      if (diagnostics != null) {
        descriptionFromDiagnostics(sb, event, diagnostics);
      }

      if (snippet != null) {
        sb.append("\nSNIPPET\n");
        sb.append(snippet.toString());
      } else if (event != null) {
        descriptionFromEvent(sb);
      } else if (source != null) {
        descriptionFromSource(sb);
      } else {
        sb.append("Unknown error");
      }
      return sb.toString();
    }

    private void descriptionFromSource(StringBuilder sb) {
      sb.append("\nSOURCE\n");
      if (source.length() < 999) {
        sb.append(source);
      } else {
        sb.append(source.substring(0, 300)).append("\n...\n").append(source.substring(source.length() - 300));
      }
    }

    private void descriptionFromEvent(StringBuilder sb) {
      sb.append("\nEVENT\n");//
      sb.append(event.toString()); //
      if (event.exception() != null) {
        sb.append("\nEXCEPTION ");
        if (event.exception() instanceof EvalException) {
          sb.append(((EvalException) event.exception()).getExceptionClassName()).append("\n");
          StringWriter writer = new StringWriter();
          PrintWriter wwriter = new PrintWriter(writer);
          event.exception().printStackTrace(wwriter);
          sb.append(writer.toString());
          wwriter.close();
        } else { // UnresolvedReferenceException ...
          sb.append(event.exception().getClass().getName()).append("\n");
          sb.append(event.exception());
        }
      }
    }

  }

  private static final Logger log = Logger.getInstance(ExtendedCommands.class);
  public static final String COMANDFILENAME = "extendedcommands.xml";
  private File unitCommandsFile;
  private Client client;
  private Hashtable<String, Script> unitCommands = new Hashtable<String, Script>();
  private Hashtable<String, Script> unitContainerCommands = new Hashtable<String, Script>();

  private Script library;

  private String defaultLibrary = "";
  private String defaultUnitScript = "";
  private String defaultContainerScript = "";

  protected boolean fireChangeEvent = true;
  protected boolean useThread = true;

  /**
   * Constructor for the extended commands container object
   * 
   * @param client The GUI client that holds the configuration etc.
   */
  public ExtendedCommands(Client client) {
    this.client = client;
    Properties properties = client.getProperties();
    String commandsFilename = properties.getProperty("extendedcommands.unitCommands");
    if (Utils.isEmpty(commandsFilename)) {
      unitCommandsFile = new File(Client.getSettingsDirectory(), COMANDFILENAME);
    } else {
      unitCommandsFile = new File(commandsFilename);
    }
    if (!unitCommandsFile.exists()) {
      // file doesn't exist. create it with an empty set.
      write(unitCommandsFile);
    }

    read(unitCommandsFile);
  }

  /**
   * Constructor for the extended commands container object. This method is for GUI-less usage only!
   * 
   * @param commandsFilename
   */
  public ExtendedCommands(String commandsFilename) {
    if (Utils.isEmpty(commandsFilename)) {
      unitCommandsFile = new File(PropertiesHelper.getSettingsDirectory(), COMANDFILENAME);
    } else {
      unitCommandsFile = new File(commandsFilename);
    }
    if (!unitCommandsFile.exists()) {
      // file doesn't exist. create it with an empty set.
      write(unitCommandsFile);
    }

    read(unitCommandsFile);
  }

  /**
   * Returns true if there are any commands set.
   */
  public boolean hasCommands() {
    return unitCommands.size() > 0 || unitContainerCommands.size() > 0;
  }

  /**
   * Returns true if there is a command for the given unitcontainer.
   */
  public boolean hasCommands(UnitContainer container) {
    if (!hasCommands())
      return false;
    if (container == null)
      return false;
    if (container.getID() == null)
      return false;
    return unitContainerCommands.containsKey(container.getID().toString());
  }

  /**
   * Returns true if there is a command for the given unitcontainer.
   */
  public boolean hasAllCommands(GameData world, UnitContainer container) {
    if (!hasCommands())
      return false;
    if (container == null)
      return false;
    if (container.getID() == null)
      return false;
    boolean ok = unitContainerCommands.containsKey(container.getID().toString());

    Collection<Unit> units = container.units();
    if (units != null && units.size() > 0) {
      for (Unit unit : units) {
        ok = ok || hasCommands(unit);
        if (ok) {
          break;
        }
      }
    }

    return ok;
  }

  private static void descriptionFromDiagnostics(StringBuilder sb, SnippetEvent event, Stream<Diag> diagnostics) {
    StringBuilder sbWarn = new StringBuilder(), sbErr = new StringBuilder();
    final SourceLines sourceLines = SourceLines.create(event.snippet().source());
    int[] counter = new int[2];
    classify(sbWarn, sbErr, counter, sourceLines, diagnostics);
    if (sbErr.length() > 0) {
      sb.append(counter[0]).append(" ERRORS:\n");
      sb.append(sbErr);
    }
    if (sbWarn.length() > 0) {
      sb.append(counter[1]).append(" WARNINGS:\n").append(sbWarn);
    }
    if (sb.length() > 0) {
      sb.insert(0, "DIAGNOSTICS\n");
    }
  }

  private static class SourceLines {

    private String source;
    private long[] starts;
    private String[] lines;

    public int getLine(long position) {
      if (starts == null) {
        init();
      }
      int line = Arrays.binarySearch(starts, position);
      if (line < 0) {
        line = -(line + 2);
      }
      return line;
    }

    public int getColumn(long position) {
      long start = starts[getLine(position)];
      return (int) (position - start);
    }

    private void init() {
      lines = source.split("(?<=\r?\n)");
      starts = new long[lines.length + 1];
      int lnr = 0;
      long pos = 0;
      for (String line : lines) {
        starts[lnr++] = pos;
        pos += line.length();
      }
      starts[lnr] = pos;
      source = null;
    }

    public String getSourceLine(int lineNr) {
      if (starts == null) {
        init();
      }
      return lines[lineNr];
    }

    public static SourceLines create(String source) {
      SourceLines result = new SourceLines();
      result.source = source;
      return result;
    }

  }

  private static void classify(StringBuilder sbWarning, StringBuilder sbError, int[] counter, SourceLines sourceLines,
      Stream<Diag> diagnosticStream) {
    diagnosticStream.forEach(new Consumer<Diag>() {
      public void accept(Diag dg) {
        if (dg.getCode().contains(".err.")) {
          counter[0]++;
          if (sbError.length() < 2999) {
            append(sbError, sourceLines, dg);
          } else {
            if (sbError.lastIndexOf("...") < sbError.length() - 5) {
              sbError.append("...\n");
            }
          }
        } else {
          counter[1]++;
          if (sbWarning.length() < 999 && sbError.length() < 2999) {
            append(sbWarning, sourceLines, dg);
          } else {
            if (sbWarning.lastIndexOf("...") < sbWarning.length() - 5) {
              sbWarning.append("...\n");
            }
          }
        }
      }
    });
  }

  private static void append(StringBuilder sb, SourceLines sourceLines, Diag dg) {
    sb.append(dg.getMessage(Locales.getGUILocale()));

    int startLine = sourceLines.getLine(dg.getStartPosition()), posLine = sourceLines.getLine(dg.getPosition()),
        endLine =
            sourceLines.getLine(dg.getEndPosition());
    int posCol = sourceLines.getColumn(dg.getPosition());
    sb.append(String.format(" at %d:%d:%d\n", posLine + 1, posCol + 1, dg.getPosition() + 1));
    if (endLine - startLine > 3) {
      if (startLine != posLine) {
        sb.append(sourceLines.getSourceLine(startLine)).append("...\n");
      }
      sb.append(sourceLines.getSourceLine(posLine));
      if (endLine != posLine) {
        sb.append("...\n").append(sourceLines.getSourceLine(endLine));
      }
    } else {
      for (int l = startLine; l <= endLine; ++l) {
        sb.append(sourceLines.getSourceLine(l));
      }
    }
    if (sb.charAt(sb.length() - 1) != '\n') {
      sb.append("\n---\n");
    } else {
      sb.append("---\n");
    }
  }

  /**
   * Returns true if there is a command for the given unit.
   */
  public boolean hasCommands(Unit unit) {
    if (!hasCommands())
      return false;
    if (unit == null)
      return false;
    if (unit instanceof TempUnit)
      return false;
    if (unit.getID() == null)
      return false;
    return unitCommands.containsKey(unit.getID().toString());
  }

  /**
   * Returns the command script for the given unitcontainer.
   */
  public Script getCommands(UnitContainer container) {
    if (!hasCommands(container))
      return null;
    return unitContainerCommands.get(container.getID().toString());
  }

  /**
   * Returns the command script for the given unit.
   */
  public Script getCommands(Unit unit) {
    if (!hasCommands(unit))
      return null;
    return unitCommands.get(unit.getID().toString());
  }

  /**
   * Sets the commands for a given unit.
   */
  public void setCommands(Unit unit, Script script) {
    if (unit == null)
      return;
    if (unit instanceof TempUnit)
      return;
    if (unit.getID() == null)
      return;

    if ((Utils.isEmpty(script) || Utils.isEmpty(script.getScript())) && hasCommands(unit)) {
      // script is empty, let's remove the script from the mapping
      unitCommands.remove(unit.getID().toString());
    } else {
      // replace the script in the mapping
      unitCommands.put(unit.getID().toString(), script);
    }

    ExtendedCommandsPlugIn.getExecuteMenu().setEnabled(hasCommands());
  }

  /**
   * Sets the commands for a given unit.
   */
  public void setCommands(UnitContainer container, Script script) {
    if (container == null)
      return;
    if (container.getID() == null)
      return;

    if ((Utils.isEmpty(script) || Utils.isEmpty(script.getScript())) && hasCommands(container)) {
      // script is empty, let's remove the script from the mapping
      unitContainerCommands.remove(container.getID().toString());
    } else {
      // replace the script in the mapping
      unitContainerCommands.put(container.getID().toString(), script);
    }

    ExtendedCommandsPlugIn.getExecuteMenu().setEnabled(hasCommands());
  }

  /**
   * Returns the value of library.
   * 
   * @return Returns library.
   */
  public Script getLibrary() {
    return library;
  }

  /**
   * Sets the value of library.
   * 
   * @param library The value for library.
   */
  public void setLibrary(Script library) {
    this.library = library;
  }

  /**
   * Returns a list of all units with commands.
   */
  public List<Unit> getUnitsWithCommands(GameData world) {
    List<Unit> units = new ArrayList<Unit>();

    for (String unitId : unitCommands.keySet()) {
      Unit unit = world.getUnit(UnitID.createUnitID(unitId, world.base));
      if (unit != null) {
        units.add(unit);
      }
    }

    return units;
  }

  /**
   * Returns a list of all unitcontainerss with commands.
   */
  public List<UnitContainer> getUnitContainersWithCommands(GameData world) {
    List<UnitContainer> containers = new ArrayList<UnitContainer>();

    for (String unitContainerId : unitContainerCommands.keySet()) {
      switch (unitContainerCommands.get(unitContainerId).getType()) {
      case REGIONTYPE: {
        UnitContainer container = world.getRegion(CoordinateID.parse(unitContainerId, ", "));
        if (container != null) {
          containers.add(container);
        }
        break;
      }
      case RACE: {
        UnitContainer container =
            world.getFaction(EntityID.createEntityID(unitContainerId, world.base));
        if (container != null) {
          containers.add(container);
        }
        break;
      }
      case BUILDINGTYPE: {
        UnitContainer container =
            world.getBuilding(EntityID.createEntityID(unitContainerId, world.base));
        if (container != null) {
          containers.add(container);
        }
        break;
      }
      case SHIPTYPE: {
        UnitContainer container =
            world.getShip(EntityID.createEntityID(unitContainerId, world.base));
        if (container != null) {
          containers.add(container);
        }
        break;
      }
      case UNKNOWN:
      }
    }

    return containers;
  }

  /**
   * Clears the commands if there are now units associated with this script
   */
  public int clearUnusedUnits(GameData world) {
    List<String> keys = new ArrayList<String>();
    int counter = 0;

    for (String unitId : unitCommands.keySet()) {
      Unit unit = world.getUnit(UnitID.createUnitID(unitId, world.base));
      if (unit == null) {
        keys.add(unitId);
        counter++;
      }
    }

    for (String key : keys) {
      unitCommands.remove(key);
    }

    return counter;
  }

  /**
   * Clears the commands if there are now unitcontainers associated with this script
   */
  public int clearUnusedContainers(GameData world) {
    List<String> keys = new ArrayList<String>();
    int counter = 0;

    for (String unitContainerId : unitContainerCommands.keySet()) {
      switch (unitContainerCommands.get(unitContainerId).getType()) {
      case REGIONTYPE: {
        UnitContainer container = world.getRegion(CoordinateID.parse(unitContainerId, ", "));
        if (container == null) {
          keys.add(unitContainerId);
          counter++;
        }
        break;
      }
      case RACE: {
        UnitContainer container =
            world.getFaction(EntityID.createEntityID(unitContainerId, world.base));
        if (container == null) {
          keys.add(unitContainerId);
          counter++;
        }
        break;
      }
      case BUILDINGTYPE: {
        UnitContainer container =
            world.getBuilding(EntityID.createEntityID(unitContainerId, world.base));
        if (container == null) {
          keys.add(unitContainerId);
          counter++;
        }
        break;
      }
      case SHIPTYPE: {
        UnitContainer container =
            world.getShip(EntityID.createEntityID(unitContainerId, world.base));
        if (container == null) {
          keys.add(unitContainerId);
          counter++;
        }
        break;
      }
      case UNKNOWN:
      }
    }

    for (String key : keys) {
      unitContainerCommands.remove(key);
    }

    return counter;
  }

  /**
   * Executes the commands/script for a given unit.
   */
  public void execute(GameData world, Unit unit) {
    if (!hasCommands(unit))
      return;

    executeWithLib(getCommands(unit), world, unit, null);
    if (client != null && isFireChangeEvent()) {
      client.getDispatcher().fire(new UnitOrdersEvent(unit, unit));
    }
  }

  /**
   * Executes the commands/script for a given unitcontainer.
   */
  public void execute(GameData world, UnitContainer container) {
    if (!hasCommands(container))
      return;

    executeWithLib(getCommands(container), world, null, container);
  }

  private void executeWithLib(Script commands, GameData world, Unit unit, UnitContainer container) {
    StringBuilder script = new StringBuilder();
    if (getLibrary() != null) {
      script.append(getLibrary().getScript());
    }
    if (script.charAt(script.length() - 1) != '\n') {
      script.append("\n");
    }
    script.append(commands.getScript());
    execute(script.toString(), world, unit, container);
  }

  /**
   * Executes the library commands/script.
   */
  public void execute(GameData world) {
    execute(getLibrary().getScript(), world, null, null);
  }

  protected void execute(final String script, final GameData world, final Unit unit,
      final UnitContainer container) {

    if (isUseThread()) {
      final UserInterface ui;
      if (client != null) {
        ui = new ProgressBarUI(client);
      } else {
        ui = new NullUserInterface();
      }
      ui.setTitle(Resources.get("dock.ExtendedCommands.title", false));
      ui.setMaximum(-1);
      ui.setProgress(unit != null ? unit.toString() : container != null ? container.toString()
          : "???", 0);
      ui.show();
      new Thread(new Runnable() {
        public void run() {
          runExecute(script, world, unit, container, ui);
        }
      }).start();

    } else {
      runExecute(script, world, unit, container, null);
    }
  }

  public static class RunHelper {
    public static GameData world;
    public static UnitContainer container;
    public static Unit unit;
    public static ExtendedCommandsHelper helper;
    public static DebugDock logDock;
  }

  protected void runExecute(final String script, final GameData world, final Unit unit,
      final UnitContainer container, final UserInterface ui) {

    log.info("ExtCmds Thread started for world " + world + ", unit " + unit + ", container "
        + container);

    ExtendedCommandsHelper helper = new ExtendedCommandsHelper(client, world, unit, container);
    if (ui != null) {
      helper.setUI(ui);
    }

    runJShell(script, world, unit, container, ui, helper);
    // runBeanShell(script, world, unit, container, ui, helper);
  }

  protected synchronized void runJShell(String script, GameData world, Unit unit,
      UnitContainer container, UserInterface ui, ExtendedCommandsHelper helper) {
    try {
      log.finest("script:\n" + script);
      final JShell sh = JShell.builder().compilerOptions("-Xlint:all")
          .executionEngine("local").build();

      RunHelper.world = world;
      RunHelper.container = container;
      RunHelper.unit = unit;
      RunHelper.helper = helper;
      RunHelper.logDock = DebugDock.getInstance();

      StringBuilder incomplete = new StringBuilder();
      CompletionInfo c = null;
      for (String line : script.split("[\r\n]+")) {
        c = sh.sourceCodeAnalysis().analyzeCompletion(line);
        if (!c.completeness().equals(Completeness.EMPTY)) {
          incomplete.append(line).append("\n");
        }
      }
      if (c != null && c.completeness().equals(Completeness.DEFINITELY_INCOMPLETE))
        throw new JShellException("source is incomplete", incomplete);

      String remaining = script;
      boolean firstStatement = true;
      int restart = 1;
      while (restart > 0) {
        restart = 0;
        for (List<Snippet> snippets = sh.sourceCodeAnalysis().sourceToSnippets(remaining); remaining
            .trim().length() > 0; snippets = sh.sourceCodeAnalysis().sourceToSnippets(remaining)) {
          for (Snippet snippet : snippets) {
            if (restart > 1) {
              break;
            }
            Kind kind = snippet.kind();
            switch (kind) {
            case ERRONEOUS:
              // throw new JShellException(snippet, sh.diagnostics(snippet));
              if (firstStatement) {
                defineGlobals(sh);
                firstStatement = false;
                restart = 1;
              }
              break;
            case IMPORT:
              if (!firstStatement) {
                log.warn("ExtendedCommands: import statement after first code statement: " + snippet
                    .source());
                firstStatement = true;
              }
              break;
            default:
              if (firstStatement) {
                defineGlobals(sh);
                firstStatement = false;
              }
              break;
            }
          }
          if (restart < 1) {
            String old = remaining;
            remaining = eval(sh, remaining);
            log.finest("evaluated:\n" + old.substring(0, old.lastIndexOf(remaining)).trim());
          } else {
            break;
          }
        }
      }
    } catch (JShellException e) {
      // TODO
      if (client != null) {
        ErrorWindow errorWindow = new ErrorWindow(client, e.getMessage(), e.getDescription(), e);
        errorWindow.setShutdownOnCancel(false);
        errorWindow.setVisible(true);
      } else {
        log.error(e.getMessage());
        log.error(e.getDescription(), e);
      }
    } catch (Throwable throwable) {
      ExtendedCommands.log.info("", throwable);

      if (client != null) {
        ErrorWindow errorWindow = new ErrorWindow(client, throwable.getMessage(), "", throwable);
        errorWindow.setShutdownOnCancel(false);
        errorWindow.setVisible(true);
      } else {
        log.error(throwable.getMessage(), throwable);
      }
    } finally {
      if (isFireChangeEvent()) {
        if (client != null) {
          client.getDispatcher().fire(new GameDataEvent(this, world));
        }
      }
      if (ui != null) {
        ui.ready();
      }
    }
  }

  private String eval(final JShell sh, String source) throws JShellException {
    CompletionInfo c = sh.sourceCodeAnalysis().analyzeCompletion(source);
    List<SnippetEvent> evaluation = sh.eval(c.source() != null ? c.source() : source);

    for (SnippetEvent event : evaluation) {
      // TODO do proper logging of warnings
      if (!Utils.isEmpty(event.value())) {
        log.finest("eval: " + event.value());
      }
      switch (event.status()) {
      case REJECTED: {
        Stream<Diag> diags = sh.diagnostics(event.snippet());
        throw new JShellException(event, diags);
      }
      default:
        if (event.exception() != null)
          throw new JShellException(event, sh.diagnostics(event.snippet()));
      }
    }
    return c.remaining();
  }

  private void defineGlobals(JShell sh) throws JShellException {
    eval(sh,
        "magellan.library.GameData world = magellan.plugin.extendedcommands.ExtendedCommands.RunHelper.world;");
    eval(sh,
        "magellan.library.Unit unit = magellan.plugin.extendedcommands.ExtendedCommands.RunHelper.unit;");
    eval(sh,
        "magellan.library.UnitContainer container = magellan.plugin.extendedcommands.ExtendedCommands.RunHelper.container;");
    eval(sh,
        "magellan.plugin.extendedcommands.ExtendedCommandsHelper helper = magellan.plugin.extendedcommands.ExtendedCommands.RunHelper.helper;");
    eval(sh,
        "magellan.client.swing.DebugDock log = magellan.plugin.extendedcommands.ExtendedCommands.RunHelper.logDock;");
  }

  /**
   * Reads a file with commands
   */
  protected void read(File file) {
    try {
      ExtendedCommands.log.info("Reading XML " + file);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      factory.setValidating(false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(file);
      Element rootNode = document.getDocumentElement();
      if (!rootNode.getNodeName().equalsIgnoreCase("extended_commands")) {
        ExtendedCommands.log.error("This is NOT an extended command configuration file");
        return;
      }

      Element libraryNode = Utils.getChildNode(rootNode, "library");
      if (libraryNode != null) {
        library = new Script(libraryNode);
      }

      List<Element> nodes = Utils.getChildNodes(rootNode, "container");
      ExtendedCommands.log.info("Found " + nodes.size() + " unitcontainer commands");
      for (Element node : nodes) {
        Script script = new Script(node);
        unitContainerCommands.put(script.getContainerId(), script);
      }

      nodes = Utils.getChildNodes(rootNode, "unit");
      ExtendedCommands.log.info("Found " + nodes.size() + " unit commands");
      for (Element node : nodes) {
        Script script = new Script(node);
        unitCommands.put(script.getContainerId(), script);
      }

    } catch (Throwable throwable) {
      ExtendedCommands.log.error("", throwable);
      if (client != null) {
        ErrorWindow errorWindow = new ErrorWindow(client, throwable.getMessage(), "", throwable);
        errorWindow.setShutdownOnCancel(false);
        errorWindow.setVisible(true);
      }
    }
  }

  /**
   *
   */
  public void save() {
    write(unitCommandsFile);
  }

  /**
   * Writes the commands
   */
  protected void write(File file) {
    try {
      FileOutputStream fos = new FileOutputStream(file);
      OutputStreamWriter osw = new OutputStreamWriter(fos, Encoding.UTF8.toString());
      PrintWriter writer = new PrintWriter(osw);

      writer.println("<?xml version=\"1.0\" encoding=\"" + Encoding.UTF8.toString() + "\"?>");
      writer.println("<extended_commands>");
      if (library != null) {
        library.toXML(writer);
      }
      for (Script script : unitContainerCommands.values()) {
        script.toXML(writer);
      }
      for (Script script : unitCommands.values()) {
        script.toXML(writer);
      }
      writer.println("</extended_commands>");

      writer.close();
      osw.close();
      fos.close();
    } catch (Throwable throwable) {
      ExtendedCommands.log.error("", throwable);
      if (client != null) {
        ErrorWindow errorWindow = new ErrorWindow(client, throwable.getMessage(), "", throwable);
        errorWindow.setShutdownOnCancel(false);
        errorWindow.setVisible(true);
      }
    }
  }

  /**
   * Returns the default libary script.
   */
  public String getDefaultLibrary() {
    return defaultLibrary;
  }

  /**
   * Returns the default libary script.
   */
  public void setDefaultLibrary(String script) {
    defaultLibrary = script;
  }

  /**
   * Return default script for units.
   */
  public String getDefaultUnitScript() {
    return defaultUnitScript;
  }

  /**
   * Sets the default script for units.
   */
  public void setDefaultUnitScript(String script) {
    defaultUnitScript = script;
  }

  /**
   * Return default script for units.
   */
  public String getDefaultContainerScript() {
    return defaultContainerScript;
  }

  /**
   * Sets the default script for units.
   */
  public void setDefaultContainerScript(String script) {
    defaultContainerScript = script;
  }

  public Script createScript(Unit unit) {
    Script script =
        new Script(unit.getID().toString(), Script.SCRIPTTYPE_UNIT, ContainerType.UNKNOWN, "");
    if (Utils.isEmpty(script) || Utils.isEmpty(script.getScript())) {
      // show some examples for beginners...
      script.setScript(getDefaultUnitScript());
    }
    return script;
  }

  public Script createScript(UnitContainer container) {
    Script script =
        new Script(container.getID().toString(), Script.SCRIPTTYPE_CONTAINER, ContainerType
            .getType(container.getType()), "");
    if (Utils.isEmpty(script.getScript())) {
      // show some examples for beginners...
      script.setScript(getDefaultContainerScript());
    }
    return script;
  }

  public Script createLibrary(GameData data) {
    Script script = new Script(null, Script.SCRIPTTYPE_LIBRARY, ContainerType.UNKNOWN, "");
    if (Utils.isEmpty(script)) {
      // show some examples for beginners...
      script.setScript(getDefaultLibrary());
    }
    return script;
  }

  /**
   * Returns true, if the current thread should fire an ChangeEvent for the whole world.
   */
  public boolean isFireChangeEvent() {
    return fireChangeEvent;
  }

  /**
   * Set to true true, if the current thread should fire an ChangeEvent for the whole world.
   */
  public void setFireChangeEvent(boolean fireChangeEvent) {
    this.fireChangeEvent = fireChangeEvent;
  }

  /**
   * Returns true, if any command execution should be done inside it's own execution thread
   */
  public boolean isUseThread() {
    return useThread;
  }

  /**
   * Set to true (default) to execute any command inside its own execution thread
   */
  public void setUseThread(boolean useThread) {
    this.useThread = useThread;
  }

}
