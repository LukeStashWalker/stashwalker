package com.stashwalker.features;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public interface Renderable {

    void render (WorldRenderContext context);
} 