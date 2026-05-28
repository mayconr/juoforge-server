package com.github.mayconr.juoserver.game.combat.commands;

import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext.CombatOrigin;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record AttackCommand(UOMobile attacker, int targetSerial, CombatOrigin origin) implements CombatCommand{
}
