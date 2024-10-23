package com.stashwalker.containers;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;

public class KDTree<T extends Entity> {

    private Node<T> root;

    private static class Node<T extends Entity> {

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

        int axis = depth % 2;
        if (axis == 0) {

            if (point.getX() < node.point.getX()) {

                node.left = insertRec(node.left, point, depth + 1);
            } else {

                node.right = insertRec(node.right, point, depth + 1);
            }
        } else {

            if (point.getZ() < node.point.getZ()) {

                node.left = insertRec(node.left, point, depth + 1);
            } else {

                node.right = insertRec(node.right, point, depth + 1);
            }
        }
        return node;
    }

    public List<T> rangeSearch (T point, double radius) {

        List<T> result = new ArrayList<>();
        rangeSearchRec(root, point, radius, 0, result);

        return result;
    }

    private void rangeSearchRec (Node<T> node, T point, double radius, int depth, List<T> result) {

        if (node == null) {

            return;
        }

        double distance = node.point.squaredDistanceTo(point);
        if (distance <= radius * radius) {

            result.add(node.point);
        }

        int axis = depth % 2;
        double diff = (axis == 0) ? point.getX() - node.point.getX() : point.getZ() - node.point.getZ();

        if (diff < 0) {

            rangeSearchRec(node.left, point, radius, depth + 1, result);
            if (diff * diff <= radius * radius) {

                rangeSearchRec(node.right, point, radius, depth + 1, result);
            }
        } else {

            rangeSearchRec(node.right, point, radius, depth + 1, result);
            if (diff * diff <= radius * radius) {

                rangeSearchRec(node.left, point, radius, depth + 1, result);
            }
        }
    }
}
