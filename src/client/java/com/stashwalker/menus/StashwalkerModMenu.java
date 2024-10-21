package com.stashwalker.menus;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class StashwalkerModMenu implements ModMenuApi {


    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory () {
        return parent -> {
            
            StashwalkerConfigScreen configScreen = new StashwalkerConfigScreen();

            return configScreen.buildMenu(parent); // Return the config screen
        };
    }
}