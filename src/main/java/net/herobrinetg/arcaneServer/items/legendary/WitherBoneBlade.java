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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WitherBoneBlade implements CommandExecutor, Listener {
    private final ArcaneServer plugin; // Store plugin instance
    private final NamespacedKey witherBladeKey; // Store the NamespacedKey directly
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_DURATION_MILLIS = 3 * 60 * 1000; // 3 minutes in milliseconds

    public WitherBoneBlade(ArcaneServer plugin) {
        this.plugin = plugin;
        this.witherBladeKey = plugin.getWitherBladeKey(); // Get the key from the main plugin
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (commandSender instanceof Player player) {
            ItemStack witherBoneBlade = new ItemStack(Material.NETHERITE_SWORD, 1);
            ItemMeta witherBoneBladeMeta = witherBoneBlade.getItemMeta();
            witherBoneBladeMeta.setDisplayName(ChatColor.RED + "Wither Bone Blade");

            List<String> witherBoneBladeLore = new ArrayList<>();
            witherBoneBladeLore.add(" ");
            witherBoneBladeLore.add(ChatColor.YELLOW + "Ability: " + ChatColor.DARK_PURPLE + "Wither's Fury " + ChatColor.YELLOW + "" + ChatColor.BOLD + "RIGHT CLICK");
            witherBoneBladeLore.add(ChatColor.GRAY + "Get Strength 2 for 10 seconds,");
            witherBoneBladeLore.add(ChatColor.GRAY + "dealing 260% melee damage.");
            witherBoneBladeLore.add(" ");
            witherBoneBladeLore.add(ChatColor.GRAY + "Legend says, the mighty" + ChatColor.MAGIC + " wither king ");
            witherBoneBladeLore.add(ChatColor.GRAY + "once hand crafted it himself..");
            witherBoneBladeLore.add(" ");
            witherBoneBladeLore.add(ChatColor.RED + "" + ChatColor.BOLD + "LEGENDARY");
            witherBoneBladeMeta.setLore(witherBoneBladeLore);
            witherBoneBladeMeta.setCustomModelData(0);

            witherBoneBladeMeta.addEnchant(Enchantment.SHARPNESS, 7, true);
            witherBoneBladeMeta.addEnchant(Enchantment.LOOTING, 4, true);

            witherBoneBladeMeta.getPersistentDataContainer().set(witherBladeKey, PersistentDataType.BYTE, (byte) 1);
            witherBoneBlade.setItemMeta(witherBoneBladeMeta);

            player.getInventory().addItem(witherBoneBlade);
            player.sendMessage(ChatColor.GREEN + "You received the Wither Bone Blade!");
            System.out.println("/givewitherblade command executed."); // Consider using plugin.getLogger().info() instead of System.out.println
        }
        return true;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the player right-clicked with an item in hand
        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            System.out.println("Right click action triggered (from onPlayerInteract() )");
            if (meta != null && meta.getPersistentDataContainer().has(witherBladeKey, PersistentDataType.BYTE)) {

                event.setCancelled(true);
                System.out.println("event.setCancelled line crossed.. .. ");
                // --- Cooldown Check ---
                if (cooldowns.containsKey(player.getUniqueId())) {
                    long timeLeft = cooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
                    if (timeLeft > 0) {
                        long secondsLeft = timeLeft / 1000;
                        player.sendMessage(ChatColor.RED + "Wither's Fury is on cooldown! " +
                                ChatColor.GRAY + "You must wait " + formatTime(secondsLeft) + ".");
                        return; // Stop here, ability is on cooldown
                    }
                }
                System.out.println("Cooldown check completed.");
                // --- Activate Ability ---
                // Apply Strength II (2x damage) for 10 seconds
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 10 * 20, 1, true, false));

                // Set cooldown
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + COOLDOWN_DURATION_MILLIS);

                player.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Wither's Fury" + ChatColor.YELLOW + " unleashed! Your attacks are empowered!");
                // Optionally, play a sound or particle effect here for visual feedback.
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
                System.out.println("activated ability");
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if the damager is a player
        if (event.getDamager() instanceof Player player) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            // Check if the player is holding the Wither Bone Blade
            if (itemInHand != null && itemInHand.hasItemMeta()) {
                ItemMeta meta = itemInHand.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(witherBladeKey, PersistentDataType.BYTE)) {

                    // Check if the player currently has the Strength potion effect (meaning Wither's Fury is active)
                    if (player.hasPotionEffect(PotionEffectType.STRENGTH)) {
                        // Play the Wither hit sound at the player's location
                        // Sound: ENTITY_WITHER_HURT
                        // Volume: 1.0f (full volume)
                        // Pitch: 1.0f (normal pitch)
                        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 1.0f);
                    }
                }
            }
        }
    }
    // --- Helper method for formatting cooldown time ---
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
