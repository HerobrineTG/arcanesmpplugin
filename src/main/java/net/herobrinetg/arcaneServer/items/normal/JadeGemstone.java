package net.herobrinetg.arcaneServer.items.normal;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class JadeGemstone {
    public ItemStack getJadeGemstone() {
        ItemStack jadeGemstone = new ItemStack(Material.EMERALD, 1);
        ItemMeta jadeGemstoneMeta = jadeGemstone.getItemMeta();
        jadeGemstoneMeta.setDisplayName(ChatColor.GREEN + "Jade Gemstone");

        List<String> jadeGemstoneLore = new ArrayList<>();
        jadeGemstoneLore.add("");
        jadeGemstoneLore.add(ChatColor.GRAY + "Originally used by " + ChatColor.MAGIC + "humans");
        jadeGemstoneLore.add(ChatColor.GRAY + "to create some powerful items");
        jadeGemstoneLore.add("");
        jadeGemstoneLore.add(ChatColor.BLUE + "RARE");
        return jadeGemstone;
    }
}
