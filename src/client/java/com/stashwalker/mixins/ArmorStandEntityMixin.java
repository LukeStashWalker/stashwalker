package com.stashwalker.mixins;

import com.stashwalker.events.ArmorStandEntityEvent;
import com.stashwalker.utils.FinderUtil;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin {

    @Inject(method = "equipStack", at = @At("HEAD"))
    private void onEquipStack(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {

        ArmorStandEntity entity = (ArmorStandEntity) (Object) this;
        if (
            FinderUtil.isEnchantedDiamondOrNetheriteArmor(stack)
            || stack.isOf(Items.ELYTRA)
        ) {
            
            ArmorStandEntityEvent.EVENT.invoker().onEquipStack(entity);
        }
    }
}