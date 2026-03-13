package com.github.mayconr.juoserver.game.model;

public interface AttributeSupport {

    void setPersistentAttribute(String key, Object value);

    <T> T getPersistentAttribute(String key, T defaultValue);

    Object getPersistentAttribute(String key);

    void setRuntimeAttribute(String key, Object value);

    <T> T getRuntimeAttribute(String key, T defaultValue);

    Object getRuntimeAttribute(String key);
}

