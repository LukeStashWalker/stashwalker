package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.EntityProcessor;
import com.stashwalker.features.Renderable;
import com.stashwalker.utils.MapUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class EntityTracersFeatureImpl extends AbstractBaseFeature implements EntityProcessor, Renderable {

    private final Map<UUID, List<Entity>> entitiesTempMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, List<ChestMinecartEntity>> chestMinecartEntitiesMap = Collections.synchronizedMap(new HashMap<>());
    private final DoubleListBuffer<Entity> buffer = new DoubleListBuffer<>();

    private final String entityColorKey = "entityColor";
    private final Color entityColorDefaultValue = Color.RED;
    private final String fillInBoxesKey = "fillInBoxes";
    private final Boolean fillInBoxesDefaultValue = true;
    private final String closeProximityChestMinecartsMinimumAmountKey = "closeProximityChestMinecartsMinimumAmount";
    private final Integer closeProximityChestMinecartsMinimumAmountDefaultValue = 10;
    private final String closeProximityChestMinecartsMaximumBlockDistanceKey = "closeProximityChestMinecartsMaximumBlockDistance";
    private final Integer closeProximityChestMinecartsMaximumBlockDistanceDefaultValue = 20;

    public EntityTracersFeatureImpl () {

        super();

        this.featureName = FEATURE_NAME_ENTITY_TRACER;

        this.defaultIntegerMap.put(this.entityColorKey, this.entityColorDefaultValue.getRGB());
        this.defaultIntegerMap.put(this.closeProximityChestMinecartsMinimumAmountKey, this.closeProximityChestMinecartsMinimumAmountDefaultValue);
        this.defaultIntegerMap.put(this.closeProximityChestMinecartsMaximumBlockDistanceKey, this.closeProximityChestMinecartsMaximumBlockDistanceDefaultValue);

        this.defaultBooleanMap.put(this.fillInBoxesKey, this.fillInBoxesDefaultValue);

        this.featureConfig.setIntegerConfigs(MapUtil.deepCopy(this.defaultIntegerMap));
        this.featureConfig.setBooleanConfigs(MapUtil.deepCopy(this.defaultBooleanMap));
        this.featureConfig.setStringConfigs(MapUtil.deepCopy(this.defaultStringMap));
    }

    @Override
    public void processEntity (UUID callIdentifier, Entity entity) {

        if (this.enabled) {

            if (!this.entitiesTempMap.containsKey(callIdentifier)) {

                this.entitiesTempMap.put(callIdentifier, new CopyOnWriteArrayList<>());
            } 

            this.isInterestingEntity(entity).ifPresent(e -> {

                this.entitiesTempMap.get(callIdentifier).add(e);
            });

            if (entity instanceof ChestMinecartEntity) {

                if (!this.chestMinecartEntitiesMap.containsKey(callIdentifier)) {

                    this.chestMinecartEntitiesMap.put(callIdentifier, Collections.synchronizedList(new ArrayList<>()));
                }

                this.chestMinecartEntitiesMap.get(callIdentifier).add((ChestMinecartEntity) entity);
            }
        }
    }

    @Override
    public void updateEntities (UUID callIdentifier) {

        if (this.enabled) {

            List<Entity> entities = this.entitiesTempMap.get(callIdentifier);

            List<ChestMinecartEntity> chestMinecartEntities = this.chestMinecartEntitiesMap.get(callIdentifier);
            entities.addAll(this.findOverlappingMinecartChests(chestMinecartEntities));
            entities.addAll(
                    this.findCloseProximityMinecartChests(
                        chestMinecartEntities,
                        this.featureConfig.getIntegerConfigs()
                            .get(this.closeProximityChestMinecartsMinimumAmountKey),
                        this.featureConfig.getIntegerConfigs()
                            .get(this.closeProximityChestMinecartsMaximumBlockDistanceKey)
                    )
            );

            this.buffer.updateBuffer(entities);
            this.entitiesTempMap.remove(callIdentifier);
            this.chestMinecartEntitiesMap.remove(callIdentifier);
        }
    }

    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            List<Entity> entities = this.buffer.readBuffer();
            if (!entities.isEmpty()) {

                for (Entity entity : entities) {

                    Vec3d entityPos;
                    if (entity instanceof ItemFrameEntity) {

                        entityPos = new Vec3d(
                                entity.getPos().getX(),
                                entity.getPos().getY(),
                                entity.getPos().getZ());
                    } else {

                        entityPos = new Vec3d(
                                entity.getPos().getX(),
                                entity.getPos().getY() + 0.5D,
                                entity.getPos().getZ());
                    }

                    Color color = new Color(this.getFeatureConfig().getIntegerConfigs().get(this.entityColorKey));
                    RenderUtil.drawLine(context, entityPos, color, true,
                            this.featureConfig.getBooleanConfigs().get(this.fillInBoxesKey));
                }
            }
        }
    }

    @Override
    public void clear () {

        this.buffer.updateBuffer(Collections.emptyList());
    }

    private Optional<Entity> isInterestingEntity (Entity entity) {

        if (entity instanceof ItemEntity) {

            ItemEntity itemEntity = (ItemEntity) entity;
            ItemStack itemStack = itemEntity.getStack();

            if (isEnchantedDiamondOrNetherite(itemStack)

                    || itemStack.getItem() == Items.ELYTRA
                    || itemStack.getItem() == Items.EXPERIENCE_BOTTLE
                    || itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE
                    || itemStack.getItem() == Items.TOTEM_OF_UNDYING
                    || itemStack.getItem() == Items.END_CRYSTAL

                    || itemStack.getItem() == Items.SHULKER_BOX
                    || itemStack.getItem() == Items.WHITE_SHULKER_BOX
                    || itemStack.getItem() == Items.ORANGE_SHULKER_BOX
                    || itemStack.getItem() == Items.MAGENTA_SHULKER_BOX
                    || itemStack.getItem() == Items.LIGHT_BLUE_SHULKER_BOX
                    || itemStack.getItem() == Items.YELLOW_SHULKER_BOX
                    || itemStack.getItem() == Items.LIME_SHULKER_BOX
                    || itemStack.getItem() == Items.PINK_SHULKER_BOX
                    || itemStack.getItem() == Items.GRAY_SHULKER_BOX
                    || itemStack.getItem() == Items.LIGHT_GRAY_SHULKER_BOX
                    || itemStack.getItem() == Items.CYAN_SHULKER_BOX
                    || itemStack.getItem() == Items.PURPLE_SHULKER_BOX
                    || itemStack.getItem() == Items.BLUE_SHULKER_BOX
                    || itemStack.getItem() == Items.BROWN_SHULKER_BOX
                    || itemStack.getItem() == Items.GREEN_SHULKER_BOX
                    || itemStack.getItem() == Items.RED_SHULKER_BOX
                    || itemStack.getItem() == Items.BLACK_SHULKER_BOX) {

                return Optional.of(entity);
            }

        } else if ((entity instanceof AbstractDonkeyEntity
                && ((AbstractDonkeyEntity) entity).hasChest()
                && !((AbstractDonkeyEntity) entity).hasPlayerRider())

                ||

                (entity instanceof LlamaEntity
                        && ((LlamaEntity) entity).hasChest()
                        && !((LlamaEntity) entity).hasPlayerRider())

                ||

                (entity instanceof ChestBoatEntity
                        && !((ChestBoatEntity) entity).hasPlayerRider())

                ||

                (entity instanceof ItemFrameEntity)) {

            return Optional.of(entity);
        }

        return Optional.empty();
    }

    private boolean isEnchantedDiamondOrNetherite (ItemStack itemStack) {

        if (itemStack.hasEnchantments()) {

            if (itemStack.getItem() instanceof ArmorItem) {

                return (itemStack.getItem() == Items.DIAMOND_BOOTS ||
                        itemStack.getItem() == Items.DIAMOND_CHESTPLATE ||
                        itemStack.getItem() == Items.DIAMOND_HELMET ||
                        itemStack.getItem() == Items.DIAMOND_LEGGINGS) ||
                        (itemStack.getItem() == Items.NETHERITE_BOOTS ||
                                itemStack.getItem() == Items.NETHERITE_CHESTPLATE ||
                                itemStack.getItem() == Items.NETHERITE_HELMET ||
                                itemStack.getItem() == Items.NETHERITE_LEGGINGS);
            } else if (itemStack.getItem() instanceof ToolItem || itemStack.getItem() instanceof SwordItem) {

                return (itemStack.getItem() == Items.DIAMOND_PICKAXE ||
                        itemStack.getItem() == Items.DIAMOND_AXE ||
                        itemStack.getItem() == Items.DIAMOND_SHOVEL ||
                        itemStack.getItem() == Items.DIAMOND_SWORD) ||
                        (itemStack.getItem() == Items.NETHERITE_PICKAXE ||
                                itemStack.getItem() == Items.NETHERITE_AXE ||
                                itemStack.getItem() == Items.NETHERITE_SHOVEL ||
                                itemStack.getItem() == Items.NETHERITE_SWORD);
            }
        }

        return false;
    }

    private List<Entity> findOverlappingMinecartChests (List<ChestMinecartEntity> entities) {

        Set<ChestMinecartEntity> minecartChests = new HashSet<>();

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

    private List<Entity> findCloseProximityMinecartChests (
        List<ChestMinecartEntity> entities, 
        int chestMinecartAmount,
        int blocksProximity
    ) {

        Set<ChestMinecartEntity> closeProximityMinecarts = new HashSet<>();

        for (int i = 0; i < entities.size(); i++) {

            ChestMinecartEntity currentMinecart = entities.get(i);
            Set<ChestMinecartEntity> nearbyMinecarts = new HashSet<>();

            for (int j = 0; j < entities.size(); j++) {

                if (i != j) {

                    ChestMinecartEntity otherMinecart = entities.get(j);
                    double distance = currentMinecart.squaredDistanceTo(otherMinecart);

                    if (distance <= blocksProximity * blocksProximity) {
                        
                        nearbyMinecarts.add(otherMinecart);
                    }
                }
            }

            if (nearbyMinecarts.size() >= chestMinecartAmount) {

                closeProximityMinecarts.addAll(nearbyMinecarts);
            }
        }

        return new ArrayList<>(closeProximityMinecarts);
    }
}
