package me.playstation451.twop2tcore;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.Map;
import java.util.Set;

public class PlayerUtilities {

    private final ConfigManager configManager;
    private final ChunkManager chunkManager;

    public PlayerUtilities(ConfigManager configManager, ChunkManager chunkManager) {
        this.configManager = configManager;
        this.chunkManager = chunkManager;
    }

    public void checkAndDeopPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
                if (!configManager.getAllowedOPs().contains(player.getName())) {
                    player.setOp(false);
                    Bukkit.getLogger().info("De-opped player " + player.getName() + " as they are not in the AllowedOPs list.");
                }
            }
        }
    }

    public void clearIllegalItems(Player player) {
        if (configManager.getAllowedOPs().contains(player.getName())) {
            return; 
        }

        Set<Material> illegalMaterials = configManager.getIllegalMaterials();
        Map<Enchantment, Integer> enchantmentLimits = configManager.getEnchantmentLimits();

        player.getInventory().forEach(item -> {
            if (item == null) {
                return;
            }

            boolean isIllegal = false;

            
            if (illegalMaterials.contains(item.getType())) {
                isIllegal = true;
            }

            
            for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                if (enchantmentLimits.containsKey(enchantment) && level > enchantmentLimits.get(enchantment)) {
                    isIllegal = true;
                    break;
                }
            }

            if (isIllegal) {
                player.getInventory().remove(item);
                Bukkit.getLogger().info("Removed illegal item " + item.getType() + " from " + player.getName() + "'s inventory.");
                player.sendMessage("An illegal item was removed from your inventory.");
            }
        });
    }
    public boolean isChunkLoadingLimitExceeded(Player player) {
        int maxChunksPerPlayer = configManager.getMaxChunksPerPlayer();
        int currentChunksLoaded = this.chunkManager.getChunksLoadedByPlayer(player);
        return currentChunksLoaded >= maxChunksPerPlayer;
    }

    public void forceLoadChunk(Player player) {
        if (isChunkLoadingLimitExceeded(player)) {
            player.sendMessage("You have reached the maximum number of chunks you can force load.");
            return;
        }
        this.chunkManager.forceLoadChunk(player);
        player.sendMessage("Chunk loaded successfully.");
    }
}