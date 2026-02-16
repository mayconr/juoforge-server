package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.world.module.ui.gump.GumpContext;
import com.github.mayconr.juoserver.game.model.AccountLoginMobile;
import com.github.mayconr.juoserver.game.model.SessionCreationContext;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.player.SessionAttributes;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.player.PlayerSession;
import io.netty.util.AttributeKey;

import java.util.Map;

public class AttributeKeys {
    public static final AttributeKey<Map<Integer, AccountLoginMobile>> CHARACTERS_SLOT = AttributeKey.valueOf("CHARACTERS_SLOT");
    public static final SessionAttributes.Key<UOAccount> ACCOUNT_KEY = new SessionAttributes.Key<>("ACCOUNT_KEY");
    public static final SessionAttributes.Key<SessionCreationContext> SESSION_CREATION_CONTEXT = new SessionAttributes.Key<>("CHARACTERS_SLOT_KEY");
    public static final SessionAttributes.Key<PlayerSession> PLAYER_SESSION_KEY = new SessionAttributes.Key<>("PLAYER_SESSION_KEY");
    public static final AttributeKey<SessionOutbound> SESSION_OUTBOUND_KEY = AttributeKey.valueOf("SESSION_OUTBOUND");

    // GUMPS
    // Reference to gump ids sent to the client
    public static final SessionAttributes.Key<Map<Integer, GumpContext>> GUMP_IDS = new SessionAttributes.Key<>(Map.class.getName());
    public static final SessionAttributes.Key<Long> LAST_GUMP_RESPONSE = new SessionAttributes.Key<>(Long.class.getName());
}
