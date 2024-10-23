package com.stashwalker.containers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.util.math.BlockPos;

public class KDTree<T> {

    private Node<T> root;
    private final Function<T, BlockPos> positionExtractor;

    public KDTree (Function<T, BlockPos> positionExtractor) {

        this.positionExtractor = positionExtractor;
    }

    private static class Node<T> {

        T point;
        Node<T> left;
        Node<T> right;

        Node(T point) {

            this.point = point;
        }
    }

    public void insert (T point) {

        root = insertRec(root, point, 0);
    }

    public void insertAll (List<T> points) {

        for (T point : points) {

            insert(point);
        }
    }

    private Node<T> insertRec (Node<T> node, T point, int depth) {

        if (node == null) {

            return new Node<>(point);
        }

        BlockPos pos = positionExtractor.apply(point);
        BlockPos nodePos = positionExtractor.apply(node.point);

        int axis = depth % 3;
        if (axis == 0) {

            if (pos.getX() < nodePos.getX()) {

                node.left = insertRec(node.left, point, depth + 1);
            } else {

                node.right = insertRec(node.right, point, depth + 1);
            }
        } else if (axis == 1) {

            if (pos.getY() < nodePos.getY()) {

                node.left = insertRec(node.left, point, depth + 1);
            } else {

                node.right = insertRec(node.right, point, depth + 1);
            }
        } else {

            if (pos.getZ() < nodePos.getZ()) {

                node.left = insertRec(node.left, point, depth + 1);
            } else {

                node.right = insertRec(node.right, point, depth + 1);
            }
        }

        return node;
    }

    public Set<T> rangeSearch (BlockPos targetPos, double radius) {

        Set<T> result = new HashSet<>();
        rangeSearchRec(root, targetPos, radius, 0, result);

        return result;
    }

    private void rangeSearchRec (Node<T> node, BlockPos targetPos, double radius, int depth, Set<T> result) {

        if (node == null) {

            return;
        }

        BlockPos nodePos = positionExtractor.apply(node.point);
        double distance = targetPos.getSquaredDistance(nodePos);

        if (distance <= radius * radius) {

            result.add(node.point);
        }

        int axis = depth % 3;
        double diff;
        if (axis == 0) {

            diff = targetPos.getX() - nodePos.getX();
        } else if (axis == 1) {

            diff = targetPos.getY() - nodePos.getY();
        } else {

            diff = targetPos.getZ() - nodePos.getZ();
        }

        if (diff < 0) {

            rangeSearchRec(node.left, targetPos, radius, depth + 1, result);
            if (diff * diff <= radius * radius) {

                rangeSearchRec(node.right, targetPos, radius, depth + 1, result);
            }
        } else {

            rangeSearchRec(node.right, targetPos, radius, depth + 1, result);
            if (diff * diff <= radius * radius) {

                rangeSearchRec(node.left, targetPos, radius, depth + 1, result);
            }
        }
    }
}
