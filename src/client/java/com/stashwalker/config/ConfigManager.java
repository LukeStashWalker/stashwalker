package com.stashwalker.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigManager {

    private static final Path CONFIG_PATH = Paths.get("config/stashwalker.properties");

    // Method to save configuration as key-value pairs
    public void saveConfig (Map<String, Boolean> configData) {

        Properties props = new Properties();

        // Set all the properties from the map
        configData.forEach((k, v) -> props.setProperty(k, v + ""));

        try {

            Files.createDirectories(CONFIG_PATH.getParent()); // Ensure the directory exists
            props.store(Files.newBufferedWriter(CONFIG_PATH), "Stashwalker Settings");
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // Method to load the configuration as key-value pairs
    public Map<String, Boolean> loadConfig () {

        Map<String, Boolean> configData = new HashMap<>();
        Properties props = new Properties();

        if (Files.exists(CONFIG_PATH)) {

            try {
                props.load(Files.newBufferedReader(CONFIG_PATH));
                
                // Copy all properties to the map
                for (String name : props.stringPropertyNames()) {

                    configData.put(name, Boolean.parseBoolean(props.getProperty(name)));
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        return configData;
    }
}
