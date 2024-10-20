package com.stashwalker.containers;

import java.util.concurrent.atomic.AtomicReference;

public class DoubleBuffer<T> {

    private T buffer1 = null;     
    private T buffer2 = null; 
    private final AtomicReference<T> currentBufferRef = new AtomicReference<>();
    private final Object lock = new Object();

    public void updateBuffer (T newData) {

        T inactiveBuffer = getInactiveBuffer();

        synchronized (lock) {

            if (inactiveBuffer == buffer1) {

                buffer1 = newData;
            } else {

                buffer2 = newData;
            }
        }

        switchBuffer();
    }

    private T getInactiveBuffer () {

        synchronized (lock) {

            return (currentBufferRef.get() == buffer1) ? buffer2 : buffer1;
        }
    }

    private void switchBuffer () {

        synchronized (lock) {

            T inactiveBuffer = getInactiveBuffer();
            currentBufferRef.set(inactiveBuffer);
        }
    }

    public T readBuffer () {

        T currentBuffer = currentBufferRef.get();

        return currentBuffer;
    }
}
