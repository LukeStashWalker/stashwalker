package com.stashwalker.finders;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;

import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.utils.*;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;

public class Finder {


    public Finder () {

    }

    public boolean isNewChunk (Chunk chunk) {

        if (chunk != null) {

            ChunkPos chunkPos = chunk.getPos();
            // if (FinderUtil.hasNewBiome(chunk)) {

            //     return true;
            // }

            // Copper ore is found at y level -16 to 112 and most commonly at level 47 and
            // 48
            int[] yLevels = new int[] {
                    48, 47, 46, 49, 50, 45, 52, 43, 54, 41, 56, 39, 58, 37, 60, 35, 62, 35, 64, 33, 66,
                    31, 68, 29, 70, 27, 72, 25, 74, 23, 76, 21, 78, 19, 80, 17, 82, 15, 84
            };
            for (int yLevel : yLevels) {

                for (BlockPos pos : BlockPos.iterate(

                        chunkPos.getStartX(), yLevel, chunkPos.getStartZ(),
                        chunkPos.getEndX(), yLevel, chunkPos.getEndZ())) {
                    if (FinderUtil.isBlockType(pos, Blocks.COPPER_ORE)) {

                        return true;
                    }
                }
            }

            return false;
        } else {

            return false;
        }
    }

    public FinderResult findBlocks () {

        World world = Constants.MC_CLIENT_INSTANCE.player.getWorld();

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

                Chunk chunk = FinderUtil.getChunkEarly(x, z);

                if (chunk != null) {

                    Set<BlockPos> blockPositions = chunk.getBlockEntityPositions();
                    if (blockPositions != null) {

                        for (BlockPos pos: blockPositions) {

                            BlockEntity blockEntity = chunk.getBlockEntity(pos);

                            // Signs
                            if (blockEntity instanceof SignBlockEntity) {

                                finderResult.addSign(blockEntity);
                            }
                            
                            // Block Entities
                            if (

                                FinderUtil.isBlockType(pos, Blocks.BARREL)

                                ||

                                (
                                    FinderUtil.areAdjacentChunksLoaded(x, z)
                                    &&
                                    (
                                        FinderUtil.isDoubleChest(world, pos)
                                        && ( 
                                            // Not a Dungeon
                                            !FinderUtil.isBlockInHorizontalRadius(world, pos.down(), 5, Blocks.MOSSY_COBBLESTONE)
                                            && !FinderUtil.isBlockInHorizontalRadius(world, pos, 5, Blocks.SPAWNER)
                                        )
                                    )
                                )
                            ) {

                                finderResult.addBlockPosition(new Pair<BlockPos,Color>(pos, new Color(210, 105, 30)));
                            }

                            if (
                            FinderUtil.isBlockType(pos, Blocks.SHULKER_BOX)
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
                            ) {

                                finderResult.addBlockPosition(new Pair<BlockPos,Color>(pos, Color.WHITE));
                            }
                            
                            if (
                                FinderUtil.isBlockType(pos, Blocks.HOPPER)
                                || FinderUtil.isBlockType(pos, Blocks.DROPPER)
                                || FinderUtil.isBlockType(pos, Blocks.DISPENSER)
                                || FinderUtil.isBlockType(pos, Blocks.BLAST_FURNACE)
                                || FinderUtil.isBlockType(pos, Blocks.FURNACE)
                            ) {

                                finderResult.addBlockPosition(new Pair<BlockPos,Color>(pos, Color.BLACK));
                            }
                            
                            if (
                                FinderUtil.isBlockType(pos, Blocks.OAK_SIGN)
                                || FinderUtil.isBlockType(pos, Blocks.SPRUCE_SIGN)
                                || FinderUtil.isBlockType(pos, Blocks.BIRCH_SIGN)
                                || FinderUtil.isBlockType(pos, Blocks.ACACIA_SIGN)
                                || FinderUtil.isBlockType(pos, Blocks.CHERRY_SIGN)
                                || FinderUtil.isBlockType(pos, Blocks.JUNGLE_SIGN)
                                || FinderUtil.isBlockType(pos, Blocks.DARK_OAK_SIGN)
                                || FinderUtil.isBlockType(pos, Blocks.CRIMSON_SIGN)
                                || FinderUtil.isBlockType(pos, Blocks.MANGROVE_SIGN)
                                || FinderUtil.isBlockType(pos, Blocks.BAMBOO_SIGN)

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
        
        entities.addAll(FinderUtil.findOverlappingMinecartChests());

        return entities;
    }
}
