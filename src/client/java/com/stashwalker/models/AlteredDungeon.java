package com.stashwalker.models;

import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class AlteredDungeon {

    private Vec3d spawnerPosition = null;
    private List<Vec3d> chestPositions = Collections.synchronizedList(new ArrayList<>());
    private List<Vec3d> dungeonPositions = Collections.synchronizedList(new ArrayList<>());
    private List<Vec3d> pillarPositions = Collections.synchronizedList(new ArrayList<>());

    private List<Vec3d> zombiePositions = Collections.synchronizedList(new ArrayList<>());
    private List<Vec3d> spiderPositions = Collections.synchronizedList(new ArrayList<>());
    private List<Vec3d> skeletonPositions = Collections.synchronizedList(new ArrayList<>());

    public synchronized Vec3d getSpawnerPosition () {

        return spawnerPosition;
    }

    public synchronized void setSpawnerPosition (Vec3d spawnerPosition) {

        this.spawnerPosition = spawnerPosition;
    }

    public List<Vec3d> getChestPositions () {

        return chestPositions;
    }

    public void setChestPositions (List<Vec3d> chestPositions) {

        this.chestPositions = Collections.synchronizedList(chestPositions);
    }

    public List<Vec3d> getDungeonPositions () {

        return dungeonPositions;
    }

    public void setDungeonPositions (List<Vec3d> dungeonPositions) {

        this.dungeonPositions = Collections.synchronizedList(dungeonPositions);
    }

    public List<Vec3d> getPillarPositions () {

        return pillarPositions;
    }

    public void setPillarPositions (List<Vec3d> pillarPositions) {

        this.pillarPositions = Collections.synchronizedList(pillarPositions);
    }

    public List<Vec3d> getZombiePositions () {

        return zombiePositions;
    }

    public void setZombiePositions (List<Vec3d> zombiePositions) {

        this.zombiePositions = Collections.synchronizedList(zombiePositions);
    }

    public List<Vec3d> getSpiderPositions () {

        return spiderPositions;
    }

    public void setSpiderPositions (List<Vec3d> spiderPositions) {

        this.spiderPositions = Collections.synchronizedList(spiderPositions);
    }

    public List<Vec3d> getSkeletonPositions () {

        return skeletonPositions;
    }

    public void setSkeletonPositions (List<Vec3d> skeletonPositions) {

        this.skeletonPositions = Collections.synchronizedList(skeletonPositions);
    }
}
