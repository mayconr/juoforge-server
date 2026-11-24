package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Area bounded by a simple or self-intersecting polygon, using the even-odd rule.
 *
 * <p>Vertices are tile corners (see {@link AreaPoint}). A tile belongs to the area when its center
 * lies inside the polygon, so the polygon {@code (x, y), (x + w, y), (x + w, y + h), (x, y + h)}
 * covers the same tiles as {@code new RectangularArea(x, y, w, h)}.
 *
 * <p>The tiles are indexed once as horizontal spans per row, which makes membership checks cheap
 * and lets {@link #randomLocation(RandomGenerator)} pick every tile with the same probability.
 */
public final class PolygonalArea implements RegionArea {

    private final List<AreaPoint> vertices;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final List<List<TileSpan>> rows;
    private final List<TileSpan> spans;
    private final long[] cumulativeTiles;

    public PolygonalArea(List<AreaPoint> vertices) {
        Objects.requireNonNull(vertices, "vertices must not be null");
        if (vertices.size() < 3) {
            throw new IllegalArgumentException("A polygon needs at least three vertices.");
        }
        this.vertices = List.copyOf(vertices);
        this.minX = this.vertices.stream().mapToInt(AreaPoint::x).min().orElseThrow();
        this.maxX = this.vertices.stream().mapToInt(AreaPoint::x).max().orElseThrow();
        this.minY = this.vertices.stream().mapToInt(AreaPoint::y).min().orElseThrow();
        this.maxY = this.vertices.stream().mapToInt(AreaPoint::y).max().orElseThrow();
        this.rows = buildRows();
        this.spans = rows.stream().flatMap(List::stream).toList();
        this.cumulativeTiles = accumulate(spans);
        if (tileCount() == 0) {
            throw new IllegalArgumentException("The polygon must cover at least one tile.");
        }
    }

    public List<AreaPoint> vertices() {
        return vertices;
    }

    /** Number of tiles covered by the area. */
    public long tileCount() {
        return cumulativeTiles.length == 0 ? 0 : cumulativeTiles[cumulativeTiles.length - 1];
    }

    @Override
    public boolean contains(Location location) {
        Objects.requireNonNull(location, "location must not be null");
        int row = location.getY() - minY;
        if (row < 0 || row >= rows.size()) {
            return false;
        }
        return rows.get(row).stream().anyMatch(span -> span.covers(location.getX()));
    }

    /**
     * Returns the center of the bounding box when it lies inside the area; otherwise, as happens in
     * some concave polygons, returns a tile inside the area.
     */
    @Override
    public Location getCenter() {
        var center = new PointInTheWorld((minX + maxX) / 2, (minY + maxY) / 2, 0);
        return contains(center) ? center : tileAt(tileCount() / 2);
    }

    @Override
    public Location randomLocation(RandomGenerator random) {
        Objects.requireNonNull(random, "random must not be null");
        return tileAt(random.nextLong(tileCount()));
    }

    private Location tileAt(long index) {
        int spanIndex = findSpan(index);
        long tilesBefore = spanIndex == 0 ? 0 : cumulativeTiles[spanIndex - 1];
        var span = spans.get(spanIndex);
        return new PointInTheWorld(span.startX() + (int) (index - tilesBefore), span.y(), 0);
    }

    private int findSpan(long index) {
        int found = Arrays.binarySearch(cumulativeTiles, index + 1);
        return found >= 0 ? found : -found - 1;
    }

    private List<List<TileSpan>> buildRows() {
        List<List<TileSpan>> result = new ArrayList<>(maxY - minY);
        for (int y = minY; y < maxY; y++) {
            result.add(spansOfRow(y));
        }
        return List.copyOf(result);
    }

    private List<TileSpan> spansOfRow(int y) {
        double[] crossings = crossingsAt(y + 0.5);
        List<TileSpan> result = new ArrayList<>();
        for (int i = 0; i + 1 < crossings.length; i += 2) {
            int startX = (int) Math.ceil(crossings[i] - 0.5);
            int endX = (int) Math.ceil(crossings[i + 1] - 0.5) - 1;
            if (endX >= startX) {
                result.add(new TileSpan(y, startX, endX));
            }
        }
        return List.copyOf(result);
    }

    private double[] crossingsAt(double scanY) {
        int count = vertices.size();
        double[] crossings = new double[count];
        int found = 0;
        for (int i = 0; i < count; i++) {
            var from = vertices.get(i);
            var to = vertices.get((i + 1) % count);
            if ((from.y() < scanY) != (to.y() < scanY)) {
                crossings[found++] = crossingX(from, to, scanY);
            }
        }
        double[] result = Arrays.copyOf(crossings, found);
        Arrays.sort(result);
        return result;
    }

    private static double crossingX(AreaPoint from, AreaPoint to, double scanY) {
        return from.x() + (scanY - from.y()) * (to.x() - from.x()) / (to.y() - from.y());
    }

    private static long[] accumulate(List<TileSpan> spans) {
        long[] result = new long[spans.size()];
        long total = 0;
        for (int i = 0; i < spans.size(); i++) {
            total += spans.get(i).size();
            result[i] = total;
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PolygonalArea polygon && vertices.equals(polygon.vertices);
    }

    @Override
    public int hashCode() {
        return vertices.hashCode();
    }

    @Override
    public String toString() {
        return "PolygonalArea" + vertices;
    }

    private record TileSpan(int y, int startX, int endX) {
        long size() {
            return (long) endX - startX + 1;
        }

        boolean covers(int x) {
            return x >= startX && x <= endX;
        }
    }
}
