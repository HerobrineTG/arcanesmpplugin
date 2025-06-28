package net.herobrinetg.arcaneServer.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLoginListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        String playerJoined = e.getPlayer().getDisplayName();
        e.setJoinMessage(ChatColor.GREEN + "[+] " + ChatColor.YELLOW + playerJoined);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        String playerLeft = e.getPlayer().getDisplayName();
        e.setQuitMessage(ChatColor.RED + "[-] " + ChatColor.YELLOW + playerLeft);
    }
}
