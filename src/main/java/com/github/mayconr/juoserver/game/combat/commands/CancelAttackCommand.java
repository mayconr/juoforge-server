package com.github.mayconr.juoserver.game.combat.commands;

import com.github.mayconr.juoserver.game.model.UOMobile;

public record CancelAttackCommand(UOMobile mobile) implements CombatCommand {
}
