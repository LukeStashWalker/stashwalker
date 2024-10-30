package com.stashwalker.utils;

import net.minecraft.block.Block;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.lang.Class;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import com.stashwalker.constants.Constants;
import com.stashwalker.containers.KDTree;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.util.math.Box;

public class FinderUtil {

    public static List<Vec3d> getEntityPositions (Class<? extends Entity> clazz, Box box) {

        return Constants.MC_CLIENT_INSTANCE.world
            .getEntitiesByClass(clazz, box, e -> { return true;})
            .stream()
            .map(e -> new Vec3d(e.getPos().x, e.getPos().y + 0.5D, e.getPos().z))
            .toList();
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

    public static boolean areAdjacentChunksLoaded (int chunkX, int chunkZ) {

        for (int xI = chunkX - 1; xI < chunkX + 2; xI++) {

            for (int zI = chunkZ - 1; zI < chunkZ + 2; zI++) {

                if (Constants.MC_CLIENT_INSTANCE.world.getChunk(xI, zI, ChunkStatus.FULL, false) == null) {

                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isBlockType (BlockPos blockPos, Block block) {

        return Constants.MC_CLIENT_INSTANCE.world.getBlockState(blockPos).isOf(block);
    }

    public static<T> List<T> findCloseProximityBlockPositionObjects (
            List<T> positionObjects,
            KDTree<T> kdTree,
            Function<T, BlockPos> positionExtractor,
            int amount,
            int proximity
    ) {

        Set<T> closeProximityPositionObjects = new HashSet<>();
        for (T current: positionObjects) {

            if (closeProximityPositionObjects.contains(current)) {

                continue;
            }

            Set<T> nearbyPositionObjects =
                kdTree.rangeSearch(positionExtractor.apply(current), proximity);

            if (nearbyPositionObjects.size() >= amount) {

                closeProximityPositionObjects.addAll(nearbyPositionObjects);
            }
        }

        return new ArrayList<>(closeProximityPositionObjects);
    }

    public static boolean isRareItemStack (ItemStack stack) {

        return stack.isOf(Items.PLAYER_HEAD)
                || stack.isOf(Items.SKELETON_SKULL)
                || stack.isOf(Items.WITHER_SKELETON_SKULL)
                || stack.isOf(Items.CREEPER_HEAD)
                || stack.isOf(Items.ZOMBIE_HEAD)
                || stack.isOf(Items.DRAGON_HEAD)
                || stack.isOf(Items.WRITTEN_BOOK)
                || stack.isOf(Items.NETHER_STAR)
                || stack.isOf(Items.BEACON)
                || stack.isOf(Items.ANCIENT_DEBRIS)
                || stack.isOf(Items.NETHERITE_INGOT)
                || stack.isOf(Items.NETHERITE_BLOCK)
                || stack.isOf(Items.DRAGON_EGG)
                || stack.isOf(Items.HEART_OF_THE_SEA)
                || stack.isOf(Items.CONDUIT)
                // Music Discs
                || stack.isOf(Items.MUSIC_DISC_13)
                || stack.isOf(Items.MUSIC_DISC_CAT)
                || stack.isOf(Items.MUSIC_DISC_BLOCKS)
                || stack.isOf(Items.MUSIC_DISC_CHIRP)
                || stack.isOf(Items.MUSIC_DISC_FAR)
                || stack.isOf(Items.MUSIC_DISC_MALL)
                || stack.isOf(Items.MUSIC_DISC_MELLOHI)
                || stack.isOf(Items.MUSIC_DISC_STAL)
                || stack.isOf(Items.MUSIC_DISC_STRAD)
                || stack.isOf(Items.MUSIC_DISC_WARD)
                || stack.isOf(Items.MUSIC_DISC_11)
                || stack.isOf(Items.MUSIC_DISC_PIGSTEP)
                || stack.isOf(Items.MUSIC_DISC_5)
                || stack.isOf(Items.MUSIC_DISC_RELIC)
                // Rare Armor Trims
                || stack.isOf(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE)
                || stack.isOf(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE)
                || stack.isOf(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE)
                || stack.isOf(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE)
                || stack.isOf(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE)
                || stack.isOf(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE)
                // Rare Goat Horns
                || stack.isOf(Items.GOAT_HORN);
    }

    public static boolean isRareEntity (Entity entity) {

        if (entity instanceof SheepEntity sheep) {

            if (sheep.getColor() == DyeColor.PINK) {

                return true;
            }
        }

        if (entity instanceof AxolotlEntity axolotl) {

            if (axolotl.getVariant() == AxolotlEntity.Variant.BLUE) {

                return true;
            }
        }

        if (entity.hasVehicle() && !(entity instanceof PlayerEntity)) {

            return true;
        }

        return false;
    }


    public static boolean isValuableItemStack (ItemStack stack) {

        return FinderUtil.isEnchantedDiamondOrNetheriteTool(stack)
            || FinderUtil.isEnchantedDiamondOrNetheriteArmor(stack)
            || FinderUtil.isEnchantedDiamondOrNetheriteWeapon(stack)
            || FinderUtil.isShulkerBox(stack.getItem())
            || stack.isOf(Items.ELYTRA)
            || stack.isOf(Items.EXPERIENCE_BOTTLE)
            || stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)
            || stack.isOf(Items.TOTEM_OF_UNDYING)
            || stack.isOf(Items.END_CRYSTAL);
    }
    
    private static boolean isShulkerBox (Item item) {

        return (item == Items.SHULKER_BOX
                || item == Items.WHITE_SHULKER_BOX
                || item == Items.ORANGE_SHULKER_BOX
                || item == Items.MAGENTA_SHULKER_BOX
                || item == Items.LIGHT_BLUE_SHULKER_BOX
                || item == Items.YELLOW_SHULKER_BOX
                || item == Items.LIME_SHULKER_BOX
                || item == Items.PINK_SHULKER_BOX
                || item == Items.GRAY_SHULKER_BOX
                || item == Items.LIGHT_GRAY_SHULKER_BOX
                || item == Items.CYAN_SHULKER_BOX
                || item == Items.PURPLE_SHULKER_BOX
                || item == Items.BLUE_SHULKER_BOX
                || item == Items.BROWN_SHULKER_BOX
                || item == Items.GREEN_SHULKER_BOX
                || item == Items.RED_SHULKER_BOX
                || item == Items.BLACK_SHULKER_BOX);
    }

    public static boolean isEnchantedDiamondOrNetheriteArmor (ItemStack itemStack) {

        Item item = itemStack.getItem();
        if (!itemStack.getEnchantments().isEmpty() && item instanceof ArmorItem) {

            return item == Items.DIAMOND_BOOTS
                    || item == Items.DIAMOND_CHESTPLATE
                    || item == Items.DIAMOND_HELMET
                    || item == Items.DIAMOND_LEGGINGS
                    || item == Items.NETHERITE_BOOTS
                    || item == Items.NETHERITE_CHESTPLATE
                    || item == Items.NETHERITE_HELMET
                    || item == Items.NETHERITE_LEGGINGS;
        } else {

            return false;
        }
    }

    private static boolean isEnchantedDiamondOrNetheriteTool (ItemStack itemStack) {

        Item item = itemStack.getItem();
        if (!itemStack.getEnchantments().isEmpty() && item instanceof ToolItem) {

            return item == Items.DIAMOND_PICKAXE
                    || item == Items.DIAMOND_AXE
                    || item == Items.DIAMOND_SHOVEL
                    || item == Items.NETHERITE_PICKAXE
                    || item == Items.NETHERITE_AXE
                    || item == Items.NETHERITE_SHOVEL;
        } else {

            return false;
        }
    }

    private static boolean isEnchantedDiamondOrNetheriteWeapon (ItemStack itemStack) {

        Item item = itemStack.getItem();
        if (!itemStack.getEnchantments().isEmpty() && item instanceof SwordItem) {

            return item == Items.DIAMOND_SWORD
                    || item == Items.NETHERITE_SWORD;
        } else {

            return false;
        }
    }
}
