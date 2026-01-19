package com.github.mayconr.juoserver.game.session.npc;

import java.util.*;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;

public class Pathfinder {

    private static class Node implements Comparable<Node> {
        Location location;
        Node parent;
        double gCost;
        double hCost;

        Node(Location loc, Node parent, double g, double h) {
            this.location = loc;
            this.parent = parent;
            this.gCost = g;
            this.hCost = h;
        }

        double fCost() {
            return gCost + hCost;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.fCost(), o.fCost());
        }
    }

    private static class PathfinderLocation implements Location {
        private int x;
        private int y;

        public PathfinderLocation(Location location) {
            this.x = location.getX();
            this.y = location.getY();
        }

        public PathfinderLocation(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PathfinderLocation other)) return false;
            return this.x == other.x && this.y == other.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private final boolean[][] walkableMap;
    private final int width, height;
    private final Set<Location> dynamicBlocks;

    public Pathfinder(boolean[][] walkableMap, Set<Location> dynamicBlocks) {
        this.walkableMap = walkableMap;
        this.width = walkableMap.length;
        this.height = walkableMap[0].length;
        this.dynamicBlocks = dynamicBlocks;
    }

    public Optional<Direction> findNextDirection(Location startLoc, Location endLoc) {
        Location start = new PathfinderLocation(startLoc);
        Location end = new PathfinderLocation(endLoc);

        if (start.getX() == end.getX() && start.getY() == end.getY()) {
            return Optional.empty(); // já está no destino
        }
        PriorityQueue<Node> openList = new PriorityQueue<>();
        Map<Location, Double> costSoFar = new HashMap<>();

        openList.add(new Node(start, null, 0, heuristic(start, end)));
        costSoFar.put(start, 0.0);

        while (!openList.isEmpty()) {
            Node current = openList.poll();

            if (current.location.equals(end)) {
                return extractFirstDirection(current);
            }

            for (Direction dir : Direction.values()) {
                int nx = current.location.getX() + dir.getDx();
                int ny = current.location.getY() + dir.getDy();
                Location neighbor = new PathfinderLocation(nx, ny);

                if (!isWalkable(neighbor)) continue;

                double newCost =
                        current.gCost
                                + ((dir.getDx() == 0 || dir.getDy() == 0) ? 1.0 : Math.sqrt(2));

                if (!costSoFar.containsKey(neighbor) || newCost < costSoFar.get(neighbor)) {
                    costSoFar.put(neighbor, newCost);
                    Node next = new Node(neighbor, current, newCost, heuristic(neighbor, end));
                    openList.add(next);
                }
            }
        }

        return Optional.empty(); // sem caminho possível
    }

    private Optional<Direction> extractFirstDirection(Node endNode) {
        Node current = endNode;

        if (current.parent == null) return Optional.empty();

        while (current.parent != null && current.parent.parent != null) {
            current = current.parent;
        }

        int dx = current.location.getX() - current.parent.location.getX();
        int dy = current.location.getY() - current.parent.location.getY();
        return Direction.fromDelta(dx, dy);
    }

    private double heuristic(Location a, Location b) {
        return Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
    }

    private boolean isWalkable(Location loc) {
        return loc.getX() >= 0
                && loc.getX() < width
                && loc.getY() >= 0
                && loc.getY() < height
                && walkableMap[loc.getX()][loc.getY()]
                && !dynamicBlocks.contains(loc);
    }
}
