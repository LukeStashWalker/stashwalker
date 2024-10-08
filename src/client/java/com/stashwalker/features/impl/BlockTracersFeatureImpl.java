package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ChunkLoadProcessor;
import com.stashwalker.features.PositionProcessor;
import com.stashwalker.features.Renderable;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.MapUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

public class BlockTracersFeatureImpl extends AbstractBaseFeature implements PositionProcessor, ChunkLoadProcessor, Renderable  {

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
    private final String signColorKey = "blastFurnaceColor";
    private final Color signColorDefaultValue = Color.CYAN;
    private final String fillInBoxesKey = "fillInBoxes";
    private final Boolean fillInBoxesDefaultValue = false;

    public BlockTracersFeatureImpl () {

        super();

        this.featureName = FEATURE_NAME_BLOCK_TRACER;

        this.defaultIntegerMap.put(chestColorKey, chestColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(barrelColorKey, barrelColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(shulkerColorKey, shulkerColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(hopperColorKey, hopperColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(dropperColorKey, dropperColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(dispenserColorKey, dispenserColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(furnaceColorKey, furnaceColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(blastFurnaceColorKey, blastFurnaceColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(signColorKey, signColorDefaultValue.getRGB());

        this.defaultBooleanMap.put(fillInBoxesKey, fillInBoxesDefaultValue);

        this.featureConfig.setIntegerConfigs(MapUtil.deepCopy(this.defaultIntegerMap));
        this.featureConfig.setBooleanConfigs(MapUtil.deepCopy(this.defaultBooleanMap));
        this.featureConfig.setStringConfigs(MapUtil.deepCopy(this.defaultStringMap));
    }

    @Override
    public void process (BlockPos pos, UUID callIdentifier) {

        if (enabled) {

            if (!this.positionsTempMap.containsKey(callIdentifier)) {

                this.positionsTempMap.put(callIdentifier, Collections.synchronizedList(new ArrayList<>()));
            } 

            this.isInterestingBlockPosition(pos).ifPresent(c -> {

                this.positionsTempMap.get(callIdentifier)
                        .add(new Pair<BlockPos, Color>(pos, c));
            });
        }
    }

    @Override
    public void update (UUID callIdentifier) {

        this.buffer.updateBuffer(this.positionsTempMap.get(callIdentifier));
        this.positionsTempMap.remove(callIdentifier);
    }

    @Override
    public void processLoadedChunk (Chunk chunk) {

        if (this.enabled) {

            if (this.hasSolidBlocksNearBuildLimit(chunk)) {

                Text styledText = Text.empty()
                        .append(Text.literal("[")
                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal("Stashwalker, ")
                                .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                        .append(Text.literal("blockEntities")
                                .setStyle(Style.EMPTY.withColor(Formatting.BLUE)))
                        .append(Text.literal("]:\n")
                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal("Blocks found near (old) build limit")
                                .setStyle(Style.EMPTY.withColor(Formatting.RED)));
                Constants.MESSAGES_BUFFER.add(styledText);
            }
        }
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

    public Optional<Color> isInterestingBlockPosition(BlockPos pos) {

        ClientWorld world = Constants.MC_CLIENT_INSTANCE.world;
        Map<String, Integer> integerMap = this.featureConfig.getIntegerConfigs();
        if (FinderUtil.isBlockType(pos, Blocks.BARREL)) {

            return Optional.of(new Color(integerMap.get(this.barrelColorKey)));
        } else if (

        (FinderUtil.isDoubleChest(world, pos)
                && (
                // Not a Dungeon
                !FinderUtil.isBlockInHorizontalRadius(world, pos.down(), 5, Blocks.MOSSY_COBBLESTONE)
                        && !FinderUtil.isBlockInHorizontalRadius(world, pos, 5, Blocks.SPAWNER)))) {

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
                || FinderUtil.isBlockType(pos, Blocks.BLACK_SHULKER_BOX)) {

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
        } else if (FinderUtil.isBlockType(pos, Blocks.OAK_SIGN)
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

    public boolean hasSolidBlocksNearBuildLimit (Chunk chunk) {

        if (chunk != null) {

            ChunkPos chunkPos = chunk.getPos();

            ClientWorld world = Constants.MC_CLIENT_INSTANCE.world;
            if (world != null) {

                RegistryKey<World> dimensionKey = world.getRegistryKey();
                if (World.OVERWORLD.equals(dimensionKey)) {

                    int[] yLevels = new int[] {
                            319, 318, 255, 254
                    };
                    for (int yLevel : yLevels) {

                        for (BlockPos pos : BlockPos.iterate(
                            chunkPos.getStartX(), yLevel, chunkPos.getStartZ(),
                            chunkPos.getEndX(), yLevel, chunkPos.getEndZ())
                        ) {

                            BlockState blockState = Constants.MC_CLIENT_INSTANCE.world.getBlockState(pos);
                            if (!blockState.isAir()) {
                                
                                return true;
                            }
                        }
                    }

                    return false;
                } else {

                    return false;
                }         
            } else {

                return false;
            }

        } else {

            return false;
        }
    }
}
