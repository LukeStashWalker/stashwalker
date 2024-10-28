package com.stashwalker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

public interface LivingEntityEvent {

    Event<LivingEntityEvent> EVENT = EventFactory.createArrayBacked(

        LivingEntityEvent.class,
        (listeners) -> (entity) -> {
            for (LivingEntityEvent listener : listeners) {

                listener.onEquipStack(entity);
            }
        }
    );

    void onEquipStack(LivingEntity entity);
}
