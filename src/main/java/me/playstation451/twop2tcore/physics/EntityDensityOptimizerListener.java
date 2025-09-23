package me.playstation451.twop2tcore.physics;

import me.playstation451.twop2tcore.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EntityDensityOptimizerListener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    public EntityDensityOptimizerListener(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        startEntityDensityCheckTask();
    }

    private void startEntityDensityCheckTask() {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            if (!configManager.isEntityDensityEnabled()) {
                return;
            }

            for (org.bukkit.World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    Bukkit.getRegionScheduler().run(plugin, chunk.getBlock(0, 0, 0).getLocation(), chunkTask -> {
                        List<Entity> entitiesInChunk = chunk.getEntities() == null ? List.of() : List.of(chunk.getEntities());

                        List<Entity> relevantEntities = entitiesInChunk.stream()
                                .filter(entity -> !configManager.getEntityDensityIgnoredEntityTypes().contains(entity.getType().name()))
                                .collect(Collectors.toList());

                        if (relevantEntities.size() > configManager.getEntityDensityMaxEntitiesPerChunk()) {
                            int entitiesToRemove = relevantEntities.size() - configManager.getEntityDensityMaxEntitiesPerChunk();
                            
                            relevantEntities.sort((e1, e2) -> Integer.compare(e2.getTicksLived(), e1.getTicksLived()));

                            for (int i = 0; i < entitiesToRemove && i < relevantEntities.size(); i++) {
                                relevantEntities.get(i).remove();
                            }
                            
                        }
                    });
                }
            }
        }, (long) 20 * configManager.getEntityDensityDespawnIntervalSeconds(), (long) 20 * configManager.getEntityDensityDespawnIntervalSeconds());
    }
}