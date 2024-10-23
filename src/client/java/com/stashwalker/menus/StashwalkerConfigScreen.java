package com.stashwalker.menus;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Map;

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

            Map<String, Boolean> booleanConfigs = f.getFeatureConfig().getBooleanConfigs();
            booleanConfigs.keySet().forEach(k -> {

                if (!k.toLowerCase().equals("enabled")) {

                    category.addEntry(
                            entryBuilder
                                    .startBooleanToggle(
                                            Text.translatable(StringUtil
                                                    .convertCamelCaseToWords(
                                                            k)),
                                            booleanConfigs.get(k))
                                    .setDefaultValue(
                                            f.getDefaultBooleanMap().get(k))
                                    .setSaveConsumer(newValue -> booleanConfigs
                                            .put(k, newValue))
                                    .build());
                }
            });

            Map<String, Integer> integerConfigs = f.getFeatureConfig().getIntegerConfigs();
            integerConfigs.keySet().forEach(k -> {

                if (!k.toLowerCase().contains("color")) {

                    category.addEntry(
                            entryBuilder
                                    .startIntField(Text.translatable(StringUtil
                                            .convertCamelCaseToWords(k)),
                                            integerConfigs.get(k))
                                    .setDefaultValue(
                                            f.getDefaultIntegerMap().get(k))
                                    .setSaveConsumer(newValue -> integerConfigs
                                            .put(k, newValue))
                                    .build());
                }
            });
            integerConfigs.keySet().forEach(k -> {

                if (k.toLowerCase().contains("color")) {

                    category.addEntry(
                            entryBuilder
                                    .startAlphaColorField(
                                            Text.translatable(StringUtil
                                                    .convertCamelCaseToWords(
                                                            k)),
                                            integerConfigs.get(k))
                                    .setDefaultValue(
                                            f.getDefaultIntegerMap().get(k))
                                    .setTooltip(Text.translatable(
                                            "Hex color value format: #<opacity><red><green><blue>,\nfor every part the values go from 00 to ff,\nfor example: #ffff0000 = red"))
                                    .setSaveConsumer(newValue -> {
                                        integerConfigs.put(k, newValue);
                                    })
                                    .build());
                }
            });

            Map<String, String> stringConfigs = f.getFeatureConfig().getStringConfigs();
            stringConfigs.keySet().forEach(k -> {

                category.addEntry(
                        entryBuilder
                                .startStrField(Text.translatable(
                                        StringUtil.convertCamelCaseToWords(k)),
                                        stringConfigs.get(k))
                                .setDefaultValue(f.getDefaultStringMap().get(k))
                                .setSaveConsumer(newValue -> f.getFeatureConfig()
                                        .getStringConfigs().put(k, newValue))
                                .build());
            });
        });

        builder.setSavingRunnable(Constants.CONFIG_MANAGER::saveConfig);

        return builder.build();
    }
}
