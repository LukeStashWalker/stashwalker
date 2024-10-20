package com.stashwalker.features;

import java.util.Map;

import com.stashwalker.models.FeatureConfig;

public interface Feature {

    public static final String FEATURE_NAME_BLOCK_TRACER = "BlockTracers";
    public static final String FEATURE_NAME_ENTITY_TRACER = "EntityTracers";
    public static final String FEATURE_NAME_NEW_CHUNKS = "NewChunks";
    public static final String FEATURE_NAME_SIGN_READER = "SignReader";
    public static final String FEATURE_NAME_ALTERED_STRUCTURES = "AlteredStructures";

    boolean isEnabled ();
    void setEnabled (boolean enabled);

    String getFeatureName ();
    
    FeatureConfig getFeatureConfig ();

    void setFeatureConfig (FeatureConfig featureConfig);

    Map<String, Integer> getDefaultIntegerMap ();
    Map<String, Boolean> getDefaultBooleanMap ();
    Map<String, String> getDefaultStringMap ();

    void clear ();
}
