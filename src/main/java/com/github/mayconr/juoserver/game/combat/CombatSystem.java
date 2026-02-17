package com.github.mayconr.juoserver.game.combat;

public interface CombatSystem {

    void requestAttack(int attackerId, int targetId);

    void cancelAttack(int attackerId);

    boolean isAttacking(int attackerId);
}
