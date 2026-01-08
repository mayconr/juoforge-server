package com.github.mayconr.juoserver.game.server;

/** Contract for a generic encoder. */
public interface Encoder {
    /**
     * Encodes a byte array
     *
     * @param packet byte array to be encoded
     * @return the encoded packet
     */
    byte[] encode(byte[] packet);
}
