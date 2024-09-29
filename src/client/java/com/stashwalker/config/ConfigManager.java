package com.stashwalker.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.stashwalker.constants.Constants;

public class ConfigManager {

    private static final Path CONFIG_PATH = Paths.get("config/stashwalker.properties");
    private Map<String, Object> configData = new ConcurrentHashMap<>();

    // Method to save configuration as key-value pairs
    public void saveConfig () {

        Properties props = new Properties();

        // Set all the properties from the map
        this.configData.forEach((k, v) -> props.setProperty(k, v + ""));

        try {

            Files.createDirectories(CONFIG_PATH.getParent()); // Ensure the directory exists
            props.store(Files.newBufferedWriter(CONFIG_PATH), "Stashwalker Settings");
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // Method to load the configuration as key-value pairs
    public void loadConfig () {

        Properties props = new Properties();

        if (Files.exists(CONFIG_PATH)) {

            try {
                props.load(Files.newBufferedReader(CONFIG_PATH));
                
                // Copy all properties to the map
                for (String name : props.stringPropertyNames()) {

                    this.configData.put(name, Boolean.parseBoolean(props.getProperty(name)));
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        Constants.FEATURE_NAMES.forEach(n -> {
            
            if (!this.configData.containsKey(n)) {

                this.configData.put(n, true);
            }
        });
    }

    public Map<String, Object> getConfig () {

        return this.configData;
    }
}
