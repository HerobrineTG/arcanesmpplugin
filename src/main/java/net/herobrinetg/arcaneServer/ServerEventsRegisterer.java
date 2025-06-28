package net.herobrinetg.arcaneServer;

import net.herobrinetg.arcaneServer.items.legendary.DragonMace;
import net.herobrinetg.arcaneServer.items.legendary.WitherBoneBlade;
import net.herobrinetg.arcaneServer.listeners.EnchantmentCapListener;
import net.herobrinetg.arcaneServer.listeners.PlayerLoginListener;
import net.herobrinetg.arcaneServer.recipes.CustomRecipeManager;

public class ServerEventsRegisterer {

    private ArcaneServer server;

    public ServerEventsRegisterer(ArcaneServer server) {
        this.server = server;
    }

    public void registerEvents() {

        // --- Initialize and Register Managers/Listeners ---
        server.currentNormalMaceCount = server.getConfig().getInt("normal-mace-count", 0);
        server.getLogger().info("Loaded normal Mace count: " + server.currentNormalMaceCount);

        // WitherBoneBlade
        WitherBoneBlade witherBoneBladeHandler = new WitherBoneBlade(server);
        server.getServer().getPluginManager().registerEvents(witherBoneBladeHandler, server);
        server.getCommand("givewitherblade").setExecutor(witherBoneBladeHandler);
        server.getLogger().info("WitherBoneBlade command and listener registered.");

        // DragonMace
        DragonMace dragonMaceHandler = new DragonMace(server);
        server.getServer().getPluginManager().registerEvents(dragonMaceHandler, server);
        server.getCommand("givedragonmace").setExecutor(dragonMaceHandler);
        server.getLogger().info("DragonMace command and listener registered.");

        // EnchantmentCapListener
        EnchantmentCapListener enchantmentCapListener = new EnchantmentCapListener(server);
        server.getServer().getPluginManager().registerEvents(enchantmentCapListener, server);
        server.getLogger().info("EnchantmentCapListener registered.");

        PlayerLoginListener playerLoginListener = new PlayerLoginListener();
        server.getServer().getPluginManager().registerEvents(playerLoginListener, server);
        server.getLogger().info("Player Login Listener registered");

        // Custom Recipes - Initialize and register through the manager
        CustomRecipeManager recipeManager = new CustomRecipeManager(server);
        recipeManager.registerRecipes();
        server.getServer().getPluginManager().registerEvents(recipeManager, server); // Register as a listener!
        server.getLogger().info("Custom recipes added via CustomRecipeManager and registered as listener.");
    }
}
