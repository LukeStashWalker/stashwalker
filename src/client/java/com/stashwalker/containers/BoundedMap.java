package com.stashwalker.containers;

import java.util.LinkedHashMap;
import java.util.Map;

public class BoundedMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public BoundedMap(int maxSize) {

        super(maxSize, 0.75f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry (Map.Entry<K, V> eldest) {

        return size() > maxSize;
    }
}