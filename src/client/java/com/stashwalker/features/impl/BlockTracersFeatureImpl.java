package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ChunkProcessor;
import com.stashwalker.features.PositionProcessor;
import com.stashwalker.features.Renderable;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.MapUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BlockTracersFeatureImpl extends AbstractBaseFeature implements PositionProcessor, ChunkProcessor, Renderable  {

    private final Map<UUID, List<Pair<BlockPos, Color>>> positionsTempMap = Collections.synchronizedMap(new HashMap<>());
    private final DoubleListBuffer<Pair<BlockPos, Color>> buffer = new DoubleListBuffer<>();

    private final String chestColorKey = "chestColor";
    private final Color chestColorDefaultValue = Color.YELLOW;
    private final String barrelColorKey = "barrelColor";
    private final Color barrelColorDefaultValue = new Color(210, 105, 30);
    private final String shulkerColorKey = "shulkerColor";
    private final Color shulkerColorDefaultValue = Color.WHITE;
    private final String hopperColorKey = "hopperColor";
    private final Color hopperColorDefaultValue = Color.BLACK;
    private final String dropperColorKey = "dropperColor";
    private final Color dropperColorDefaultValue = Color.BLACK;
    private final String dispenserColorKey = "dispenserColor";
    private final Color dispenserColorDefaultValue = Color.BLACK;
    private final String furnaceColorKey = "furnaceColor";
    private final Color furnaceColorDefaultValue = Color.BLACK;
    private final String blastFurnaceColorKey = "blastFurnaceColor";
    private final Color blastFurnaceColorDefaultValue = Color.BLACK;
    private final String signColorKey = "signColorColor";
    private final Color signColorDefaultValue = Color.CYAN;

    private final String closeProximitySingleChestsMinimumAmountKey = "closeProximitySingleChestsMinimumAmount";
    private final Integer closeProximitySingleChestsMinimumAmountDefaultValue = 10;
    private final String closeProximitySingleChestsMaximumBlockDistanceKey = "closeProximitySingleChestsMaximumBlockDistance";
    private final Integer closeProximitySingleChestsMaximumBlockDistanceDefaultValue = 10;

    private final String fillInBoxesKey = "fillInBoxes";
    private final Boolean fillInBoxesDefaultValue = true;
    private final String messageSoundKey = "messageSound";
    private final Boolean messageSoundDefaultValue = true;

    public BlockTracersFeatureImpl () {

        super();

        this.featureName = FEATURE_NAME_BLOCK_TRACER;

        this.defaultIntegerMap.put(this.chestColorKey, this.chestColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.barrelColorKey, this.barrelColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.shulkerColorKey, this.shulkerColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.hopperColorKey, this.hopperColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.dropperColorKey, this.dropperColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.dispenserColorKey, this.dispenserColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.furnaceColorKey, this.furnaceColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.blastFurnaceColorKey, this.blastFurnaceColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.signColorKey, this.signColorDefaultValue.getRGB());

        this.defaultIntegerMap.put(this.closeProximitySingleChestsMinimumAmountKey, this.closeProximitySingleChestsMinimumAmountDefaultValue);
        this.defaultIntegerMap.put(this.closeProximitySingleChestsMaximumBlockDistanceKey, this.closeProximitySingleChestsMaximumBlockDistanceDefaultValue);

        this.defaultBooleanMap.put(this.messageSoundKey, this.messageSoundDefaultValue);
        this.defaultBooleanMap.put(this.fillInBoxesKey, this.fillInBoxesDefaultValue);

        this.featureConfig.setIntegerConfigs(MapUtil.deepCopy(this.defaultIntegerMap));
        this.featureConfig.setBooleanConfigs(MapUtil.deepCopy(this.defaultBooleanMap));
        this.featureConfig.setStringConfigs(MapUtil.deepCopy(this.defaultStringMap));
    }

    @Override
    public void processBlockPos (UUID callIdentifier, BlockPos pos) {

        if (this.enabled) {

            if (!this.positionsTempMap.containsKey(callIdentifier)) {

                this.positionsTempMap.put(callIdentifier, new ArrayList<>());
            } 

            this.isInterestingBlockPosition(pos).ifPresent(c -> {

                this.positionsTempMap.get(callIdentifier)
                        .add(new Pair<BlockPos, Color>(pos, c));
            });
        }
    }

    @Override
    public void updateBlockPositions (UUID callIdentifier) {

        if (this.enabled) {

            this.buffer.updateBuffer(this.positionsTempMap.get(callIdentifier));
            this.positionsTempMap.remove(callIdentifier);
        }
    }

    @Override
    public void processChunkLoad (Chunk chunk) {

        if (this.enabled) {

            this.checkChunkForBlockNearBuildLimit(chunk);
        }
    }

    @Override
    public void processChunkUnload (Chunk chunk) {

    }

    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            List<Pair<BlockPos, Color>> blockpositions = this.buffer.readBuffer();
            if (blockpositions != null) {

                for (Pair<BlockPos, Color> pair : blockpositions) {

                    BlockPos blockPos = pair.getKey();
                    Color color = pair.getValue();
                    Vec3d newBlockPos = new Vec3d(
                            blockPos.getX() + 0.5D,
                            blockPos.getY() + 0.5D,
                            blockPos.getZ() + 0.5D
                    );

                    RenderUtil.drawLine(context, newBlockPos, color, false, this.featureConfig.getBooleanConfigs().get(fillInBoxesKey));
                }
            }
        }
    }

    @Override
    public void clear () {
        
        this.buffer.updateBuffer(Collections.emptyList());
    }

    private Optional<Color> isInterestingBlockPosition (BlockPos pos) {

        Map<String, Integer> integerMap = this.featureConfig.getIntegerConfigs();
        if (FinderUtil.isBlockType(pos, Blocks.BARREL)) {

            return Optional.of(new Color(integerMap.get(this.barrelColorKey)));
        } else if (

        (
            (
                this.isDoubleChest(pos)
                && (
                    // Not a Dungeon
                    !this.isBlockInHorizontalRadius(pos.down(), 5, Blocks.MOSSY_COBBLESTONE)
                    && !this.isBlockInHorizontalRadius(pos, 5, Blocks.SPAWNER))
                )
            )

            ||

            this.isProximitySingleChest(pos)
        ) {

            return Optional.of(new Color(integerMap.get(this.chestColorKey)));
        } else if (

            FinderUtil.isBlockType(pos, Blocks.SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.WHITE_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.ORANGE_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.MAGENTA_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.LIGHT_BLUE_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.YELLOW_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.LIME_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.PINK_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.GRAY_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.LIGHT_GRAY_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.CYAN_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.PURPLE_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.BLUE_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.BROWN_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.GREEN_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.RED_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.BLACK_SHULKER_BOX)
        ) {

            return Optional.of(new Color(integerMap.get(this.shulkerColorKey)));
        } else if (

        FinderUtil.isBlockType(pos, Blocks.HOPPER)) {

            return Optional.of(new Color(integerMap.get(this.hopperColorKey)));
        } else if (

        FinderUtil.isBlockType(pos, Blocks.DROPPER)) {

            return Optional.of(new Color(integerMap.get(this.dropperColorKey)));
        } else if (

        FinderUtil.isBlockType(pos, Blocks.DISPENSER)) {

            return Optional.of(new Color(integerMap.get(this.dispenserColorKey)));
        } else if (

        FinderUtil.isBlockType(pos, Blocks.BLAST_FURNACE)) {

            return Optional.of(new Color(integerMap.get(this.blastFurnaceColorKey)));
        } else if (

        FinderUtil.isBlockType(pos, Blocks.FURNACE)) {

            return Optional.of(new Color(integerMap.get(this.furnaceColorKey)));
        } else if (
            FinderUtil.isBlockType(pos, Blocks.OAK_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.SPRUCE_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.BIRCH_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.ACACIA_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.CHERRY_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.JUNGLE_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.DARK_OAK_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.CRIMSON_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.MANGROVE_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.BAMBOO_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.WARPED_SIGN)

            || FinderUtil.isBlockType(pos, Blocks.OAK_WALL_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.SPRUCE_WALL_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.BIRCH_WALL_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.ACACIA_WALL_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.CHERRY_WALL_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.JUNGLE_WALL_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.DARK_OAK_WALL_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.CRIMSON_WALL_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.MANGROVE_WALL_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.BAMBOO_WALL_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.WARPED_WALL_SIGN)

            || FinderUtil.isBlockType(pos, Blocks.OAK_HANGING_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.SPRUCE_HANGING_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.BIRCH_HANGING_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.ACACIA_HANGING_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.CHERRY_HANGING_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.JUNGLE_HANGING_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.DARK_OAK_HANGING_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.CRIMSON_HANGING_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.MANGROVE_HANGING_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.BAMBOO_HANGING_SIGN)
            || FinderUtil.isBlockType(pos, Blocks.WARPED_HANGING_SIGN)) {

            return Optional.of(new Color(integerMap.get(this.signColorKey)));
        } else {

            return Optional.empty();
        }
    }

    public void checkChunkForBlockNearBuildLimit (Chunk chunk) {

        if (chunk != null) {

            ChunkPos chunkPos = chunk.getPos();

            ClientWorld world = Constants.MC_CLIENT_INSTANCE.world;
            if (world != null) {

                RegistryKey<World> dimensionKey = world.getRegistryKey();
                if (World.OVERWORLD.equals(dimensionKey)) {

                    boolean sentMessage = false;
                    int[] yLevels = new int[] {
                            319, 318, 255, 254
                    };
                    for (int yLevel : yLevels) {

                        for (BlockPos pos : BlockPos.iterate(
                                chunkPos.getStartX(), yLevel, chunkPos.getStartZ(),
                                chunkPos.getEndX(), yLevel, chunkPos.getEndZ())) {

                            BlockState blockState = Constants.MC_CLIENT_INSTANCE.world.getBlockState(pos);
                            if (!blockState.isAir() && !sentMessage) {

                                Text styledText = Text.empty()
                                        .append(Text.literal("[")
                                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                                        .append(Text.literal("Stashwalker, ")
                                                .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                                        .append(Text.literal("blockEntities")
                                                .setStyle(Style.EMPTY.withColor(Formatting.BLUE)))
                                        .append(Text.literal("]:\n")
                                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                                        .append(Text.literal(String.format("Blocks found near (old) build limit: %s", pos.toShortString()))
                                                .setStyle(Style.EMPTY.withColor(Formatting.RED)));
                                Constants.MESSAGES_BUFFER.add(new Pair<>(styledText, this.featureConfig.getBooleanConfigs().get(this.messageSoundKey)));

                                sentMessage = true;
                            }
                        }
                    }

                }
            }
        }
    }

    private boolean isProximitySingleChest (BlockPos pos) {

        pos = new BlockPos(pos);

        if (!this.isSingleChest(pos)) {

            return false;
        }

        int distance = this.featureConfig.getIntegerConfigs().get(this.closeProximitySingleChestsMaximumBlockDistanceKey);
        int amount = this.featureConfig.getIntegerConfigs().get(this.closeProximitySingleChestsMinimumAmountKey);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int count = 0;
        for (int i = x - distance; i < x + distance + 1; i++) {

            for (int j = y - distance; j < y + distance + 1; j++) {

                for (int k = z - distance; k < z + distance + 1; k++) {

                    BlockPos p = new BlockPos(i, j, k);
                    if (
                        !p.equals(pos)
                        && isSingleChest(p)
                    ) {

                        count++;

                        if (1 + count >= amount) { // The count is not including the pos itself

                            return true;
                        }
                    }

                }
            }
        }

        return false;
    }

    private boolean isSingleChest (BlockPos pos) {

        if (!FinderUtil.isBlockType(pos, Blocks.CHEST) && !FinderUtil.isBlockType(pos, Blocks.TRAPPED_CHEST)) {

            return false;
        }

        ClientWorld world = Constants.MC_CLIENT_INSTANCE.world;
        BlockState state = world.getBlockState(pos);

        Direction facing = state.get(Properties.HORIZONTAL_FACING);

        Direction[] relevantSides = (facing == Direction.NORTH || facing == Direction.SOUTH)
                ? new Direction[] { Direction.WEST, Direction.EAST }
                : new Direction[] { Direction.NORTH, Direction.SOUTH };

        for (Direction direction : relevantSides) {

            BlockPos adjacentPos = pos.offset(direction);
            BlockState adjacentState = world.getBlockState(adjacentPos);
            Block adjacentBlock = adjacentState.getBlock();

            if (
                (adjacentBlock == Blocks.CHEST || adjacentBlock == Blocks.TRAPPED_CHEST)
                && adjacentState.get(Properties.HORIZONTAL_FACING) == facing
            ) {

                return false;
            }
        }

        return true;
    }

    private boolean isDoubleChest (BlockPos pos) {

        if (!FinderUtil.isBlockType(pos, Blocks.CHEST) && !FinderUtil.isBlockType(pos, Blocks.TRAPPED_CHEST)) {

            return false;
        }

        ClientWorld world = Constants.MC_CLIENT_INSTANCE.world;
        BlockState state = world.getBlockState(pos);

        Direction facing = state.get(Properties.HORIZONTAL_FACING);

        Direction[] relevantSides = (facing == Direction.NORTH || facing == Direction.SOUTH)
                ? new Direction[] { Direction.WEST, Direction.EAST }
                : new Direction[] { Direction.NORTH, Direction.SOUTH };

        for (Direction direction : relevantSides) {

            BlockPos adjacentPos = pos.offset(direction);
            BlockState adjacentState = world.getBlockState(adjacentPos);
            Block adjacentBlock = adjacentState.getBlock();

            if ((adjacentBlock == Blocks.CHEST || adjacentBlock == Blocks.TRAPPED_CHEST)
                    && adjacentState.get(Properties.HORIZONTAL_FACING) == facing) {

                return true;
            }
        }

        return false;
    }

    private boolean isBlockInHorizontalRadius (BlockPos pos, int radius, Block block) {


            BlockPos startPos = new BlockPos(pos.getX() - radius, pos.getY(), pos.getZ() - radius);
            BlockPos endPos = new BlockPos(pos.getX() + radius, pos.getY(), pos.getZ() + radius);
            for (BlockPos p : BlockPos.iterate(startPos, endPos)) {

                if (
                    !p.equals(pos)
                    && FinderUtil.isBlockType(p, block)
                ) {
                    
                    return true;
                }
            }

            return false;
    }
}
