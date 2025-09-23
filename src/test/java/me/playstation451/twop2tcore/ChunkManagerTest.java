package me.playstation451.twop2tcore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.bukkit.entity.Player;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChunkManagerTest {

    private ChunkManager chunkManager;
    private Player mockPlayer;

    @BeforeEach
    void setUp() {
        chunkManager = new ChunkManager();
        mockPlayer = mock(Player.class);
    }

    @Test
    void testAddChunk() {
        chunkManager.addChunk(mockPlayer);
        assertEquals(1, chunkManager.getChunkCount(mockPlayer));
    }

    @Test
    void testRemoveChunk() {
        chunkManager.addChunk(mockPlayer);
        chunkManager.addChunk(mockPlayer);
        chunkManager.removeChunk(mockPlayer);
        assertEquals(1, chunkManager.getChunkCount(mockPlayer));
    }

    @Test
    void testGetChunkCount() {
        assertEquals(0, chunkManager.getChunkCount(mockPlayer));
        chunkManager.addChunk(mockPlayer);
        assertEquals(1, chunkManager.getChunkCount(mockPlayer));
    }

    @Test
    void testGetChunksLoadedByPlayer() {
        assertEquals(0, chunkManager.getChunksLoadedByPlayer(mockPlayer));
        chunkManager.addChunk(mockPlayer);
        assertEquals(1, chunkManager.getChunksLoadedByPlayer(mockPlayer));
    }

    @Test
    void testForceLoadChunk() {

        chunkManager.forceLoadChunk(mockPlayer);
        assertEquals(1, chunkManager.getChunkCount(mockPlayer));
    }
}