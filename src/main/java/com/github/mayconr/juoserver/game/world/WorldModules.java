package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.map.WorldMap;
import com.github.mayconr.juoserver.game.storage.WorldStorage;
import com.github.mayconr.juoserver.game.random.WorldRandom;
import com.github.mayconr.juoserver.game.ai.WorldAI;
import com.github.mayconr.juoserver.game.economy.WorldEconomy;
import com.github.mayconr.juoserver.game.devtools.WorldDevTools;
import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.combat.WorldCombat;
import com.github.mayconr.juoserver.game.player.WorldPlayer;
import com.github.mayconr.juoserver.game.messaging.WorldMessage;
import com.github.mayconr.juoserver.game.npc.WorldNpc;
import com.github.mayconr.juoserver.game.spell.WorldSpell;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.game.skill.WorldSkill;
import com.github.mayconr.juoserver.game.damage.WorldDamage;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.interaction.WorldInteraction;

/** Public domain APIs, available after world initialization. */
public interface WorldModules {
    WorldStorage storage();

    WorldRandom random();

    WorldAI ai();

    WorldEconomy economy();

    WorldDevTools devTools();

    WorldItem item();

    WorldPlayer player();

    WorldCombat combat();

    WorldMap map();

    WorldInteraction interaction();

    WorldMobile mobile();

    WorldUI ui();

    WorldSkill skill();

    WorldDamage damage();

    WorldSpell spell();

    WorldNpc npc();

    WorldMessage message();
}
