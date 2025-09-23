package me.playstation451.twop2tcore.redstone;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.playstation451.twop2tcore.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RedstoneOptimizerListener implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<Chunk, Integer> redstoneUpdatesPerTick = new ConcurrentHashMap<>();

    public RedstoneOptimizerListener(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        startRedstoneUpdateResetTask();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (!configManager.isRedstoneEnabled()) {
            return;
        }

        Block block = event.getBlock();
        Chunk chunk = block.getChunk();

        redstoneUpdatesPerTick.compute(chunk, (k, v) -> (v == null) ? 1 : v + 1);

        if (redstoneUpdatesPerTick.get(chunk) > configManager.getRedstoneMaxUpdatesPerTickPerChunk()) {
            event.setNewCurrent(event.getOldCurrent()); 
            
            
        }
    }

    private void startRedstoneUpdateResetTask() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            redstoneUpdatesPerTick.clear();
        }, 20L, 20L); 
    }
}