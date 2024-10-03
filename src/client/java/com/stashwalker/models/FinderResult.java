package com.stashwalker.models;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.stashwalker.containers.Pair;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class FinderResult {

    private List<Pair<BlockPos, Color>> blockPositions = new ArrayList<>();
    private List<BlockEntity> signs = new ArrayList<>();

    public List<Pair<BlockPos, Color>> getBlockPositions () {

        return this.blockPositions;
    }

    public void setBlockPositions (List<Pair<BlockPos, Color>> blockPositions) {

        this.blockPositions = blockPositions;
    }

    public List<BlockEntity> getSigns () {

        return this.signs;
    }

    public void setSigns (List<BlockEntity> signs) {

        this.signs = signs;
    }

    // Methods to add elements
    public void addBlockPosition (Pair<BlockPos, Color> blockPosition) {

        this.blockPositions.add(blockPosition);
    }

    public void addSign (BlockEntity sign) {


        this.signs.add(sign);
    }
}
