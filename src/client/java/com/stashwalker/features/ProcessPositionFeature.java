package com.stashwalker.features;

import net.minecraft.util.math.BlockPos;

public interface ProcessPositionFeature {

    void processPosition (BlockPos pos, int chunkX, int chunkZ);
    
    void update ();
} 