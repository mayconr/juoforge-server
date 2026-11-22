package com.github.mayconr.juoserver.game.ai.pathfinding;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.BiFunction;

public class Pathfinder {

    private record Tile(int x, int y, int z) implements Location {
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
            return z;
        }
    }

    private record Node(Tile tile, Node parent, Direction direction, double cost, double estimatedTotal) {}

    private final BiFunction<Location, Direction, OptionalInt> resolveStep;
    private final int maxVisited;
    private final boolean flat;

    public Pathfinder(boolean[][] walkableMap, Set<Location> dynamicBlocks) {
        this((from, direction) -> {
            int x = from.getX() + direction.getDx();
            int y = from.getY() + direction.getDy();
            if (x < 0 || x >= walkableMap.length || y < 0 || y >= walkableMap[x].length || !walkableMap[x][y]) {
                return OptionalInt.empty();
            }
            boolean blocked = dynamicBlocks.stream().anyMatch(tile -> tile.getX() == x && tile.getY() == y);
            return blocked ? OptionalInt.empty() : OptionalInt.of(0);
        }, Integer.MAX_VALUE, true);
    }

    public Pathfinder(BiFunction<Location, Direction, OptionalInt> resolveStep, int maxVisited) {
        this(resolveStep, maxVisited, false);
    }

    private Pathfinder(BiFunction<Location, Direction, OptionalInt> resolveStep, int maxVisited, boolean flat) {
        this.resolveStep = resolveStep;
        this.maxVisited = maxVisited;
        this.flat = flat;
    }

    public Optional<Direction> findNextDirection(Location start, Location goal) {
        return findPath(start, goal).stream().findFirst();
    }

    public List<Direction> findPath(Location start, Location goal) {
        Tile origin = toTile(start);
        Tile destination = toTile(goal);
        var open = new PriorityQueue<>(Comparator.comparingDouble(Node::estimatedTotal));
        Map<Tile, Double> bestCosts = new HashMap<>();
        open.add(new Node(origin, null, null, 0, distance(origin, destination)));
        bestCosts.put(origin, 0.0);

        int visited = 0;
        while (!open.isEmpty() && visited < maxVisited) {
            Node current = open.remove();
            if (current.cost() > bestCosts.get(current.tile())) {
                continue;
            }
            if (current.tile().equals(destination)) {
                return directionsTo(current);
            }
            visited++;
            expand(current, destination, open, bestCosts);
        }
        return List.of();
    }

    private void expand(Node current, Tile goal, PriorityQueue<Node> open, Map<Tile, Double> bestCosts) {
        for (Direction direction : Direction.values()) {
            var z = resolveStep.apply(current.tile(), direction);
            if (z.isEmpty()) {
                continue;
            }
            var neighbor = new Tile(current.tile().x() + direction.getDx(),
                    current.tile().y() + direction.getDy(), z.getAsInt());
            double cost = current.cost() + distance(current.tile(), neighbor);
            if (cost < bestCosts.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                bestCosts.put(neighbor, cost);
                open.add(new Node(neighbor, current, direction, cost, cost + distance(neighbor, goal)));
            }
        }
    }

    private Tile toTile(Location position) {
        return new Tile(position.getX(), position.getY(), flat ? 0 : position.getZ());
    }

    private List<Direction> directionsTo(Node goal) {
        var directions = new ArrayDeque<Direction>();
        for (Node node = goal; node.parent() != null; node = node.parent()) {
            directions.addFirst(node.direction());
        }
        return List.copyOf(directions);
    }

    private double distance(Tile from, Tile to) {
        return Math.hypot(from.x() - to.x(), from.y() - to.y());
    }
}
