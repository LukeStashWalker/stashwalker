package com.stashwalker.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stashwalker.constants.Constants;
import com.stashwalker.models.StashwalkerConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.awt.Color;
import java.util.Map;

import com.stashwalker.containers.Pair;

public class ConfigManager {

    private static final Path CONFIG_PATH = Paths.get("config/stashwalker_v117.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private StashwalkerConfig configData = new StashwalkerConfig();

    // Method to save configuration as JSON
    public void saveConfig () {

        try {

            Constants.FEATURES.forEach(f -> {

                this.configData.getFeatureSettings().put(f.getFeatureName(), f.isEnabled());

                f.getFeatureColors().entrySet()
                .forEach(e -> {

                    if (e.getValue().getKey() != null) {

                        this.configData.getFeatureColors().put(e.getKey(), e.getValue().getKey().getRGB());
                    } else {

                        // Save the default if not found
                        this.configData.getFeatureColors().put(e.getKey(), e.getValue().getValue().getRGB());
                    }
                });
            });

            Files.createDirectories(CONFIG_PATH.getParent()); // Ensure the directory exists
            String json = GSON.toJson(this.configData);
            Files.writeString(CONFIG_PATH, json); // Write the JSON string to the file
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // Method to load the configuration from JSON
    public void loadConfig () {

        if (Files.exists(CONFIG_PATH)) {

            try {

                String json = Files.readString(CONFIG_PATH);
                this.configData = GSON.fromJson(json, StashwalkerConfig.class);

                Constants.FEATURES.forEach(f -> {

                    Boolean state = configData.getFeatureSettings().get(f.getFeatureName());
                    f.setEnabled(state != null ? state : false);

                    f.getFeatureColors().entrySet()
                            .forEach(e -> {

                                Map<String, Integer> configDataFeatureColors = configData.getFeatureColors();
                                if (configDataFeatureColors.containsKey(e.getKey())) {

                                    Pair<Color, Color> featureColorsEntryValue = e.getValue();
                                    featureColorsEntryValue
                                            .setKey(new Color(
                                                    configDataFeatureColors.get(e.getKey())));
                                } else {

                                    // Take the default of not found
                                    e.getValue().setKey(e.getValue().getValue());
                                }
                            });

                });
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
}
