package com.stashwalker.features.impl;

import java.awt.Color;
import com.stashwalker.constants.Constants;
import com.stashwalker.containers.BoundedMap;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.PositionProcessor;
import com.stashwalker.features.Processor;
import com.stashwalker.features.Renderable;
import com.stashwalker.models.AlteredDungeon;
import com.stashwalker.models.AlteredMine;
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
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AlteredStructuresFeatureImpl extends AbstractBaseFeature implements PositionProcessor, Processor, Renderable  {

    // Cache dungeon when found
    private final Map<BlockPos, AlteredDungeon> dungeonsCache = new BoundedMap<>(100);
    // Every call has it's own List to reduce contention between writing threads
    private final Map<UUID, List<AlteredDungeon>> dungeonsTempMap = Collections.synchronizedMap(new HashMap<>());

    private final Map<BlockPos, AlteredMine> minesCache = new BoundedMap<>(100);
    private final Map<UUID, List<AlteredMine>> minesTempMap = Collections.synchronizedMap(new HashMap<>());
    // Use double buffer for fast rendering
    private final DoubleListBuffer<AlteredDungeon> dungeonsBuffer = new DoubleListBuffer<>();
    private final DoubleListBuffer<AlteredMine> minesBuffer = new DoubleListBuffer<>();

    private final String alteredDungeonSpawnerColorKey = "alteredDungeonSpawnerColor";
    private final Color alteredDungeonSpawnerColorDefaultValue = Color.BLUE;

    private final String alteredDungeonCobbleColorKey = "alteredDungeonCobbleColor";
    private final Color alteredDungeonCobbleColorDefaultValue = Color.GRAY;
    private final String alteredDungeonMossyCobbleColorKey = "alteredDungeonMossyCobbleColor";
    private final Color alteredDungeonMossyCobbleColorDefaultValue = new Color(54, 120, 22);
    private final String alteredDungeonChestColorKey = "alteredDungeonChestColor";
    private final Color alteredDungeonChestColorDefaultValue = Color.YELLOW;
    private final String alteredDungeonPillarDefaultColorKey = "alteredDungeonPillarDefaultColor";
    private final Color alteredDungeonPillarDefaultDefaultValue = Color.DARK_GRAY;

    private final String alteredDungeonSpiderColorKey = "alteredDungeonSpiderColor";
    private final Color alteredDungeonSpiderColorDefaultValue = Color.BLACK;
    private final String  alteredDungeonSkeletonColorKey = "alteredDungeonSkeletonColor";
    private final Color alteredDungeonSkeletonColorDefaultValue = Color.WHITE;
    private final String alteredDungeonZombieColorKey = "alteredDungeonZombieColor";
    private final Color alteredDungeonZombieColorDefaultValue = Color.GREEN;

    private final String alteredDungeonMinimumPillarHeightKey = "alteredDungeonMinimumPillarHeight";
    private final Integer alteredDungeonMinimumPillarHeightDefaultValue = 10;
    private final String alteredDungeonPillarSearchRadiusKey = "alteredDungeonPillarSearchRadius";
    private final Integer alteredDungeonPillarSearchRadiusDefaultValue = 10;

    private final String alteredMineChestMinecartColorKey = "alteredMineChestMinecartDefaultColor";
    private final Color alteredMineChestMinecartColorDefaultValue = Color.RED;
    private final String alteredMinePillarDefaultColorKey = "alteredMinePillarDefaultColor";
    private final Color alteredMinePillarDefaultDefaultValue = Color.DARK_GRAY;

    private final String alteredMineMinimumPillarHeightKey = "alteredMineMinimumPillarHeight";
    private final Integer alteredMineMinimumPillarHeightDefaultValue = 10;
    private final String alteredMinePillarSearchRadiusKey = "alteredMinePillarSearchRadius";
    private final Integer alteredMinePillarSearchRadiusDefaultValue = 4;

    private final String fillInBoxesKey = "fillInBoxes";
    private final Boolean fillInBoxesDefaultValue = true;

    public AlteredStructuresFeatureImpl () {

        super();

        this.featureName = FEATURE_NAME_ALTERED_STRUCTURES;

        this.defaultIntegerMap.put(this.alteredDungeonSpawnerColorKey, this.alteredDungeonSpawnerColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.alteredDungeonCobbleColorKey, this.alteredDungeonCobbleColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.alteredDungeonMossyCobbleColorKey, this.alteredDungeonMossyCobbleColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.alteredDungeonChestColorKey, this.alteredDungeonChestColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.alteredDungeonPillarDefaultColorKey, this.alteredDungeonPillarDefaultDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.alteredDungeonSpiderColorKey, this.alteredDungeonSpiderColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.alteredDungeonSkeletonColorKey, this.alteredDungeonSkeletonColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.alteredDungeonZombieColorKey, this.alteredDungeonZombieColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.alteredDungeonMinimumPillarHeightKey, this.alteredDungeonMinimumPillarHeightDefaultValue);
        this.defaultIntegerMap.put(this.alteredDungeonPillarSearchRadiusKey, this.alteredDungeonPillarSearchRadiusDefaultValue);

        this.defaultIntegerMap.put(this.alteredMineChestMinecartColorKey, alteredMineChestMinecartColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.alteredMinePillarDefaultColorKey, this.alteredMinePillarDefaultDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.alteredMineMinimumPillarHeightKey, this.alteredMineMinimumPillarHeightDefaultValue);
        this.defaultIntegerMap.put(this.alteredMinePillarSearchRadiusKey, this.alteredMinePillarSearchRadiusDefaultValue);

        this.defaultBooleanMap.put(fillInBoxesKey, fillInBoxesDefaultValue);

        this.featureConfig.setIntegerConfigs(MapUtil.deepCopy(this.defaultIntegerMap));
        this.featureConfig.setBooleanConfigs(MapUtil.deepCopy(this.defaultBooleanMap));
        this.featureConfig.setStringConfigs(MapUtil.deepCopy(this.defaultStringMap));
    }

    @Override
    public void processBlockPos (UUID callIdentifier, BlockPos pos) {

        if (this.enabled) {

            if (!this.dungeonsTempMap.containsKey(callIdentifier)) {

                this.dungeonsTempMap.put(callIdentifier, new ArrayList<>());
            } 

            RegistryKey<World> dimensionKey = Constants.MC_CLIENT_INSTANCE.world.getRegistryKey();
            if (World.OVERWORLD.equals(dimensionKey)) {

                if (this.dungeonsCache.containsKey(pos)) {

                    this.dungeonsTempMap.get(callIdentifier).add(this.dungeonsCache.get(pos));
                } else {

                    if (
                        this.isSpawnerInDungeonWithChest(pos)
                        && this.hasPillar(
                            pos, 
                            this.featureConfig.getIntegerConfigs().get(this.alteredDungeonMinimumPillarHeightKey), 
                            this.featureConfig.getIntegerConfigs().get(this.alteredDungeonMinimumPillarHeightKey)
                        )
                    ) {

                        AlteredDungeon result = this.getAlteredDungeonsBlocksWithPillars(pos);
                        this.dungeonsCache.put(pos, result);
                        this.dungeonsTempMap.get(callIdentifier).add(result);
                    }
                }
            }
        }
    }

    @Override
    public void process () {

        if (this.enabled) {

            List<AlteredMine> mines = Collections.synchronizedList(new ArrayList<>());
            Constants.MC_CLIENT_INSTANCE.world.getEntities().forEach(e -> {
            
                if (e instanceof ChestMinecartEntity) {
            
                    BlockPos pos = new BlockPos(e.getBlockPos());
                    if (this.minesCache.containsKey(pos)) {
            
                        mines.add(this.minesCache.get(pos));
            
                        return; // Skip this iteration
                    }
            
                    Map<String, Integer> integerConfigs = this.featureConfig.getIntegerConfigs();
                    Integer horizontalSearchRadius = integerConfigs.get(this.alteredMinePillarSearchRadiusKey);
                    Integer minimumPillarHeight = integerConfigs.get(this.alteredMineMinimumPillarHeightKey);
                    if (
                            this.hasPillar(
                                pos,
                                horizontalSearchRadius,
                                minimumPillarHeight
                            )
                        ) {
            
                            final AlteredMine alteredMine = new AlteredMine();
                            List<Pair<Vec3d, Color>> pillarPositions = alteredMine.getPillarPositions();
                            addPillarPositions(pos, horizontalSearchRadius, minimumPillarHeight, pillarPositions);
            
                            alteredMine.setChestMinecartPosition(RenderUtil.toVec3d(pos));
                            mines.add(alteredMine);
                            this.minesCache.put(pos, alteredMine);
                    }
                }
            });
            this.minesBuffer.updateBuffer(mines);
        }
    }

    @Override
    public void updateBlockPositions (UUID callIdentifier) {

        if (this.enabled) {

            this.dungeonsBuffer.updateBuffer(this.dungeonsTempMap.get(callIdentifier));
            this.dungeonsTempMap.remove(callIdentifier);
        }
    }

    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            final boolean fillBoxes = this.featureConfig.getBooleanConfigs().get(this.fillInBoxesKey);

            List<AlteredDungeon> alteredDungeons = dungeonsBuffer.readBuffer();
            for (AlteredDungeon alteredDungeon : alteredDungeons) {
                
                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getCobblePositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.alteredDungeonCobbleColorKey)), 
                    false, 
                    fillBoxes
                );
                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getMossyCobblePositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.alteredDungeonMossyCobbleColorKey)), 
                    false, 
                    fillBoxes
                );

                for (Pair<Vec3d, Color> pair: alteredDungeon.getPillarPositions()) {

                    RenderUtil.drawBlockSquare(context, pair.getLeft(), pair.getRight(), false, fillBoxes);
                }

                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getChestPositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.alteredDungeonChestColorKey)), 
                    false, 
                    fillBoxes
                );

                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getSpiderPositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.alteredDungeonSpiderColorKey)), 
                    true, 
                    fillBoxes
                );
                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getSkeletonPositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.alteredDungeonSkeletonColorKey)), 
                    true, 
                    fillBoxes
                );
                RenderUtil.drawBlockSquares(
                    context, 
                    alteredDungeon.getZombiePositions(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.alteredDungeonZombieColorKey)), 
                    true, 
                    fillBoxes
                );

                RenderUtil.drawLine(
                    context, 
                    alteredDungeon.getSpawnerPosition(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.alteredDungeonSpawnerColorKey)), 
                    false, 
                    fillBoxes
                );
            }

            List<AlteredMine> alteredMines = minesBuffer.readBuffer();
            for (AlteredMine alteredMine: alteredMines) {
                
                for (Pair<Vec3d, Color> pair: alteredMine.getPillarPositions()) {

                    RenderUtil.drawBlockSquare(context, pair.getLeft(), pair.getRight(), false, fillBoxes);
                }

                RenderUtil.drawLine(
                    context, 
                    alteredMine.getchestMinecartPosition(), 
                    new Color(this.featureConfig.getIntegerConfigs().get(this.alteredMineChestMinecartColorKey)), 
                    false, 
                    fillBoxes
                );
            }
        }
    }

    @Override
    public void clear () {
        
        this.dungeonsBuffer.updateBuffer(Collections.emptyList());
        this.dungeonsTempMap.clear();
        this.dungeonsCache.clear();

        this.minesBuffer.updateBuffer(Collections.emptyList());
        this.minesTempMap.clear();
        this.minesCache.clear();
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

    public boolean hasPillar (BlockPos pos, int horizontalSearchRadius, int minimumPillarHeight) {

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

        final AlteredDungeon alteredDungeon = new AlteredDungeon();
        final int minimumPillarHeight = this.featureConfig.getIntegerConfigs().get(this.alteredDungeonMinimumPillarHeightKey);
        final int horizontalSearchRadius = this.featureConfig.getIntegerConfigs().get(this.alteredDungeonMinimumPillarHeightKey);
        List<Pair<Vec3d, Color>> pillarPositions = alteredDungeon.getPillarPositions();
        this.addPillarPositions(pos, horizontalSearchRadius, minimumPillarHeight, pillarPositions);

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

    private void addPillarPositions (
        BlockPos pos, 
        Integer horizontalSearchRadius, 
        Integer minimumPillarHeight,
        List<Pair<Vec3d, Color>> pillarPositions
    ) {
        for (int x = pos.getX() - horizontalSearchRadius; x <= pos.getX() + horizontalSearchRadius; x++) {

            for (int z = pos.getZ() - horizontalSearchRadius; z <= pos.getZ() + horizontalSearchRadius; z++) {

                int topY = Constants.MC_CLIENT_INSTANCE.world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z) - 1;
                BlockPos bottomPos = new BlockPos(x, pos.getY(), z);
                BlockPos topPos = new BlockPos(x, topY, z);
                List<BlockPos> result = new ArrayList<>();
                for (BlockPos pillarPos : BlockPos.iterate(bottomPos, topPos)) {

                    BlockPos pillarPosCopy = new BlockPos(pillarPos);
                    Color defaultColor = new Color(this.featureConfig.getIntegerConfigs()
                            .get(this.alteredDungeonPillarDefaultColorKey));
                    if (isDifferentFromSurroundingSolidBlocks(pillarPosCopy)) {

                        result.add(pillarPosCopy);

                        if (pillarPosCopy.getY() == topY
                                && result.size() >= minimumPillarHeight) {

                            pillarPositions
                                    .addAll(result.stream().map(
                                            r -> new Pair<>(RenderUtil.toVec3d(r),
                                                    getBlockPosColor(r, defaultColor)))
                                            .toList());
                        }
                    } else {

                        if (result.size() >= minimumPillarHeight) {

                            pillarPositions
                                    .addAll(result.stream().map(
                                            r -> new Pair<>(RenderUtil.toVec3d(r),
                                                    getBlockPosColor(r, defaultColor)))
                                            .toList());
                        }

                        result.clear();
                    }
                }
            }
        }
    }

    private Color getBlockPosColor (BlockPos pos, Color defaultColor) {

        BlockState blockState = Constants.MC_CLIENT_INSTANCE.world.getBlockState(pos);
        MapColor mapColor = blockState.getMapColor(Constants.MC_CLIENT_INSTANCE.world, pos);
        if (mapColor != null) {

            return new Color(mapColor.color);
        } else {

            return defaultColor;
        }
    }
}
