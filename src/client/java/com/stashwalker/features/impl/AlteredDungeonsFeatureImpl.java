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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

public class AlteredDungeonsFeatureImpl extends AbstractBaseFeature implements PositionProcessor, Renderable  {

    private final Map<UUID, List<AlteredDungeon>> dungeonsTempMap = Collections.synchronizedMap(new HashMap<>());
    private final DoubleListBuffer<AlteredDungeon> buffer = new DoubleListBuffer<>();
    private String spawnerColorKey;
    private String dungeonColorKey;
    private String chestColorKey;
    private String pillarDefaultColorKey;
    private String spiderColorKey;
    private String skeletonColorKey;
    private String zombieColorKey;
    private int minimumPillarHeight = 10;
    private int horizontalSearchRadius = 10;

    {

        this.featureName = FEATURE_NAME_ALTERED_DUNGEONS;
        this.featureColorsKeyStart = "Altered_Dungeons";
        this.spawnerColorKey = this.featureColorsKeyStart + "_Spawner";
        this.dungeonColorKey = this.featureColorsKeyStart + "_Dungeon";
        this.chestColorKey = this.featureColorsKeyStart + "_Chest";
        this.pillarDefaultColorKey = this.featureColorsKeyStart + "_PillarDefault";

        this.spiderColorKey = this.featureColorsKeyStart + "_Spider";
        this.skeletonColorKey = this.featureColorsKeyStart + "_Skeleton";
        this.zombieColorKey = this.featureColorsKeyStart + "_Zombie";

        this.featureColors.put(spawnerColorKey, new Pair<>(Color.BLUE, Color.BLUE));
        this.featureColors.put(dungeonColorKey, new Pair<>(Color.GRAY, Color.GRAY));
        this.featureColors.put(chestColorKey, new Pair<>(Color.YELLOW, Color.YELLOW));
        this.featureColors.put(pillarDefaultColorKey, new Pair<>(Color.RED, Color.RED));

        this.featureColors.put(spiderColorKey, new Pair<>(Color.RED, Color.RED));
        this.featureColors.put(skeletonColorKey, new Pair<>(Color.RED, Color.RED));
        this.featureColors.put(zombieColorKey, new Pair<>(Color.RED, Color.RED));
    }

    @Override
    public void process (BlockPos pos, UUID callIdentifier) {

        if (enabled) {

            if (!this.dungeonsTempMap.containsKey(callIdentifier)) {

                this.dungeonsTempMap.put(callIdentifier, Collections.synchronizedList(new ArrayList<>()));
            } 

            RegistryKey<World> dimensionKey = Constants.MC_CLIENT_INSTANCE.world.getRegistryKey();
            if (World.OVERWORLD.equals(dimensionKey)) {

                if (
                    this.isSpawnerInDungeonWithChest(pos)
                    && this.isHiddenAlteredDungeon(pos)
                ) {

                    AlteredDungeon result = this.getAlteredDungeonsBlocksWithPillars(pos);
                    this.dungeonsTempMap.get(callIdentifier).add(result);
                }
            }
        }
    }

    @Override
    public void update (UUID callIdentifier) {

        this.buffer.updateBuffer(this.dungeonsTempMap.get(callIdentifier));
        this.dungeonsTempMap.remove(callIdentifier);
    }

    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            List<AlteredDungeon> alteredDungeons = buffer.readBuffer();

            for (AlteredDungeon alteredDungeon : alteredDungeons) {
                
                RenderUtil.drawBlockSquares(context, alteredDungeon.getDungeonPositions(), featureColors.get(dungeonColorKey).getKey(), false, false);

                for (Pair<Vec3d, Color> pair: alteredDungeon.getPillarPositions()) {

                    RenderUtil.drawBlockSquare(context, pair.getKey(), pair.getValue(), false, false);
                }

                RenderUtil.drawBlockSquares(context, alteredDungeon.getChestPositions(), featureColors.get(chestColorKey).getKey(), false, false);

                RenderUtil.drawBlockSquares(context, alteredDungeon.getSpiderPositions(), featureColors.get(spiderColorKey).getKey(), true, false);
                RenderUtil.drawBlockSquares(context, alteredDungeon.getSkeletonPositions(), featureColors.get(skeletonColorKey).getKey(), true, false);
                RenderUtil.drawBlockSquares(context, alteredDungeon.getZombiePositions(), featureColors.get(zombieColorKey).getKey(), true, false);

                RenderUtil.drawLine(context, alteredDungeon.getSpawnerPosition(), featureColors.get(spawnerColorKey).getKey(), false, false);
            }
        }
    }

    @Override
    public void clear () {
        
        this.buffer.updateBuffer(Collections.emptyList());
    }

    public boolean isSpawnerInDungeonWithChest (BlockPos pos) {

        if (FinderUtil.isBlockType(pos, Blocks.SPAWNER)) {

            boolean mossyCobbleFound = false;
            boolean cobbleFound = false;
            boolean chestFound = false;
            int dungeonSearchRadius = 4;
            BlockPos startPos = new BlockPos(pos.getX() - dungeonSearchRadius, pos.getY() - 1, pos.getZ() - dungeonSearchRadius);
            BlockPos endPos = new BlockPos(pos.getX() + dungeonSearchRadius, pos.getY(), pos.getZ() + dungeonSearchRadius);
            for (BlockPos p : BlockPos.iterate(startPos, endPos)) {

                if (FinderUtil.isBlockType(p, Blocks.MOSSY_COBBLESTONE)) {

                    mossyCobbleFound = true;
                } else if (FinderUtil.isBlockType(p, Blocks.COBBLESTONE)) {

                    cobbleFound = true;
                } else if (FinderUtil.isBlockType(p, Blocks.CHEST)) {

                    chestFound = true;
                }

                if (mossyCobbleFound && cobbleFound && chestFound) {

                    return true;
                }
            }
        }

        return false;
    }

    public boolean isHiddenAlteredDungeon (BlockPos pos) {

        for (int x = pos.getX() - this.horizontalSearchRadius; x <= pos.getX() + this.horizontalSearchRadius; x++) {

            for (int z = pos.getZ() - this.horizontalSearchRadius; z <= pos.getZ() + this.horizontalSearchRadius; z++) {

                int topY = Constants.MC_CLIENT_INSTANCE.world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z) - 1;
                BlockPos bottomPos = new BlockPos(x, pos.getY(), z);
                BlockPos topPos = new BlockPos(x, topY, z);
                List<BlockPos> result = new ArrayList<>();
                for (BlockPos pillarPos : BlockPos.iterate(bottomPos, topPos)) {

                    BlockPos pillarPosCopy = new BlockPos(pillarPos);
                    if (isDifferentFromSurroundingSolidBlocks(pillarPosCopy)) {

                        result.add(pillarPosCopy);

                        if (
                            result.size() >= this.minimumPillarHeight
                        ) {

                            return true;
                        }
                    } else {

                        result.clear();
                    }
                }
            }
        }

        return false;
    }

    public AlteredDungeon getAlteredDungeonsBlocksWithPillars (BlockPos pos) {

        final AlteredDungeon alteredDungeon = new AlteredDungeon();

        // Add the pillar positions
        for (int x = pos.getX() - this.horizontalSearchRadius; x <= pos.getX() + this.horizontalSearchRadius; x++) {

            for (int z = pos.getZ() - this.horizontalSearchRadius; z <= pos.getZ() + this.horizontalSearchRadius; z++) {

                int topY = Constants.MC_CLIENT_INSTANCE.world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z) - 1;
                BlockPos bottomPos = new BlockPos(x, pos.getY(), z);
                BlockPos topPos = new BlockPos(x, topY, z);
                List<BlockPos> result = new ArrayList<>();
                for (BlockPos pillarPos : BlockPos.iterate(bottomPos, topPos)) {

                    BlockPos pillarPosCopy = new BlockPos(pillarPos);
                    if (isDifferentFromSurroundingSolidBlocks(pillarPosCopy)) {

                        result.add(pillarPosCopy);

                        if (
                            pillarPosCopy.getY() == topY
                            && result.size() >= this.minimumPillarHeight
                        ) {

                            alteredDungeon.getPillarPositions().addAll(result.stream().map(r -> new Pair<>(RenderUtil.toVec3d(r), getBlockPosColor(r))).toList());
                        }
                    } else {

                        if (result.size() >= this.minimumPillarHeight) {

                            alteredDungeon.getPillarPositions().addAll(result.stream().map(r -> new Pair<>(RenderUtil.toVec3d(r), getBlockPosColor(r))).toList());
                        }
                        
                        result.clear();
                    }
                }
            }
        }

        // Add the dungeon positions
        int dungeonHorizontalRadius = 4;
        int dungeonHeight = 5;
        BlockPos startPos = new BlockPos(pos.getX() - dungeonHorizontalRadius, pos.getY() - 1, pos.getZ() - dungeonHorizontalRadius);
        BlockPos endPos = new BlockPos(pos.getX() + dungeonHorizontalRadius, pos.getY() + (dungeonHeight - 1),
                pos.getZ() + dungeonHorizontalRadius);
        for (BlockPos boxPos : BlockPos.iterate(startPos, endPos)) {

            if (FinderUtil.isBlockType(boxPos, Blocks.COBBLESTONE) || FinderUtil.isBlockType(boxPos, Blocks.MOSSY_COBBLESTONE)) {

                alteredDungeon.getDungeonPositions().add(RenderUtil.toVec3d(boxPos));
            } else if (FinderUtil.isBlockType(boxPos, Blocks.CHEST)) {

                alteredDungeon.getChestPositions().add(RenderUtil.toVec3d(boxPos));
            }
        }
        alteredDungeon.setSpawnerPosition(RenderUtil.toVec3d(pos));

        // Add the entity Positions
        int boxHorizontalRadius = 6; // Some of the mobs may have wandered out of the Dungeon a little
        int boxHeight = 4;
        Box entitiesBox = new Box(
            pos.getX() - boxHorizontalRadius, pos.getY() - 1, pos.getZ() - boxHorizontalRadius,
            pos.getX() + boxHorizontalRadius, pos.getY() + (boxHeight - 1), pos.getZ() + boxHorizontalRadius
        );
        alteredDungeon.setZombiePositions(
            FinderUtil.getEntityPositions(ZombieEntity.class, entitiesBox)
        );
        alteredDungeon.setSkeletonPositions(
            FinderUtil.getEntityPositions(SkeletonEntity.class, entitiesBox)
        );
        alteredDungeon.setSpiderPositions(
            FinderUtil.getEntityPositions(SpiderEntity.class, entitiesBox)
        );

        return alteredDungeon;
    }

    private boolean isDifferentFromSurroundingSolidBlocks (BlockPos pos) {

        BlockPos[] surroundingPositions = {
            pos.north(), 
            pos.south(), 
            pos.east(), 
            pos.west(),
            pos.north().east(),  // North-East
            pos.north().west(),  // North-West
            pos.south().east(),  // South-East
            pos.south().west(),  // South-West
        };

        for (BlockPos adjacentPos : surroundingPositions) {

            if (
                FinderUtil.isBlockType(pos, Constants.MC_CLIENT_INSTANCE.world.getBlockState(adjacentPos).getBlock())
                || !Constants.MC_CLIENT_INSTANCE.world.getBlockState(adjacentPos).isSolidBlock(Constants.MC_CLIENT_INSTANCE.world, adjacentPos)
            ) {

                return false;            
            }
        }

        return true;    
    }

    private Color getBlockPosColor (BlockPos pos) {

        BlockState blockState = Constants.MC_CLIENT_INSTANCE.world.getBlockState(pos);
        MapColor mapColor = blockState.getMapColor(Constants.MC_CLIENT_INSTANCE.world, pos);
        if (mapColor != null) {

            return new Color(mapColor.color);
        } else {

            return this.featureColors.get(dungeonColorKey).getKey();
        }
    }
}
