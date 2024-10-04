package com.stashwalker.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

public class StashwalkerConfig {

    private Map<String, Boolean> featureSettings = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Integer> featureColors = Collections.synchronizedMap(new HashMap<>());

    public Map<String, Boolean> getFeatureSettings () {

        return featureSettings;
    }

    public void setFeatureSettings (Map<String, Boolean> featureSettings) {

        this.featureSettings = featureSettings;
    }

    public Map<String, Integer> getFeatureColors () {

        return featureColors;
    }

    public void setBlockColors (SortedMap<String, Integer> blockColors) {

        this.featureColors = blockColors;
    }
}

