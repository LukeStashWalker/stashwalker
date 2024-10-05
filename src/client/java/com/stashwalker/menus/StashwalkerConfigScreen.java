package com.stashwalker.menus;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
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

            Set<String> textDescriptions = new HashSet<>();
            f.getFeatureColors()
                    .entrySet()
                    .stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                    .forEach(e -> {

                        if (!textDescriptions.contains(f.getFeatureColorKeyStart())) {

                            general.addEntry(
                                entryBuilder.startTextDescription(Text.translatable(f.getFeatureColorKeyStart().replace("_", " "))).build()
                            );
                        }
                        textDescriptions.add(f.getFeatureColorKeyStart());

                        Color color = e.getValue().getKey() != null
                                ? e.getValue().getKey()
                                : e.getValue().getValue();
                        general.addEntry(
                                entryBuilder
                                        .startAlphaColorField(Text.translatable("\t\t" + e.getKey().replaceAll("_", " ")),
                                                color.getRGB())
                                        .setTooltip(Text.translatable("Hex color value format: #<opaqueness><red><green><blue>,\nfor every part the values go from 00 to ff,\nfor example: #ffff0000 = red"))
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
