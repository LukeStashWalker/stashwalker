package com.stashwalker.features.impl;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.stashwalker.constants.Constants;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.ChunkProcessor;
import com.stashwalker.features.Renderable;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.MapUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;


public class NewChunksFeatureImpl extends AbstractBaseFeature implements ChunkProcessor, Renderable  {

    // This List implementation is optimized for fast iteration and slow add and remove, so rendering will be fast
    private final List<ChunkPos> buffer = new CopyOnWriteArrayList<>();
    private final String newChunksColorKey = "newChunksColor";
    private final Color newChunksColorDefaultValue = Color.RED;
    private final String fillInSquaresKey = "fillInSquares";
    private final Boolean fillInSequaresDefaultValue = true;

    public NewChunksFeatureImpl () {

        super();

        this.featureName = FEATURE_NAME_NEW_CHUNKS;

        this.defaultIntegerMap.put(this.newChunksColorKey, this.newChunksColorDefaultValue.getRGB());

        this.defaultBooleanMap.put(this.fillInSquaresKey, this.fillInSequaresDefaultValue);

        this.featureConfig.setIntegerConfigs(MapUtil.deepCopy(this.defaultIntegerMap));
        this.featureConfig.setBooleanConfigs(MapUtil.deepCopy(this.defaultBooleanMap));
        this.featureConfig.setStringConfigs(MapUtil.deepCopy(this.defaultStringMap));
    }

    @Override
    public void processChunkLoad (Chunk chunk) {

        if (this.enabled) {

            if (this.isNewChunk(chunk)) {

                    buffer.add(chunk.getPos());
            }
        }
    }

    @Override
    public void processChunkUnload(Chunk chunk) {

        if (this.enabled) {

            buffer.remove(chunk.getPos());
        }
    }

    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            Color color = new Color(this.featureConfig.getIntegerConfigs().get(this.newChunksColorKey));

            RenderUtil
                    .drawChunkSquares(
                            context,
                            this.buffer,
                            63,
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            50,
                            this.featureConfig.getBooleanConfigs().get(this.fillInSquaresKey)
                    );
        }
    }

    @Override
    public void clear () {
        
        this.buffer.clear();;
    }

    public boolean isNewChunk (Chunk chunk) {

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
}
