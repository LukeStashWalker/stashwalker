package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.containers.ConcurrentBoundedSet;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ChunkLoadProcessFeature;
import com.stashwalker.features.RenderableFeature;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;


public class NewChunksFeatureImpl extends AbstractBaseFeature implements ChunkLoadProcessFeature, RenderableFeature  {

    private final ConcurrentBoundedSet<ChunkPos> buffer = new ConcurrentBoundedSet<>(64 * 64);

    {

        this.enabled = false;
        this.featureName = FEATURE_NAME_NEW_CHUNKS;
        this.featureColorsKeyStart = "New_Chunks";

        this.featureColors.put(this.featureColorsKeyStart, new Pair<>(Color.RED, Color.RED));
    }

    @Override
    public void processChunk (Chunk chunk) {

        if (this.enabled) {

            if (FinderUtil.isNewChunk(chunk)) {

                this.buffer.add(chunk.getPos());
            }
        }
    }

    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            Color color = featureColors.get(featureColorsKeyStart).getKey();

            RenderUtil
                    .drawChunkSquare(
                            context,
                            this.buffer,
                            63,
                            16,
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            color.getAlpha()
                    );
        }
    }

    @Override
    public void clear () {
        
        this.buffer.clear();;
    }
}
