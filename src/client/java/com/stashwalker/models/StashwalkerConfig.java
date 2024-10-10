package com.stashwalker.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StashwalkerConfig {

    private Map<String, FeatureConfig> featureConfigs = Collections.synchronizedMap(new HashMap<>());

    public Map<String, FeatureConfig> getFeatureConfigs () {

        return featureConfigs;
    }

    public void setFeatureConfigs (Map<String, FeatureConfig> featureConfigs) {

        this.featureConfigs = featureConfigs;
    }
}

