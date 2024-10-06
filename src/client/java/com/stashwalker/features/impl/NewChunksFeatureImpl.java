package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.containers.ConcurrentBoundedCubeLineSet;
import com.stashwalker.containers.ConcurrentBoundedSet;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ChunkLoadProcessor;
import com.stashwalker.features.Renderable;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.RenderUtil;
import com.stashwalker.models.CubeLine;
import com.stashwalker.containers.ConcurrentBoundedCubeLineSet;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;


public class NewChunksFeatureImpl extends AbstractBaseFeature implements ChunkLoadProcessor, Renderable  {

    private final ConcurrentBoundedCubeLineSet buffer = new ConcurrentBoundedCubeLineSet(32 * 32);

    {

        this.featureName = FEATURE_NAME_NEW_CHUNKS;
        this.featureColorsKeyStart = "New_Chunks";

        this.featureColors.put(this.featureColorsKeyStart, new Pair<>(Color.RED, Color.RED));
    }

    @Override
    public void processLoadedChunk (Chunk chunk) {

        if (this.enabled) {

            if (FinderUtil.isNewChunk(chunk)) {

                this.buffer.addAll(RenderUtil.toCubeLines(chunk.getPos()));
                this.buffer.updateRenderableLines();
            }
        }
    }

    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            Color color = featureColors.get(featureColorsKeyStart).getKey();

            RenderUtil
                    .drawLines(
                            context,
                            this.buffer.getRenderableLines(),
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            color.getAlpha()
                    );

            // this.buffer.getRenderableLines().forEach(l -> {
            // RenderUtil
            //         .drawLine(
            //                 context,
            //                 l.getStart(),
            //                 l.getEnd(),
            //                 color.getRed(),
            //                 color.getGreen(),
            //                 color.getBlue(),
            //                 color.getAlpha()
            //         );
            // });
        }
    }

    @Override
    public void clear () {
        
        this.buffer.clear();;
    }
}
