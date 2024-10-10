package com.stashwalker.utils;

import java.util.*;
import java.util.stream.Collectors;

public class MapUtil {

    public static <K, V> Map<K, V> deepCopy (Map<K, V> original) {

        return original.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,   // Copy the key directly
                Map.Entry::getValue  // Copy the value directly (since String, Integer, and Boolean are immutable)
            ));
    }
}

