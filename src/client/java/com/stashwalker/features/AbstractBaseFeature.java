package com.stashwalker.features;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.stashwalker.models.FeatureConfig;

public abstract class AbstractBaseFeature implements Feature {

    protected boolean enabled = true;
    protected String featureName;
    protected FeatureConfig featureConfig = new FeatureConfig();

    protected Map<String, Integer> defaultIntegerMap;
    protected Map<String, Boolean> defaultBooleanMap;
    protected Map<String, String> defaultStringMap;

    public AbstractBaseFeature () {

        this.defaultIntegerMap = Collections.synchronizedMap(new TreeMap<>());
        this.defaultBooleanMap = Collections.synchronizedMap(new TreeMap<>());
        this.defaultStringMap = Collections.synchronizedMap(new TreeMap<>());
    }
    
    @Override
    public boolean isEnabled () {

        return this.enabled;
    }

    @Override
    public void setEnabled (boolean enabled) {

        this.enabled = enabled;
    }

    @Override
    public String getFeatureName () {

        return this.featureName;
    }

    @Override
    public FeatureConfig getFeatureConfig () {

        return this.featureConfig;
    }

    @Override
    public void setFeatureConfig (FeatureConfig featureConfig) {

        this.featureConfig = featureConfig;
    }

    @Override
    public Map<String, Integer> getDefaultIntegerMap () {

        return this.defaultIntegerMap;
    }
    @Override
    public Map<String, Boolean> getDefaultBooleanMap () {

        return this.defaultBooleanMap;
    }
    @Override
    public Map<String, String> getDefaultStringMap () {

        return this.defaultStringMap;
    }

    @Override
    public void clear ()  {

    }
}
