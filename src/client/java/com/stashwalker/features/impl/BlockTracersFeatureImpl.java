package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ChunkLoadProcessor;
import com.stashwalker.features.Processor;
import com.stashwalker.features.Renderable;
import com.stashwalker.utils.FinderUtil;
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
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class BlockTracersFeatureImpl extends AbstractBaseFeature implements Processor, ChunkLoadProcessor, Renderable  {

    private final DoubleListBuffer<Pair<BlockPos, Color>> buffer = new DoubleListBuffer<>();

    {

        this.featureName = FEATURE_NAME_BLOCK_TRACER;
        this.featureColorsKeyStart = "Block_Tracers";

        // Default render colors
        this.featureColors.put(this.featureColorsKeyStart + "_" + Blocks.CHEST.getName().getString().replace(" ", "_"), new Pair<>(Color.YELLOW, Color.YELLOW));

        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.BARREL.getName().getString().replace(" ", "_"), new Pair<>(new Color(210, 105, 30), new Color(210, 105, 30)));

        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.WHITE_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.ORANGE_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.MAGENTA_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.LIGHT_BLUE_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.YELLOW_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.LIME_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.PINK_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.GRAY_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.LIGHT_GRAY_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.CYAN_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.PURPLE_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.BLUE_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.BROWN_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.GREEN_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.RED_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.BLACK_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.HOPPER.getName().getString().replace(" ", "_"), new Pair<>(Color.BLACK,  Color.BLACK));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.DROPPER.getName().getString().replace(" ", "_"), new Pair<>(Color.BLACK, Color.BLACK));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.DISPENSER.getName().getString().replace(" ", "_"), new Pair<>(Color.BLACK, Color.BLACK));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.BLAST_FURNACE.getName().getString().replace(" ", "_"), new Pair<>(Color.BLACK, Color.BLACK));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.FURNACE.getName().getString().replace(" ", "_"), new Pair<>(Color.BLACK, Color.BLACK));

        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.OAK_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.SPRUCE_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.BIRCH_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.ACACIA_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.CHERRY_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.JUNGLE_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.DARK_OAK_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.CRIMSON_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.MANGROVE_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.BAMBOO_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.WARPED_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));

        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.OAK_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.SPRUCE_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.BIRCH_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.ACACIA_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.CHERRY_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.JUNGLE_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.DARK_OAK_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.CRIMSON_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.MANGROVE_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.BAMBOO_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.WARPED_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));

        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.OAK_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.SPRUCE_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.BIRCH_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.ACACIA_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.CHERRY_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.JUNGLE_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.DARK_OAK_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.CRIMSON_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.MANGROVE_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.BAMBOO_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        this.featureColors.put(this.featureColorsKeyStart +  "_" + Blocks.WARPED_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
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
            final List<Pair<BlockPos, Color>> positionsTemp = Collections.synchronizedList(new ArrayList<>());

            for (int x = xStart; x < xEnd; x++) {

                for (int z = zStart; z < zEnd; z++) {

                    Chunk chunk = FinderUtil.getChunkEarly(x, z);

                    if (chunk != null) {

                        Set<BlockPos> blockPositions = chunk.getBlockEntityPositions();
                        if (blockPositions != null) {

                            for (BlockPos pos : blockPositions) {

                                RegistryKey<World> dimensionKey = Constants.MC_CLIENT_INSTANCE.world.getRegistryKey();
                                if (World.OVERWORLD.equals(dimensionKey)) {

                                    if (this.isInterestingBlockPosition(pos, x, z)) {

                                        String key = featureColorsKeyStart + "_"
                                                + Constants.MC_CLIENT_INSTANCE.world.getBlockState(pos).getBlock()
                                                        .getName().getString().replace(" ", "_");
                                        positionsTemp
                                                .add(new Pair<BlockPos, Color>(pos, featureColors.get(key).getKey()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            this.buffer.updateBuffer(positionsTemp);
        }
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

                    RenderUtil.drawLine(context, newBlockPos, color, false);
                }
            }
        }
    }

    @Override
    public void clear () {
        
        this.buffer.updateBuffer(Collections.emptyList());
    }

    public boolean isInterestingBlockPosition (BlockPos pos, int chunkX, int chunkZ) {

        ClientWorld world = Constants.MC_CLIENT_INSTANCE.world;
        if (

                FinderUtil.isBlockType(pos, Blocks.BARREL)

                ||

                (FinderUtil.areAdjacentChunksLoaded(chunkX, chunkZ)
                        &&
                        (FinderUtil.isDoubleChest(world, pos)
                                && (
                                // Not a Dungeon
                                !FinderUtil.isBlockInHorizontalRadius(world, pos.down(), 5, Blocks.MOSSY_COBBLESTONE)
                                        && !FinderUtil.isBlockInHorizontalRadius(world, pos, 5, Blocks.SPAWNER))))

                || FinderUtil.isBlockType(pos, Blocks.SHULKER_BOX)
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

                || FinderUtil.isBlockType(pos, Blocks.HOPPER)
                || FinderUtil.isBlockType(pos, Blocks.DROPPER)
                || FinderUtil.isBlockType(pos, Blocks.DISPENSER)
                || FinderUtil.isBlockType(pos, Blocks.BLAST_FURNACE)
                || FinderUtil.isBlockType(pos, Blocks.FURNACE)

                || FinderUtil.isBlockType(pos, Blocks.OAK_SIGN)
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
                || FinderUtil.isBlockType(pos, Blocks.WARPED_HANGING_SIGN)

        ) {

            return true;
        } else {

            return false;
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
