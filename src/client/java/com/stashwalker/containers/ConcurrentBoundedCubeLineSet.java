package com.stashwalker.containers;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import com.stashwalker.models.CubeLine;

public class ConcurrentBoundedCubeLineSet {

    private final int maxSize;
    private final AtomicReference<Set<CubeLine>> atomicSet;
    private final AtomicReference<Map<Integer, Integer>> occurrencesMap; // Change key to Integer (hash code)
    private final AtomicReference<Set<CubeLine>> renderableLines; // Thread-safe set of renderable lines

    public ConcurrentBoundedCubeLineSet(int maxSize) {
        this.maxSize = maxSize;
        this.atomicSet = new AtomicReference<>(new LinkedHashSet<>());
        this.occurrencesMap = new AtomicReference<>(new LinkedHashMap<>());
        this.renderableLines = new AtomicReference<>(new LinkedHashSet<>()); // Initialize the renderable lines set
    }

    // Add a single CubeLine to the set with thread safety and bounding
    public boolean add(CubeLine line) {
        return atomicSet.updateAndGet(currentSet -> {
            Set<CubeLine> newSet = new LinkedHashSet<>(currentSet);

            // Remove the oldest element if the set size reaches maxSize
            if (newSet.size() >= maxSize) {
                CubeLine firstElement = newSet.iterator().next();
                newSet.remove(firstElement); // Remove the oldest element
                // Remove from occurrences map as well
                occurrencesMap.get().remove(firstElement.hashCode());
            }

            // Update occurrences map
            occurrencesMap.updateAndGet(currentMap -> {
                int hash = line.hashCode();
                currentMap.put(hash, currentMap.getOrDefault(hash, 0) + 1);
                return currentMap;
            });

            // Add the new CubeLine
            newSet.add(line);
            return newSet;

        }).contains(line);  // Return whether the line was successfully added
    }

    // Add multiple CubeLines to the set with thread safety and bounding
    public void addAll(Set<CubeLine> lines) {
        atomicSet.updateAndGet(currentSet -> {
            Set<CubeLine> newSet = new LinkedHashSet<>(currentSet);

            // Loop over the new lines and add them
            for (CubeLine line : lines) {
                if (newSet.size() >= maxSize) {
                    CubeLine firstElement = newSet.iterator().next();
                    newSet.remove(firstElement); // Remove the oldest element
                    occurrencesMap.get().remove(firstElement.hashCode());
                }

                // Update occurrences map
                occurrencesMap.updateAndGet(currentMap -> {
                    int hash = line.hashCode();
                    currentMap.put(hash, currentMap.getOrDefault(hash, 0) + 1);
                    return currentMap;
                });

                newSet.add(line);
            }

            return newSet;
        });
    }

    // Update renderable lines based on atomicSet and occurrencesMap
    public void updateRenderableLines() {
        Set<CubeLine> currentRenderableLines = Collections.synchronizedSet(new LinkedHashSet<>());
        Map<Integer, Integer> currentOccurrences = occurrencesMap.get();

        // Iterate through the occurrences map and filter for renderable lines
        for (Map.Entry<Integer, Integer> entry : currentOccurrences.entrySet()) {
            int hash = entry.getKey();
            int count = entry.getValue();

            // Find the CubeLine associated with the hash
            for (CubeLine line : atomicSet.get()) {
                if (line.hashCode() == hash) {
                    if (line.isHorizontal() && count == 1) {
                        currentRenderableLines.add(line);
                    } else if (line.isVertical() && (count == 1 || count == 3)) {
                        currentRenderableLines.add(line);
                    }
                    break; // No need to continue once found
                }
            }
        }

        // Update the renderableLines field atomically
        renderableLines.set(currentRenderableLines);
    }

    /**
     * Returns a set of CubeLine objects that are marked for rendering.
     * 
     * @return Set<CubeLine> renderable CubeLines
     */
    public Set<CubeLine> getRenderableLines() {
        return new LinkedHashSet<>(renderableLines.get()); // Return a copy of the renderable lines for safe iteration
    }

    // Clear all elements in the set atomically
    public void clear() {
        atomicSet.set(new LinkedHashSet<>());
        occurrencesMap.set(new LinkedHashMap<>());
        renderableLines.set(new LinkedHashSet<>()); // Clear renderable lines as well
    }

    // Check if a CubeLine exists in the set
    public boolean contains(CubeLine line) {
        return atomicSet.get().contains(line);
    }

    // Get the current size of the set
    public int size() {
        return atomicSet.get().size();
    }

    // Iterate over the CubeLines
    public Iterator<CubeLine> iterator() {
        return new LinkedHashSet<>(atomicSet.get()).iterator();
    }
}
