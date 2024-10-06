package com.stashwalker.models;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.util.math.Vec3d;

public class CubeLine {
    private final Vec3d start;  // Start point of the line
    private final Vec3d end;    // End point of the line

    public CubeLine(Vec3d start, Vec3d end) {
        // Store the points in sorted order
        if (start.hashCode() > end.hashCode()) {
            this.start = end;
            this.end = start;
        } else {
            this.start = start;
            this.end = end;
        }
    }

    // Get the start point
    public Vec3d getStart() {
        return start;
    }

    // Get the end point
    public Vec3d getEnd() {
        return end;
    }

public boolean isHorizontal() {
    // return start.getY() == end.getY() && start.getX() != end.getX();
    return start.getY() == end.getY();
}

public boolean isVertical() {
    // return start.getX() == end.getX() && start.getZ() != end.getZ();
    return start.getY() != end.getY();
}

    // hashCode and equals for proper Set behavior based on coordinates
    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CubeLine other = (CubeLine) obj;
        return start.equals(other.start) && end.equals(other.end);
    }
}
