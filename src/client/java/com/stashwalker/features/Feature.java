package com.stashwalker.features;

import java.awt.Color;
import java.util.Map;

import com.stashwalker.containers.Pair;

public interface Feature {

    public static final String FEATURE_NAME_BLOCK_TRACER = "Block Tracers";
    public static final String FEATURE_NAME_ENTITY_TRACER = "Entity Tracers";
    public static final String FEATURE_NAME_NEW_CHUNKS = "New Chunks";
    public static final String FEATURE_NAME_SIGN_READER = "Sign Reader";
    public static final String FEATURE_NAME_ALTERED_DUNGEONS = "Altered Dungeons";

    boolean isEnabled ();
    void setEnabled (boolean enabled);

    Map<String, Pair<Color, Color>> getFeatureColors ();

    String getFeatureName ();
    
    String getFeatureColorKeyStart ();

    void clear ();
}
