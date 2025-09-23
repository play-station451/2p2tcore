package me.playstation451.twop2tcore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.bukkit.entity.Player;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlayerUtilitiesTest {

    private PlayerUtilities playerUtilities;
    private ConfigManager mockConfigManager;
    private ChunkManager mockChunkManager;
    private Player mockPlayer;

    @BeforeEach
    void setUp() {
        mockConfigManager = mock(ConfigManager.class);
        mockChunkManager = mock(ChunkManager.class);
        playerUtilities = new PlayerUtilities(mockConfigManager, mockChunkManager);
        mockPlayer = mock(Player.class);
    }

    @Test
    void testIsChunkLoadingLimitExceeded_underLimit() {
        when(mockConfigManager.getMaxChunksPerPlayer()).thenReturn(10);
        when(mockChunkManager.getChunksLoadedByPlayer(mockPlayer)).thenReturn(5);
        assertFalse(playerUtilities.isChunkLoadingLimitExceeded(mockPlayer));
    }

    @Test
    void testIsChunkLoadingLimitExceeded_atLimit() {
        when(mockConfigManager.getMaxChunksPerPlayer()).thenReturn(10);
        when(mockChunkManager.getChunksLoadedByPlayer(mockPlayer)).thenReturn(10);
        assertTrue(playerUtilities.isChunkLoadingLimitExceeded(mockPlayer));
    }

    @Test
    void testIsChunkLoadingLimitExceeded_overLimit() {
        when(mockConfigManager.getMaxChunksPerPlayer()).thenReturn(10);
        when(mockChunkManager.getChunksLoadedByPlayer(mockPlayer)).thenReturn(11);
        assertTrue(playerUtilities.isChunkLoadingLimitExceeded(mockPlayer));
    }

    @Test
    void testForceLoadChunk_underLimit() {
        when(mockConfigManager.getMaxChunksPerPlayer()).thenReturn(10);
        when(mockChunkManager.getChunksLoadedByPlayer(mockPlayer)).thenReturn(5);

        playerUtilities.forceLoadChunk(mockPlayer);

        verify(mockChunkManager, times(1)).forceLoadChunk(mockPlayer);
        verify(mockPlayer, times(1)).sendMessage("Chunk loaded successfully.");
        verify(mockPlayer, never()).sendMessage("You have reached the maximum number of chunks you can force load.");
    }

    @Test
    void testForceLoadChunk_atLimit() {
        when(mockConfigManager.getMaxChunksPerPlayer()).thenReturn(10);
        when(mockChunkManager.getChunksLoadedByPlayer(mockPlayer)).thenReturn(10);
        playerUtilities.forceLoadChunk(mockPlayer);

        verify(mockChunkManager, never()).forceLoadChunk(mockPlayer);
        verify(mockPlayer, times(1)).sendMessage("You have reached the maximum number of chunks you can force load.");
        verify(mockPlayer, never()).sendMessage("Chunk loaded successfully.");
    }
}