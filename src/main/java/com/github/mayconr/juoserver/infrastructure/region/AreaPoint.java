package com.github.mayconr.juoserver.infrastructure.region;

/**
 * Vertex of a polygonal area. Coordinates are tile corners: the square tile {@code (x, y)} spans
 * from {@code (x, y)} to {@code (x + 1, y + 1)}.
 */
public record AreaPoint(int x, int y) {
}
