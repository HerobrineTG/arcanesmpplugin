package net.herobrinetg.arcaneServer.items.legendary;

import net.herobrinetg.arcaneServer.ArcaneServer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Particle; // Import for particles
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DragonMace implements CommandExecutor, Listener {

    private final ArcaneServer plugin;
    private final NamespacedKey dragonMaceKey;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_DURATION_MILLIS = 2 * 60 * 1000;
    private final int DASH_DISTANCE = 15;
    private final int DASH_DURATION_TICKS = 10; // 0.5 seconds (20 ticks/sec)

    public DragonMace(ArcaneServer plugin) {
        this.plugin = plugin;
        this.dragonMaceKey = plugin.getDragonMaceKey();
        plugin.getLogger().info("DragonMace initialized with key: " + dragonMaceKey.getKey());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (commandSender instanceof Player player) {
            ItemStack dragonMace = new ItemStack(Material.MACE, 1);
            ItemMeta dragonMaceMeta = dragonMace.getItemMeta();

            dragonMaceMeta.setDisplayName(ChatColor.RED + "Dragon Mace");

            List<String> dragonMaceLore = new ArrayList<>();
            dragonMaceLore.add(" ");
            dragonMaceLore.add(ChatColor.YELLOW + "Ability: " + ChatColor.DARK_PURPLE + "Smash, Dash! " + ChatColor.YELLOW + "" + ChatColor.BOLD + "RIGHT CLICK");
            // Updated lore to reflect "leap"
            dragonMaceLore.add(ChatColor.GRAY + "Leap and dash up to " + DASH_DISTANCE + " blocks in front of you.");
            dragonMaceLore.add(ChatColor.GRAY + "Cooldown: " + (COOLDOWN_DURATION_MILLIS / 1000 / 60) + " minutes");
            dragonMaceLore.add(" ");
            dragonMaceLore.add(ChatColor.GRAY + "Dang this thing is heavy..");
            dragonMaceLore.add(" ");
            dragonMaceLore.add(ChatColor.RED + "" + ChatColor.BOLD + "LEGENDARY");

            dragonMaceMeta.setLore(dragonMaceLore);
            dragonMaceMeta.setCustomModelData(1);
            dragonMaceMeta.addEnchant(Enchantment.DENSITY, 7, true);

            dragonMaceMeta.getPersistentDataContainer().set(dragonMaceKey, PersistentDataType.BYTE, (byte) 1);
            dragonMace.setItemMeta(dragonMaceMeta);

            player.getInventory().addItem(dragonMace);
            player.sendMessage(ChatColor.GREEN + "You received the Dragon Mace!");
            plugin.getLogger().info(player.getName() + " received Dragon Mace.");
        } else {
            commandSender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
        }
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = player.getInventory().getItemInMainHand();

        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(dragonMaceKey, PersistentDataType.BYTE)) {

                event.setCancelled(true);
                plugin.getLogger().info(player.getName() + " right-clicked Dragon Mace. Checking cooldown...");

                if (cooldowns.containsKey(player.getUniqueId())) {
                    long timeLeft = cooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
                    if (timeLeft > 0) {
                        long secondsLeft = timeLeft / 1000;
                        player.sendMessage(ChatColor.RED + "Smash, Dash! is on cooldown! " +
                                ChatColor.GRAY + "You must wait " + formatTime(secondsLeft) + ".");
                        return;
                    }
                }

                // --- Initiate Leap & Dash ---
                Location startLoc = player.getLocation();
                Vector direction = startLoc.getDirection().normalize(); // Get player's facing direction

                // Play activation sound and message
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
                player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Smash, Dash!" + ChatColor.YELLOW + " initiated!");

                // Add an initial upward velocity for the "leap" effect
                player.setVelocity(player.getVelocity().add(new Vector(0, 0.4, 0))); // Adjust 0.4 for higher/lower jump

                final double distancePerTick = (double) DASH_DISTANCE / DASH_DURATION_TICKS;

                new BukkitRunnable() {
                    int ticksElapsed = 0;

                    @Override
                    public void run() {
                        if (!player.isOnline() || ticksElapsed >= DASH_DURATION_TICKS) {
                            this.cancel(); // Stop the dash if player logs out or dash is complete
                            return;
                        }

                        Location currentLoc = player.getLocation();
                        // Calculate the next step's location
                        // Use currentLoc to ensure smooth movement from player's actual position
                        Location nextLoc = currentLoc.clone().add(direction.clone().multiply(distancePerTick));

                        // --- Improved Collision Detection ---
                        boolean pathBlocked = false;
                        // Check intermediate points between currentLoc and nextLoc
                        // This helps prevent clipping through thin walls
                        for (double i = 0; i <= distancePerTick; i += 0.5) { // Check every 0.5 blocks
                            if (i > distancePerTick) i = distancePerTick; // Cap at max step distance
                            Location checkPoint = currentLoc.clone().add(direction.clone().multiply(i));

                            // Check both the head and feet level for collision
                            if (checkPoint.getBlock().isSolid() || checkPoint.clone().add(0, 1, 0).getBlock().isSolid()) {
                                pathBlocked = true;
                                break;
                            }
                        }

                        if (pathBlocked) {
                            this.cancel(); // Dash stops if it hits a wall
                            player.sendMessage(ChatColor.YELLOW + "Dash stopped due to obstruction!");
                            return;
                        }

                        // Teleport player to the next small increment
                        // Keep player's pitch and yaw (view direction)
                        player.teleport(nextLoc.setDirection(player.getLocation().getDirection()));

                        // Spawn particles along the path for visual animation
                        player.spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.2, 0.2, 0.2, 0.05); // Adjust particle type, count, offset, speed

                        ticksElapsed++;
                    }
                }.runTaskTimer(plugin, 0L, 1L); // Start immediately, repeat every tick (1L)

                // Set cooldown
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + COOLDOWN_DURATION_MILLIS);
                plugin.getLogger().info(player.getName() + " activated Dragon Mace ability. Cooldown set.");
            }
        }
    }

    private String formatTime(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes > 0) {
            if (seconds > 0) {
                return String.format("%d minute%s and %d second%s", minutes, (minutes == 1 ? "" : "s"), seconds, (seconds == 1 ? "" : "s"));
            } else {
                return String.format("%d minute%s", minutes, (minutes == 1 ? "" : "s"));
            }
        } else {
            return String.format("%d second%s", seconds, (seconds == 1 ? "" : "s"));
        }
    }
}