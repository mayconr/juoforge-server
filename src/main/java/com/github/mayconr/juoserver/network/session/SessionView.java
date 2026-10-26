package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.network.session.i18n.ClientLocale;
import java.util.function.Supplier;

/** Read-only access to the current session; handlers do not own lifecycle state. */
public record SessionView(Supplier<UOPlayer> playerSupplier, Supplier<ClientLocale> localeSupplier,
                   GamePlaySettings settings) {
    public UOPlayer player() {
        return playerSupplier.get();
    }

    public ClientLocale locale() {
        return localeSupplier.get();
    }

    public boolean isPlayer(UOMobile mobile) {
        return player() != null && player().equals(mobile);
    }

    public boolean isInRange(Location target) {
        var player = player();
        return player != null && target != null
                && GameMath.isInRange(player, target, settings.world().visibility().range());
    }

    public boolean shouldReceiveUpdate(Location target) {
        var player = player();
        return player != null
                && (!(target instanceof UOMobile mobile) || MobileVisibility.isPresent(mobile))
                && isInRange(target);
    }
}
