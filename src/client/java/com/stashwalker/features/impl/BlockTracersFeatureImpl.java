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
    private final List<Pair<BlockPos, Color>> positionsTemp = Collections.synchronizedList(new ArrayList<>());

    {

        this.enabled = false;
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
    public void processPosition (BlockPos pos, int chunkX, int chunkZ) {

        if (this.enabled) {

            if (FinderUtil.isInterestingBlockPosition(pos, chunkX, chunkZ)) {

                String key = featureColorsKeyStart + "_"
                        + Constants.MC_CLIENT_INSTANCE.world.getBlockState(pos).getBlock()
                                .getName().getString().replace(" ", "_");
                this.positionsTemp.add(new Pair<BlockPos, Color>(pos, featureColors.get(key).getKey()));
            }
        }
    }

    @Override
    public void processChunk (Chunk chunk) {

        if (this.enabled) {

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

        if (this.enabled) {

            this.buffer.updateBuffer(Collections.synchronizedList(positionsTemp));
            this.positionsTemp.clear();
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
