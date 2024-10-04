package com.stashwalker.menus;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Collections;
import java.awt.Color;

import com.stashwalker.constants.Constants;

public class StashwalkerConfigScreen {

    public StashwalkerConfigScreen() {
    }

    public Screen buildMenu (Screen parent) {

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("Stashwalker config"));
                // Category text is only visible if there are multiple categories
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("Block tracer colors"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        Constants.FEATURES.forEach(f -> {

            f.getFeatureColors()
                    .entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                    .forEach(e -> {

                        Color color = e.getValue().getKey() != null
                                ? e.getValue().getKey()
                                : e.getValue().getValue();
                        general.addEntry(
                                entryBuilder
                                        .startAlphaColorField(Text.translatable(e.getKey()),
                                                color.getRGB())
                                        .setDefaultValue(e.getValue().getValue().getRGB())
                                        .setSaveConsumer(newColor -> {
                                            e.getValue().setKey(new Color(newColor));
                                        })
                                        .build()
                        );
                    });
        });

        builder.setSavingRunnable(Constants.CONFIG_MANAGER::saveConfig);

        return builder.build();
    }
}
