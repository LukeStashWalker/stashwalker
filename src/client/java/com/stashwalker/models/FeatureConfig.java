package com.stashwalker.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FeatureConfig {

    private Map<String, Boolean> booleanConfigs = Collections.synchronizedMap(new HashMap<>());
    private Map<String, String> stringConfigs = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Integer> integerConfigs = Collections.synchronizedMap(new HashMap<>());

    public Map<String, Boolean> getBooleanConfigs () {

        return booleanConfigs;
    }

    public void setBooleanConfigs (Map<String, Boolean> booleanConfigs) {

        this.booleanConfigs = booleanConfigs;
    }

    public Map<String, String> getStringConfigs () {

        return stringConfigs;
    }

    public void setStringConfigs (Map<String, String> stringConfigs) {

        this.stringConfigs = stringConfigs;
    }

    public Map<String, Integer> getIntegerConfigs () {

        return integerConfigs;
    }

    public void setIntegerConfigs (Map<String, Integer> integerConfigs) {

        this.integerConfigs = integerConfigs;
    }
}
