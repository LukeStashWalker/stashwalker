package com.stashwalker.mixins;

import com.stashwalker.events.ItemFrameEntityEvent;
import com.stashwalker.utils.FinderUtil;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin {

    @Inject(method = "setHeldItemStack(Lnet/minecraft/item/ItemStack;Z)V", at = @At("HEAD"))
    private void onSetHeldItemStack (ItemStack value, boolean update, CallbackInfo ci) {

        ItemFrameEntity entity = (ItemFrameEntity) (Object) this;
        if (FinderUtil.isIterestingItemStack(value)) {

            ItemFrameEntityEvent.EVENT.invoker().onSetHeldStack(entity);
        }
    }
}
