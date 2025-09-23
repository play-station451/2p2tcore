package me.playstation451.core2p2t;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Random;

public class EventManager implements Listener {

    private final ConfigManager configManager;
    private final PlayerUtilities playerUtilities;

    public EventManager(ConfigManager configManager, PlayerUtilities playerUtilities) {
        this.configManager = configManager;
        this.playerUtilities = playerUtilities;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (player.isOp()) {
            return;
        }

        boolean isAllowed = false;
        for (String allowedCmd : configManager.getAllowedCommands()) {
            if (command.startsWith(allowedCmd.toLowerCase())) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            player.sendMessage("Command unavailable.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(null);

        List<String> joinMessages = configManager.getJoinMessages();
        if (!joinMessages.isEmpty()) {
            Random random = new Random();
            String message = joinMessages.get(random.nextInt(joinMessages.size()));
            event.setJoinMessage(message.replace("%player%", player.getName()));
        } else {
            event.setJoinMessage(player.getName() + " joined the game.");
        }
        playerUtilities.clearIllegalItems(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null); 

        List<String> leaveMessages = configManager.getLeaveMessages();
        if (!leaveMessages.isEmpty()) {
            Random random = new Random();
            String message = leaveMessages.get(random.nextInt(leaveMessages.size()));
            event.setQuitMessage(message.replace("%player%", player.getName()));
        } else {
            event.setQuitMessage(player.getName() + " left the game."); 
        }
    }
}