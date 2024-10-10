package com.stashwalker.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stashwalker.constants.Constants;
import com.stashwalker.features.Feature;
import com.stashwalker.models.FeatureConfig;
import com.stashwalker.models.StashwalkerConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class ConfigManager {

    private static final Path CONFIG_PATH = Paths.get("config/stashwalker_v121.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public void loadConfig () {

        if (Files.exists(CONFIG_PATH)) {

            try {

                String json = Files.readString(CONFIG_PATH);
                StashwalkerConfig stashwalkerConfigDisk = GSON.fromJson(json, StashwalkerConfig.class);

                Constants.FEATURES.forEach(f -> {

                    FeatureConfig featureConfigDisk = 
                        stashwalkerConfigDisk
                            .getFeatureConfigs()
                            .get(this.getFeatureNameKey(f));
                    if (featureConfigDisk != null) {

                        f.setEnabled(featureConfigDisk.getBooleanConfigs().get("enabled"));
                        Map<String, Boolean> booleanMapDisk = featureConfigDisk.getBooleanConfigs();    
                        Map<String, Integer> integerMapDisk = featureConfigDisk.getIntegerConfigs();    
                        Map<String, String> stringMapDisk = featureConfigDisk.getStringConfigs();    
                        if (booleanMapDisk != null) {

                            booleanMapDisk.entrySet().forEach(e -> {

                                Boolean value = e.getValue();
                                if (value != null && e.getKey() != null) {

                                    f.getFeatureConfig().getBooleanConfigs().put(e.getKey(), value);
                                }
                            });
                            
                        }
                        if (integerMapDisk != null) {

                            integerMapDisk.entrySet().forEach(e -> {

                                Integer value = e.getValue();
                                if (value != null && e.getKey() != null) {

                                    f.getFeatureConfig().getIntegerConfigs().put(e.getKey(), e.getValue());
                                }
                            });
                            
                        }
                        if (stringMapDisk != null) {

                            stringMapDisk.entrySet().forEach(e -> {

                                String value = e.getValue();
                                if (value != null && e.getKey() != null) {

                                    f.getFeatureConfig().getStringConfigs().put(e.getKey(), e.getValue());
                                }
                            });
                        }
                    }
                });
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    public void saveConfig () {

        try {

            StashwalkerConfig configData = new StashwalkerConfig();
            Constants.FEATURES.forEach(f -> {

                f.getFeatureConfig().getBooleanConfigs().put("enabled", f.isEnabled());
                configData.getFeatureConfigs().put(getFeatureNameKey(f), f.getFeatureConfig());
            });

            Files.createDirectories(CONFIG_PATH.getParent()); // Ensure the directory exists
            String json = GSON.toJson(configData);
            Files.writeString(CONFIG_PATH, json); // Write the JSON string to the file
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private String getFeatureNameKey (Feature feature) {

        String featureName = feature.getFeatureName();

        return (featureName.toCharArray()[0] + "").toLowerCase() + featureName.substring(1);
    }
}
