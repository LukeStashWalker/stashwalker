package com.stashwalker.features;

import java.util.UUID;

import net.minecraft.entity.Entity;

public interface EntityProcessor {
    
    void processEntity (UUID callIdentifier, Entity entity);

    void updateEntity (UUID callIdentifier);
}
