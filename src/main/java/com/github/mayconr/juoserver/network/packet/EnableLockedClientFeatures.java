package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class EnableLockedClientFeatures extends AbstractPacket {

    public enum ClientFeatureFlag {

        T2A(0x01),
        RENAISSANCE(0x02),
        THIRD_DAWN(0x04),
        LBR(0x08),
        AOS(0x10),
        CHARACTER_SLOT_6(0x20),
        SE(0x40),
        ML(0x80),
        SPLASH_8TH_AGE(0x100),
        SPLASH_9TH_AGE(0x200),
        SPLASH_10TH_AGE(0x400),
        INCREASED_HOUSING_AND_BANK_STORAGE(0x800),
        CHARACTER_SLOT_7(0x1000),
        KR_FACES(0x2000),
        TRIAL_ACCOUNT(0x4000),
        LIVE_ACCOUNT(0x8000),
        SA(0x10000),
        HSA(0x20000),
        GOTHIC_HOUSING(0x40000),
        RUSTIC_HOUSING(0x80000),
        JUNGLE_HOUSING(0x100000),
        SHADOWGUARD_HOUSING(0x200000),
        TOL(0x400000),
        ENDLESS_JOURNEY(0x800000);

        private final int mask;

        ClientFeatureFlag(int mask) {
            this.mask = mask;
        }

        public int mask() {
            return mask;
        }

        public boolean isEnabled(int flags) {
            return (flags & mask) != 0;
        }

        public static int combine(ClientFeatureFlag... flags) {
            int result = 0;

            for (ClientFeatureFlag flag : flags) {
                result |= flag.mask;
            }

            return result;
        }
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
