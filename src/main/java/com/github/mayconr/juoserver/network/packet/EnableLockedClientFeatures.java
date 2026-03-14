package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class EnableLockedClientFeatures extends AbstractPacket {

    public static final class ClientFeatureFlags {
        private ClientFeatureFlags() {}

        public static final int T2A = 0x01;
        public static final int RENAISSANCE = 0x02;
        public static final int THIRD_DAWN = 0x04;
        public static final int LBR = 0x08;
        public static final int AOS = 0x10;
        public static final int CHARACTER_SLOT_6 = 0x20;
        public static final int SE = 0x40;
        public static final int ML = 0x80;
        public static final int SPLASH_8TH_AGE = 0x100;
        public static final int SPLASH_9TH_AGE = 0x200;
        public static final int SPLASH_10TH_AGE = 0x400;
        public static final int INCREASED_HOUSING_AND_BANK_STORAGE = 0x800;
        public static final int CHARACTER_SLOT_7 = 0x1000;
        public static final int KR_FACES = 0x2000;
        public static final int TRIAL_ACCOUNT = 0x4000;
        public static final int LIVE_ACCOUNT = 0x8000;
        public static final int SA = 0x10000;
        public static final int HSA = 0x20000;
        public static final int GOTHIC_HOUSING = 0x40000;
        public static final int RUSTIC_HOUSING = 0x80000;
        public static final int JUNGLE_HOUSING = 0x100000;
        public static final int SHADOWGUARD_HOUSING = 0x200000;
        public static final int TOL = 0x400000;
        public static final int ENDLESS_JOURNEY = 0x800000;
    }

    public static final int CODE = 0xB9;

    private final int featureFlags;
    private final boolean useExtendedFormat;

    /**
     * @param featureFlags bitmask das features a habilitar
     * @param useExtendedFormat true para clients 6.0.14.2+, false para legacy
     */
    public EnableLockedClientFeatures(int featureFlags, boolean useExtendedFormat) {
        super(CODE, useExtendedFormat ? 5 : 3);
        this.featureFlags = featureFlags;
        this.useExtendedFormat = useExtendedFormat;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);

        if (useExtendedFormat) {
            buf.writeInt(featureFlags);
        } else {
            buf.writeShort(featureFlags & 0xFFFF);
        }
    }
}
