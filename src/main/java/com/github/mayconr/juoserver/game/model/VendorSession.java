package com.github.mayconr.juoserver.game.model;

import java.util.Map;

public record VendorSession(Map<Integer, VendorSessionItem> items) {
}
