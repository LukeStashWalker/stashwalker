package com.stashwalker.mixins;

import com.mojang.logging.LogUtils;
import com.stashwalker.events.AbstractDonkeyEntityEvent;
import com.stashwalker.mixininterfaces.IDataTrackerMixin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractDonkeyEntity.class)
public abstract class AbstractDonkeyEntityMixin {

    @Shadow
	private static TrackedData<Boolean> CHEST;

    @Inject(method = "setHasChest", at = @At("HEAD"))
    private void onSetHasChest(boolean hasChest, CallbackInfo ci) {

        AbstractDonkeyEntity donkey = (AbstractDonkeyEntity) (Object) this;
        if (hasChest) {

            AbstractDonkeyEntityEvent.EVENT.invoker().onSetHasChest(donkey);
        }
    }

    // Inject into the constructor to set up the tracked data listener
    @Inject(method = "<init>", at = @At("TAIL"))
    private void configureTrackedDataListener(EntityType<? extends AbstractDonkeyEntity> entityType, World world, CallbackInfo ci) {
        AbstractDonkeyEntity donkeyEntity = (AbstractDonkeyEntity) (Object) this;
        DataTracker dataTracker = donkeyEntity.getDataTracker();

        // Call the addTrackedDataListener method in DataTrackerMixin
        ((IDataTrackerMixin) dataTracker).addTrackedDataListener(CHEST, donkeyEntity);
    }
}
