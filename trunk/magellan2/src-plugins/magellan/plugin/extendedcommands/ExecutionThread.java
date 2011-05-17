// class magellan.plugin.extendedcommands.ExecutionThread
// created on 15.03.2008
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

import java.util.Collections;
import java.util.List;

import magellan.client.Client;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;

/**
 * Executes all scripts for all units and containers.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 12.04.2008
 */
public class ExecutionThread extends Thread {
  private static final Logger log = Logger.getInstance(ExecutionThread.class);

  private Client client = null;
  private UserInterface ui = null;
  private ExtendedCommands commands = null;

  /**
   * 
   */
  public ExecutionThread(Client client, UserInterface ui, ExtendedCommands commands) {
    this.client = client;
    this.ui = ui;
    this.commands = commands;
  }

  /**
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    List<Unit> units = commands.getUnitsWithCommands(client.getData());
    List<UnitContainer> containers = commands.getUnitContainersWithCommands(client.getData());

    GameData data = client.getData();

    ui.setMaximum(units.size() + containers.size());
    ui.show();

    int counter = 0;

    commands.setFireChangeEvent(false);

    ExecutionThread.log.info("Executing commands for all configured containers...");
    Collections.sort(containers, new ContainerPriorityComparator(commands));

    for (UnitContainer container : containers) {
      commands.execute(data, container);
      ui.setProgress(container.getName(), counter++);
    }

    ExecutionThread.log.info("Executing commands for all configured units...");
    Collections.sort(units, new UnitPriorityComparator(commands));

    for (Unit unit : units) {
      commands.execute(data, unit);
      ui.setProgress(unit.getName(), counter++);
    }

    commands.setFireChangeEvent(true);
    ExecutionThread.log.info("Fire event - gamedata changed");
    client.getDispatcher().fire(new GameDataEvent(commands, client.getData()));
    ui.ready();
  }
}
