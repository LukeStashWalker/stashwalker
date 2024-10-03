package com.stashwalker.menus;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.Map;

import com.stashwalker.constants.Constants;

public class StashwalkerConfigScreen {

    public StashwalkerConfigScreen() {
    }

    public Screen buildMenu (Screen parent) {

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("Stashwalker config"));
        builder.getOrCreateCategory(Text.translatable(""));
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("Block tracer colors"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        Constants.CONFIG_MANAGER.getConfig().getBlockColors()
        .entrySet()
        .stream()
        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .forEach(e -> {
                    String key = e.getKey();

                    int defaultColor = Constants.DEFAULT_BLOCK_COLOR_MAP.get(key).getRGB();
                    general.addEntry(
                            entryBuilder
                                    .startAlphaColorField(Text.translatable(key), e.getValue())
                                    .setDefaultValue(defaultColor)
                                    .setSaveConsumer(newColor -> {
                                        Constants.CONFIG_MANAGER.getConfig().getBlockColors().put(key, newColor);
                                    })
                                    .build());
        });

        builder.setSavingRunnable(Constants.CONFIG_MANAGER::saveConfig);

        return builder.build();
    }
}
