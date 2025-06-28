package net.herobrinetg.arcaneServer.listeners; // Consider a new package for listeners

import net.herobrinetg.arcaneServer.ArcaneServer; // Your main plugin class
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent; // For enchanting tables
import org.bukkit.event.inventory.PrepareAnvilEvent; // For anvils
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta; // For enchanted books
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentCapListener implements Listener {

    private final ArcaneServer plugin;
    // Define your enchantment caps here
    private final Map<Enchantment, Integer> enchantmentCaps = new HashMap<>();

    public EnchantmentCapListener(ArcaneServer plugin) {
        this.plugin = plugin;
        loadEnchantmentCaps(); // Populate the map with your desired caps
    }

    private void loadEnchantmentCaps() {
        // Protection (all types of protection including Blast, Fire, Projectile, Feather Falling)
        // Protection IV (level 4) is default max, we cap at III (level 3)
        enchantmentCaps.put(Enchantment.PROTECTION, 3);
        enchantmentCaps.put(Enchantment.BLAST_PROTECTION, 3);
        enchantmentCaps.put(Enchantment.FIRE_PROTECTION, 3);
        enchantmentCaps.put(Enchantment.PROJECTILE_PROTECTION, 3);

        // Sharpness (Sharpness V (level 5) is default max, we cap at IV (level 4))
        enchantmentCaps.put(Enchantment.SHARPNESS, 4);

        // Power (Power V (level 5) is default max, we cap at IV (level 4))
        enchantmentCaps.put(Enchantment.POWER, 4);

        // You can add more enchantments here as needed:
        // enchantmentCaps.put(Enchantment.DURABILITY, 2); // Example: Unbreaking capped at II
        // enchantmentCaps.put(Enchantment.LOOT_BONUS_MOBS, 2); // Example: Looting capped at II

        plugin.getLogger().info("Loaded enchantment caps: " + enchantmentCaps.size() + " enchantments capped.");
    }

    // --- Event Listener for Enchanting Tables ---
    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        Map<Enchantment, Integer> enchantsToApply = event.getEnchantsToAdd();
        boolean changed = false;

        for (Map.Entry<Enchantment, Integer> entry : enchantsToApply.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int proposedLevel = entry.getValue();

            if (enchantmentCaps.containsKey(enchantment)) {
                int cap = enchantmentCaps.get(enchantment);
                if (proposedLevel > cap) {
                    // Reduce the enchantment level to the cap
                    enchantsToApply.put(enchantment, cap);
                    changed = true;
                }
            }
        }
        // No need to set event.setEnchantsToAdd() explicitly;
        // modifying the map returned by getEnchantsToAdd() directly affects the outcome.
    }

    // --- Event Listener for Anvils ---
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult(); // The item that *would* be created
        if (result == null || !result.hasItemMeta()) {
            return;
        }

        ItemMeta meta = result.getItemMeta();
        boolean changed = false;

        // Collect all enchantments from the result item
        Map<Enchantment, Integer> currentEnchants = new HashMap<>();
        if (meta instanceof EnchantmentStorageMeta) { // If it's an enchanted book
            currentEnchants.putAll(((EnchantmentStorageMeta) meta).getStoredEnchants());
        } else { // If it's a regular item
            currentEnchants.putAll(meta.getEnchants());
        }

        for (Map.Entry<Enchantment, Integer> entry : currentEnchants.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int currentLevel = entry.getValue();

            if (enchantmentCaps.containsKey(enchantment)) {
                int cap = enchantmentCaps.get(enchantment);
                if (currentLevel > cap) {
                    // Cap the enchantment level
                    currentEnchants.put(enchantment, cap);
                    changed = true;
                }
            }
        }

        if (changed) {
            // Create a new ItemMeta to apply the capped enchantments
            ItemMeta newMeta = result.getItemMeta().clone(); // Clone to preserve other meta

            // Remove all existing enchantments and add the capped ones back
            if (newMeta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) newMeta;
                for (Enchantment ench : bookMeta.getStoredEnchants().keySet()) {
                    bookMeta.removeStoredEnchant(ench);
                }
                for (Map.Entry<Enchantment, Integer> entry : currentEnchants.entrySet()) {
                    bookMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true); // 'true' to ignore level restrictions if needed
                }
            } else {
                for (Enchantment ench : newMeta.getEnchants().keySet()) {
                    newMeta.removeEnchant(ench);
                }
                for (Map.Entry<Enchantment, Integer> entry : currentEnchants.entrySet()) {
                    newMeta.addEnchant(entry.getKey(), entry.getValue(), true); // 'true' to ignore level restrictions
                }
            }

            ItemStack newResult = result.clone();
            newResult.setItemMeta(newMeta);
            event.setResult(newResult); // Set the modified item as the anvil's result
        }
    }
}