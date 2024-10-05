package com.stashwalker.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.stashwalker.mixininterfaces.IBossBarHudMixin;

import java.util.Map;
import java.util.UUID;

@Mixin(BossBarHud.class)
@Environment(EnvType.CLIENT)
public abstract class BossBarHudMixin implements IBossBarHudMixin {

    @Shadow
    private Map<UUID, ClientBossBar> bossBars; // Shadow the bossBars field

    @Shadow
    private static int WIDTH;

    @Override
    public Map<UUID, ClientBossBar> getBossBars () {

        return bossBars; // Access the shadowed bossBars field
    }

    @OverrideOnly
    public int getWidth () {
        
        return WIDTH;
    }
}
