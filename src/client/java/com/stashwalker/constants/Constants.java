package com.stashwalker.constants;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import java.awt.Color;
import java.util.*;

import com.stashwalker.configs.ConfigManager;

public class Constants {

    public static final ConfigManager CONFIG_MANAGER = new ConfigManager();
    public static final String BLOCK_KEY_START = "BLOCK_";
    public static final MinecraftClient MC_CLIENT_INSTANCE = MinecraftClient.getInstance();
    public static final List<String> FEATURE_NAMES = new ArrayList<>();
    public static final String ENTITY_TRACERS = "entityTracers";
    public static final String BLOCK_TRACERS = "blockTracers";
    public static final String NEW_CHUNKS = "newChunks";
    public static final String SIGN_READER = "signReader";
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

    public static Map<String, Color> BLOCK_DEFAULT_COLOR_MAP = Collections.synchronizedMap(new LinkedHashMap<>());

    static {

        FEATURE_NAMES.add(ENTITY_TRACERS);
        FEATURE_NAMES.add(BLOCK_TRACERS);
        FEATURE_NAMES.add(NEW_CHUNKS);
        FEATURE_NAMES.add(SIGN_READER);

        // Default render colors

        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.CHEST.getName().getString(), new Color(210, 105, 30));

        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.BARREL.getName().getString(), new Color(210, 105, 30));

        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.WHITE_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.ORANGE_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.MAGENTA_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.LIGHT_BLUE_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.YELLOW_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.LIME_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.PINK_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.GRAY_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.LIGHT_GRAY_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.CYAN_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.PURPLE_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.BLUE_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.BROWN_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.GREEN_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.RED_SHULKER_BOX.getName().getString(), Color.WHITE);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.BLACK_SHULKER_BOX.getName().getString(), Color.WHITE);
        
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.HOPPER.getName().getString(), Color.BLACK);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.DROPPER.getName().getString(), Color.BLACK);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.DISPENSER.getName().getString(), Color.BLACK);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.BLAST_FURNACE.getName().getString(), Color.BLACK);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.FURNACE.getName().getString(), Color.BLACK);

        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.OAK_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.SPRUCE_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.BIRCH_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.ACACIA_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.CHERRY_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.JUNGLE_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.DARK_OAK_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.CRIMSON_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.MANGROVE_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.BAMBOO_SIGN.getName().getString(), Color.CYAN);

        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.OAK_WALL_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.SPRUCE_WALL_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.BIRCH_WALL_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.ACACIA_WALL_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.CHERRY_WALL_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.JUNGLE_WALL_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.DARK_OAK_WALL_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.CRIMSON_WALL_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.MANGROVE_WALL_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.BAMBOO_WALL_SIGN.getName().getString(), Color.CYAN);

        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.OAK_HANGING_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.SPRUCE_HANGING_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.BIRCH_HANGING_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.ACACIA_HANGING_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.CHERRY_HANGING_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.JUNGLE_HANGING_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.DARK_OAK_HANGING_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.CRIMSON_HANGING_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.MANGROVE_HANGING_SIGN.getName().getString(), Color.CYAN);
        BLOCK_DEFAULT_COLOR_MAP.put(BLOCK_KEY_START + Blocks.BAMBOO_HANGING_SIGN.getName().getString(), Color.CYAN);
    }
}
