package me.playstation451.core2p2t;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.entity.Player;

public final class Core2p2t extends JavaPlugin {

    private ConfigManager configManager;
    private PlayerUtilities playerUtilities;
    private EventManager eventManager;
    private P2tCommandExecutor p2tCommandExecutor;

    @Override
    public void onEnable() {
        getLogger().info("P2tCore has been enabled!");

        this.configManager = new ConfigManager(this);
        this.playerUtilities = new PlayerUtilities(configManager);
        this.eventManager = new EventManager(configManager, playerUtilities);
        this.p2tCommandExecutor = new P2tCommandExecutor(configManager);

        getServer().getPluginManager().registerEvents(eventManager, this);

        
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