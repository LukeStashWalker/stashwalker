package com.stashwalker.features.impl;

import java.awt.Color;

import com.stashwalker.constants.Constants;
import com.stashwalker.containers.DoubleListBuffer;
import com.stashwalker.features.AbstractBaseFeature;
import com.stashwalker.features.Processor;
import com.stashwalker.features.Renderable;
import com.stashwalker.utils.MapUtil;
import com.stashwalker.utils.RenderUtil;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.network.ClientPlayerEntity;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityTracersFeatureImpl extends AbstractBaseFeature implements Processor, Renderable  {

    private final DoubleListBuffer<Entity> buffer = new DoubleListBuffer<>();

    private final String entityColorKey = "entityColor";
    private final Color entityColorDefaultValue = Color.RED;
    private final String fillInBoxesKey = "fillInBoxes";
    private final Boolean fillInBoxesDefaultValue = false;

    public EntityTracersFeatureImpl () {

        super();

        this.featureName = FEATURE_NAME_ENTITY_TRACER;
        
        this.defaultIntegerMap.put(entityColorKey, entityColorDefaultValue.getRGB());

        this.defaultBooleanMap.put(fillInBoxesKey, fillInBoxesDefaultValue);

        this.featureConfig.setIntegerConfigs(MapUtil.deepCopy(this.defaultIntegerMap));
        this.featureConfig.setBooleanConfigs(MapUtil.deepCopy(this.defaultBooleanMap));
        this.featureConfig.setStringConfigs(MapUtil.deepCopy(this.defaultStringMap));
    }

    @Override
    public void process () {

        if (this.enabled) {

            this.buffer.updateBuffer(this.findEntities());
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
                    RenderUtil.drawLine(context, entityPos, color, true, this.featureConfig.getBooleanConfigs().get(this.fillInBoxesKey));
                }
            }
        }
    }

    @Override
    public void clear () {
        
        this.buffer.updateBuffer(Collections.emptyList());
    }

    public List<Entity> findEntities () {

        int playerRenderDistance = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance();
        double renderDistanceInBlocks = playerRenderDistance * 16; // Convert render distance to blocks
        Vec3d playerPos = Constants.MC_CLIENT_INSTANCE.player.getPos();

        Box boundingBox = new Box(
                playerPos.x - renderDistanceInBlocks, // X min
                -64, // Y min (lowest level)
                playerPos.z - renderDistanceInBlocks, // Z min
                playerPos.x + renderDistanceInBlocks, // X max
                320, // Y max (build limit)
                playerPos.z + renderDistanceInBlocks // Z max
        );

        List<Entity> entities = Constants.MC_CLIENT_INSTANCE.world.getEntitiesByClass(
                Entity.class,
                boundingBox, e -> {

                    if (e instanceof ItemEntity) {

                        ItemEntity itemEntity = (ItemEntity) e;
                        ItemStack itemStack = itemEntity.getStack();

                        if (
                            isEnchantedDiamondOrNetherite(itemStack)

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
                            || itemStack.getItem() == Items.BLACK_SHULKER_BOX
                        ) {

                            return true;
                        } 

                    } else if (
                        (
                            e instanceof AbstractDonkeyEntity
                            && ((AbstractDonkeyEntity) e).hasChest()
                            && !((AbstractDonkeyEntity) e).hasPlayerRider()
                        )

                        ||

                        (
                            e instanceof LlamaEntity
                            && ((LlamaEntity) e).hasChest()
                            && !((LlamaEntity) e).hasPlayerRider()
                        )

                        ||

                        (
                            e instanceof ChestBoatEntity
                            && !((ChestBoatEntity) e).hasPlayerRider()
                        )

                        ||

                        (e instanceof ItemFrameEntity)
                    ) {

                        return true;
                    }

                    return false;
                });

        entities.addAll(findOverlappingMinecartChests());

        return entities;
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



    private List<Entity> findOverlappingMinecartChests () {

        ClientPlayerEntity player = Constants.MC_CLIENT_INSTANCE.player;
        int renderDistanceChunks = Constants.MC_CLIENT_INSTANCE.options.getClampedViewDistance();
        double renderDistanceInBlocks = renderDistanceChunks * 16;

        Set<ChestMinecartEntity> minecartChests = new HashSet<>();

        Vec3d playerPos = Constants.MC_CLIENT_INSTANCE.player.getPos();
        Box boundingBox = new Box(
                playerPos.x - renderDistanceInBlocks,
                -64,
                playerPos.z - renderDistanceInBlocks,
                playerPos.x + renderDistanceInBlocks,
                320,
                playerPos.z + renderDistanceInBlocks
        );

        // Get all MinecartChests within the calculated radius
        List<ChestMinecartEntity> entities = player.getWorld().getEntitiesByClass(ChestMinecartEntity.class,
                boundingBox, e -> true);

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

}
