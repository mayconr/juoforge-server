package com.github.mayconr.juoserver.game.core.model;

public interface AttributeSupport {

    void addAttribute(String key, Object value);

    <T> T getAttribute(String key, T defaultValue);

    <T> T getAndSetAttribute(String key, T newValue);

    boolean hasAttribute(String key);
}
