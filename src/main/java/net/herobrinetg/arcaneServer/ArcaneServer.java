package net.herobrinetg.arcaneServer;

import net.herobrinetg.arcaneServer.commands.ArcaneServerCommandExecutor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import net.herobrinetg.arcaneServer.recipes.CustomRecipeManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArcaneServer extends JavaPlugin {

    private ServerEventsRegisterer serverEvents;
    private NamespacedKey witherBladeKey;
    private NamespacedKey dragonMaceKey;
    private CustomRecipeManager recipeManager;
    private boolean witherBoneBladeExists = false;
    private boolean dragonMaceExists = false;
    public int currentNormalMaceCount;

    @Override
    public void onEnable() {
        // Load configuration to get previous status of legendary items
        saveDefaultConfig(); // Creates config.yml if it doesn't exist
        loadLegendaryStatus(); // Loads existing status from config

        witherBladeKey = new NamespacedKey(this, "wither_bone_blade_id");
        dragonMaceKey = new NamespacedKey(this, "dragon_mace_id");

        getLogger().info("NamespacedKeys initialized for items.");

        // --- Register Events and Listeners from RegisterEvents.java
        this.serverEvents = new ServerEventsRegisterer(this);
        this.serverEvents.registerEvents();

        // --- Register Admin Commands ---
        this.getCommand("arcaneserver").setExecutor(new ArcaneServerCommandExecutor(this)); // Register this class as the command executor
        getLogger().info("Admin command /arcaneserver registered.");

        getLogger().info("ArcaneServer enabled!");
    }

    @Override
    public void onDisable() {
        if (recipeManager != null) {
            recipeManager.unregisterRecipes();
        }
        getConfig().set("normal-mace-count", this.currentNormalMaceCount);
        saveConfig();
        getLogger().info("Saved final normal Mace count: " + this.currentNormalMaceCount);

        saveLegendaryStatus(); // Save status of legendary items on disable
        getLogger().info("ArcaneServer disabled!");
    }

    // --- Persistence Methods for Legendary Item Status ---
    private void loadLegendaryStatus() {
        this.witherBoneBladeExists = getConfig().getBoolean("legendaries.witherBoneBladeExists", false);
        this.dragonMaceExists = getConfig().getBoolean("legendaries.dragonMaceExists", false);
        getLogger().info("Loaded legendary status: Wither Bone Blade: " + witherBoneBladeExists + ", Dragon Mace: " + dragonMaceExists);
    }

    private void saveLegendaryStatus() {
        getConfig().set("legendaries.witherBoneBladeExists", this.witherBoneBladeExists);
        getConfig().set("legendaries.dragonMaceExists", this.dragonMaceExists);
        saveConfig();
        getLogger().info("Saved legendary status.");
    }

    public int getNormalMaceCount() {
        return this.currentNormalMaceCount;
    }

    public void incrementNormalMaceCount() {
        this.currentNormalMaceCount++;
        saveConfig();
    }

    public void decrementNormalMaceCount() {
        if (this.currentNormalMaceCount > 0) {
            this.currentNormalMaceCount--;
        }
        saveConfig();
    }

    // --- Getters for Legendary Item Status ---
    public boolean getWitherBoneBladeExists() {
        return witherBoneBladeExists;
    }

    public boolean getDragonMaceExists() {
        return dragonMaceExists;
    }

    // --- Setters for Legendary Item Status (called by CustomRecipeManager) ---
    public void setWitherBoneBladeExists(boolean status) {
        this.witherBoneBladeExists = status;
        saveLegendaryStatus(); // Save immediately when status changes
    }

    public void setDragonMaceExists(boolean status) {
        this.dragonMaceExists = status;
        saveLegendaryStatus(); // Save immediately when status changes
    }

    // --- Helper methods to get custom item instances (remain here as they are used by recipes and commands) ---
    public NamespacedKey getWitherBladeKey() {
        return witherBladeKey;
    }

    public NamespacedKey getDragonMaceKey() {
        return dragonMaceKey;
    }

    public ItemStack getWitherBoneBlade() {
        ItemStack witherBoneBlade = new ItemStack(Material.NETHERITE_SWORD, 1);
        ItemMeta witherBoneBladeMeta = witherBoneBlade.getItemMeta();

        witherBoneBladeMeta.setDisplayName(ChatColor.RED + "Wither Bone Blade");

        List<String> witherBoneBladeLore = new ArrayList<>();
        witherBoneBladeLore.add(" ");
        witherBoneBladeLore.add(ChatColor.YELLOW + "Ability: " + ChatColor.DARK_PURPLE + "Wither's Fury " + ChatColor.YELLOW + "" + ChatColor.BOLD + "RIGHT CLICK");
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
        return witherBoneBlade;
    }

    public ItemStack getDragonMace() {
        ItemStack dragonMace = new ItemStack(Material.MACE, 1);
        ItemMeta dragonMaceMeta = dragonMace.getItemMeta();

        dragonMaceMeta.setDisplayName(ChatColor.RED + "Dragon Mace");

        List<String> dragonMaceLore = new ArrayList<>();
        dragonMaceLore.add(" ");
        dragonMaceLore.add(ChatColor.YELLOW + "Ability: " + ChatColor.DARK_PURPLE + "Smash, Dash! " + ChatColor.YELLOW + "" + ChatColor.BOLD + "RIGHT CLICK");
        dragonMaceLore.add(ChatColor.GRAY + "Leap and dash up to 15 blocks in front of you.");
        dragonMaceLore.add(ChatColor.GRAY + "Cooldown: 2 minutes");
        dragonMaceLore.add(" ");
        dragonMaceLore.add(ChatColor.GRAY + "Dang this thing is heavy..");
        dragonMaceLore.add(" ");
        dragonMaceLore.add(ChatColor.RED + "" + ChatColor.BOLD + "LEGENDARY");

        dragonMaceMeta.setLore(dragonMaceLore);
        dragonMaceMeta.setCustomModelData(1);
        dragonMaceMeta.addEnchant(Enchantment.DENSITY, 7, true);

        dragonMaceMeta.getPersistentDataContainer().set(dragonMaceKey, PersistentDataType.BYTE, (byte) 1);
        dragonMace.setItemMeta(dragonMaceMeta);
        return dragonMace;
    }
}