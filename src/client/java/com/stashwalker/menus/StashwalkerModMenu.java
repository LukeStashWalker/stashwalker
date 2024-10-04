package com.stashwalker.menus;

import com.stashwalker.constants.Constants;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class StashwalkerModMenu implements ModMenuApi {


    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory () {
        return parent -> {
            
            Constants.CONFIG_MANAGER.loadConfig(); // Load the configuration before showing the screen
            StashwalkerConfigScreen configScreen = new StashwalkerConfigScreen();
            return configScreen.buildMenu(parent); // Return the config screen
        };
    }
}