package com.stashwalker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.decoration.ItemFrameEntity;

public interface ItemFrameEntityEvent {

    Event<ItemFrameEntityEvent> EVENT = EventFactory.createArrayBacked(

        ItemFrameEntityEvent.class,
        (listeners) -> (entity) -> {
            for (ItemFrameEntityEvent listener : listeners) {

                listener.onSetHeldStack(entity);
            }
        }
    );

    void onSetHeldStack(ItemFrameEntity entity);
}
