package com.stashwalker.features;

import net.minecraft.world.chunk.Chunk;

public interface ChunkProcessor {

    void processChunkLoad (Chunk chunk);
    
    void processChunkUnload (Chunk chunk);
} 