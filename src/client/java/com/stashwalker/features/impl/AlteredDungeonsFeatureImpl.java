package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ProcessPositionFeature;
import com.stashwalker.features.RenderableFeature;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class AlteredDungeonsFeatureImpl extends AbstractBaseFeature implements ProcessPositionFeature, RenderableFeature  {

    private final DoubleListBuffer<Pair<BlockPos, Color>> buffer = new DoubleListBuffer<>();
    private final List<Pair<BlockPos, Color>> dungeonsTemp = new ArrayList<>();

    {

        this.enabled = false;
        this.featureName = FEATURE_NAME_ALTERED_DUNGEONS;
        this.featureColorsKeyStart = "Altered_Dungeons";

        this.featureColors.put(this.featureColorsKeyStart + "_Spawner", new Pair<>(Color.BLUE, Color.BLUE));
        this.featureColors.put(this.featureColorsKeyStart + "_Dungeon", new Pair<>(Color.GRAY, Color.GRAY));
    }

    @Override
    public void processPosition (BlockPos pos, int chunkX, int chunkZ) {

        if (this.enabled) {

                RegistryKey<World> dimensionKey = Constants.MC_CLIENT_INSTANCE.world.getRegistryKey();
                if (World.OVERWORLD.equals(dimensionKey)) {

                    Pair<BlockPos, List<BlockPos>> result = FinderUtil.getAlteredDungeonsBlocksWithPillars(pos, chunkX,
                            chunkZ);
                    if (result.getValue().size() > 0) {

                        result.getValue().forEach(r -> dungeonsTemp.add(new Pair<BlockPos, Color>(r, Color.GRAY)));
                        dungeonsTemp.add(new Pair<BlockPos, Color>(result.getKey(), Color.BLUE));
                    }
                }
        }
    }

    @Override
    public void update () {

        if (this.enabled) {

            this.buffer.updateBuffer(this.dungeonsTemp);
            this.dungeonsTemp.clear();
        }
    }


    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            List<Pair<BlockPos, Color>> blockpositions = buffer.readBuffer();
            if (blockpositions != null) {

                for (Pair<BlockPos, Color> pair : blockpositions) {

                    BlockPos blockPos = pair.getKey();
                    Color color = pair.getValue();
                    Vec3d newBlockPos = new Vec3d(
                            blockPos.getX() + 0.5D,
                            blockPos.getY() + 0.5D,
                            blockPos.getZ() + 0.5D);
                    
                    if (color.equals(Color.BLUE)) {

                        Color configuredColor = featureColors.get(featureColorsKeyStart + "_Spawner").getKey();
                        RenderUtil.drawLine(context, newBlockPos, configuredColor.getRed(), configuredColor.getGreen(), configuredColor.getBlue(),
                            color.getAlpha(), false);
                    } else if (color.equals(Color.GRAY)) {

                        Color configuredColor = featureColors.get(featureColorsKeyStart + "_Dungeon").getKey();
                        Vec3d cameraPos = Constants.MC_CLIENT_INSTANCE.gameRenderer.getCamera().getPos();
                        newBlockPos = newBlockPos.subtract(cameraPos);
                        RenderUtil.drawBlockSquare(context, newBlockPos, configuredColor.getRed(), configuredColor.getGreen(), configuredColor.getBlue(),
                            color.getAlpha(), false);
                    }
                }
            }
        }
    }

    @Override
    public void clear () {
        
        this.buffer.updateBuffer(Collections.emptyList());
    }
}
