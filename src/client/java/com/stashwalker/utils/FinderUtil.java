package com.stashwalker.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.stashwalker.constants.Constants;

import java.util.HashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

public class FinderUtil {

    public static boolean hasSolidBlocksNearBuildLimit (Chunk chunk) {

        if (chunk != null) {

            ChunkPos chunkPos = chunk.getPos();

            ClientWorld world = Constants.MC_CLIENT_INSTANCE.world;
            if (world != null) {

                RegistryKey<World> dimensionKey = world.getRegistryKey();
                if (World.OVERWORLD.equals(dimensionKey)) {

                    int[] yLevels = new int[] {
                            319, 318, 255, 254
                    };
                    for (int yLevel : yLevels) {

                        for (BlockPos pos : BlockPos.iterate(
                            chunkPos.getStartX(), yLevel, chunkPos.getStartZ(),
                            chunkPos.getEndX(), yLevel, chunkPos.getEndZ())
                        ) {

                            BlockState blockState = Constants.MC_CLIENT_INSTANCE.world.getBlockState(pos);
                            if (blockState.isSolidBlock(world, pos)) {
                                
                                return true;
                            }
                        }
                    }

                    return false;
                } else {

                    return false;
                }         
            } else {

                return false;
            }

        } else {

            return false;
        }
    }

    public static boolean isNewChunk (Chunk chunk) {

        if (chunk != null) {

            ChunkPos chunkPos = chunk.getPos();

            ClientWorld world = Constants.MC_CLIENT_INSTANCE.world;
            if (world != null) {

                RegistryKey<World> dimensionKey = world.getRegistryKey();
                if (World.OVERWORLD.equals(dimensionKey)) {

                    // if (hasNewBiome(chunk)) {

                    // return true;
                    // }

                    // Copper ore is found at y level -16 to 112 and most commonly at level 47 and 48
                    int[] yLevels = new int[] {
                            48, 47, 46, 49, 50, 45, 52, 43, 54, 41, 56, 39, 58, 37, 60, 35, 62, 35, 64, 33, 
                            66, 31, 68, 29, 70, 27, 72, 25, 74, 23, 76, 21, 78, 19, 80, 17, 82, 15, 84
                    };
                    for (int yLevel : yLevels) {

                        for (BlockPos pos : BlockPos.iterate(
                            chunkPos.getStartX(), yLevel, chunkPos.getStartZ(),
                            chunkPos.getEndX(), yLevel, chunkPos.getEndZ())
                        ) {

                            if (FinderUtil.isBlockType(pos, Blocks.COPPER_ORE)) {

                                return true;
                            }
                        }
                    }

                    return false;
                } else if (World.NETHER.equals(dimensionKey)) {

                    // Ancient debris are found at y level 9 to 119 and most commonly at level 15
                    int[] yLevels = new int[] {
                        15, 16, 14, 17, 13, 18, 12, 19, 11, 20, 10, 21, 9, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
                        37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64,
                        65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92,
                        93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116,
                        117, 118, 119
                    };
                    for (int yLevel : yLevels) {

                        for (BlockPos pos : BlockPos.iterate(
                            chunkPos.getStartX(), yLevel, chunkPos.getStartZ(),
                            chunkPos.getEndX(), yLevel, chunkPos.getEndZ())
                        ) {

                            if (FinderUtil.isBlockType(pos, Blocks.ANCIENT_DEBRIS)) {

                                return true;
                            }
                        }
                    }

                    return false;
                } else if (World.END.equals(dimensionKey)) {

                    return false;
                } else {

                    return false;
                }
            } else {

                return false;
            }
        } else {

            return false;
        }
    }

    public static boolean isInterestingBlockPosition (BlockPos pos, Chunk chunk, int x, int z) {

        ClientWorld world = Constants.MC_CLIENT_INSTANCE.world;
        if (

                FinderUtil.isBlockType(pos, Blocks.BARREL)

                ||

                (FinderUtil.areAdjacentChunksLoaded(x, z)
                        &&
                        (FinderUtil.isDoubleChest(world, pos)
                                && (
                                // Not a Dungeon
                                !FinderUtil.isBlockInHorizontalRadius(world, pos.down(), 5, Blocks.MOSSY_COBBLESTONE)
                                        && !FinderUtil.isBlockInHorizontalRadius(world, pos, 5, Blocks.SPAWNER))))

                || FinderUtil.isBlockType(pos, Blocks.SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.WHITE_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.ORANGE_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.MAGENTA_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.LIGHT_BLUE_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.YELLOW_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.LIME_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.PINK_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.GRAY_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.LIGHT_GRAY_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.CYAN_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.PURPLE_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.BLUE_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.BROWN_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.GREEN_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.RED_SHULKER_BOX)
                || FinderUtil.isBlockType(pos, Blocks.BLACK_SHULKER_BOX)

                || FinderUtil.isBlockType(pos, Blocks.HOPPER)
                || FinderUtil.isBlockType(pos, Blocks.DROPPER)
                || FinderUtil.isBlockType(pos, Blocks.DISPENSER)
                || FinderUtil.isBlockType(pos, Blocks.BLAST_FURNACE)
                || FinderUtil.isBlockType(pos, Blocks.FURNACE)

                || FinderUtil.isBlockType(pos, Blocks.OAK_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.SPRUCE_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.BIRCH_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.ACACIA_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.CHERRY_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.JUNGLE_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.DARK_OAK_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.CRIMSON_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.MANGROVE_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.BAMBOO_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.WARPED_SIGN)

                || FinderUtil.isBlockType(pos, Blocks.OAK_WALL_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.SPRUCE_WALL_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.BIRCH_WALL_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.ACACIA_WALL_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.CHERRY_WALL_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.JUNGLE_WALL_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.DARK_OAK_WALL_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.CRIMSON_WALL_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.MANGROVE_WALL_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.BAMBOO_WALL_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.WARPED_WALL_SIGN)

                || FinderUtil.isBlockType(pos, Blocks.OAK_HANGING_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.SPRUCE_HANGING_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.BIRCH_HANGING_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.ACACIA_HANGING_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.CHERRY_HANGING_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.JUNGLE_HANGING_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.DARK_OAK_HANGING_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.CRIMSON_HANGING_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.MANGROVE_HANGING_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.BAMBOO_HANGING_SIGN)
                || FinderUtil.isBlockType(pos, Blocks.WARPED_HANGING_SIGN)

        ) {

            return true;
        } else {

            return false;
        }
    }

    public static List<Entity> findEntities () {

        int playerRenderDistance = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance();
        double renderDistanceInBlocks = playerRenderDistance * 16; // Convert render distance to blocks
        // Get player position
        Vec3d playerPos = Constants.MC_CLIENT_INSTANCE.player.getPos();

        // Define the bounding box to cover the entire Y range (-64 to 320) and expand
        // based on render distance in X and Z
        Box boundingBox = new Box(
                playerPos.x - renderDistanceInBlocks, // X min
                -64, // Y min (lowest level)
                playerPos.z - renderDistanceInBlocks, // Z min
                playerPos.x + renderDistanceInBlocks, // X max
                320, // Y max (build limit)
                playerPos.z + renderDistanceInBlocks // Z max
        );

        List<Entity> entities = Constants.MC_CLIENT_INSTANCE.world.getEntitiesByClass(
                Entity.class,
                boundingBox, e -> {

                    if (e instanceof ItemEntity) {

                        ItemEntity itemEntity = (ItemEntity) e;
                        ItemStack itemStack = itemEntity.getStack();
                        if (itemStack.getItem() == Items.ELYTRA

                                || itemStack.getItem() == Items.EXPERIENCE_BOTTLE

                                || itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE

                                || itemStack.getItem() == Items.TOTEM_OF_UNDYING

                                || itemStack.getItem() == Items.END_CRYSTAL

                                || itemStack.getItem() == Items.NETHERITE_BOOTS
                                || itemStack.getItem() == Items.NETHERITE_CHESTPLATE
                                || itemStack.getItem() == Items.NETHERITE_HELMET
                                || itemStack.getItem() == Items.NETHERITE_HOE
                                || itemStack.getItem() == Items.NETHERITE_LEGGINGS
                                || itemStack.getItem() == Items.NETHERITE_PICKAXE
                                || itemStack.getItem() == Items.NETHERITE_AXE
                                || itemStack.getItem() == Items.NETHERITE_SHOVEL
                                || itemStack.getItem() == Items.NETHERITE_SWORD

                                || itemStack.getItem() == Items.DIAMOND_BOOTS
                                || itemStack.getItem() == Items.DIAMOND_CHESTPLATE
                                || itemStack.getItem() == Items.DIAMOND_HELMET
                                || itemStack.getItem() == Items.DIAMOND_LEGGINGS
                                || itemStack.getItem() == Items.DIAMOND_PICKAXE
                                || itemStack.getItem() == Items.DIAMOND_AXE
                                || itemStack.getItem() == Items.DIAMOND_SHOVEL
                                || itemStack.getItem() == Items.DIAMOND_SWORD

                                || itemStack.getItem() == Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE

                                || itemStack.getItem() == Items.SHULKER_BOX
                                || itemStack.getItem() == Items.WHITE_SHULKER_BOX
                                || itemStack.getItem() == Items.ORANGE_SHULKER_BOX
                                || itemStack.getItem() == Items.MAGENTA_SHULKER_BOX
                                || itemStack.getItem() == Items.LIGHT_BLUE_SHULKER_BOX
                                || itemStack.getItem() == Items.YELLOW_SHULKER_BOX
                                || itemStack.getItem() == Items.LIME_SHULKER_BOX
                                || itemStack.getItem() == Items.PINK_SHULKER_BOX
                                || itemStack.getItem() == Items.GRAY_SHULKER_BOX
                                || itemStack.getItem() == Items.LIGHT_GRAY_SHULKER_BOX
                                || itemStack.getItem() == Items.CYAN_SHULKER_BOX
                                || itemStack.getItem() == Items.PURPLE_SHULKER_BOX
                                || itemStack.getItem() == Items.BLUE_SHULKER_BOX
                                || itemStack.getItem() == Items.BROWN_SHULKER_BOX
                                || itemStack.getItem() == Items.GREEN_SHULKER_BOX
                                || itemStack.getItem() == Items.RED_SHULKER_BOX
                                || itemStack.getItem() == Items.BLACK_SHULKER_BOX) {

                            return true;
                        } else {

                            return false;
                        }
                    } else if (
                        (
                            e instanceof AbstractDonkeyEntity
                            && ((AbstractDonkeyEntity) e).hasChest()
                            && !((AbstractDonkeyEntity) e).hasPlayerRider()
                        )
                        ||
                        (
                            e instanceof LlamaEntity
                            && ((LlamaEntity) e).hasChest()
                            && !((LlamaEntity) e).hasPlayerRider()
                        )
                        ||
                        (
                            e instanceof ChestBoatEntity
                            && !((ChestBoatEntity) e).hasPlayerRider()
                        )
                            ||
                        (e instanceof ItemFrameEntity)) {

                        return true;
                    } else {

                        return false;
                    }
                });

        entities.addAll(findOverlappingMinecartChests());

        return entities;
    }

    public static List<Entity> findOverlappingMinecartChests () {

        ClientPlayerEntity player = Constants.MC_CLIENT_INSTANCE.player;
        int renderDistanceChunks = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance();
        double renderDistanceInBlocks = renderDistanceChunks * 16;

        Set<ChestMinecartEntity> minecartChests = new HashSet<>();

        Vec3d playerPos = Constants.MC_CLIENT_INSTANCE.player.getPos();
        Box boundingBox = new Box(
                playerPos.x - renderDistanceInBlocks,
                -64,
                playerPos.z - renderDistanceInBlocks,
                playerPos.x + renderDistanceInBlocks,
                320,
                playerPos.z + renderDistanceInBlocks
        );

        // Get all MinecartChests within the calculated radius
        List<ChestMinecartEntity> entities = player.getWorld().getEntitiesByClass(ChestMinecartEntity.class,
                boundingBox, e -> true);

        Set<ChestMinecartEntity> foundChestMinecastEntities = new HashSet<>();

        for (ChestMinecartEntity minecart : entities) {

            Box minecartBox = minecart.getBoundingBox();

            // Check for overlaps with existing minecarts
            for (ChestMinecartEntity otherMinecart : minecartChests) {

                if (minecart != otherMinecart && minecartBox.intersects(otherMinecart.getBoundingBox())) {

                    foundChestMinecastEntities.add(minecart);
                    foundChestMinecastEntities.add(otherMinecart);
                }
            }

            minecartChests.add(minecart);
        }

        return new ArrayList<>(foundChestMinecastEntities);
    }

    public static boolean isDoubleChest (World world, BlockPos pos) {

        if (
            !isBlockType(pos, Blocks.CHEST)
            && !isBlockType(pos, Blocks.TRAPPED_CHEST)
        ) {

            return false;
        }

        BlockState state = world.getBlockState(pos);

        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction direction : directions) {

            BlockPos adjacentPos = pos.offset(direction);
            BlockState adjacentState = world.getBlockState(adjacentPos);
            Block adjacentBlock = adjacentState.getBlock();

            if (
                (
                    adjacentBlock == Blocks.CHEST 
                    || adjacentBlock == Blocks.TRAPPED_CHEST
                )
                && adjacentState.get(Properties.HORIZONTAL_FACING) == state.get(Properties.HORIZONTAL_FACING)
            ) {

                return true;
            }
        }

        return false;
    }

    public static boolean isBlockInHorizontalRadius (World world, BlockPos pos, int radius, Block block) {

        Box searchBox = new Box(pos).expand(radius, 0, radius);

        return BlockPos.stream(searchBox).anyMatch(bp -> {
            return world.getBlockState(bp).getBlock() == block;
        });
    }

    public static Chunk getChunkEarly (int x, int z) {

        Chunk chunk = null;

        for (ChunkStatus status : Constants.CHUNK_STATUSES) {

            chunk = Constants.MC_CLIENT_INSTANCE.world.getChunk(x, z, status);
            if (chunk != null) {

                return chunk;
            }
        }

        return chunk;
    }

    public static boolean areAdjacentChunksLoaded (int x, int z) {

        for (int xI = x - 1; xI < x + 2; xI++) {

            for (int zI = z - 1; zI < z + 2; zI++) {

                if (Constants.MC_CLIENT_INSTANCE.world.getChunk(xI, zI, ChunkStatus.FULL, false) == null) {

                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isBlockType (BlockPos blockPos, Block block) {

        return Constants.MC_CLIENT_INSTANCE.world.getBlockState(blockPos).getBlock() == block;
    }

    public static boolean hasNewBiome (Chunk chunk) {

        List<BlockPos> checkPositions = getCheckPositionsInChunk(chunk.getPos());

        for (BlockPos pos : checkPositions) {

            RegistryKey<Biome> biome = Constants.MC_CLIENT_INSTANCE.world.getBiome(pos).getKey().get();
            if (Constants.NEW_BIOMES.contains(biome)) {

                return true;
            }
        }

        return false;
    }

    private static List<BlockPos> getCheckPositionsInChunk (ChunkPos chunkPos) {

        int chunkXStart = chunkPos.getStartX();
        int chunkZStart = chunkPos.getStartZ();
        int[] yLevels = {64, 0}; // Sample at multiple Y levels
        List<BlockPos> checkPositions = new ArrayList<>();

        // Loop over all 4x4 grid points in the chunk (0, 4, 8, 12 in both X and Z
        // directions)
        for (int yLevel : yLevels) {

            for (int xOffset = 0; xOffset <= 12; xOffset += 4) {

                for (int zOffset = 0; zOffset <= 12; zOffset += 4) {

                    checkPositions.add(new BlockPos(chunkXStart + xOffset, yLevel, chunkZStart + zOffset));
                }
            }
        }

        return checkPositions;
    }

    public static Text createStyledTextForFeature (String featureName, boolean featureToggle) {

                return Text.empty()
                        .append(Text.literal("[")
                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal("Stashwalker, ")
                                .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                        .append(Text.literal(featureName)
                                .setStyle(Style.EMPTY.withColor(Formatting.BLUE)))
                        .append(Text.literal("]:")
                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal(featureToggle ? " enabled" : " disabled")
                                .setStyle(Style.EMPTY.withColor(featureToggle ? Formatting.GREEN : Formatting.RED)));
    }
}
