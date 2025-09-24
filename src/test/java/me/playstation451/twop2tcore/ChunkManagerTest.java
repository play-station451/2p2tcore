package me.playstation451.twop2tcore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChunkManagerTest {

    private ChunkManager chunkManager;
    private Player mockPlayer;
    private MockedStatic<Bukkit> mockedBukkit;
    private Server mockServer;

    @BeforeEach
    void setUp() {
        mockServer = mock(Server.class);
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mockServer);
        mockedBukkit.when(Bukkit::getLogger).thenReturn(java.util.logging.Logger.getGlobal());
        when(mockServer.getLogger()).thenReturn(java.util.logging.Logger.getGlobal());

        chunkManager = new ChunkManager();
        mockPlayer = mock(Player.class);
    }

    @AfterEach
    void tearDown() {
        mockedBukkit.close();
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