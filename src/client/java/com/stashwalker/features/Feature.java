package com.stashwalker.features;

import java.awt.Color;
import java.util.Map;

import com.stashwalker.containers.Pair;

public interface Feature {

    public static final String FEATURE_NAME_BLOCK_TRACER = "blockTracers";
    public static final String FEATURE_NAME_ENTITY_TRACER = "entityTracers";
    public static final String FEATURE_NAME_NEW_CHUNKS = "newChunks";
    public static final String FEATURE_NAME_SIGN_READER = "signReader";
    public static final String FEATURE_NAME_ALTERED_DUNGEONS = "alteredDungeons";

    boolean isEnabled ();
    void setEnabled (boolean enabled);

    Map<String, Pair<Color, Color>> getFeatureColors ();

    String getFeatureName ();
    
    String getFeatureColorKeyStart ();

    void clear ();
}
