package com.stashwalker.finders;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
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

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.utils.*;

import java.util.HashSet;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

public class Finder {

    Set<Integer> chunksCache = new MaxSizeSet<>(4096);

    public Set<ChunkPos> findChunkPositions () {

        World world = Constants.MC_CLIENT_INSTANCE.player.getWorld();

        int playerChunkPosX = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().x;
        int playerChunkPosZ = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().z;

        int playerRenderDistance = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance();
        int xStart = playerChunkPosX - playerRenderDistance;
        int xEnd = playerChunkPosX + playerRenderDistance + 1;
        int zStart = playerChunkPosZ - playerRenderDistance;
        int zEnd = playerChunkPosZ + playerRenderDistance + 1;

        Set<ChunkPos> result = new HashSet<>();

        for (int x = xStart; x < xEnd; x++) {

            for (int z = zStart; z < zEnd; z++) {

                // Chunk chunk = this.getChunkEarly(x, z);
                Chunk chunk = Constants.MC_CLIENT_INSTANCE.world.getChunk(x, z);

                if (chunk != null) {

                    // if (!this.chunksCache.contains(chunk.getPos().hashCode())) {

                        for (
                            BlockPos pos : BlockPos.iterate(

                                chunk.getPos().getStartX(), world.getBottomY(), chunk.getPos().getStartZ(),
                                chunk.getPos().getEndX(), world.getTopY(), chunk.getPos().getEndZ())
                            ) {

                            if (this.isBlockType(pos, Blocks.COPPER_ORE)
                                    || this.isBlockType(pos, Blocks.ANCIENT_DEBRIS)) {

                                result.add(chunk.getPos());
                                // this.chunksCache.add(pos.hashCode());

                                break;
                            }
                        }
                    // } else {

                    //     result.add(chunk.getPos());
                    // }

                }
            }
        }

        return result;
    }

    public FinderResult findBlocks () {

        World world = Constants.MC_CLIENT_INSTANCE.player.getWorld();
        ClientPlayerEntity player = Constants.MC_CLIENT_INSTANCE.player;

        int playerChunkPosX = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().x;
        int playerChunkPosZ = Constants.MC_CLIENT_INSTANCE.player.getChunkPos().z;

        int playerRenderDistance = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance();
        int xStart = playerChunkPosX - playerRenderDistance;
        int xEnd = playerChunkPosX + playerRenderDistance + 1;
        int zStart = playerChunkPosZ - playerRenderDistance;
        int zEnd = playerChunkPosZ + playerRenderDistance + 1;

        FinderResult finderResult = new FinderResult();

        for (int x = xStart; x < xEnd; x++) {

            for (int z = zStart; z < zEnd; z++) {

                Chunk chunk = this.getChunkEarly(x, z);

                if (chunk != null) {

                    Set<BlockPos> blockPositions = chunk.getBlockEntityPositions();
                    if (blockPositions != null) {

                        for (BlockPos pos: blockPositions) {

                            BlockEntity blockEntity = chunk.getBlockEntity(pos);

                            // Signs
                            if (blockEntity instanceof SignBlockEntity) {

                                finderResult.addSign(blockEntity);
                            }
                            

                            // New chunks
                            if (
                                this.isBlockType(pos, Blocks.COPPER_ORE)
                                || this.isBlockType(pos, Blocks.ANCIENT_DEBRIS)
                            ) {

                                // finderResult.addChunkPosition(chunk.getPos());
                            } 

                            // Block Entities
                            if (

                                this.isBlockType(pos, Blocks.BARREL)

                                ||

                                (
                                    this.areAdjacentChunksLoaded(x, z)
                                    &&
                                    (
                                        this.isDoubleChest(world, pos)
                                        && ( 
                                            // Not a Dungeon
                                            !this.isBlockInHorizontalRadius(world, pos.down(), 5, Blocks.MOSSY_COBBLESTONE)
                                            && !this.isBlockInHorizontalRadius(world, pos, 5, Blocks.SPAWNER)
                                        )
                                    )
                                )
                            ) {

                                finderResult.addBlockPosition(new Pair<BlockPos,Color>(pos, new Color(210, 105, 30)));
                            }

                            if (this.isBlockType(pos, Blocks.SHULKER_BOX)) {

                                finderResult.addBlockPosition(new Pair<BlockPos,Color>(pos, Color.WHITE));
                            }
                            
                            if (
                                this.isBlockType(pos, Blocks.HOPPER)
                                || this.isBlockType(pos, Blocks.DROPPER)
                                || this.isBlockType(pos, Blocks.DISPENSER)
                                || this.isBlockType(pos, Blocks.BLAST_FURNACE)
                                || this.isBlockType(pos, Blocks.FURNACE)
                            ) {

                                finderResult.addBlockPosition(new Pair<BlockPos,Color>(pos, Color.BLACK));
                            }
                            
                            if (
                                this.isBlockType(pos, Blocks.OAK_SIGN)
                                || this.isBlockType(pos, Blocks.SPRUCE_SIGN)
                                || this.isBlockType(pos, Blocks.BIRCH_SIGN)
                                || this.isBlockType(pos, Blocks.ACACIA_SIGN)
                                || this.isBlockType(pos, Blocks.CHERRY_SIGN)
                                || this.isBlockType(pos, Blocks.JUNGLE_SIGN)
                                || this.isBlockType(pos, Blocks.DARK_OAK_SIGN)
                                || this.isBlockType(pos, Blocks.CRIMSON_SIGN)
                                || this.isBlockType(pos, Blocks.MANGROVE_SIGN)
                                || this.isBlockType(pos, Blocks.BAMBOO_SIGN)

                                || this.isBlockType(pos, Blocks.OAK_WALL_SIGN)
                                || this.isBlockType(pos, Blocks.SPRUCE_WALL_SIGN)
                                || this.isBlockType(pos, Blocks.BIRCH_WALL_SIGN)
                                || this.isBlockType(pos, Blocks.ACACIA_WALL_SIGN)
                                || this.isBlockType(pos, Blocks.CHERRY_WALL_SIGN)
                                || this.isBlockType(pos, Blocks.JUNGLE_WALL_SIGN)
                                || this.isBlockType(pos, Blocks.DARK_OAK_WALL_SIGN)
                                || this.isBlockType(pos, Blocks.CRIMSON_WALL_SIGN)
                                || this.isBlockType(pos, Blocks.MANGROVE_WALL_SIGN)
                                || this.isBlockType(pos, Blocks.BAMBOO_WALL_SIGN)

                                || this.isBlockType(pos, Blocks.OAK_HANGING_SIGN)
                                || this.isBlockType(pos, Blocks.SPRUCE_HANGING_SIGN)
                                || this.isBlockType(pos, Blocks.BIRCH_HANGING_SIGN)
                                || this.isBlockType(pos, Blocks.ACACIA_HANGING_SIGN)
                                || this.isBlockType(pos, Blocks.CHERRY_HANGING_SIGN)
                                || this.isBlockType(pos, Blocks.JUNGLE_HANGING_SIGN)
                                || this.isBlockType(pos, Blocks.DARK_OAK_HANGING_SIGN)
                                || this.isBlockType(pos, Blocks.CRIMSON_HANGING_SIGN)
                                || this.isBlockType(pos, Blocks.MANGROVE_HANGING_SIGN)
                                || this.isBlockType(pos, Blocks.BAMBOO_HANGING_SIGN)
                            ) {

                                finderResult.addBlockPosition(new Pair<BlockPos,Color>(pos, Color.CYAN));
                            }
                        }
                    }
                }
            }
        }

        return finderResult;
    }

    public List<Entity> findEntities () {

        int playerRenderDistance = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance();
        List<Entity> entities = Constants.MC_CLIENT_INSTANCE.world.getEntitiesByClass(Entity.class,
                Constants.MC_CLIENT_INSTANCE.player.getBoundingBox().expand(playerRenderDistance), e -> {

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

                                || itemStack.getItem() == Items.SHULKER_BOX
                        ) {

                            return true;
                        } else {

                            return false;
                        }
                    } else if (
                        (e instanceof AbstractDonkeyEntity
                            && ((AbstractDonkeyEntity) e).hasChest()
                            && !((AbstractDonkeyEntity) e).hasPlayerRider())
                            ||
                            (e instanceof LlamaEntity
                                    && ((LlamaEntity) e).hasChest()
                                    && !((LlamaEntity) e).hasPlayerRider())
                            ||
                            (e instanceof ChestBoatEntity
                                    && !((ChestBoatEntity) e).hasPlayerRider())
                            || 
                            (e instanceof ItemFrameEntity)
                            ) {

                        return true;
                    } else {

                        return false;
                    }
                });
        
        entities.addAll(findOverlappingMinecartChests());

        return entities;
    }

    private List<Entity> findOverlappingMinecartChests () {

        ClientPlayerEntity player = Constants.MC_CLIENT_INSTANCE.player;
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

        if (
            !this.isBlockType(pos, Blocks.CHEST)
            && !this.isBlockType(pos, Blocks.TRAPPED_CHEST)
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

    private boolean isBlockType (BlockPos blockPos, Block block) {

        return Constants.MC_CLIENT_INSTANCE.world.getBlockState(blockPos).getBlock() == block;
    }
}
