package com.github.mayconr.juoserver.game.model;

import java.util.Map;

public record SessionCreationContext(Map<Integer, AccountLoginMobile> mobiles, Map<Integer, UOCity> cities) {
}
