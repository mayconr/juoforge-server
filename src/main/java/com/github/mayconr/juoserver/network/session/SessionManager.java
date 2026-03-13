package com.github.mayconr.juoserver.network.session;

import io.netty.channel.Channel;

public interface SessionManager {

    PlayerSession createSession(Channel channel);

}
