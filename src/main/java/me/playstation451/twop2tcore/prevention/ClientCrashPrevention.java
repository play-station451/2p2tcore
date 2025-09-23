package me.playstation451.twop2tcore.prevention;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import me.playstation451.twop2tcore.Core2p2t;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class ClientCrashPrevention implements Listener {

    private final Core2p2t plugin;

    public ClientCrashPrevention(Core2p2t plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        if (!plugin.getConfigManager().isBookCrashPreventionEnabled()) {
            return;
        }

        BookMeta newBookMeta = event.getNewBookMeta();
        List<String> pages = newBookMeta.getPages();

        for (int i = 0; i < pages.size(); i++) {
            String page = pages.get(i);
            if (page.length() > plugin.getConfigManager().getMaxBookPageLength()) {
                pages.set(i, page.substring(0, plugin.getConfigManager().getMaxBookPageLength()));
            }
        }
        newBookMeta.setPages(pages);
        event.setNewBookMeta(newBookMeta);
    }

    @EventHandler
    public void onPlayerOpenSign(PlayerOpenSignEvent event) {
        if (!plugin.getConfigManager().isSignCrashPreventionEnabled()) {
            return;
        }
    }
}