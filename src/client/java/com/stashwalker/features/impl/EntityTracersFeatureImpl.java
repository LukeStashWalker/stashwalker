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
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
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

public class EntityTracersFeatureImpl extends AbstractBaseFeature implements EntityProcessor, Renderable {

    private final Map<UUID, List<Vec3d>> entitiesTempMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, List<Vec3d>> chestMinecartEntitiesMap = Collections.synchronizedMap(new HashMap<>());
    private final DoubleListBuffer<Vec3d> buffer = new DoubleListBuffer<>();

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

                this.entitiesTempMap.put(callIdentifier, new ArrayList<>());
            } 

            this.isInterestingEntity(entity).ifPresent(v -> {

                this.entitiesTempMap.get(callIdentifier).add(v);
            });

            if (entity instanceof ChestMinecartEntity) {

                if (!this.chestMinecartEntitiesMap.containsKey(callIdentifier)) {

                    this.chestMinecartEntitiesMap.put(callIdentifier, Collections.synchronizedList(new ArrayList<>()));
                }

                this.chestMinecartEntitiesMap.get(callIdentifier).add(this.copyVec3d(entity.getPos()));
            }
        }
    }

    @Override
    public void updateEntities (UUID callIdentifier) {

        if (this.enabled) {

            List<Vec3d> entityVecs = this.entitiesTempMap.get(callIdentifier);

            List<Vec3d> chestMinecartVecs = this.chestMinecartEntitiesMap.get(callIdentifier);
            entityVecs.addAll(this.findOverlappingMinecartChests(chestMinecartVecs));
            entityVecs.addAll(
                this.findCloseProximityMinecartChests(
                    chestMinecartVecs,
                    this.featureConfig.getIntegerConfigs()
                        .get(this.closeProximityChestMinecartsMinimumAmountKey),
                    this.featureConfig.getIntegerConfigs()
                        .get(this.closeProximityChestMinecartsMaximumBlockDistanceKey)
                )
            );

            this.buffer.updateBuffer(entityVecs);
            this.entitiesTempMap.remove(callIdentifier);
            this.chestMinecartEntitiesMap.remove(callIdentifier);
        }
    }

    @Override
    public void render (WorldRenderContext context) {

        if (this.enabled) {

            List<Vec3d> entityVecs = this.buffer.readBuffer();
            if (!entityVecs.isEmpty()) {

                for (Vec3d vec3d: entityVecs) {

                    Color color = new Color(this.getFeatureConfig().getIntegerConfigs().get(this.entityColorKey));
                    RenderUtil.drawLine(context, vec3d, color, true,
                        this.featureConfig.getBooleanConfigs().get(this.fillInBoxesKey));
                }
            }
        }
    }

    @Override
    public void clear () {

        this.buffer.updateBuffer(Collections.emptyList());
    }

    private Optional<Vec3d> isInterestingEntity (Entity entity) {

        if (
            isInterestingItem(entity)
            || isArmorStandWithEnchantedDiamondOrNetheriteArmor(entity)
            || isChestAnimal(entity)
            || isChestBoat(entity)
            || entity instanceof ItemFrameEntity
        ) {

            if (entity instanceof ItemFrameEntity) {

                return Optional.of(this.copyVec3d(entity.getPos()));
            } else {

                return Optional.of(new Vec3d(entity.getX(), entity.getY() + 0.5D, entity.getZ()));
            }
        } else {

            return Optional.empty();
        }
    }

    private boolean isInterestingItem (Entity entity) {

        if (entity instanceof ItemEntity) {

            ItemEntity itemEntity = (ItemEntity) entity;
            ItemStack itemStack = itemEntity.getStack();

            Item item = itemStack.getItem();
            if (
                isEnchantedDiamondOrNetheriteArmor(itemStack)
                || isEnchantedDiamondOrNetheriteTool(itemStack)
                || isEnchantedDiamondOrNetheriteWeapon(itemStack)

                || isShulkerBox(item)

                || item == Items.ELYTRA 
                || item == Items.EXPERIENCE_BOTTLE
                || item == Items.ENCHANTED_GOLDEN_APPLE 
                || item == Items.TOTEM_OF_UNDYING
                || item == Items.END_CRYSTAL
            ) {

                return true;
            } else {

                return false;
            }

        } else {

            return false;
        }
    }

    private boolean isArmorStandWithEnchantedDiamondOrNetheriteArmor (Entity entity) {

        if (entity instanceof ArmorStandEntity) {

            ArmorStandEntity armorStand = (ArmorStandEntity) entity;
            for (ItemStack itemStack : armorStand.getArmorItems()) {

                if (isEnchantedDiamondOrNetheriteArmor(itemStack)) {

                    return true;
                }
            }

            return false;
        } else {

            return false;
        }
    }

    private boolean isChestBoat (Entity entity) {

        return (
            entity instanceof ChestBoatEntity
            && !((ChestBoatEntity) entity).hasPlayerRider()
        );
    }

    private boolean isChestAnimal (Entity entity) {

        return (
            entity instanceof AbstractDonkeyEntity
            && ((AbstractDonkeyEntity) entity).hasChest()
            && !((AbstractDonkeyEntity) entity).hasPlayerRider()
        )
        
        ||

        (
            entity instanceof LlamaEntity
            && ((LlamaEntity) entity).hasChest()
            && !((LlamaEntity) entity).hasPlayerRider()
        );
    }

    private boolean isShulkerBox (Item item) {

        return (
            item == Items.SHULKER_BOX 
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
            || item == Items.BLACK_SHULKER_BOX
        );
    }

    private boolean isEnchantedDiamondOrNetheriteArmor (ItemStack itemStack) {

        Item item = itemStack.getItem();
        if (item instanceof ArmorItem) {

            return 
                item == Items.DIAMOND_BOOTS
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

    private boolean isEnchantedDiamondOrNetheriteTool (ItemStack itemStack) {

        Item item = itemStack.getItem();
        if (item instanceof ToolItem) {

            return 
                item == Items.DIAMOND_PICKAXE
                || item == Items.DIAMOND_AXE
                || item == Items.DIAMOND_SHOVEL
                || item == Items.NETHERITE_PICKAXE
                || item == Items.NETHERITE_AXE
                || item == Items.NETHERITE_SHOVEL;
        } else {

            return false;
        }
    }

    private boolean isEnchantedDiamondOrNetheriteWeapon (ItemStack itemStack) {

        Item item = itemStack.getItem();
        if (item instanceof SwordItem) {

            return 
                item == Items.DIAMOND_SWORD
                || item == Items.NETHERITE_SWORD;
        } else {

            return false;
        }
    }

    private List<Vec3d> findOverlappingMinecartChests (List<Vec3d> vecs) {

        List<Vec3d> overlappingVecs = new ArrayList<>();

        double overlapThreshold = 1.0;
        for (int i = 0; i < vecs.size(); i++) {

            Vec3d vec1 = vecs.get(i);
            boolean isOverlapping = false;

            for (int j = i + 1; j < vecs.size(); j++) {
                Vec3d vec2 = vecs.get(j);

                if (vec1.distanceTo(vec2) < overlapThreshold) {

                    isOverlapping = true;

                    break;
                }
            }

            if (isOverlapping) {

                overlappingVecs.add(vec1);
            }
        }

        return overlappingVecs;
    }

    private List<Vec3d> findCloseProximityMinecartChests (
        List<Vec3d> vecs, 
        int chestMinecartAmount,
        int blocksProximity
    ) {

        Set<Vec3d> closeProximityMinecartVecs = new HashSet<>();
        for (int i = 0; i < vecs.size(); i++) {

            Vec3d currentVec = vecs.get(i);
            Set<Vec3d> nearbyVecs = new HashSet<>();

            for (int j = 0; j < vecs.size(); j++) {

                if (i != j) {

                    Vec3d otherVec = vecs.get(j);
                    double distance = currentVec.squaredDistanceTo(otherVec);

                    if (distance <= blocksProximity * blocksProximity) {
                        
                        nearbyVecs.add(otherVec);
                    }
                }
            }

            if (nearbyVecs.size() >= chestMinecartAmount) {

                closeProximityMinecartVecs.addAll(nearbyVecs);
            }
        }

        return new ArrayList<>(closeProximityMinecartVecs);
    }

    private Vec3d copyVec3d (Vec3d vec3d) {

        return new Vec3d(vec3d.x, vec3d.y, vec3d.z);
    }
}
