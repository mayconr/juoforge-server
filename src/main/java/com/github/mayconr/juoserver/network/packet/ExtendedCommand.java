package com.github.mayconr.juoserver.network.packet;

public sealed interface ExtendedCommand permits ClientVersionExtendedCommand, CloseStatusExtendedCommand, LanguageExtendedCommand, ScreenSizeExtendedCommand, SpellSelectionExtendedCommand, UnknownExtendedCommand {

}
