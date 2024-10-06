package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ChunkLoadProcessor;
import com.stashwalker.utils.SignTextExtractor;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.Set;

public class SignReaderFeatureImpl extends AbstractBaseFeature implements ChunkLoadProcessor  {

    {

        this.featureName = FEATURE_NAME_SIGN_READER;
        this.featureColorsKeyStart = "Sign_Reader";

        this.featureColors.put(this.featureColorsKeyStart, new Pair<>(Color.CYAN, Color.CYAN));
    }

    @Override
    public void processLoadedChunk (Chunk chunk) {

        if (enabled) {

            Set<BlockPos> blockPositions = chunk.getBlockEntityPositions();
            if (blockPositions != null) {

                int signCount = 0;

                for (BlockPos pos : blockPositions) {

                    BlockEntity blockEntity = chunk.getBlockEntity(pos);

                    // Check for signs
                    if (blockEntity instanceof SignBlockEntity) {

                        String signText = SignTextExtractor.getSignText((SignBlockEntity) blockEntity);
                        if (
                            !signText.isEmpty()
                            && !signText.equals("<----\n---->")
                            && signCount < 50 // Limit to 50 signs per chunk in case somebody goes nuts with the sign placement
                        ) {

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

                            signCount++;
                        }
                    }
                }
            }
        }
    }
}
