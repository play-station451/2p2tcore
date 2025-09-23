package me.playstation451.twop2tcore;

import me.playstation451.twop2tcore.explosion.OptimizedExplosionListener;
import me.playstation451.twop2tcore.physics.FallingBlockOptimizerListener;
import me.playstation451.twop2tcore.physics.FluidFlowOptimizerListener;
import me.playstation451.twop2tcore.physics.EntityDensityOptimizerListener;
import me.playstation451.twop2tcore.redstone.RedstoneOptimizerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.entity.Player;

public final class Core2p2t extends JavaPlugin {

    private ConfigManager configManager;
    private PlayerUtilities playerUtilities;
    private EventManager eventManager;
    private P2tCommandExecutor p2tCommandExecutor;
    private ChunkManager chunkManager;

    @Override
    public void onEnable() {
        getLogger().info("P2tCore has been enabled!");

        this.configManager = new ConfigManager(this);
        this.chunkManager = new ChunkManager();
        this.playerUtilities = new PlayerUtilities(configManager, chunkManager);
        this.eventManager = new EventManager(configManager, playerUtilities);
        this.p2tCommandExecutor = new P2tCommandExecutor(configManager);

        getServer().getPluginManager().registerEvents(eventManager, this);
        getServer().getPluginManager().registerEvents(new OptimizedExplosionListener(this, configManager), this);
        getServer().getPluginManager().registerEvents(new RedstoneOptimizerListener(this, configManager), this);
        getServer().getPluginManager().registerEvents(new FallingBlockOptimizerListener(this, configManager), this);
        getServer().getPluginManager().registerEvents(new FluidFlowOptimizerListener(this, configManager), this);
        
        new EntityDensityOptimizerListener(this, configManager);

        
        Bukkit.getScheduler().runTaskTimer(this, playerUtilities::checkAndDeopPlayers, 0L, 6000L);
        
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerUtilities.clearIllegalItems(player);
            }
        }, 0L, 6000L);

        getCommand("p2t").setExecutor(p2tCommandExecutor);
    }

    @Override
    public void onDisable() {
        getLogger().info("P2tCore has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}