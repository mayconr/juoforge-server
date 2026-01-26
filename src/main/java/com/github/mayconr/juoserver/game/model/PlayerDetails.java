package com.github.mayconr.juoserver.game.model;

import java.util.List;

public record PlayerDetails(UOAccount account, String password, String name, List<UOItem> equipedItems) {}
