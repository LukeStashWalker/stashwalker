package com.stashwalker.menus;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import com.stashwalker.constants.Constants;
import com.stashwalker.utils.StringUtil;

public class StashwalkerConfigScreen {

    public StashwalkerConfigScreen() {
    }

    public Screen buildMenu (Screen parent) {

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("Stashwalker config"));
        
        Constants.FEATURES.forEach(f -> {

            ConfigCategory category = builder.getOrCreateCategory(Text.translatable(f.getFeatureName()));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            f.getFeatureConfig().getBooleanConfigs().entrySet().forEach(e -> {

                if (!e.getKey().toLowerCase().equals("enabled")) {

                    category.addEntry(
                            entryBuilder
                                    .startBooleanToggle(
                                            Text.translatable(StringUtil.convertCamelCaseToWords(e.getKey())),
                                            e.getValue())
                                    .setDefaultValue(f.getDefaultBooleanMap().get(e.getKey()))
                                    .setSaveConsumer(newValue -> f.getFeatureConfig().getBooleanConfigs()
                                            .put(e.getKey(), newValue))
                                    .build());
                }
            });

            f.getFeatureConfig().getIntegerConfigs().entrySet().forEach(e -> {

                if (e.getKey().toLowerCase().contains("color")) {

                    category.addEntry(
                            entryBuilder
                                    .startAlphaColorField(
                                            Text.translatable(StringUtil.convertCamelCaseToWords(e.getKey())),
                                            e.getValue())
                                    .setDefaultValue(f.getDefaultIntegerMap().get(e.getKey()))
                                    .setTooltip(Text.translatable(
                                            "Hex color value format: #<opaqueness><red><green><blue>,\nfor every part the values go from 00 to ff,\nfor example: #ffff0000 = red"))
                                    .setSaveConsumer(newValue -> f.getFeatureConfig().getIntegerConfigs().put(e.getKey(), newValue))
                                    .build());
                } else {
                    
                    category.addEntry(
                            entryBuilder
                                    .startIntField(Text.translatable(StringUtil.convertCamelCaseToWords(e.getKey())),
                                            e.getValue())
                                    .setDefaultValue(f.getDefaultIntegerMap().get(e.getKey()))
                                    .setSaveConsumer(newValue -> f.getFeatureConfig().getIntegerConfigs().put(e.getKey(), newValue))
                                    .build());
                }
            });

            f.getFeatureConfig().getStringConfigs().entrySet().forEach(e -> {

                    category.addEntry(
                            entryBuilder
                                    .startStrField(Text.translatable(StringUtil.convertCamelCaseToWords(e.getKey())),
                                            e.getValue())
                                    .setDefaultValue(f.getDefaultStringMap().get(e.getKey()))
                                    .setSaveConsumer(newValue -> f.getFeatureConfig().getStringConfigs().put(e.getKey(), newValue))
                                    .build());
            });
        });

        builder.setSavingRunnable(Constants.CONFIG_MANAGER::saveConfig);

        return builder.build();
    }
}
