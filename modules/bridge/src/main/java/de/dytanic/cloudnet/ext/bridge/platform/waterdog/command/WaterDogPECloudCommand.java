/*
 * Copyright 2019-2021 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dytanic.cloudnet.ext.bridge.platform.waterdog.command;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.command.CommandInfo;
import de.dytanic.cloudnet.ext.bridge.platform.PlatformBridgeManagement;
import dev.waterdog.waterdogpe.command.Command;
import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.command.CommandSettings;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import dev.waterdog.waterdogpe.utils.types.TextContainer;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public final class WaterDogPECloudCommand extends Command {

  private final PlatformBridgeManagement<?, ?> management;

  public WaterDogPECloudCommand(@NotNull PlatformBridgeManagement<?, ?> management) {
    super("cloudnet", CommandSettings.builder()
      .setAliases(new String[]{"cloud", "cn"})
      .setPermission("cloudnet.command.cloudnet")
      .build());
    this.management = management;
  }

  @Override
  public boolean onExecute(@NotNull CommandSender sender, @NotNull String alias, String @NotNull [] args) {
    // check if any arguments are provided
    if (args.length == 0) {
      // <prefix> /cloudnet <command>
      sender.sendMessage(new TextContainer(this.management.getConfiguration().getPrefix() + "/cloudnet <command>"));
      return true;
    }
    // get the full command line
    String commandLine = String.join(" ", args);
    // skip the permission check if the source is the console
    if (sender instanceof ProxiedPlayer) {
      // get the command info
      CommandInfo command = CloudNetDriver.getInstance().getNodeInfoProvider().getConsoleCommand(commandLine);
      // check if the sender has the required permission to execute the command
      if (command != null && command.getPermission() != null) {
        if (!sender.hasPermission(command.getPermission())) {
          sender.sendMessage(new TextContainer(this.management.getConfiguration().getMessage(
            Locale.ENGLISH,
            "command-cloud-sub-command-no-permission"
          ).replace("%command%", command.getName())));
          return true;
        }
      }
    }
    // execute the command
    CloudNetDriver.getInstance().getNodeInfoProvider().sendCommandLineAsync(commandLine).onComplete(messages -> {
      for (String line : messages) {
        sender.sendMessage(new TextContainer(this.management.getConfiguration().getPrefix() + line));
      }
    });
    return true;
  }
}