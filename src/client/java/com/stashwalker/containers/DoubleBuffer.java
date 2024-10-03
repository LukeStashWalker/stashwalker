package com.stashwalker.containers;

import java.util.concurrent.atomic.AtomicReference;

// DoubleBuffer class that uses a generic type T
public class DoubleBuffer<T> {

    private T buffer1 = null;     
    private T buffer2 = null; 
    private final AtomicReference<T> currentBufferRef = new AtomicReference<>();
    private final Object lock = new Object(); // Lock object to manage buffer switching

    // Method to add multiple items to the inactive buffer
    public void updateBuffer (T newData) {

        T inactiveBuffer = getInactiveBuffer();

        synchronized (lock) {

            // Update the inactive buffer directly
            if (inactiveBuffer == buffer1) {

                buffer1 = newData;
            } else {

                buffer2 = newData;
            }
        }

        // Switch to the newly updated buffer
        switchBuffer();
    }

    // Method to get the inactive buffer
    private T getInactiveBuffer () {

        synchronized (lock) {

            // Determine which buffer is inactive
            return (currentBufferRef.get() == buffer1) ? buffer2 : buffer1;
        }
    }

    // Method to switch the buffers
    private void switchBuffer () {

        synchronized (lock) {

            T inactiveBuffer = getInactiveBuffer();
            currentBufferRef.set(inactiveBuffer);
        }
    }

    // Method for the reader to get data from the current buffer
    public T readBuffer () {

        T currentBuffer = currentBufferRef.get();

        return currentBuffer; // No need for additional synchronization here
    }
}
