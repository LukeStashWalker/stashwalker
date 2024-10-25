package com.stashwalker.mixins;

import com.stashwalker.events.ItemEntityEvent;
import com.stashwalker.utils.FinderUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Inject(method = "setStack", at = @At("HEAD"))
    private void onSetStack (ItemStack stack, CallbackInfo ci) {

        ItemEntity entity = (ItemEntity) (Object) this;
        if (entity.getItemAge() != 0 && FinderUtil.isIterestingItemStack(stack)) {

            ItemEntityEvent.EVENT.invoker().onSetStack(entity);
        }
    }
}
