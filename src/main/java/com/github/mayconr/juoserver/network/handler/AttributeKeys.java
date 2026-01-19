package com.github.mayconr.juoserver.network.handler;

import java.util.Map;

import com.github.mayconr.juoserver.game.gump.GumpContext;
import com.github.mayconr.juoserver.game.model.AccountLoginMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.session.SessionAttributes;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;

import io.netty.util.AttributeKey;

public class AttributeKeys {
    public static final AttributeKey<Map<Integer, AccountLoginMobile>> CHARACTERS_SLOT =
            AttributeKey.valueOf("CHARACTERS_SLOT");

    // Account Attributes
    public static final AttributeKey<UOAccount> ACCOUNT_LOGGED_IN =
            AttributeKey.valueOf("ACCOUNT_LOGGED_IN");

    // Player Session
    public static final AttributeKey<PlayerSession> PLAYER_SESSION =
            AttributeKey.valueOf(PlayerSession.class.getName());

    public static final SessionAttributes.Key<UOAccount> ACCOUNT_KEY = new SessionAttributes.Key<>("ACCOUNT_KEY");
    public static final SessionAttributes.Key<Map<Integer, AccountLoginMobile>> CHARACTERS_SLOT_KEY = new SessionAttributes.Key<>("CHARACTERS_SLOT_KEY");
    public static final SessionAttributes.Key<PlayerSession> PLAYER_SESSION_KEY = new SessionAttributes.Key<>("PLAYER_SESSION_KEY");
    public static final AttributeKey<SessionOutbound> SESSION_OUTBOUND_KEY = AttributeKey.valueOf("SESSION_OUTBOUND");

    // GUMPS
    // Reference to gump ids sent to the client
    public static final SessionAttributes.Key<Map<Integer, GumpContext>> GUMP_IDS = new SessionAttributes.Key<>(Map.class.getName());
    public static final SessionAttributes.Key<Long> LAST_GUMP_RESPONSE = new SessionAttributes.Key<>(Long.class.getName());
}
