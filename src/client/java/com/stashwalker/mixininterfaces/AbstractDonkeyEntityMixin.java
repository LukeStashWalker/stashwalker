package com.stashwalker.mixininterfaces;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.stashwalker.events.AbstractDonkeyChestEvent;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.AbstractDonkeyEntity;

@Mixin(AbstractDonkeyEntity.class)
public abstract class AbstractDonkeyEntityMixin {
    
    @Shadow
    private static TrackedData<Boolean> CHEST;

    @Inject(method = "setHasChest", at = @At("HEAD"))
    private void onSetHasChest(boolean hasChest, CallbackInfo ci) {

        AbstractDonkeyEntity donkey = (AbstractDonkeyEntity) (Object) this;

        if (hasChest) {

            AbstractDonkeyChestEvent.EVENT.invoker().onChestChanged(donkey);
        }
    }
}