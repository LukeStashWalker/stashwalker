package com.stashwalker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.ItemEntity;

public interface ItemEntityEvent {

    Event<ItemEntityEvent> EVENT = EventFactory.createArrayBacked(

        ItemEntityEvent.class,
        (listeners) -> (entity) -> {
            for (ItemEntityEvent listener : listeners) {

                listener.onSetStack(entity);
            }
        }
    );

    void onSetStack(ItemEntity entity);
}
