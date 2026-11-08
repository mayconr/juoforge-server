package com.github.mayconr.juoserver.network.packet;

public sealed interface ExtendedCommand permits ClientTypeExtendedCommand, ClientVersionExtendedCommand, CloseStatusExtendedCommand, LanguageExtendedCommand, ScreenSizeExtendedCommand, SpellSelectionExtendedCommand, UnknownExtendedCommand {

}
