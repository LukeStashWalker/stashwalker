package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ProcessFeature;
import com.stashwalker.features.RenderableFeature;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.List;

public class EntityTracersFeatureImpl extends AbstractBaseFeature implements ProcessFeature, RenderableFeature  {

    private final DoubleListBuffer<Entity> buffer = new DoubleListBuffer<>();

    {

        enabled = false;
        featureName = FEATURE_NAME_ENTITY_TRACER;
        featureColorsKeyStart = "Entity_Tracers";

        featureColors.put(featureColorsKeyStart, new Pair<>(Color.RED, Color.RED));
    }

    @Override
    public void process () {

        if (enabled) {

            this.buffer.updateBuffer(FinderUtil.findEntities());
        }
    }

    @Override
    public void render (WorldRenderContext context) {

        if (enabled) {

            List<Entity> entities = this.buffer.readBuffer();
            if (!entities.isEmpty()) {

                for (Entity entity : entities) {

                    Vec3d entityPos;
                    if (entity instanceof ItemFrameEntity) {

                        entityPos = new Vec3d(
                                entity.getPos().getX(),
                                entity.getPos().getY(),
                                entity.getPos().getZ());
                    } else {

                        entityPos = new Vec3d(
                                entity.getPos().getX(),
                                entity.getPos().getY() + 0.5D,
                                entity.getPos().getZ());
                    }

                    Color color = featureColors.get(featureColorsKeyStart).getKey();
                    RenderUtil.drawLine(context, entityPos, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), true);
                }
            }
        }
    }

    @Override
    public void clear () {
        
        this.buffer.updateBuffer(Collections.emptyList());
    }
}
