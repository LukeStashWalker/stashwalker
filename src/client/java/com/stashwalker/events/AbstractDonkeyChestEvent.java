package com.stashwalker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.passive.AbstractDonkeyEntity;

public interface AbstractDonkeyChestEvent {

    Event<AbstractDonkeyChestEvent> EVENT = EventFactory.createArrayBacked(

        AbstractDonkeyChestEvent.class,
        (listeners) -> (donkey) -> {
            for (AbstractDonkeyChestEvent listener : listeners) {

                listener.onChestChanged(donkey);
            }
        }
    );

    void onChestChanged(AbstractDonkeyEntity donkey);
}
