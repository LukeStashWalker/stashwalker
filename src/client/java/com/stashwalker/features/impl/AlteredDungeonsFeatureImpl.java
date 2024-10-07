package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.PositionProcessor;
import com.stashwalker.features.Renderable;
import com.stashwalker.models.AlteredDungeon;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class AlteredDungeonsFeatureImpl extends AbstractBaseFeature implements PositionProcessor, Renderable  {

    private final DoubleListBuffer<AlteredDungeon> buffer = new DoubleListBuffer<>();
    private final List<AlteredDungeon> dungeonsTemp = Collections.synchronizedList(new ArrayList<>());
    private String spawnerColorKey;
    private String dungeonColorKey;
    private String chestColorKey;
    private String pillarColorKey;
    private String spiderColorKey;
    private String skeletonColorKey;
    private String zombieColorKey;

    {

        this.featureName = FEATURE_NAME_ALTERED_DUNGEONS;
        this.featureColorsKeyStart = "Altered_Dungeons";
        this.spawnerColorKey = this.featureColorsKeyStart + "_Spawner";
        this.dungeonColorKey = this.featureColorsKeyStart + "_Dungeon";
        this.chestColorKey = this.featureColorsKeyStart + "_Chest";
        this.pillarColorKey = this.featureColorsKeyStart + "_Pillar";

        this.spiderColorKey = this.featureColorsKeyStart + "_Spider";
        this.skeletonColorKey = this.featureColorsKeyStart + "_Skeleton";
        this.zombieColorKey = this.featureColorsKeyStart + "_Zombie";

        this.featureColors.put(spawnerColorKey, new Pair<>(Color.BLUE, Color.BLUE));
        this.featureColors.put(dungeonColorKey, new Pair<>(Color.GRAY, Color.GRAY));
        this.featureColors.put(chestColorKey, new Pair<>(Color.YELLOW, Color.YELLOW));
        this.featureColors.put(pillarColorKey, new Pair<>(Color.RED, Color.RED));

        this.featureColors.put(spiderColorKey, new Pair<>(Color.RED, Color.RED));
        this.featureColors.put(skeletonColorKey, new Pair<>(Color.RED, Color.RED));
        this.featureColors.put(zombieColorKey, new Pair<>(Color.RED, Color.RED));
    }

    @Override
    public void processPosition (BlockPos pos, int chunkX, int chunkZ) {

        if (this.enabled) {

                RegistryKey<World> dimensionKey = Constants.MC_CLIENT_INSTANCE.world.getRegistryKey();
                if (World.OVERWORLD.equals(dimensionKey)) {

                    if (
                        FinderUtil.areAdjacentChunksLoaded(chunkX, chunkZ)
                        && FinderUtil.isDungeonWithChest(pos)
                        && FinderUtil.isHiddenAlteredDungeon(pos)
                    ) {

                        AlteredDungeon result = FinderUtil.getAlteredDungeonsBlocksWithPillars(pos);
                        dungeonsTemp.add(result);
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

            List<AlteredDungeon> alteredDungeons = buffer.readBuffer();

            for (AlteredDungeon alteredDungeon : alteredDungeons) {
                
                RenderUtil.drawVec3dBoxes(context, alteredDungeon.getDungeonPositions(), featureColors.get(dungeonColorKey).getKey(), false);
                RenderUtil.drawVec3dBoxes(context, alteredDungeon.getPillarPositions(), featureColors.get(pillarColorKey).getKey(), false);
                RenderUtil.drawVec3dBoxes(context, alteredDungeon.getChestPositions(), featureColors.get(chestColorKey).getKey(), false);

                RenderUtil.drawVec3dBoxes(context, alteredDungeon.getSpiderPositions(), featureColors.get(spiderColorKey).getKey(), true);
                RenderUtil.drawVec3dBoxes(context, alteredDungeon.getSkeletonPositions(), featureColors.get(skeletonColorKey).getKey(), true);
                RenderUtil.drawVec3dBoxes(context, alteredDungeon.getZombiePositions(), featureColors.get(zombieColorKey).getKey(), true);

                Color spawnerColor = featureColors.get(spawnerColorKey).getKey();
                RenderUtil.drawLine(context, alteredDungeon.getSpawnerPosition(), spawnerColor.getRed(), spawnerColor.getGreen(),
                        spawnerColor.getBlue(),
                        spawnerColor.getAlpha(), false);
            }
        }
    }

    @Override
    public void clear () {
        
        this.buffer.updateBuffer(Collections.emptyList());
    }
}
