package com.stashwalker.features;

import java.util.UUID;

import net.minecraft.util.math.BlockPos;

public interface PositionProcessor {
    
    void processBlockPos (UUID callIdentifier, BlockPos pos);

    void updateBlockPositions (UUID callIdentifier);
}
