package com.stashwalker.mixininterfaces;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;

public interface IDataTrackerMixin {
    <T> void addTrackedDataListener(TrackedData<T> trackedData, Entity entity);
}