package com.stashwalker.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

public class FinderUtil {

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
