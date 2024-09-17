package com.stashwalker.constants;

import net.minecraft.client.MinecraftClient;
import java.util.*;

public class Constants {
    
    public static MinecraftClient MC_CLIENT_INSTANCE = MinecraftClient.getInstance();
    public static List<String> FEATURE_NAMES = new ArrayList<>();
    public static String ENTITY_TRACERS = "entityTracers";
    public static String BLOCK_TRACERS = "blockTracers";
    public static String NEW_CHUNKS = "newChunks";
    public static String SIGN_READER = "signReader";
    
    static {
        
        FEATURE_NAMES.add(ENTITY_TRACERS);
        FEATURE_NAMES.add(BLOCK_TRACERS);
        FEATURE_NAMES.add(NEW_CHUNKS);
        FEATURE_NAMES.add(SIGN_READER);
    }
}
