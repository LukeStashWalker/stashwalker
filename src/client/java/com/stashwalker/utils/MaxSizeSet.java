package com.stashwalker.utils;

import java.util.HashSet;

public class MaxSizeSet<E> extends HashSet<E> {

    private static final long serialVersionUID = -23456691722L;
    private final int limit;

    public MaxSizeSet(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E object) {
        if (this.size() > limit) return false;
        return super.add(object);
    }
} 