package com.stashwalker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.decoration.ArmorStandEntity;

public interface ArmorStandEntityEvent {

    Event<ArmorStandEntityEvent> EVENT = EventFactory.createArrayBacked(

        ArmorStandEntityEvent.class,
        (listeners) -> (entity) -> {
            for (ArmorStandEntityEvent listener : listeners) {

                listener.onEquipStack(entity);
            }
        }
    );

    void onEquipStack(ArmorStandEntity entity);
}
