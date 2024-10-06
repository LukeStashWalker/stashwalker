package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.PositionProcessor;
import com.stashwalker.features.Renderable;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class AlteredDungeonsFeatureImpl extends AbstractBaseFeature implements PositionProcessor, Renderable  {

    private final DoubleListBuffer<Pair<BlockPos, Block>> buffer = new DoubleListBuffer<>();
    private final List<Pair<BlockPos, Block>> dungeonsTemp = Collections.synchronizedList(new ArrayList<>());
    private String spawnerColorKey;
    private String dungeonColorKey;
    private String chestColorKey;

    {

        this.featureName = FEATURE_NAME_ALTERED_DUNGEONS;
        this.featureColorsKeyStart = "Altered_Dungeons";
        this.spawnerColorKey = this.featureColorsKeyStart + "_Spawner";
        this.dungeonColorKey = this.featureColorsKeyStart + "_Dungeon";
        this.chestColorKey = this.featureColorsKeyStart + "_Chest";

        this.featureColors.put(spawnerColorKey, new Pair<>(Color.BLUE, Color.BLUE));
        this.featureColors.put(dungeonColorKey, new Pair<>(Color.GRAY, Color.GRAY));
        this.featureColors.put(chestColorKey, new Pair<>(Color.YELLOW, Color.YELLOW));
    }

    @Override
    public void processPosition (BlockPos pos, int chunkX, int chunkZ) {

        if (this.enabled) {

                RegistryKey<World> dimensionKey = Constants.MC_CLIENT_INSTANCE.world.getRegistryKey();
                if (World.OVERWORLD.equals(dimensionKey)) {

                    List<Pair<BlockPos, Block>> result = FinderUtil.getAlteredDungeonsBlocksWithPillars(pos, chunkX, chunkZ);
                    if (result.size() > 0) {

                        dungeonsTemp.addAll(result);
                    }
                }
        }
    }

    @Override
    public void update () {

        if (this.enabled) {

            this.buffer.updateBuffer(Collections.synchronizedList(this.dungeonsTemp));
            this.dungeonsTemp.clear();
        }
    }


    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            List<Pair<BlockPos, Block>> blockpositions = buffer.readBuffer();
            if (blockpositions != null) {

                for (Pair<BlockPos, Block> p: blockpositions) {

                    Vec3d newBlockPos = new Vec3d(
                            p.getKey().getX() + 0.5D,
                            p.getKey().getY() + 0.5D,
                            p.getKey().getZ() + 0.5D);
                    
                    if (p.getValue() == Blocks.SPAWNER) {

                        Color configuredColor = featureColors.get(spawnerColorKey).getKey();
                        RenderUtil.drawLine(context, newBlockPos, configuredColor.getRed(), configuredColor.getGreen(), configuredColor.getBlue(),
                            configuredColor.getAlpha(), false);
                    } else if (p.getValue() ==  Blocks.CHEST) {

                        Color configuredColor = featureColors.get(chestColorKey).getKey();
                        RenderUtil.drawBlockSquare(context, newBlockPos, configuredColor);
                    } else {

                        Color configuredColor = featureColors.get(dungeonColorKey).getKey();
                        RenderUtil.drawBlockSquare(context, newBlockPos, configuredColor);
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
