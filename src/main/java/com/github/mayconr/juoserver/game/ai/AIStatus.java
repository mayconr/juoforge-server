package com.github.mayconr.juoserver.game.ai;

/**
 * Snapshot of an NPC's installed AI. Type is null when no AI is installed.
 * Active means enabled in the engine; activation policies may still prevent execution.
 */
public record AIStatus(String type, boolean installed, boolean active) {}
