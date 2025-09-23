package me.playstation451.twop2tcore.physics;

import me.playstation451.twop2tcore.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FluidFlowOptimizerListener implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<Chunk, Integer> fluidUpdatesPerTick = new ConcurrentHashMap<>();

    public FluidFlowOptimizerListener(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        startFluidUpdateResetTask();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!configManager.isFluidFlowEnabled()) {
            return;
        }

        Material blockType = event.getBlock().getType();

        if (configManager.isDisableWaterFlow() && blockType == Material.WATER) {
            event.setCancelled(true);
            return;
        }

        if (configManager.isDisableLavaFlow() && blockType == Material.LAVA) {
            event.setCancelled(true);
            return;
        }

        Chunk chunk = event.getToBlock().getChunk();
        fluidUpdatesPerTick.compute(chunk, (k, v) -> (v == null) ? 1 : v + 1);

        if (fluidUpdatesPerTick.get(chunk) > configManager.getFluidFlowMaxUpdatesPerTickPerChunk()) {
            event.setCancelled(true);
            
        }
    }

    private void startFluidUpdateResetTask() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            fluidUpdatesPerTick.clear();
        }, 20L, 20L); 
    }
}