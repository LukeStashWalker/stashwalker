package com.stashwalker.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import com.stashwalker.events.AbstractDonkeyEntityEvent;
import com.stashwalker.mixininterfaces.IDataTrackerMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataTracker.class)
public abstract class DataTrackerMixin implements IDataTrackerMixin {

    // Define a tracked data field and entity reference for monitoring
    @Unique private TrackedData<?> trackedData;
    @Unique private Entity entity;

    // Method to add a listener to the tracked data
    public <T> void addTrackedDataListener(TrackedData<T> trackedData, Entity entity) {
        // Assign the tracked data and entity to the fields
        this.trackedData = trackedData;
        this.entity = entity;
    }

    // Hook into the data tracking mechanism to detect changes
    @Inject(method = "set", at = @At("HEAD"))
    private <T> void onDataTrackerSet(TrackedData<T> trackedData, T value, CallbackInfo ci) {
        // Ensure we are only reacting to changes in the specific tracked data instance
        if (trackedData == this.trackedData && value instanceof Boolean && (Boolean) value) {

            AbstractDonkeyEntity donkey = (AbstractDonkeyEntity) (Object) this.entity;
            System.out.println(donkey + " " + trackedData + " " + value);
            // Trigger the event if the tracked data changes to true
            AbstractDonkeyEntityEvent.EVENT.invoker().onSetHasChest(donkey);
        }
    }
}
