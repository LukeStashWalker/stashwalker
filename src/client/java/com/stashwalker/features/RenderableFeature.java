package com.stashwalker.features;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public interface RenderableFeature {

    void render (WorldRenderContext context);
} 