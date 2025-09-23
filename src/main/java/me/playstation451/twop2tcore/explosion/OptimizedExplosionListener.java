package me.playstation451.twop2tcore.explosion;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.playstation451.twop2tcore.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OptimizedExplosionListener implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    public OptimizedExplosionListener(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntityType() == EntityType.TNT) { // Corrected to EntityType.TNT
            event.setCancelled(true); // Cancel the vanilla explosion to handle it ourselves

            Location explosionLocation = event.getLocation();
            List<Block> blocks = event.blockList();
            float yield = event.getYield(); // Explosion power

            // Asynchronously process block destruction and drops
            processBlocksAsync(explosionLocation, blocks, yield);

            // Manually find entities within the explosion radius and handle damage/physics
            List<Entity> affectedEntities = new ArrayList<>();
            explosionLocation.getWorld().getNearbyEntities(explosionLocation, yield * 2, yield * 2, yield * 2)
                    .forEach(entity -> {
                        if (entity.getLocation().distance(explosionLocation) <= yield * 2) {
                            affectedEntities.add(entity);
                        }
                    });
            processEntities(explosionLocation, affectedEntities, yield);
        }
    }

    private void processBlocksAsync(Location explosionLocation, List<Block> blocks, float yield) {
        if (blocks.isEmpty()) {
            return;
        }

        // Schedule block processing on the global region scheduler for async execution
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            List<Block> blocksToDestroy = new ArrayList<>(blocks);
            int totalBlocks = blocksToDestroy.size();
            int blocksPerTick = configManager.getExplosionBlocksPerTick(); // Assuming this is configurable

            for (int i = 0; i < totalBlocks; i += blocksPerTick) {
                int endIndex = Math.min(i + blocksPerTick, totalBlocks);
                List<Block> currentBatch = blocksToDestroy.subList(i, endIndex);

                // Schedule each batch to be processed on the main thread (or block's region)
                // This is crucial because block manipulation must happen on the main thread/region
                Bukkit.getRegionScheduler().run(plugin, explosionLocation, batchTask -> {
                    for (Block block : currentBatch) {
                        if (block.getType() != Material.AIR) {
                            // Calculate drops
                            List<ItemStack> drops = new ArrayList<>(block.getDrops());
                            block.setType(Material.AIR); // Destroy block

                            // Drop items
                            for (ItemStack drop : drops) {
                                block.getWorld().dropItemNaturally(block.getLocation(), drop);
                            }
                        }
                    }
                });
            }
        });
    }

    private void processEntities(Location explosionLocation, List<Entity> entities, float yield) {
        for (Entity entity : entities) {
            if (entity.getType() == EntityType.FALLING_BLOCK) {
                // Streamline physics for falling blocks - maybe apply a strong initial impulse
                // and let vanilla physics handle the rest, or custom handle if performance is an issue.
                // For now, just apply a strong impulse.
                Vector direction = entity.getLocation().toVector().subtract(explosionLocation.toVector()).normalize();
                entity.setVelocity(direction.multiply(yield * 0.5)); // Adjust multiplier as needed
            } else {
                // Optimized entity damage calculation
                double distance = entity.getLocation().distance(explosionLocation);
                double damage = (1 - (distance / (yield * 2))) * yield * 4; // Simplified damage calculation
                if (damage > 0) {
                    // Schedule damage application on the entity's region
                    Bukkit.getRegionScheduler().run(plugin, entity.getLocation(), task -> {
                        if (entity instanceof org.bukkit.entity.LivingEntity) { // Cast to LivingEntity for damage method
                            ((org.bukkit.entity.LivingEntity) entity).damage(damage); // Removed event.getEntity() as it's not in scope
                        }
                    });
                }
            }
        }
    }
}