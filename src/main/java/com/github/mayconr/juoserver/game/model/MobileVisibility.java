package com.github.mayconr.juoserver.game.model;

/** Presence rules shared by client visibility queries. */
public final class MobileVisibility {
    private MobileVisibility() { }

    public static boolean isPresent(UOMobile mobile) {
        return !(mobile instanceof UOPlayer player) || player.isConnected();
    }

    public static boolean isVisible(UOMobile mobile) {
        return mobile.isAlive() && isPresent(mobile);
    }
}
