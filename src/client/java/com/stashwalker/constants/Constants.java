package com.stashwalker.constants;

import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.*;

public class Constants {

    public static MinecraftClient MC_CLIENT_INSTANCE = MinecraftClient.getInstance();
    public static List<String> FEATURE_NAMES = new ArrayList<>();
    public static String ENTITY_TRACERS = "entityTracers";
    public static String BLOCK_TRACERS = "blockTracers";
    public static String NEW_CHUNKS = "newChunks";
    public static String SIGN_READER = "signReader";
    public static final Set<RegistryKey<Biome>> NEW_BIOMES = Set.of(

            // 1.13 biomes
            BiomeKeys.WARM_OCEAN,
            BiomeKeys.LUKEWARM_OCEAN,
            BiomeKeys.COLD_OCEAN,
            // BiomeKeys.DEEP_WARM_OCEAN,
            BiomeKeys.DEEP_LUKEWARM_OCEAN,
            BiomeKeys.DEEP_COLD_OCEAN,
            BiomeKeys.DEEP_FROZEN_OCEAN,
            // 1.14 biomes
            BiomeKeys.BAMBOO_JUNGLE,
            // BiomeKeys.BAMBOO_JUNGLE_HILLS,
            // 1.16 biomes
            BiomeKeys.CRIMSON_FOREST,
            BiomeKeys.WARPED_FOREST,
            BiomeKeys.BASALT_DELTAS,
            BiomeKeys.SOUL_SAND_VALLEY,
            // 1.17-1.18 biomes
            BiomeKeys.LUSH_CAVES,
            BiomeKeys.DRIPSTONE_CAVES,
            BiomeKeys.MEADOW,
            BiomeKeys.GROVE,
            BiomeKeys.SNOWY_SLOPES,
            BiomeKeys.JAGGED_PEAKS,
            BiomeKeys.FROZEN_PEAKS,
            BiomeKeys.STONY_PEAKS,
            // 1.19 biomes
            BiomeKeys.MANGROVE_SWAMP,
            BiomeKeys.DEEP_DARK,
            // 1.20 biomes
            BiomeKeys.CHERRY_GROVE);

    static {

        FEATURE_NAMES.add(ENTITY_TRACERS);
        FEATURE_NAMES.add(BLOCK_TRACERS);
        FEATURE_NAMES.add(NEW_CHUNKS);
        FEATURE_NAMES.add(SIGN_READER);
    }
}
