package me.playstation451.twop2tcore.prevention;

import me.playstation451.twop2tcore.Core2p2t;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkBanPrevention implements Listener {

    private final Core2p2t plugin;
    private final Map<UUID, Long> lastChunkMoveTime = new HashMap<>();
    private final Map<UUID, Location> lastPlayerLocation = new HashMap<>();
    private final Map<Chunk, Integer> chunkPhysicsUpdates = new HashMap<>();
    private final Map<Chunk, Long> lastChunkPhysicsReset = new HashMap<>();

    private final long rapidChunkMoveThresholdMs;
    private final int maxChunkMovesPerThreshold;
    private final int maxPhysicsUpdatesPerChunk;
    private final long physicsResetIntervalMs;
    private final int maxSignLineLength;
    private final int maxBlockNbtSize;

    public ChunkBanPrevention(Core2p2t plugin) {
        this.plugin = plugin;
        this.rapidChunkMoveThresholdMs = plugin.getConfigManager().getRapidChunkMoveThresholdMs();
        this.maxChunkMovesPerThreshold = plugin.getConfigManager().getMaxChunkMovesPerThreshold();
        this.maxPhysicsUpdatesPerChunk = plugin.getConfigManager().getMaxPhysicsUpdatesPerChunk();
        this.physicsResetIntervalMs = plugin.getConfigManager().getPhysicsResetIntervalMs();
        this.maxSignLineLength = plugin.getConfigManager().getMaxSignLineLength();
        this.maxBlockNbtSize = plugin.getConfigManager().getMaxBlockNbtSize();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (!plugin.getConfigManager().isChunkBanPreventionEnabled()) {
            return;
        }

        if (to == null || from.getChunk().equals(to.getChunk())) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        lastPlayerLocation.put(playerId, to);

        if (lastChunkMoveTime.containsKey(playerId)) {
            long lastMove = lastChunkMoveTime.get(playerId);
            if (currentTime - lastMove < rapidChunkMoveThresholdMs) {
                plugin.getLogger().warning("Player " + player.getName() + " is moving between chunks too rapidly!");
                event.setCancelled(true);
                player.sendMessage("§cYou are moving too fast!");
            }
        }
        lastChunkMoveTime.put(playerId, currentTime);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (!plugin.getConfigManager().isChunkBanPreventionEnabled()) {
            return;
        }

        Chunk chunk = event.getBlock().getChunk();
        long currentTime = System.currentTimeMillis();

        chunkPhysicsUpdates.putIfAbsent(chunk, 0);
        lastChunkPhysicsReset.putIfAbsent(chunk, currentTime);

        if (currentTime - lastChunkPhysicsReset.get(chunk) > physicsResetIntervalMs) {
            chunkPhysicsUpdates.put(chunk, 0);
            lastChunkPhysicsReset.put(chunk, currentTime);
        }

        int currentUpdates = chunkPhysicsUpdates.get(chunk);
        chunkPhysicsUpdates.put(chunk, currentUpdates + 1);

        if (currentUpdates > maxPhysicsUpdatesPerChunk) {
            plugin.getLogger().warning("Chunk " + chunk.getX() + "," + chunk.getZ() + " is experiencing excessive physics updates!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfigManager().isChunkBanPreventionEnabled()) {
            return;
        }

        BlockState blockState = event.getBlockPlaced().getState();
        if (blockState instanceof TileState) {
            TileState tileState = (TileState) blockState;
            PersistentDataContainer container = tileState.getPersistentDataContainer();
            
            if (container != null) {
                int nbtSize = getNbtSize(container);
                if (nbtSize > maxBlockNbtSize) {
                    plugin.getLogger().warning("Player " + event.getPlayer().getName() + " attempted to place a block with excessive NBT data (" + nbtSize + " bytes) at " + event.getBlockPlaced().getLocation());
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("§cYou cannot place blocks with excessive data!");
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!plugin.getConfigManager().isChunkBanPreventionEnabled()) {
            return;
        }

        for (String line : event.getLines()) {
            if (line != null && line.length() > maxSignLineLength) {
                plugin.getLogger().warning("Player " + event.getPlayer().getName() + " attempted to create a sign with an excessively long line (" + line.length() + " characters) at " + event.getBlock().getLocation());
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cSign text is too long!");
                return;
            }
        }
    }

    private int getNbtSize(PersistentDataContainer container) {
        int size = 0;
        for (NamespacedKey key : container.getKeys()) {
            if (container.has(key, PersistentDataType.STRING)) {
                size += container.get(key, PersistentDataType.STRING).getBytes().length;
            } else if (container.has(key, PersistentDataType.BYTE_ARRAY)) {
                size += container.get(key, PersistentDataType.BYTE_ARRAY).length;
            }
        }
        return size;
    }
}