package com.stashwalker.utils;

import java.util.*;
import java.util.stream.Collectors;

public class MapUtil {

    public static <K, V> Map<K, V> deepCopy (Map<K, V> original) {

        return new TreeMap<>(
            original.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ))
        );
    }
}

