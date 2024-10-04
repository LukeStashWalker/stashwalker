package com.stashwalker.features;

import net.minecraft.world.chunk.Chunk;

public interface ChunkLoadProcessFeature {

    void processChunk (Chunk chunk);
} 