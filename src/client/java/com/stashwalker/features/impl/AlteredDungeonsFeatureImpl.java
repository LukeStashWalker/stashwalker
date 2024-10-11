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
import com.stashwalker.utils.MapUtil;
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

    private final String spawnerColorKey = "spawnerColor";
    private final Color spawnerColorDefaultValue = Color.BLUE;

    private final String cobbleColorKey = "cobbyColor";
    private final Color cobbleColorDefaultValue = Color.GRAY;
    private final String mossyCobbleColorKey = "mossyCobbyColor";
    private final Color mossyCobbleColorDefaultValue = new Color(54, 120, 22);
    private final String chestColorKey = "chestColor";
    private final Color chestColorDefaultValue = Color.YELLOW;
    private final String pillarDefaultColorKey = "pillarDefaultColor";
    private final Color pillarDefaultDefaultValue = Color.DARK_GRAY;

    private final String spiderColorKey = "spiderColor";
    private final Color spiderColorDefaultValue = Color.BLACK;
    private final String  skeletonColorKey = "skeletonColor";
    private final Color skeletonColorDefaultValue = Color.WHITE;
    private final String zombieColorKey = "zombieColor";
    private final Color zombieColorDefaultValue = Color.GREEN;

    private final String minimumPillarHeightKey = "minimumPillarHeight";
    private final Integer minimumPillarHeightDefaultValue = 10;
    private final String pillarSearchRadiusKey = "pillarSearchRadius";
    private final Integer pillarSearchRadiusDefaultValue = 10;
    private final String fillInBoxesKey = "fillInBoxes";
    private final Boolean fillInBoxesDefaultValue = true;

    public AlteredDungeonsFeatureImpl () {

        super();

        this.featureName = FEATURE_NAME_ALTERED_DUNGEONS;

        this.defaultIntegerMap.put(spawnerColorKey, spawnerColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(cobbleColorKey, cobbleColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(mossyCobbleColorKey, mossyCobbleColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(chestColorKey, chestColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(pillarDefaultColorKey, pillarDefaultDefaultValue.getRGB());
        this.defaultIntegerMap.put(spiderColorKey, spiderColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(skeletonColorKey, skeletonColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(zombieColorKey, zombieColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(minimumPillarHeightKey, minimumPillarHeightDefaultValue);
        this.defaultIntegerMap.put(pillarSearchRadiusKey, pillarSearchRadiusDefaultValue);

        this.defaultBooleanMap.put(fillInBoxesKey, fillInBoxesDefaultValue);

        this.featureConfig.setIntegerConfigs(MapUtil.deepCopy(this.defaultIntegerMap));
        this.featureConfig.setBooleanConfigs(MapUtil.deepCopy(this.defaultBooleanMap));
        this.featureConfig.setStringConfigs(MapUtil.deepCopy(this.defaultStringMap));
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

            final boolean fillBoxes = this.featureConfig.getBooleanConfigs().get(this.fillInBoxesKey);

            List<AlteredDungeon> alteredDungeons = buffer.readBuffer();

            for (AlteredDungeon alteredDungeon : alteredDungeons) {
                
                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getCobblePositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.cobbleColorKey)), 
                    false, 
                    fillBoxes
                );
                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getMossyCobblePositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.mossyCobbleColorKey)), 
                    false, 
                    fillBoxes
                );

                for (Pair<Vec3d, Color> pair: alteredDungeon.getPillarPositions()) {

                    RenderUtil.drawBlockSquare(context, pair.getKey(), pair.getValue(), false, fillBoxes);
                }

                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getChestPositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.chestColorKey)), 
                    false, 
                    fillBoxes
                );

                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getSpiderPositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.spiderColorKey)), 
                    true, 
                    fillBoxes
                );
                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getSkeletonPositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.skeletonColorKey)), 
                    true, 
                    fillBoxes
                );
                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getZombiePositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.zombieColorKey)), 
                    true, 
                    fillBoxes
                );

                RenderUtil.drawLine(
                    context, 
                    alteredDungeon.getSpawnerPosition(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.spawnerColorKey)), 
                    false, 
                    fillBoxes
                );
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

        final int horizontalSearchRadius = this.featureConfig.getIntegerConfigs().get(this.minimumPillarHeightKey);
        final int minimumPillarHeight = this.featureConfig.getIntegerConfigs().get(this.minimumPillarHeightKey);
        for (int x = pos.getX() - horizontalSearchRadius; x <= pos.getX() + horizontalSearchRadius; x++) {

            for (int z = pos.getZ() - horizontalSearchRadius; z <= pos.getZ() + horizontalSearchRadius; z++) {

                int topY = Constants.MC_CLIENT_INSTANCE.world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z) - 1;
                BlockPos bottomPos = new BlockPos(x, pos.getY(), z);
                BlockPos topPos = new BlockPos(x, topY, z);
                List<BlockPos> result = new ArrayList<>();
                for (BlockPos pillarPos : BlockPos.iterate(bottomPos, topPos)) {

                    BlockPos pillarPosCopy = new BlockPos(pillarPos);
                    if (isDifferentFromSurroundingSolidBlocks(pillarPosCopy)) {

                        result.add(pillarPosCopy);

                        if (
                            result.size() >= minimumPillarHeight
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

        // Add the pillar positions
        final AlteredDungeon alteredDungeon = new AlteredDungeon();
        final int minimumPillarHeight = this.featureConfig.getIntegerConfigs().get(this.minimumPillarHeightKey);
        final int horizontalSearchRadius = this.featureConfig.getIntegerConfigs().get(this.minimumPillarHeightKey);
        for (int x = pos.getX() - horizontalSearchRadius; x <= pos.getX() + horizontalSearchRadius; x++) {

            for (int z = pos.getZ() - horizontalSearchRadius; z <= pos.getZ() + horizontalSearchRadius; z++) {

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
                            && result.size() >= minimumPillarHeight
                        ) {

                            alteredDungeon.getPillarPositions().addAll(result.stream().map(r -> new Pair<>(RenderUtil.toVec3d(r), getBlockPosColor(r))).toList());
                        }
                    } else {

                        if (result.size() >= minimumPillarHeight) {

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

            if (FinderUtil.isBlockType(boxPos, Blocks.COBBLESTONE)) {

                alteredDungeon.getCobblePositions().add(RenderUtil.toVec3d(boxPos));
            } else if (FinderUtil.isBlockType(boxPos, Blocks.MOSSY_COBBLESTONE)) {

                alteredDungeon.getMossyCobblePositions().add(RenderUtil.toVec3d(boxPos));
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

            return new Color(this.featureConfig.getIntegerConfigs().get(this.pillarDefaultColorKey));
        }
    }
}
