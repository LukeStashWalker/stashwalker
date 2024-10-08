package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.Processor;
import com.stashwalker.features.Renderable;
import com.stashwalker.models.AlteredDungeon;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.Heightmap;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class AlteredDungeonsFeatureImpl extends AbstractBaseFeature implements Processor, Renderable  {

    private final DoubleListBuffer<AlteredDungeon> buffer = new DoubleListBuffer<>();
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
    public void process() {

        if (this.enabled) {

            int playerChunkPosX = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().x;
            int playerChunkPosZ = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().z;

            int playerRenderDistance = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance();
            int xStart = playerChunkPosX - playerRenderDistance;
            int xEnd = playerChunkPosX + playerRenderDistance + 1;
            int zStart = playerChunkPosZ - playerRenderDistance;
            int zEnd = playerChunkPosZ + playerRenderDistance + 1;
            final List<AlteredDungeon> dungeonsTemp = Collections.synchronizedList(new ArrayList<>());

            for (int x = xStart; x < xEnd; x++) {

                for (int z = zStart; z < zEnd; z++) {

                    Chunk chunk = FinderUtil.getChunkEarly(x, z);

                    if (chunk != null) {

                        Set<BlockPos> blockPositions = chunk.getBlockEntityPositions();
                        if (blockPositions != null) {

                            for (BlockPos pos : blockPositions) {

                                RegistryKey<World> dimensionKey = Constants.MC_CLIENT_INSTANCE.world.getRegistryKey();
                                if (World.OVERWORLD.equals(dimensionKey)) {

                                    if (FinderUtil.areAdjacentChunksLoaded(x, z)
                                            && this.isSpawnerInDungeonWithChest(pos)
                                            && this.isHiddenAlteredDungeon(pos)
                                    ) {

                                        AlteredDungeon result = this.getAlteredDungeonsBlocksWithPillars(pos);
                                        dungeonsTemp.add(result);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            this.buffer.updateBuffer(dungeonsTemp);
        }
    }

    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            List<AlteredDungeon> alteredDungeons = buffer.readBuffer();

            for (AlteredDungeon alteredDungeon : alteredDungeons) {
                
                RenderUtil.drawBlockSquares(context, alteredDungeon.getDungeonPositions(), featureColors.get(dungeonColorKey).getKey(), false);
                RenderUtil.drawBlockSquares(context, alteredDungeon.getPillarPositions(), featureColors.get(pillarColorKey).getKey(), false);
                RenderUtil.drawBlockSquares(context, alteredDungeon.getChestPositions(), featureColors.get(chestColorKey).getKey(), false);

                RenderUtil.drawBlockSquares(context, alteredDungeon.getSpiderPositions(), featureColors.get(spiderColorKey).getKey(), true);
                RenderUtil.drawBlockSquares(context, alteredDungeon.getSkeletonPositions(), featureColors.get(skeletonColorKey).getKey(), true);
                RenderUtil.drawBlockSquares(context, alteredDungeon.getZombiePositions(), featureColors.get(zombieColorKey).getKey(), true);

                Color spawnerColor = featureColors.get(spawnerColorKey).getKey();
                RenderUtil.drawLine(context, alteredDungeon.getSpawnerPosition(), spawnerColor, false);
            }
        }
    }

    @Override
    public void clear () {
        
        this.buffer.updateBuffer(Collections.emptyList());
    }

    public boolean isSpawnerInDungeonWithChest (BlockPos pos) {

        boolean chestFound = false;
        boolean mossyCobbleFound = false;
        if (FinderUtil.isBlockType(pos, Blocks.SPAWNER)) {

            int dungeonSearchRadius = 4;
            BlockPos startPos = new BlockPos(pos.getX() - dungeonSearchRadius, pos.getY() - 1, pos.getZ() - dungeonSearchRadius);
            BlockPos endPos = new BlockPos(pos.getX() + dungeonSearchRadius, pos.getY(), pos.getZ() + dungeonSearchRadius);
            for (BlockPos p : BlockPos.iterate(startPos, endPos)) {

                if (FinderUtil.isBlockType(p, Blocks.MOSSY_COBBLESTONE)) {

                    mossyCobbleFound = true;
                } else if (FinderUtil.isBlockType(p, Blocks.CHEST)) {

                    chestFound = true;
                }

                if (chestFound && mossyCobbleFound) {

                    return true;
                }
            }
        }

        return false;
    }

    public boolean isHiddenAlteredDungeon (BlockPos pos) {

        final int horizontalSearchRadius = 10;
        final int minimumPillarHeight = 5;

        for (int x = pos.getX() - horizontalSearchRadius; x <= pos.getX() + horizontalSearchRadius; x++) {

            for (int z = pos.getZ() - horizontalSearchRadius; z <= pos.getZ() + horizontalSearchRadius; z++) {

                int topY = Constants.MC_CLIENT_INSTANCE.world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z) - 1;
                BlockPos bottomPos = new BlockPos(x, pos.getY(), z);
                BlockPos topPos = new BlockPos(x, topY, z);
                List<BlockPos> result = new ArrayList<>();
                for (BlockPos pillarPos : BlockPos.iterate(bottomPos, topPos)) {

                    if (
                        isDifferentFromSurroundingSolidBlocks(pillarPos)
                        && !FinderUtil.isBlockType(pillarPos, Blocks.MUDDY_MANGROVE_ROOTS)
                    ) {

                        result.add(new BlockPos(pillarPos));

                        if (
                            result.size() >= minimumPillarHeight
                        ) {

                            // Not trying to conceal
                            if (
                                FinderUtil.isBlockType(topPos, Blocks.NETHERRACK)
                                || FinderUtil.isBlockType(topPos, Blocks.OBSIDIAN)
                                || isDifferentFromSurroundingSolidBlocks(new BlockPos(x, topY + 1, z)) // Hole
                                || topY <= (bottomPos.getY() + 5) // Deep hole or exposed Dungeon
                            ) {

                                return false;
                            }

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

        final int horizontalSearchRadius = 10;
        final int minimumPillarHeight = 5;
        final AlteredDungeon alteredDungeon = new AlteredDungeon();

        // Add the pillar positions
        for (int x = pos.getX() - horizontalSearchRadius; x <= pos.getX() + horizontalSearchRadius; x++) {

            for (int z = pos.getZ() - horizontalSearchRadius; z <= pos.getZ() + horizontalSearchRadius; z++) {

                int topY = Constants.MC_CLIENT_INSTANCE.world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z) - 1;
                BlockPos bottomPos = new BlockPos(x, pos.getY(), z);
                BlockPos topPos = new BlockPos(x, topY, z);
                List<BlockPos> result = new ArrayList<>();
                for (BlockPos pillarPos : BlockPos.iterate(bottomPos, topPos)) {

                    BlockPos pillarPosCopy = new BlockPos(pillarPos);
                    if (
                        isDifferentFromSurroundingSolidBlocks(pillarPosCopy)
                        && !FinderUtil.isBlockType(pillarPos, Blocks.MUDDY_MANGROVE_ROOTS)
                    ) {

                        result.add(pillarPosCopy);

                        if (
                            pillarPosCopy.getY() == topY
                            && result.size() >= minimumPillarHeight
                        ) {

                            // Not trying to conceal
                            if (
                                !FinderUtil.isBlockType(topPos, Blocks.NETHERRACK)
                                && !FinderUtil.isBlockType(topPos, Blocks.OBSIDIAN)
                                && !isDifferentFromSurroundingSolidBlocks(new BlockPos(x, topY + 1, z)) // Hole
                                && !(topY <= (bottomPos.getY() + 5)) // Deep hole or exposed Dungeon
                            ) {

                                alteredDungeon.getPillarPositions().addAll(result.stream().map(r -> RenderUtil.toVec3d(r)).toList());
                            }
                        }
                    } else {

                        if (result.size() >= minimumPillarHeight) {

                            alteredDungeon.getPillarPositions().addAll(result.stream().map(r -> RenderUtil.toVec3d(r)).toList());
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

    private static boolean isDifferentFromSurroundingSolidBlocks (BlockPos pos) {

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
}
