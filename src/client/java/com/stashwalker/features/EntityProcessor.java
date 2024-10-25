package com.stashwalker.features;

import net.minecraft.entity.Entity;

public interface EntityProcessor {
    
    void loadEntity (Entity entity);
    void unloadEntity (Entity entity);
}