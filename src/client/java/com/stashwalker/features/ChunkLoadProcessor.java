package com.stashwalker.features;

import net.minecraft.world.chunk.Chunk;

public interface ChunkLoadProcessor {

    void processLoadedChunk (Chunk chunk);
} 