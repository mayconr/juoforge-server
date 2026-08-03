package com.github.mayconr.juoserver.network.packet;

public sealed interface ExtendedCommand permits ClientVersionExtendedCommand, LanguageExtendedCommand, ScreenSizeExtendedCommand, SpellSelectionExtendedCommand, UnknownExtendedCommand {

}
