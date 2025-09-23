package me.playstation451.twop2tcore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class P2tCommandExecutor implements CommandExecutor {

    private final ConfigManager configManager;

    public P2tCommandExecutor(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("p2t")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender instanceof Player && !sender.isOp()) {
                    sender.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                configManager.loadConfig();
                sender.sendMessage("P2tCore configuration reloaded.");
                return true;
            }
        }
        return false;
    }
}