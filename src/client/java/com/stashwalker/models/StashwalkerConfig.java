package com.stashwalker.models;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class StashwalkerConfig {

    private Map<String, FeatureConfig> featureConfigs = Collections.synchronizedMap(new TreeMap<>());

    public Map<String, FeatureConfig> getFeatureConfigs () {

        return featureConfigs;
    }

    public void setFeatureConfigs (Map<String, FeatureConfig> featureConfigs) {

        this.featureConfigs = featureConfigs;
    }
}

