package com.stashwalker.utils;

import net.minecraft.block.Block;
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
            Function<T, BlockPos> positionExtractor,
            int amount,
            int proximity
    ) {

        Set<T> closeProximityPositionObjects = new HashSet<>();
        KDTree<T> kdTree = new KDTree<>(positionExtractor);
        kdTree.insertAll(positionObjects);
        for (T current: positionObjects) {

            if (closeProximityPositionObjects.contains(current)) {

                continue;
            }

            List<T> nearbyPositionObjects =
                    kdTree.rangeSearch(positionExtractor.apply(current), proximity);

            if (nearbyPositionObjects.size() >= amount) {
                
                closeProximityPositionObjects.addAll(nearbyPositionObjects);
            }
        }

        return new ArrayList<>(closeProximityPositionObjects);
    }
}
