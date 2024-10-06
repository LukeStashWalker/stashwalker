package com.stashwalker.features;

import net.minecraft.world.chunk.Chunk;

public interface ChunkScanProcessor {

    void processScannedChunk (Chunk chunk);
} 