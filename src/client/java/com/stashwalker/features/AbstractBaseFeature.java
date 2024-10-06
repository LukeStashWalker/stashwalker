package com.stashwalker.features;

import java.awt.Color;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;

import com.stashwalker.containers.Pair;

import java.util.Collections;
import java.util.LinkedHashMap;

public abstract class AbstractBaseFeature implements Feature {

    protected boolean enabled = true;
    protected String featureName;
    protected Map<String, Pair<Color, Color>> featureColors = Collections.synchronizedMap(new LinkedHashMap<>());

    protected String featureColorsKeyStart;
    
    @Override
    public boolean isEnabled () {

        return enabled;
    }

    @Override
    public void setEnabled (boolean enabled) {

        this.enabled = enabled;
    }

    @Override
    public String getFeatureName () {

        return featureName;
    }

    @Override
    public String getFeatureColorKeyStart () {

        return featureColorsKeyStart;
    }


    @Override
    public Map<String, Pair<Color, Color>> getFeatureColors () {

        return featureColors;
    }

    @Override
    public void clear ()  {
        
    }
}
