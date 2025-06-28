package net.herobrinetg.arcaneServer.recipes;

import net.herobrinetg.arcaneServer.ArcaneServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class CustomRecipeManager implements Listener {

    private final ArcaneServer plugin;
    private final NamespacedKey witherBoneBladeRecipeKey;
    private final NamespacedKey dragonMaceRecipeKey;

    private final ItemStack witherBoneBladeResult;
    private final ItemStack dragonMaceResult;

    public CustomRecipeManager(ArcaneServer plugin) {
        this.plugin = plugin;
        this.witherBoneBladeRecipeKey = new NamespacedKey(plugin, "wither_bone_blade_recipe");
        this.dragonMaceRecipeKey = new NamespacedKey(plugin, "dragon_mace_recipe");

        this.witherBoneBladeResult = plugin.getWitherBoneBlade();
        this.dragonMaceResult = plugin.getDragonMace();
    }

    public void registerRecipes() {
        createWitherBoneBladeRecipe();
        createDragonMaceRecipe();
        plugin.getLogger().info("Custom recipes registered.");
    }

    public void unregisterRecipes() {
        Bukkit.removeRecipe(witherBoneBladeRecipeKey);
        Bukkit.removeRecipe(dragonMaceRecipeKey);
        plugin.getLogger().info("Custom recipes unregistered.");
    }

    private void createWitherBoneBladeRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(witherBoneBladeRecipeKey, witherBoneBladeResult);

        recipe.shape(
                "WWW",
                "NSN",
                " X "
        );

        recipe.setIngredient('W', Material.WITHER_SKELETON_SKULL);
        recipe.setIngredient('N', Material.NETHER_STAR);
        recipe.setIngredient('S', Material.NETHERITE_SWORD);
        recipe.setIngredient('X', Material.BLAZE_ROD);

        Bukkit.addRecipe(recipe);
    }

    private void createDragonMaceRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(dragonMaceRecipeKey, dragonMaceResult);

        recipe.shape(
                "DDD",
                "CPC",
                " X "
        );

        recipe.setIngredient('D', Material.DRAGON_BREATH);
        recipe.setIngredient('C', Material.ENDER_EYE);
        recipe.setIngredient('P', Material.DRAGON_EGG);
        recipe.setIngredient('X', Material.MACE);

        Bukkit.addRecipe(recipe);
    }

    // --- Corrected Event Listener for PREPARING Crafting (before taking item) ---
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack result = inventory.getResult();

        if (result == null || !result.hasItemMeta()) {
            return;
        }

        ItemMeta resultMeta = result.getItemMeta();
        boolean isWitherBlade = resultMeta.getPersistentDataContainer().has(plugin.getWitherBladeKey(), PersistentDataType.BYTE);
        boolean isDragonMace = resultMeta.getPersistentDataContainer().has(plugin.getDragonMaceKey(), PersistentDataType.BYTE);

        // --- CORRECTED WAY TO GET CRAFTER IN PrepareItemCraftEvent ---
        HumanEntity humanCrafter = event.getView().getPlayer();
        Player crafter = null;
        if (humanCrafter instanceof Player) {
            crafter = (Player) humanCrafter;
        }
        // --- END CORRECTION ---

        // --- ENFORCE "ONLY ONE EXISTS" RULE ---
        if (isWitherBlade && plugin.getWitherBoneBladeExists()) {
            if (isValidWitherBoneBladeRecipe(inventory.getMatrix())) {
                inventory.setResult(null);
                if (crafter != null) crafter.sendMessage(ChatColor.RED + "Only one Wither Bone Blade can exist on the server at a time!");
            }
            return;
        }
        if (isDragonMace && plugin.getDragonMaceExists()) {
            if (isValidDragonMaceRecipe(inventory.getMatrix())) {
                inventory.setResult(null);
                if (crafter != null) crafter.sendMessage(ChatColor.RED + "Only one Dragon Mace can exist on the server at a time!");
            }
            return;
        }

        // --- Manual Quantity/Exact Match Check for VALID crafts ---
        boolean recipeValid = true;

        if (isWitherBlade) {
            recipeValid = isValidWitherBoneBladeRecipe(inventory.getMatrix());
        } else if (isDragonMace) {
            recipeValid = isValidDragonMaceRecipe(inventory.getMatrix());
        }

        if (!recipeValid) {
            inventory.setResult(null);
        }
    }

    // --- Event Listener for when a player TAKES the crafted item ---
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack craftedItem = event.getCurrentItem();

        HumanEntity humanCrafter = event.getWhoClicked(); // getWhoClicked() works here as CraftItemEvent extends InventoryClickEvent
        Player crafter = null;
        if (humanCrafter instanceof Player) {
            crafter = (Player) humanCrafter;
        }

        if (craftedItem == null || !craftedItem.hasItemMeta() || crafter == null) {
            plugin.getLogger().warning("DEBUG: CraftItemEvent: Invalid crafted item, no meta, or crafter is not a player. Ignoring event.");
            return;
        }

        ItemMeta craftedMeta = craftedItem.getItemMeta();
        boolean isWitherBlade = craftedMeta.getPersistentDataContainer().has(plugin.getWitherBladeKey(), PersistentDataType.BYTE);
        boolean isDragonMace = craftedMeta.getPersistentDataContainer().has(plugin.getDragonMaceKey(), PersistentDataType.BYTE);

        if (craftedItem.getType() == Material.MACE && !isDragonMace) {
            final int MAX_NORMAL_MACES = 2; // Set your desired limit here

            if (plugin.getNormalMaceCount() >= MAX_NORMAL_MACES) {
                event.setCancelled(true); // Prevent the craft
                crafter.sendMessage(ChatColor.RED + "Only " + MAX_NORMAL_MACES + " Maces can exist on the server at a time!");
                plugin.getLogger().info("DEBUG: Prevented " + crafter.getName() + " from crafting a normal Mace (count " + plugin.getNormalMaceCount() + ").");
                // No need to manually consume ingredients as event is cancelled
                return; // Stop processing this event
            } else {
                // Allow the craft, and increment the count
                plugin.incrementNormalMaceCount();
                plugin.getLogger().info("DEBUG: " + crafter.getName() + " crafted a normal Mace. New count: " + plugin.getNormalMaceCount());
                // Let Bukkit handle the rest (consuming ingredients, adding item to inventory)
                return; // Stop processing this event, it's a vanilla craft
            }
        }

        // --- Only apply manual handling for our custom items ---
        if (isWitherBlade || isDragonMace) {
            event.setCancelled(true); // <--- CRUCIAL: Cancel the default craft behavior

            plugin.getLogger().info("DEBUG: CraftItemEvent triggered by " + crafter.getName() + " for custom item: " + craftedItem.getType().name());

            // --- Manually add the item to the player's inventory ---
            HashMap<Integer, ItemStack> remainingItems = crafter.getInventory().addItem(craftedItem);
            if (!remainingItems.isEmpty()) {
                for (ItemStack item : remainingItems.values()) {
                    crafter.getWorld().dropItemNaturally(crafter.getLocation(), item);
                    plugin.getLogger().warning("DEBUG: Crafted item could not fit in inventory for " + crafter.getName() + ". Dropped on ground.");
                }
            } else {
                plugin.getLogger().info("DEBUG: Custom item successfully added to " + crafter.getName() + "'s inventory.");
            }

            // --- Manually consume ingredients from the crafting grid ---
            CraftingInventory inv = event.getInventory();
            ItemStack[] matrix = inv.getMatrix();

            boolean isActualWitherBladeRecipe = isValidWitherBoneBladeRecipe(matrix);
            boolean isActualDragonMaceRecipe = isValidDragonMaceRecipe(matrix);

            if (isActualWitherBladeRecipe || isActualDragonMaceRecipe) {
                Map<Integer, Map.Entry<Material, Integer>> consumedSlots = isActualWitherBladeRecipe ? getWitherBoneBladeRecipeMap() : getDragonMaceRecipeMap();
                for (Map.Entry<Integer, Map.Entry<Material, Integer>> entry : consumedSlots.entrySet()) {
                    int slotIndex = entry.getKey();
                    if (matrix[slotIndex] != null && matrix[slotIndex].getType() != Material.AIR) {
                        matrix[slotIndex].subtract(1);
                    }
                }
                inv.setResult(null); // Clear the output slot after manual consumption
                inv.setMatrix(matrix); // Update the crafting grid
                plugin.getLogger().info("DEBUG: Crafting ingredients consumed from grid.");
            } else {
                plugin.getLogger().warning("DEBUG: Ingredients not consumed - matrix did not match custom recipe.");
            }

            // --- Update existence flag and broadcast ---
            if (isWitherBlade) {
                plugin.setWitherBoneBladeExists(true);
                Bukkit.broadcastMessage(ChatColor.GOLD + crafter.getName() + " has forged the legendary " +
                        ChatColor.RED + ChatColor.BOLD + "Wither Bone Blade" + ChatColor.GOLD + "!");
                plugin.getLogger().info(crafter.getName() + " crafted Wither Bone Blade. Status updated to 'exists'.");
            } else if (isDragonMace) {
                plugin.setDragonMaceExists(true);
                Bukkit.broadcastMessage(ChatColor.GOLD + crafter.getName() + " has forged the legendary " +
                        ChatColor.RED + ChatColor.BOLD + "Dragon Mace" + ChatColor.GOLD + "!");
                plugin.getLogger().info(crafter.getName() + " crafted Dragon Mace. Status updated to 'exists'.");
            }
        }
    }

    // --- Helper methods to check exact recipe match & get recipe maps ---

    private Map<Integer, Map.Entry<Material, Integer>> getWitherBoneBladeRecipeMap() {
        Map<Integer, Map.Entry<Material, Integer>> recipeMap = new HashMap<>();
        recipeMap.put(0, new AbstractMap.SimpleEntry<>(Material.WITHER_SKELETON_SKULL, 1));
        recipeMap.put(1, new AbstractMap.SimpleEntry<>(Material.WITHER_SKELETON_SKULL, 1));
        recipeMap.put(2, new AbstractMap.SimpleEntry<>(Material.WITHER_SKELETON_SKULL, 1));
        recipeMap.put(3, new AbstractMap.SimpleEntry<>(Material.NETHER_STAR, 1));
        recipeMap.put(4, new AbstractMap.SimpleEntry<>(Material.NETHERITE_SWORD, 1));
        recipeMap.put(5, new AbstractMap.SimpleEntry<>(Material.NETHER_STAR, 1));
        recipeMap.put(7, new AbstractMap.SimpleEntry<>(Material.BLAZE_ROD, 1));
        return recipeMap;
    }

    private boolean isValidWitherBoneBladeRecipe(ItemStack[] matrix) {
        return checkMatrix(matrix, getWitherBoneBladeRecipeMap());
    }

    private Map<Integer, Map.Entry<Material, Integer>> getDragonMaceRecipeMap() {
        Map<Integer, Map.Entry<Material, Integer>> recipeMap = new HashMap<>();
        recipeMap.put(0, new AbstractMap.SimpleEntry<>(Material.DRAGON_BREATH, 1));
        recipeMap.put(1, new AbstractMap.SimpleEntry<>(Material.DRAGON_BREATH, 1));
        recipeMap.put(2, new AbstractMap.SimpleEntry<>(Material.DRAGON_BREATH, 1));
        recipeMap.put(3, new AbstractMap.SimpleEntry<>(Material.ENDER_EYE, 1));
        recipeMap.put(4, new AbstractMap.SimpleEntry<>(Material.DRAGON_EGG, 1));
        recipeMap.put(5, new AbstractMap.SimpleEntry<>(Material.ENDER_EYE, 1));
        recipeMap.put(7, new AbstractMap.SimpleEntry<>(Material.MACE, 1));
        return recipeMap;
    }

    private boolean isValidDragonMaceRecipe(ItemStack[] matrix) {
        return checkMatrix(matrix, getDragonMaceRecipeMap());
    }

    private boolean checkMatrix(ItemStack[] matrix, Map<Integer, Map.Entry<Material, Integer>> requiredIngredients) {
        for (int i = 0; i < matrix.length; i++) {
            ItemStack item = matrix[i];
            if (requiredIngredients.containsKey(i)) {
                Map.Entry<Material, Integer> required = requiredIngredients.get(i);
                if (item == null || item.getType() != required.getKey() || item.getAmount() < required.getValue()) {
                    return false;
                }
            } else {
                if (item != null && item.getType() != Material.AIR) {
                    return false;
                }
            }
        }
        return true;
    }
}