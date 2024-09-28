package com.stashwalker.utils;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentBoundedSet<E> implements Iterable<E> {

    private final int maxSize;
    private final AtomicReference<Set<E>> atomicSet;

    public ConcurrentBoundedSet (int maxSize) {

        this.maxSize = maxSize;
        this.atomicSet = new AtomicReference<>(new LinkedHashSet<>());
    }

    public boolean add (E element) {

        return atomicSet.updateAndGet(currentSet -> {

            Set<E> newSet = new LinkedHashSet<>(currentSet);

            // If the element is already in the set, remove it first to reinsert it as the newest
            if (newSet.contains(element)) {

                newSet.remove(element);
            }

            // Remove the oldest element if the set size reaches maxSize
            if (newSet.size() >= maxSize) {

                E firstElement = newSet.iterator().next();
                newSet.remove(firstElement); // Remove the oldest element
            }

            // Add the new element
            newSet.add(element);

            return newSet;

        }).contains(element);  // Return whether the element was added successfully
    }

    public Set<E> getElements () {

        return new LinkedHashSet<>(atomicSet.get()); // Return a copy of the current set
    }

    // Clear method to remove all elements atomically
    public void clear () {

        atomicSet.set(new LinkedHashSet<>()); // Reset the set atomically to an empty set
    }

    // Contains method to check if an element is in the set
    public boolean contains (E element) {

        return atomicSet.get().contains(element); // Check if the current set contains the element
    }

    // Size method to get the number of elements in the set
    public int size() {

        return atomicSet.get().size(); // Return the size of the current set
    }

    // Make the set iterable by implementing Iterable<E>
    @Override
    public Iterator<E> iterator() {

        return new LinkedHashSet<>(atomicSet.get()).iterator(); // Return an iterator based on the current set
    }
}
