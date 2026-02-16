package com.github.mayconr.juoserver.game.world.module.combat;

public interface CombatSystem {

    void requestAttack(int attackerId, int targetId);

    void cancelAttack(int attackerId);

    boolean isAttacking(int attackerId);
}
