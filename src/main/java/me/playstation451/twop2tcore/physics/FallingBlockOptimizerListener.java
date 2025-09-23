package me.playstation451.twop2tcore.physics;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.playstation451.twop2tcore.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FallingBlockOptimizerListener implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<Chunk, Integer> fallingBlocksInChunk = new ConcurrentHashMap<>();

    public FallingBlockOptimizerListener(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        startFallingBlockDespawnTask();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!configManager.isFallingBlocksEnabled()) {
            return;
        }

        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            Chunk chunk = event.getBlock().getChunk();
            fallingBlocksInChunk.compute(chunk, (k, v) -> (v == null) ? 1 : v + 1);

            if (fallingBlocksInChunk.get(chunk) > configManager.getFallingBlocksMaxPerChunk()) {
                event.setCancelled(true);
                event.getEntity().remove(); 
                
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        if (!configManager.isFallingBlocksEnabled()) {
            return;
        }

        
        
        Chunk chunk = event.getBlock().getChunk();
        fallingBlocksInChunk.computeIfPresent(chunk, (k, v) -> v - 1);
        fallingBlocksInChunk.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    private void startFallingBlockDespawnTask() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            if (!configManager.isFallingBlocksEnabled()) {
                return;
            }
            for (Chunk chunk : fallingBlocksInChunk.keySet()) {
                Bukkit.getRegionScheduler().run(plugin, chunk.getBlock(0, 0, 0).getLocation(), chunkTask -> {
                    AtomicInteger removedCount = new AtomicInteger(0);
                    for (Entity entity : chunk.getEntities()) {
                        if (entity.getType() == EntityType.FALLING_BLOCK) {
                            if (entity.getTicksLived() > configManager.getFallingBlocksDespawnDelaySeconds() * 20) { 
                                entity.remove();
                                removedCount.incrementAndGet();
                            }
                        }
                    }
                    if (removedCount.get() > 0) {
                        fallingBlocksInChunk.computeIfPresent(chunk, (k, v) -> v - removedCount.get());
                        fallingBlocksInChunk.entrySet().removeIf(entry -> entry.getValue() <= 0);
                    }
                });
            }
        }, (long) 20 * configManager.getFallingBlocksDespawnDelaySeconds(), (long) 20 * configManager.getFallingBlocksDespawnDelaySeconds());
    }
}