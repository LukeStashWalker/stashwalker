package com.stashwalker.models;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.stashwalker.containers.Pair;

import net.minecraft.util.math.Vec3d;

public class AlteredMine {
    
    private Vec3d chestMinecartPosition = null;
    private List<Pair<Vec3d, Color>> pillarPositions = Collections.synchronizedList(new ArrayList<>());

    public Vec3d getchestMinecartPosition () {

        return this.chestMinecartPosition;
    }

    public void setChestMinecartPosition (Vec3d chestMinecartPosition) {

        this.chestMinecartPosition = chestMinecartPosition;
    }

    public List<Pair<Vec3d, Color>> getPillarPositions () {

        return pillarPositions;
    }

    public void setPillarPositions (List<Pair<Vec3d, Color>> pillarPositions) {

        this.pillarPositions = Collections.synchronizedList(pillarPositions);
    }
}
