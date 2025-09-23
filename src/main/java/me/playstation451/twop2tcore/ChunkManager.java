package me.playstation451.twop2tcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private final Map<Player, Integer> playerChunkCounts;

    public ChunkManager() {
        this.playerChunkCounts = new HashMap<>();
    }

    public int getChunksLoadedByPlayer(Player player) {
        return playerChunkCounts.getOrDefault(player, 0);
    }

    public void forceLoadChunk(Player player) {
        addChunk(player);
        Bukkit.getLogger().info("Chunk loaded for player " + player.getName());
    }

    public void addChunk(Player player) {
        playerChunkCounts.put(player, playerChunkCounts.getOrDefault(player, 0) + 1);
    }

    public void removeChunk(Player player) {
        playerChunkCounts.put(player, playerChunkCounts.getOrDefault(player, 0) - 1);
    }

    public int getChunkCount(Player player) {
        return playerChunkCounts.getOrDefault(player, 0);
    }
}