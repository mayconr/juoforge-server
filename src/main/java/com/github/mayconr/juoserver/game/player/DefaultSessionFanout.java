package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.handler.AttributeKeys;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class DefaultSessionFanout implements SessionFanout {

    private final ChannelGroup channelGroup;

    @Override
    public void writeAndFlush(Object message) {
        for (var channel : channelGroup) {
            if (!channel.isActive()) {
                continue;
            }
            channel.eventLoop().execute(() -> {
                channel.writeAndFlush(message);
            });
        }
    }

    @Override
    public void writeAndFlush(Object message, Predicate<UOPlayer> predicate) {
        for (var channel : channelGroup) {
            if (!channel.isActive()) {
                continue;
            }

            var outbound = channel.attr(AttributeKeys.SESSION_OUTBOUND_KEY).get();
            if (outbound == null) {
                continue;
            }

            final var playerSession = outbound.attr().get(AttributeKeys.PLAYER_SESSION_KEY);
            if (!predicate.test((UOPlayer) playerSession.getPlayer())) {
                continue;
            }

            channel.eventLoop().execute(() -> {
                channel.writeAndFlush(message);
            });
        }
    }

    @Override
    public void write(Object message) {
        for (var channel : channelGroup) {
            if (!channel.isActive()) {
                continue;
            }

            channel.eventLoop().execute(() -> {
                channel.write(message);
            });
        }
    }

    @Override
    public void write(Object message, Predicate<UOPlayer> predicate) {
        for (var channel : channelGroup) {
            if (!channel.isActive()) {
                continue;
            }
            var outbound = channel.attr(AttributeKeys.SESSION_OUTBOUND_KEY).get();
            if (outbound == null) {
                continue;
            }
            final var playerSession = outbound.attr().get(AttributeKeys.PLAYER_SESSION_KEY);
            if (!predicate.test((UOPlayer) playerSession.getPlayer())) {
                continue;
            }
            channel.eventLoop().execute(() -> {
                channel.write(message);
            });
        }
    }

    @Override
    public void flush() {
        for (var channel : channelGroup) {
            if (!channel.isActive()) {
                continue;
            }
            channel.eventLoop().execute(channel::flush);
        }
    }

    @Override
    public Optional<SessionOutbound> getSessionOutbound(UOMobile mobile) {
        return channelGroup.stream()
                .map(ch -> ch.attr(AttributeKeys.SESSION_OUTBOUND_KEY).get())
                .filter(Objects::nonNull)
                .filter(out -> {
                    var s = out.attr().get(AttributeKeys.PLAYER_SESSION_KEY);
                    return s != null && s.getPlayer().getId().equals(mobile.getId());
                })
                .findFirst();
    }
}
