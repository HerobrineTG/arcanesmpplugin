package net.herobrinetg.arcaneServer.commands;

import net.herobrinetg.arcaneServer.ArcaneServer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ArcaneServerCommandExecutor implements CommandExecutor {

    private ArcaneServer server;

    public ArcaneServerCommandExecutor(ArcaneServer server) {
        this.server = server;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("arcaneserver")) {
            return false;
        }

        if (!sender.hasPermission("arcaneserver.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("resetlegendary")) {
            String legendaryName = args[1].toLowerCase();
            switch (legendaryName) {
                case "witherboneblade":
                    if (server.getWitherBoneBladeExists() == true) {
                        server.setWitherBoneBladeExists(false);
                        sender.sendMessage(ChatColor.GREEN + "Wither Bone Blade legendary status reset. Recipe is now available.");
                        server.getLogger().info(sender.getName() + " reset Wither Bone Blade legendary status.");
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "Wither Bone Blade is already marked as not existing.");
                    }
                    break;
                case "dragonmace":
                    if (server.getDragonMaceExists() == true) {
                        server.setDragonMaceExists(false);
                        sender.sendMessage(ChatColor.GREEN + "Dragon Mace legendary status reset. Recipe is now available.");
                        server.getLogger().info(sender.getName() + " reset Dragon Mace legendary status.");
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "Dragon Mace is already marked as not existing.");
                    }
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Unknown legendary item. Use: /arcaneserver resetlegendary <witherboneblade|dragonmace>");
                    break;
            }
            return true;
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("give")) {
            String item = args[1].toLowerCase();
            switch (item) {
                case "witherboneblade":
                    if (sender instanceof Player player) {
                        ItemStack witherboneblade = server.getWitherBoneBlade();
                        player.getInventory().addItem(witherboneblade);
                        player.sendMessage("You received the Wither Bone Blade");
                    } else {
                        sender.sendMessage("This command can only be ran by a player.");
                    }
                    break;
                case "dragonmace":
                    if (sender instanceof Player player) {
                        ItemStack dragonmace = server.getDragonMace();
                        player.getInventory().addItem(dragonmace);
                        player.sendMessage("You received the Dragon Mace");
                    } else {
                        sender.sendMessage("This command can only be ran by a player.");
                    }
                    break;
                default:
                    sender.sendMessage("Unknown item. Try again.");
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /arcaneserver give <witherboneblade | dragonmace>");
                    break;
            }
        }
        return true;
    }
}
