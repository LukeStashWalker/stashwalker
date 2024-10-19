package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.Pair;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ChunkProcessor;
import com.stashwalker.utils.MapUtil;
import com.stashwalker.utils.SignTextExtractor;

import io.netty.util.internal.StringUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SignReaderFeatureImpl extends AbstractBaseFeature implements ChunkProcessor  {


    private final String signTextColorKey = "signTextColor";
    private final Color signTextColorDefaultValue = Color.CYAN;

    private final String ignoreWordListKey = "ignoreWordList";
    private final String ignoreWordListDefaultValue = "cody,example1,example2";

    private final String messageSoundKey = "messageSound";
    private final Boolean messageSoundDefaultValue = true;
    private final String announceMessagesInChatKey = "announceMessagesInChat";
    private final Boolean announceMessagesInChatDefaultValue = false;

    public SignReaderFeatureImpl () {

        super();

        this.featureName = FEATURE_NAME_SIGN_READER;

        this.defaultIntegerMap.put(this.signTextColorKey, this.signTextColorDefaultValue.getRGB());

        this.defaultStringMap.put(this.ignoreWordListKey, this.ignoreWordListDefaultValue);

        this.defaultBooleanMap.put(this.messageSoundKey, this.messageSoundDefaultValue);
        this.defaultBooleanMap.put(this.announceMessagesInChatKey, this.announceMessagesInChatDefaultValue);

        this.featureConfig.setIntegerConfigs(MapUtil.deepCopy(this.defaultIntegerMap));
        this.featureConfig.setBooleanConfigs(MapUtil.deepCopy(this.defaultBooleanMap));
        this.featureConfig.setStringConfigs(MapUtil.deepCopy(this.defaultStringMap));
    }

    @Override
    public void processChunkLoad (Chunk chunk) {

        if (enabled) {

            Set<BlockPos> blockPositions = chunk.getBlockEntityPositions();
            if (blockPositions != null) {

                int signCount = 0;

                for (BlockPos pos : blockPositions) {

                    BlockEntity blockEntity = chunk.getBlockEntity(pos);

                    // Check for signs
                    if (blockEntity instanceof SignBlockEntity) {

                        String signText = SignTextExtractor.getSignText((SignBlockEntity) blockEntity);
                        String ignoreListString = this.featureConfig.getStringConfigs().get(this.ignoreWordListKey);
                        List<String> ignoreList = Arrays.stream(ignoreListString != null ? ignoreListString.split(",") : new String[]{})
                            .filter(s -> !StringUtil.isNullOrEmpty(s))
                            .map(s -> s.toLowerCase())
                            .toList();
                        if (
                            !signText.isEmpty()
                            && !signText.equals("<----\n---->")
                            && signCount < 50 // Limit to 50 signs per chunk in case somebody goes nuts with the sign placement
                            && !ignoreList.stream().anyMatch(i -> signText.toLowerCase().contains(i))
                        ) {

                            Boolean sound = this.featureConfig.getBooleanConfigs().get(this.messageSoundKey);
                            if (this.featureConfig.getBooleanConfigs().get(this.announceMessagesInChatKey)) {

                                Constants.CHAT_BUFFER.add(new Pair<>("I just found a sign that says: " + signText, sound));
                            } else {

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
                                                .setStyle(Style.EMPTY.withColor(this.featureConfig.getIntegerConfigs()
                                                        .get(this.signTextColorKey))));

                                Constants.MESSAGES_BUFFER.add(new Pair<>(styledText, sound));

                                signCount++;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void update () {

    }

    @Override
    public void processChunkUnload(Chunk chunk) {
    }
}
