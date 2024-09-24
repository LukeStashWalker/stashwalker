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

            // Overworld Biomes

            // Plains – Added in Alpha v1.2.0 (Halloween Update, October 2010)
            // Ice Plains (Snowy Tundra) – Added in Alpha v1.2.0 (Halloween Update, October 2010)
            // Ice Spike Plains – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Sunflower Plains – Added in 1.7.2 (The Update that Changed the World, October 2013)
            BiomeKeys.SNOWY_PLAINS, // Snowy Plains – Added in 1.18 (Caves & Cliffs: Part II, November 2021)
            // Mushroom Field (Mushroom Island) – Added in Beta 1.9 Prerelease 3 (October 2011)
            // Savanna – Added in 1.7.2 (The Update that Changed the World, October 2013)

            // Woodlands

            // Forest – Added in Alpha v1.2.0 (Halloween Update, October 2010)
            // Birch Forest – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Dark Forest – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Flower Forest – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Old Growth Birch Forest (Tall Birch Forest) – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Taiga – Added in Alpha v1.2.0 (Halloween Update, October 2010)
            // Old Growth Spruce Taiga – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Old Growth Pine Taiga – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Snowy Taiga – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Jungle – Added in 1.2.1 (March 2012)
            BiomeKeys.BAMBOO_JUNGLE, // Bamboo Jungle – Added in 1.14 (Village & Pillage, April 2019)
            BiomeKeys.SPARSE_JUNGLE, // Sparse Jungle – Added in 1.18 (Caves & Cliffs: Part II, November 2021)
            BiomeKeys.GROVE, // Grove – Added in 1.18 (Caves & Cliffs: Part II, November 2021)
            BiomeKeys.CHERRY_GROVE, // Cherry Grove – Added in 1.20 (Trails & Tales, June 2023)

            // Caves

            // BiomeKeys.DEEP_DARK, // Deep Dark – Added in 1.19 (The Wild Update, June 2022)
            // BiomeKeys.DRIPSTONE_CAVES, // Dripstone Caves – Added in 1.18 (Caves & Cliffs: Part II, November 2021)
            // BiomeKeys.LUSH_CAVES, // Lush Caves – Added in 1.18 (Caves & Cliffs: Part II, November 2021)

            // Mountains

            BiomeKeys.JAGGED_PEAKS, // Jagged Peaks – Added in 1.18 (Caves & Cliffs: Part II, November 2021)
            BiomeKeys.FROZEN_PEAKS, // Frozen Peaks – Added in 1.18 (Caves & Cliffs: Part II, November 2021)
            BiomeKeys.STONY_PEAKS, // Stony Peaks – Added in 1.18 (Caves & Cliffs: Part II, November 2021)
            BiomeKeys.SNOWY_SLOPES, // Snowy Slopes – Added in 1.18 (Caves & Cliffs: Part II, November 2021)
            // Windswept Hills – Added in Beta 1.8 (Adventure Update, September 2011)
            BiomeKeys.WINDSWEPT_FOREST, // Windswept Forest – Added in 1.18 (Caves & Cliffs: Part II, November 2021)
            BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, // Windswept Gravelly Hills – Added in 1.18 (Caves & Cliffs: Part II, November 2021)
            BiomeKeys.MEADOW, // Meadow – Added in 1.18 (Caves & Cliffs: Part II, November 2021)
            BiomeKeys.STONY_SHORE, // Stony Shores – Added in 1.13 (Update Aquatic, July 2018)
            // Savanna Plateau – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Windswept Savanna – Added in 1.7.2 (The Update that Changed the World, October 2013)

            // Swamps

            // Swamp – Added in Alpha v1.2.0 (Halloween Update, October 2010)
            BiomeKeys.MANGROVE_SWAMP, // Mangrove Swamp – Added in 1.19 (The Wild Update, June 2022)

            // Sandy Areas

            // Badlands – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Wooded Badlands – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Eroded Badlands – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Beach – Added in Alpha v1.2.0 (Halloween Update, October 2010)
            // Snowy Beach – Added in 1.9 (Combat Update, February 2016)
            // Desert – Added in Alpha v1.2.0 (Halloween Update, October 2010)

            // Water Areas

            // River – Added in Beta 1.8 (Adventure Update, September 2011)
            BiomeKeys.FROZEN_RIVER, // Frozen River – Added in 1.13 (Update Aquatic, July 2018)
            // Ocean – Added in Alpha v1.2.0 (Halloween Update, October 2010)
            BiomeKeys.COLD_OCEAN, // Cold Ocean – Added in 1.13 (Update Aquatic, July 2018)
            // Deep Ocean – Added in 1.7.2 (The Update that Changed the World, October 2013)
            // Frozen Ocean – Added in 1.7.2 (The Update that Changed the World, October 2013)
            BiomeKeys.LUKEWARM_OCEAN, // Lukewarm Ocean – Added in 1.13 (Update Aquatic, July 2018)
            BiomeKeys.WARM_OCEAN // Warm Ocean – Added in 1.13 (Update Aquatic, July 2018)
    );

    static {

        FEATURE_NAMES.add(ENTITY_TRACERS);
        FEATURE_NAMES.add(BLOCK_TRACERS);
        FEATURE_NAMES.add(NEW_CHUNKS);
        FEATURE_NAMES.add(SIGN_READER);
    }
}
