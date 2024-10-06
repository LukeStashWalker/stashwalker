package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.ConcurrentBoundedSet;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ChunkScanProcessor;
import com.stashwalker.utils.SignTextExtractor;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.Set;

public class SignReaderFeatureImpl extends AbstractBaseFeature implements ChunkScanProcessor  {

    private final ConcurrentBoundedSet<Integer> displayedSignsCache = new ConcurrentBoundedSet<>(5000);

    {

        this.featureName = FEATURE_NAME_SIGN_READER;
        this.featureColorsKeyStart = "Sign_Reader";

        this.featureColors.put(this.featureColorsKeyStart, new Pair<>(Color.CYAN, Color.CYAN));
    }

    @Override
    public void processScannedChunk (Chunk chunk) {

        if (enabled) {

            Set<BlockPos> blockPositions = chunk.getBlockEntityPositions();
            if (blockPositions != null) {

                for (BlockPos pos : blockPositions) {

                    BlockEntity blockEntity = chunk.getBlockEntity(pos);

                    // Check for signs
                    if (blockEntity instanceof SignBlockEntity) {

                        // Get the player's current position
                        BlockPos playerPos = Constants.MC_CLIENT_INSTANCE.player.getBlockPos();

                        // Calculate the distance between the player's position and the sign's
                        // position
                        double squaredDistance = blockEntity.getPos().getSquaredDistance(playerPos);

                        String signText = SignTextExtractor.getSignText((SignBlockEntity) blockEntity);
                        if (!this.displayedSignsCache.contains(blockEntity.getPos().toShortString().hashCode())
                                && !signText.isEmpty()
                                && squaredDistance > 5 * 5
                                && !signText.equals("<----\n---->")) {

                            Text styledText = Text.empty()
                                    .append(Text.literal("[")
                                            .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                                    .append(Text.literal("Stashwalker, ")
                                            .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                                    .append(Text.literal("signReader")
                                            .setStyle(Style.EMPTY.withColor(Formatting.BLUE)))
                                    .append(Text.literal("]:\n")
                                            .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                                    .append(Text.literal(signText)
                                            .setStyle(Style.EMPTY.withColor(this.featureColors.get(featureColorsKeyStart).getKey().getRGB())));

                            Constants.MESSAGES_BUFFER.add(styledText);

                            this.displayedSignsCache
                                    .add(blockEntity.getPos().toShortString().hashCode());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void clear () {
        
        displayedSignsCache.clear();
    }
}
