/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2020  Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package plugily.projects.murdermystery.commands.arguments.game;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.plajerlair.commonsbox.minecraft.compat.XMaterial;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaManager;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.commands.arguments.ArgumentsRegistry;
import plugily.projects.murdermystery.commands.arguments.data.CommandArgument;
import plugily.projects.murdermystery.commands.arguments.data.LabelData;
import plugily.projects.murdermystery.commands.arguments.data.LabeledCommandArgument;
import plugily.projects.murdermystery.handlers.ChatManager;
import plugily.projects.murdermystery.handlers.language.LanguageManager;
import plugily.projects.murdermystery.utils.Utils;

import java.util.ArrayList;

/**
 * @author 2Wild4You
 * <p>
 * Created at 09.08.2020
 */
public class ArenaSelectorArgument implements Listener {

  public ArenaSelectorArgument(ArgumentsRegistry registry) {
    registry.getPlugin().getServer().getPluginManager().registerEvents(this, registry.getPlugin());
    registry.mapArgument("murdermystery", new LabeledCommandArgument("arenas", "murdermystery.arenas", CommandArgument.ExecutorType.PLAYER,
      new LabelData("/mm arenas", "/mm arenas", "&7Select an arena\n&6Permission: &7murdermystery.arenas")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (ArenaRegistry.getArenas().size() == 0){
          player.sendMessage(ChatManager.colorMessage("Validator.No-Instances-Created"));
          return;
        }
        Inventory inventory = Bukkit.createInventory(player, Utils.serializeInt(ArenaRegistry.getArenas().size()), ChatManager.colorMessage("Arena-Selector.Inv-Title"));
        for (Arena arena : ArenaRegistry.getArenas()) {
          ItemStack itemStack;
          switch (arena.getArenaState()) {
            case WAITING_FOR_PLAYERS:
              itemStack = XMaterial.LIME_CONCRETE.parseItem();
              break;
            case STARTING:
              itemStack = XMaterial.YELLOW_CONCRETE.parseItem();
              break;
            default:
              itemStack = XMaterial.RED_CONCRETE.parseItem();
              break;
          }
          ItemMeta itemMeta = itemStack.getItemMeta();
          itemMeta.setDisplayName(arena.getId());

          ArrayList<String> lore = new ArrayList<>();
          for (String string : LanguageManager.getLanguageList("Arena-Selector.Item.Lore")) {
            lore.add(formatItem(string, arena, registry.getPlugin()));
          }

          itemMeta.setLore(lore);
          itemStack.setItemMeta(itemMeta);
          inventory.addItem(itemStack);
        }
        player.openInventory(inventory);
      }
    });

  }

  private String formatItem(String string, Arena arena, Main plugin) {
    String formatted = string;
    formatted = StringUtils.replace(formatted, "%mapname%", arena.getMapName());
    if (arena.getPlayers().size() >= arena.getMaximumPlayers()) {
      formatted = StringUtils.replace(formatted, "%state%", ChatManager.colorMessage("Signs.Game-States.Full-Game"));
    } else {
      formatted = StringUtils.replace(formatted, "%state%", plugin.getSignManager().getGameStateToString().get(arena.getArenaState()));
    }
    formatted = StringUtils.replace(formatted, "%playersize%", String.valueOf(arena.getPlayers().size()));
    formatted = StringUtils.replace(formatted, "%maxplayers%", String.valueOf(arena.getMaximumPlayers()));
    formatted = ChatManager.colorRawMessage(formatted);
    return formatted;
  }

  @EventHandler
  public void onArenaSelectorMenuClick(InventoryClickEvent e) {
    if (!e.getView().getTitle().equals(ChatManager.colorMessage("Arena-Selector.Inv-Title"))) {
      return;
    }
    if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) {
      return;
    }
    Player player = (Player) e.getWhoClicked();
    player.closeInventory();

    Arena arena = ArenaRegistry.getArena(e.getCurrentItem().getItemMeta().getDisplayName());
    if (arena != null) {
      ArenaManager.joinAttempt(player, arena);
    } else {
      player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.No-Arena-Like-That"));
    }
  }

}
