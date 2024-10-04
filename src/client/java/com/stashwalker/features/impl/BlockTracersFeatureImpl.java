package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ChunkLoadProcessFeature;
import com.stashwalker.features.ProcessPositionFeature;
import com.stashwalker.features.RenderableFeature;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Blocks;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class BlockTracersFeatureImpl extends AbstractBaseFeature implements ProcessPositionFeature, ChunkLoadProcessFeature, RenderableFeature  {

    private final DoubleListBuffer<Pair<BlockPos, Color>> buffer = new DoubleListBuffer<>();
    private final List<Pair<BlockPos, Color>> positionsTemp = new ArrayList<>();

    {

        enabled = false;
        featureName = FEATURE_NAME_BLOCK_TRACER;
        featureColorsKeyStart = "Block_Tracers_Block_";

        // Default render colors
        featureColors.put(featureColorsKeyStart + Blocks.CHEST.getName().getString().replace(" ", "_"), new Pair<>(Color.YELLOW, Color.YELLOW));

        featureColors.put(featureColorsKeyStart + Blocks.BARREL.getName().getString().replace(" ", "_"), new Pair<>(new Color(210, 105, 30), new Color(210, 105, 30)));

        featureColors.put(featureColorsKeyStart + Blocks.SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.WHITE_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.ORANGE_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.MAGENTA_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.LIGHT_BLUE_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.YELLOW_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.LIME_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.PINK_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.GRAY_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.LIGHT_GRAY_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.CYAN_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.PURPLE_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.BLUE_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.BROWN_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.GREEN_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.RED_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        featureColors.put(featureColorsKeyStart + Blocks.BLACK_SHULKER_BOX.getName().getString().replace(" ", "_"), new Pair<>(Color.WHITE, Color.WHITE));
        
        featureColors.put(featureColorsKeyStart + Blocks.HOPPER.getName().getString().replace(" ", "_"), new Pair<>(Color.BLACK,  Color.BLACK));
        featureColors.put(featureColorsKeyStart + Blocks.DROPPER.getName().getString().replace(" ", "_"), new Pair<>(Color.BLACK, Color.BLACK));
        featureColors.put(featureColorsKeyStart + Blocks.DISPENSER.getName().getString().replace(" ", "_"), new Pair<>(Color.BLACK, Color.BLACK));
        featureColors.put(featureColorsKeyStart + Blocks.BLAST_FURNACE.getName().getString().replace(" ", "_"), new Pair<>(Color.BLACK, Color.BLACK));
        featureColors.put(featureColorsKeyStart + Blocks.FURNACE.getName().getString().replace(" ", "_"), new Pair<>(Color.BLACK, Color.BLACK));

        featureColors.put(featureColorsKeyStart + Blocks.OAK_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.SPRUCE_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.BIRCH_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.ACACIA_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.CHERRY_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.JUNGLE_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.DARK_OAK_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.CRIMSON_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.MANGROVE_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.BAMBOO_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.WARPED_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));

        featureColors.put(featureColorsKeyStart + Blocks.OAK_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.SPRUCE_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.BIRCH_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.ACACIA_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.CHERRY_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.JUNGLE_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.DARK_OAK_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.CRIMSON_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.MANGROVE_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.BAMBOO_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.WARPED_WALL_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));

        featureColors.put(featureColorsKeyStart + Blocks.OAK_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.SPRUCE_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.BIRCH_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.ACACIA_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.CHERRY_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.JUNGLE_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.DARK_OAK_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.CRIMSON_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.MANGROVE_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.BAMBOO_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
        featureColors.put(featureColorsKeyStart + Blocks.WARPED_HANGING_SIGN.getName().getString().replace(" ", "_"), new Pair<>(Color.CYAN, Color.CYAN));
    }

    @Override
    public void processPosition (BlockPos pos, int chunkX, int chunkZ) {

        if (enabled) {

            if (FinderUtil.isInterestingBlockPosition(pos, chunkX, chunkZ)) {

                String key = featureColorsKeyStart
                        + Constants.MC_CLIENT_INSTANCE.world.getBlockState(pos).getBlock()
                                .getName().getString().replace(" ", "_");
                this.positionsTemp.add(new Pair<BlockPos, Color>(pos, featureColors.get(key).getKey()));
            }
        }
    }

    @Override
    public void processChunk (Chunk chunk) {

        if (enabled) {

            if (FinderUtil.hasSolidBlocksNearBuildLimit(chunk)) {

                Text styledText = Text.empty()
                        .append(Text.literal("[")
                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal("Stashwalker, ")
                                .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                        .append(Text.literal("blockEntities")
                                .setStyle(Style.EMPTY.withColor(Formatting.BLUE)))
                        .append(Text.literal("]:\n")
                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal("Solid blocks found near (old) build limit")
                                .setStyle(Style.EMPTY.withColor(Formatting.RED)));
                Constants.MESSAGES_BUFFER.add(styledText);
            }
        }
    }

    @Override
    public void update () {

        if (enabled) {

            this.buffer.updateBuffer(positionsTemp);
            this.positionsTemp.clear();
        }
    }


    @Override
    public void render (WorldRenderContext context) {

        if (enabled) {

            List<Pair<BlockPos, Color>> blockpositions = this.buffer.readBuffer();
            if (blockpositions != null) {

                for (Pair<BlockPos, Color> pair : blockpositions) {

                    BlockPos blockPos = pair.getKey();
                    Color color = pair.getValue();
                    Vec3d newBlockPos = new Vec3d(
                            blockPos.getX() + 0.5D,
                            blockPos.getY() + 0.5D,
                            blockPos.getZ() + 0.5D);

                    RenderUtil.drawLine(context, newBlockPos, color.getRed(), color.getGreen(),
                            color.getBlue(),
                            color.getAlpha(), false);
                }
            }
        }
    }

    @Override
    public void clear () {
        
        this.buffer.updateBuffer(Collections.emptyList());
    }
}
