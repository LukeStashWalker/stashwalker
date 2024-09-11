package com.stashwalker.rendering;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

// DoubleBuffer class that uses a generic type T
public class DoubleBuffer<T> {

    private final List<T> buffer1 = new ArrayList<>();
    private final List<T> buffer2 = new ArrayList<>();
    private final AtomicReference<List<T>> currentBufferRef = new AtomicReference<>(buffer1);
    private final Object lock = new Object(); // Lock object to manage buffer switching

    // Method to add multiple items to the inactive buffer
    public void updateBuffer (List<T> newData) {

        List<T> inactiveBuffer = getInactiveBuffer();

        // Update the inactive buffer
        synchronized (inactiveBuffer) {

            inactiveBuffer.clear();
            inactiveBuffer.addAll(newData);
        }

        // Switch to the newly updated buffer
        switchBuffer();
    }

    // Method to get the inactive buffer
    private List<T> getInactiveBuffer () {

        synchronized (lock) {

            // Determine which buffer is inactive
            return (currentBufferRef.get() == buffer1) ? buffer2 : buffer1;
        }
    }

    // Method to switch the buffers
    private void switchBuffer () {

        synchronized (lock) {

            List<T> inactiveBuffer = getInactiveBuffer();
            currentBufferRef.set(inactiveBuffer);
        }
    }

    // Method for the reader to get data from the current buffer
    public List<T> readBuffer () {

        List<T> currentBuffer = currentBufferRef.get();

        // Return a copy of the current buffer to avoid modification issues
        synchronized (currentBuffer) {

            return new ArrayList<>(currentBuffer);
        }
    }
}