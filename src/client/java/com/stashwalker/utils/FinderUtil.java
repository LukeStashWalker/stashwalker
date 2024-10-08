package com.stashwalker.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.lang.Class;
import java.util.List;

import com.stashwalker.constants.Constants;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public class FinderUtil {

    public static List<Vec3d> getEntityPositions (Class<? extends Entity> clazz, Box box) {

        return Constants.MC_CLIENT_INSTANCE.world
            .getEntitiesByClass(clazz, box, e -> { return true;})
            .stream()
            .map(e -> new Vec3d(e.getPos().x, e.getPos().y + 0.5D, e.getPos().z))
            .toList();
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
}
