package com.stashwalker.constants;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.*;
import java.util.stream.Collectors;
import com.stashwalker.configs.ConfigManager;
import com.stashwalker.features.Feature;
import com.stashwalker.features.impl.AlteredStructuresFeatureImpl;
import com.stashwalker.features.impl.BlockTracersFeatureImpl;
import com.stashwalker.features.impl.EntityTracersFeatureImpl;
import com.stashwalker.features.impl.NewChunksFeatureImpl;
import com.stashwalker.features.impl.SignReaderFeatureImpl;

public class Constants {

    public static final MinecraftClient MC_CLIENT_INSTANCE = MinecraftClient.getInstance();
    public static final ConfigManager CONFIG_MANAGER = new ConfigManager();
    public static final List<Feature> FEATURES = Collections.synchronizedList(
        List.of(
            new EntityTracersFeatureImpl(),
            new BlockTracersFeatureImpl(),
            new NewChunksFeatureImpl(),
            new SignReaderFeatureImpl(),
            new AlteredStructuresFeatureImpl()
        )
    );

    public static final List<Pair<Text, Boolean>> MESSAGES_BUFFER = Collections.synchronizedList(new LinkedList<>());
    public static final List<Pair<String, Boolean>> CHAT_BUFFER = Collections.synchronizedList(new LinkedList<>());

    public static final List<ChunkStatus> CHUNK_STATUSES = 
        Collections.synchronizedList(
            ChunkStatus.createOrderedList().stream()
                .filter(cs -> !cs.equals(ChunkStatus.EMPTY) && !cs.equals(ChunkStatus.SPAWN))
                .collect(Collectors.toList())
        );
}
