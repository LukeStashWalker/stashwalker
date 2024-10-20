package com.stashwalker.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DoubleListBuffer<T> {

    private final List<T> buffer1 = new ArrayList<>();
    private final List<T> buffer2 = new ArrayList<>();
    private final AtomicReference<List<T>> currentBufferRef = new AtomicReference<>(buffer1);
    private final Object lock = new Object();

    public void updateBuffer (List<T> newData) {

        List<T> inactiveBuffer = getInactiveBuffer();

        synchronized (inactiveBuffer) {

            inactiveBuffer.clear();
            inactiveBuffer.addAll(newData);
        }

        switchBuffer();
    }

    public List<T> readBuffer () {

        List<T> currentBuffer = currentBufferRef.get();

        synchronized (currentBuffer) {

            return new ArrayList<>(currentBuffer);
        }
    }

    private List<T> getInactiveBuffer () {

        synchronized (lock) {

            return (currentBufferRef.get() == buffer1) ? buffer2 : buffer1;
        }
    }

    private void switchBuffer () {

        synchronized (lock) {

            List<T> inactiveBuffer = getInactiveBuffer();
            currentBufferRef.set(inactiveBuffer);
        }
    }
}