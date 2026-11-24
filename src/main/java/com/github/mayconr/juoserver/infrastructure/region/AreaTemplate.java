package com.github.mayconr.juoserver.infrastructure.region;

import java.util.List;

/**
 * Area of a region template. {@code rectangle} uses {@code x}, {@code y}, {@code width} and
 * {@code height}; {@code polygon} uses {@code points}.
 */
public record AreaTemplate(String type, int x, int y, int width, int height, List<AreaPoint> points) {
}
