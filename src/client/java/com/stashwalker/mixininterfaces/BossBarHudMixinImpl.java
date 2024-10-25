package com.stashwalker.mixininterfaces;


import java.util.Map;
import java.util.UUID;

import net.minecraft.client.gui.hud.ClientBossBar;

public interface BossBarHudMixinImpl {

    Map<UUID, ClientBossBar> getBossBars ();
    int getWidth ();
}
