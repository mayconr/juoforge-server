package com.github.mayconr.juoserver.game.packet.handler;

import java.util.Map;

import com.github.mayconr.juoserver.game.core.model.AccountLoginMobile;
import com.github.mayconr.juoserver.game.core.model.UOAccount;
import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;

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
}
