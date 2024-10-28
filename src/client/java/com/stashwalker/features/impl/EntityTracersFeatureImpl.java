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
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityTracersFeatureImpl extends AbstractBaseFeature
        implements EntityProcessor, RenderFeature {

    private final DoubleListBuffer<Entity> buffer = new DoubleListBuffer<>();
    private final Set<Entity> valuableEntities = new CopyOnWriteArraySet<>();
    private final Set<Entity> rareEntities = new CopyOnWriteArraySet<>();
    private final Function<Entity, BlockPos> positionExtractor = e -> e.getBlockPos();

    private KDTree<Entity> kdTree = new KDTree<>(positionExtractor);

    private final String valuableEntityColorKey = "valuableEntityColor";
    private final Color valuableEntityColorDefaultValue = Color.RED;
    private final String rareEntityColorKey = "rareEntityColor";
    private final Color rareEntityColorDefaultValue = Color.GREEN;
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

        this.defaultIntegerMap.put(this.valuableEntityColorKey, this.valuableEntityColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.rareEntityColorKey, this.rareEntityColorDefaultValue.getRGB());
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

        log.info("Loading Entity {}{}", entity, entity instanceof AbstractDonkeyEntity donkey ? " chest=" + donkey.hasChest() : "");

        switch (entity) {

            case StorageMinecartEntity minecartEntity -> {

                Map<String, Integer> integerConfigs = this.featureConfig.getIntegerConfigs();
                this.kdTree.insert(minecartEntity);
                this.valuableEntities
                        .addAll(
                                FinderUtil
                                        .findCloseProximityBlockPositionObjects(
                                                List.of(minecartEntity),
                                                kdTree,
                                                positionExtractor,
                                                3,
                                                1));
                this.valuableEntities
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
            }
            case AbstractDonkeyEntity donkey -> {

                this.valuableEntities.add(entity);
            }
            case LivingEntity livingEntity -> {

                if (!(livingEntity instanceof PlayerEntity)) {

                    livingEntity.getEquippedItems().forEach(s -> {

                        if (FinderUtil.isValuableItemStack(s)) {

                            this.valuableEntities.add(entity);
                        }
                        if (FinderUtil.isRareItemStack(s)) {

                            this.rareEntities.add(entity);
                        }
                    });
                }
            }
            case ItemFrameEntity itemFrameEntity -> {

                ItemStack heldItemStack = itemFrameEntity.getHeldItemStack();
                if (FinderUtil.isValuableItemStack(heldItemStack)) {

                    this.valuableEntities.add(entity);
                } else if (FinderUtil.isRareItemStack(heldItemStack)) {

                    this.rareEntities.add(entity);
                }
            }
            case ItemEntity itemEntity -> {

                ItemStack stack = itemEntity.getStack();
                if (FinderUtil.isValuableItemStack(stack)) {

                    this.valuableEntities.add(entity);
                } else if (FinderUtil.isRareItemStack(stack)) {

                    this.rareEntities.add(entity);
                }
            }
            default -> {

                this.valuableEntities.add(entity);
            }
        }
    }

    @Override
    public void unloadEntity (Entity entity) {

        log.info("Unloading Entity {}{}", entity, entity instanceof AbstractDonkeyEntity donkey ? " chest=" + donkey.hasChest() : "");

        this.kdTree.remove(entity);
        this.valuableEntities.remove(entity);
        this.rareEntities.remove(entity);
    }

    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            ClientWorld world = Constants.MC_CLIENT_INSTANCE.world;
            if (world != null) {

                Color valuableColor = new Color(this.featureConfig.getIntegerConfigs()
                        .get(this.valuableEntityColorKey));
                for (Entity entity : this.valuableEntities) {

                    renderEntitieHelper(context, valuableColor, entity);
                }

                Color rareColor = new Color(this.featureConfig.getIntegerConfigs()
                        .get(this.rareEntityColorKey));
                for (Entity entity : this.rareEntities) {

                    renderEntitieHelper(context, rareColor, entity);
                }
            }
        }
    }

    @Override
    public void clear () {

        this.buffer.updateBuffer(Collections.emptyList());
        this.rareEntities.clear();
        this.valuableEntities.clear();
        this.kdTree = new KDTree<>(positionExtractor);
    }

    private void renderEntitieHelper (WorldRenderContext context, Color color, Entity entity) {

        switch (entity) {

            case ItemFrameEntity i -> {

                renderHelper(context, entity, entity.getPos().getY(), color);
            }
            case AbstractDonkeyEntity d -> {

                if (d.hasChest()) {

                    renderHelper(context, entity, entity.getPos().getY() + 0.75D, color);
                }
            }
            default -> {

                renderHelper(context, entity, entity.getPos().getY() + 0.60D, color);
            }
        }
    }

    private void renderHelper (WorldRenderContext context, Entity entity, double y, Color color) {

        Vec3d vecEnd = new Vec3d(
                entity.getPos().getX(),
                y,
                entity.getPos().getZ()
        );

        RenderUtil.drawLine(
                context,
                vecEnd,
                color,
                true,
                this.featureConfig.getBooleanConfigs().get(this.fillInBoxesKey)
        );
    }
}
