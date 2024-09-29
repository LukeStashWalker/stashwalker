package com.stashwalker.configs;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

public class StashwalkerConfig {

    private Map<String, Boolean> featureSettings = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Integer> blockColors = Collections.synchronizedMap(new HashMap<>());

    public Map<String, Boolean> getFeatureSettings () {

        return featureSettings;
    }

    public void setFeatureSettings (Map<String, Boolean> featureSettings) {

        this.featureSettings = featureSettings;
    }

    public Map<String, Integer> getBlockColors () {

        return blockColors;
    }

    public void setBlockColors (SortedMap<String, Integer> blockColors) {

        this.blockColors = blockColors;
    }
}

