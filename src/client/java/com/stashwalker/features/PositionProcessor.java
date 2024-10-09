package com.stashwalker.features;

import java.util.UUID;

import net.minecraft.util.math.BlockPos;

public interface PositionProcessor {
    
    void process (BlockPos pos, UUID callIdentifier);

    void update (UUID callIdentifier);
}
