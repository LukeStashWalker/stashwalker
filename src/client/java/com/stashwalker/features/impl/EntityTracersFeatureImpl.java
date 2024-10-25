package com.stashwalker.features.impl;

import java.awt.Color;
import com.stashwalker.constants.Constants;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.containers.KDTree;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.EntityProcessor;
import com.stashwalker.features.RenderFeature;
import com.stashwalker.utils.FinderUtil;
import com.stashwalker.utils.MapUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

public class EntityTracersFeatureImpl extends AbstractBaseFeature
        implements EntityProcessor, RenderFeature {

    private final DoubleListBuffer<Entity> buffer = new DoubleListBuffer<>();
    private final Map<Integer, Set<Entity>> entitiesBuffer = new ConcurrentHashMap<>();
    private final Function<Entity, BlockPos> positionExtractor = e -> e.getBlockPos();
    private final KDTree<Entity> kdTree = new KDTree<>(positionExtractor);

    private final String entityColorKey = "entityColor";
    private final Color entityColorDefaultValue = Color.RED;
    private final String fillInBoxesKey = "fillInBoxes";
    private final Boolean fillInBoxesDefaultValue = true;
    private final String closeProximityStorageMinecartsMinimumAmountKey =
            "closeProximityStorageMinecartsMinimumAmount";
    private final Integer closeProximityStorageMinecartsMinimumAmountDefaultValue = 10;
    private final String closeProximityStorageMinecartsMaximumBlockDistanceKey =
            "closeProximityStorageMinecartsMaximumBlockDistance";
    private final Integer closeProximityStorageMinecartsMaximumBlockDistanceDefaultValue = 20;

    public EntityTracersFeatureImpl() {

        super();

        this.featureName = FEATURE_NAME_ENTITY_TRACER;

        this.defaultIntegerMap.put(this.entityColorKey, this.entityColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.closeProximityStorageMinecartsMinimumAmountKey,
                this.closeProximityStorageMinecartsMinimumAmountDefaultValue);
        this.defaultIntegerMap.put(this.closeProximityStorageMinecartsMaximumBlockDistanceKey,
                this.closeProximityStorageMinecartsMaximumBlockDistanceDefaultValue);

        this.defaultBooleanMap.put(this.fillInBoxesKey, this.fillInBoxesDefaultValue);

        this.featureConfig.setIntegerConfigs(MapUtil.deepCopy(this.defaultIntegerMap));
        this.featureConfig.setBooleanConfigs(MapUtil.deepCopy(this.defaultBooleanMap));
        this.featureConfig.setStringConfigs(MapUtil.deepCopy(this.defaultStringMap));
    }

    @Override
    public void loadEntity (Entity entity) {

        int dimensionHash =
                Constants.MC_CLIENT_INSTANCE.world.getRegistryKey().getRegistry().hashCode();

        if (!this.entitiesBuffer.containsKey(dimensionHash)) {

            this.entitiesBuffer.put(dimensionHash, new CopyOnWriteArraySet<>());
        }

        Set<Entity> entities = this.entitiesBuffer.get(dimensionHash);
        if (entity instanceof StorageMinecartEntity minecartEntity) {

            Map<String, Integer> integerConfigs = this.featureConfig.getIntegerConfigs();
            this.kdTree.insert(minecartEntity);
            entities
                    .addAll(
                            FinderUtil
                                    .findCloseProximityBlockPositionObjects(
                                            List.of(minecartEntity),
                                            kdTree,
                                            positionExtractor,
                                            3,
                                            1));
            entities
                    .addAll(
                            FinderUtil
                                    .findCloseProximityBlockPositionObjects(
                                            List.of(minecartEntity),
                                            kdTree,
                                            positionExtractor,
                                            integerConfigs.get(
                                                    this.closeProximityStorageMinecartsMinimumAmountKey),
                                            integerConfigs.get(
                                                    this.closeProximityStorageMinecartsMaximumBlockDistanceKey)));
        } else {

            entities.add(entity);
        }
    }

    @Override
    public void unloadEntity (Entity entity) {

        this.kdTree.remove(entity);
        this.entitiesBuffer.values().forEach(l -> l.remove(entity));
    }

    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            ClientWorld world = Constants.MC_CLIENT_INSTANCE.world;

            if (world != null && world.getRegistryKey() != null) {

                int dimensionHash =
                        world.getRegistryKey().getRegistry().hashCode();

                if (!this.entitiesBuffer.containsKey(dimensionHash)) {

                    this.entitiesBuffer.put(dimensionHash, new CopyOnWriteArraySet<>());
                }
                for (Entity entity : this.entitiesBuffer.get(dimensionHash)) {

                    Vec3d vecEnd;
                    if (entity instanceof ItemFrameEntity) {

                        vecEnd = new Vec3d(
                                entity.getPos().getX(),
                                entity.getPos().getY(),
                                entity.getPos().getZ());
                    } else {

                        vecEnd = new Vec3d(
                                entity.getPos().getX(),
                                entity.getPos().getY() + 0.5D,
                                entity.getPos().getZ());
                    }

                    RenderUtil.drawLine(
                            context,
                            vecEnd,
                            new Color(this.featureConfig.getIntegerConfigs()
                                    .get(this.entityColorKey)),
                            true,
                            this.featureConfig.getBooleanConfigs().get(this.fillInBoxesKey));
                }
            }
        }
    }

    @Override
    public void clear () {

        this.buffer.updateBuffer(Collections.emptyList());
    }
}
