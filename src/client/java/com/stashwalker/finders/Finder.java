package com.stashwalker.finders;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.stashwalker.constants.Constants;

import java.util.HashSet;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public class Finder {

    public List<BlockPos> findBlockPositions (PlayerEntity player) {

        World world = player.getWorld(); // Use getWorld() method
        List<BlockPos> doubleChests = new ArrayList<>();

        int playerChunkPosX = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().x;
        int playerChunkPosZ = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().z;

        int playerRenderDistance = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance() - 2;
        int xStart = playerChunkPosX - playerRenderDistance;
        int xEnd = playerChunkPosX + playerRenderDistance + 1;
        int zStart = playerChunkPosZ - playerRenderDistance;
        int zEnd = playerChunkPosZ + playerRenderDistance + 1;

        for (int x = xStart; x < xEnd; x++) {

            for (int z = zStart; z < zEnd; z++) {

                Chunk chunk = Constants.MC_CLIENT_INSTANCE.world.getChunk(x, z);
                if (chunk != null) {

                    Set<BlockPos> blockPositions = chunk.getBlockEntityPositions();
                    if (blockPositions != null) {

                        for (BlockPos blockPos : blockPositions) {

                            if (
                                Constants.MC_CLIENT_INSTANCE.world.getBlockState(blockPos).getBlock() == Blocks.CHEST
                                && this.areAdjacentChunksLoaded(x, z)
                            ) {

                                if (
                                        (
                                            this.isDoubleChest(world, blockPos)
                                            && ( 
                                                // Not a Dungeon
                                                !this.isBlockInHorizontalRadius(world, blockPos.down(), 5, Blocks.MOSSY_COBBLESTONE)
                                                && !this.isBlockInHorizontalRadius(world, blockPos, 5, Blocks.SPAWNER)
                                            )
                                        )

                                        ||

                                        // Potential shop drop off spot
                                        (
                                            this.isBlockInHorizontalRadius(world, blockPos.down(), 5,Blocks.MOSSY_COBBLESTONE)
                                            && !this.isBlockInHorizontalRadius(world, blockPos, 5, Blocks.SPAWNER)
                                        )

                                ) {

                                    doubleChests.add(blockPos);
                                }
                            }

                        }
                    }
                }
            }
        }

        return doubleChests;
    }

    public List<BlockEntity> findSigns (PlayerEntity player) {

        List<BlockEntity> signs = new ArrayList<>();

        int playerChunkPosX = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().x;
        int playerChunkPosZ = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().z;

        int playerRenderDistance = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance();
        int xStart = playerChunkPosX - playerRenderDistance;
        int xEnd = playerChunkPosX + playerRenderDistance + 1;
        int zStart = playerChunkPosZ - playerRenderDistance;
        int zEnd = playerChunkPosZ + playerRenderDistance + 1;

        for (int x = xStart; x < xEnd; x++) {

            for (int z = zStart; z < zEnd; z++) {

                Chunk chunk = this.getChunkEarly(x, z);
                Set<BlockPos> blockPositions = chunk.getBlockEntityPositions();
                for (BlockPos blockPos : blockPositions) {

                    BlockEntity blockEntity = chunk.getBlockEntity(blockPos);

                    if (blockEntity instanceof SignBlockEntity) {

                        signs.add(blockEntity);
                    }
                }
            }
        }

        return signs;
    }

    public List<Entity> findEntities (PlayerEntity player) {

        int renderDistanceChunks = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance(); // Render distance in
                                                                                                  // chunks
        double searchRadius = renderDistanceChunks;

        List<Entity> entities = player.getWorld().getEntitiesByClass(Entity.class,
                player.getBoundingBox().expand(searchRadius), e -> {

                    if (e instanceof ItemEntity) {

                        ItemEntity itemEntity = (ItemEntity) e;
                        ItemStack itemStack = itemEntity.getStack();
                        if (itemStack.getItem() == Items.ELYTRA

                                || itemStack.getItem() == Items.EXPERIENCE_BOTTLE

                                || itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE

                                || itemStack.getItem() == Items.TOTEM_OF_UNDYING

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

                                || itemStack.getItem() == Items.SHULKER_BOX) {

                            return true;
                        } else {

                            return false;
                        }
                    } else if ((e instanceof AbstractDonkeyEntity
                            && ((AbstractDonkeyEntity) e).hasChest()
                            && !((AbstractDonkeyEntity) e).hasPlayerRider())
                            ||
                            (e instanceof LlamaEntity
                                    && ((LlamaEntity) e).hasChest()
                                    && !((LlamaEntity) e).hasPlayerRider())
                            ||
                            (e instanceof ChestBoatEntity
                                    && !((ChestBoatEntity) e).hasPlayerRider())) {

                        return true;
                    } else {

                        return false;
                    }
                });

        entities.addAll(findOverlappingMinecartChests(player));

        return entities;
    }

    public List<Chunk> findNewChunks (PlayerEntity player) {

        List<Chunk> result = new ArrayList<>();
        World world = player.getWorld();
        int chunkRadius = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance(); // Render distance in chunks
        int startX = player.getChunkPos().x - chunkRadius;
        int endX = player.getChunkPos().x + chunkRadius;
        int startZ = player.getChunkPos().z - chunkRadius;
        int endZ = player.getChunkPos().z + chunkRadius;

        // Iterate through the chunks around the player
        for (int chunkX = startX; chunkX <= endX; chunkX++) {

            for (int chunkZ = startZ; chunkZ <= endZ; chunkZ++) {

                Chunk chunk = this.getChunkEarly(chunkX, chunkZ);
                for (BlockPos pos : BlockPos.iterate(

                        chunk.getPos().getStartX(), world.getBottomY(), chunk.getPos().getStartZ(),
                        chunk.getPos().getEndX(), world.getTopY(), chunk.getPos().getEndZ())) {

                    Block block = chunk.getBlockState(pos).getBlock();
                    if (block == Blocks.COPPER_ORE || block == Blocks.ANCIENT_DEBRIS) {

                        result.add(chunk);

                        break;
                    }
                }
            }
        }

        return result;
    }

    private List<Entity> findOverlappingMinecartChests (PlayerEntity player) {

        int renderDistanceChunks = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance(); // Render distance in
                                                                                                  // chunks
        double searchRadius = renderDistanceChunks * 512; // Convert chunks to blocks

        Set<ChestMinecartEntity> minecartChests = new HashSet<>();

        // Get all MinecartChests within the calculated radius
        List<ChestMinecartEntity> entities = player.getWorld().getEntitiesByClass(ChestMinecartEntity.class,
                player.getBoundingBox().expand(searchRadius), e -> true);

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

    private boolean isDoubleChest (World world, BlockPos pos) {

        BlockState state = world.getBlockState(pos);

        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction direction : directions) {

            BlockPos adjacentPos = pos.offset(direction);
            BlockState adjacentState = world.getBlockState(adjacentPos);
            Block adjacentBlock = adjacentState.getBlock();

            if (
                adjacentBlock == Blocks.CHEST
                && adjacentState.get(Properties.HORIZONTAL_FACING) == state.get(Properties.HORIZONTAL_FACING)
            ) {

                return true;
            }
        }

        return false;
    }

    private boolean isBlockInHorizontalRadius (World world, BlockPos pos, int radius, Block block) {

        Box searchBox = new Box(pos).expand(radius, 0, radius);

        return BlockPos.stream(searchBox).anyMatch(bp -> {
            return world.getBlockState(bp).getBlock() == block;
        });
    }

    private Chunk getChunkEarly (int x, int z) {

        Chunk chunk = null;

        List<ChunkStatus> chunkStatuses = new ArrayList<>();
        chunkStatuses.add(ChunkStatus.BIOMES);
        chunkStatuses.add(ChunkStatus.CARVERS);
        chunkStatuses.add(ChunkStatus.FEATURES);
        chunkStatuses.add(ChunkStatus.FULL);
        chunkStatuses.add(ChunkStatus.INITIALIZE_LIGHT);
        chunkStatuses.add(ChunkStatus.LIGHT);
        chunkStatuses.add(ChunkStatus.NOISE);
        // chunkStatuses.add(ChunkStatus.SPAWN);
        chunkStatuses.add(ChunkStatus.STRUCTURE_REFERENCES);
        chunkStatuses.add(ChunkStatus.STRUCTURE_STARTS);
        chunkStatuses.add(ChunkStatus.SURFACE);
        for (ChunkStatus status : chunkStatuses) {

            chunk = Constants.MC_CLIENT_INSTANCE.world.getChunk(x, z, status);
            if (chunk != null) {

                return chunk;
            }
        }

        return chunk;
    }

    private boolean areAdjacentChunksLoaded (int x, int z) {

        for (int xI = x - 1; xI < x + 1; xI++) {

            for (int zI = z - 1; zI < z + 1; zI++) {

                if (Constants.MC_CLIENT_INSTANCE.world.getChunk(xI, zI, ChunkStatus.FULL, false) == null) {

                    return false;
                }
            }
        }

        return true;
    }
}
