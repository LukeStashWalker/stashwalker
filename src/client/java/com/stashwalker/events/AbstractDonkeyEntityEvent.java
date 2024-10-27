package com.stashwalker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.passive.AbstractDonkeyEntity;

public interface AbstractDonkeyEntityEvent {

    Event<AbstractDonkeyEntityEvent> EVENT = EventFactory.createArrayBacked(

        AbstractDonkeyEntityEvent.class,
        (listeners) -> (donkey) -> {
            for (AbstractDonkeyEntityEvent listener : listeners) {

                listener.onSetHasChest(donkey);
            }
        }
    );

    void onSetHasChest(AbstractDonkeyEntity donkey);
}
