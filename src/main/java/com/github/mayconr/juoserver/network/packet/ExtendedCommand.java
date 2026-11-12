package com.github.mayconr.juoserver.network.packet;

public sealed interface ExtendedCommand permits
        ContextMenuRequestExtendedCommand,
        ContextMenuSelectionExtendedCommand,
        ClientTypeExtendedCommand,
        ClientVersionExtendedCommand,
        CloseStatusExtendedCommand,
        LanguageExtendedCommand,
        ScreenSizeExtendedCommand,
        SpellSelectionExtendedCommand,
        UnknownExtendedCommand {
}
