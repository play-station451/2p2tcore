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
        if (event.getEntityType() == EntityType.TNT) { 
            event.setCancelled(true); 

            Location explosionLocation = event.getLocation();
            List<Block> blocks = event.blockList();
            float yield = event.getYield(); 

            
            processBlocksAsync(explosionLocation, blocks, yield);

            
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

        
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            List<Block> blocksToDestroy = new ArrayList<>(blocks);
            int totalBlocks = blocksToDestroy.size();
            int blocksPerTick = configManager.getExplosionBlocksPerTick(); 

            for (int i = 0; i < totalBlocks; i += blocksPerTick) {
                int endIndex = Math.min(i + blocksPerTick, totalBlocks);
                List<Block> currentBatch = blocksToDestroy.subList(i, endIndex);

                
                
                Bukkit.getRegionScheduler().run(plugin, explosionLocation, batchTask -> {
                    for (Block block : currentBatch) {
                        if (block.getType() != Material.AIR) {
                            
                            List<ItemStack> drops = new ArrayList<>(block.getDrops());
                            block.setType(Material.AIR); 

                            
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
                
                
                
                Vector direction = entity.getLocation().toVector().subtract(explosionLocation.toVector()).normalize();
                entity.setVelocity(direction.multiply(yield * 0.5)); 
            } else {
                
                double distance = entity.getLocation().distance(explosionLocation);
                double damage = (1 - (distance / (yield * 2))) * yield * 4; 
                if (damage > 0) {
                    
                    Bukkit.getRegionScheduler().run(plugin, entity.getLocation(), task -> {
                        if (entity instanceof org.bukkit.entity.LivingEntity) { 
                            ((org.bukkit.entity.LivingEntity) entity).damage(damage); 
                        }
                    });
                }
            }
        }
    }
}