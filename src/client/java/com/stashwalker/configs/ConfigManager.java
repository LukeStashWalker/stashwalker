package com.stashwalker.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stashwalker.constants.Constants;
import com.stashwalker.models.StashwalkerConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {

    private static final Path CONFIG_PATH = Paths.get("config/stashwalker.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private StashwalkerConfig configData = new StashwalkerConfig();

    // Method to save configuration as JSON
    public void saveConfig () {

        try {

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
                this.configData.getBlockColors().entrySet()
                    .removeIf(entry -> entry.getKey().contains(" "));
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        // Set default values for missing entries
        Constants.FEATURE_NAMES.forEach(featureName -> {

            configData.getFeatureSettings().putIfAbsent(featureName, true); // Default value for features
        });

        Constants.BLOCK_DEFAULT_COLOR_MAP.forEach((blockName, color) -> {

            configData.getBlockColors().putIfAbsent(blockName, color.getRGB()); // Default color
        });
    }

    public StashwalkerConfig getConfig () {

        return this.configData;
    }
}
