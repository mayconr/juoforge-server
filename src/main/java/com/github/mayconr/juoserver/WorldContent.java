package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.economy.template.RegionStockTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.messaging.template.MessageStyleTemplate;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.mobile.template.NpcStatProfile;
import com.github.mayconr.juoserver.game.mobile.template.NpcAIProfile;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.game.player.template.StartKitTemplate;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.infrastructure.region.RegionTemplate;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;

/** Content sources supplied by the shard; the engine does not prescribe a filesystem layout. */
public record WorldContent(
        GamePlaySettings settings,
        TemplateLoader<ItemTemplate> items,
        TemplateLoader<NpcTemplate> npcs,
        TemplateLoader<NpcStatProfile> npcStatProfiles,
        TemplateLoader<NpcAIProfile> npcAIProfiles,
        TemplateLoader<SpellTemplate> spells,
        TemplateLoader<BodyTemplate> bodies,
        TemplateLoader<StartKitTemplate> startingKits,
        TemplateLoader<MountTemplate> mounts,
        TemplateLoader<RegionTemplate> regions,
        TemplateLoader<MessageStyleTemplate> messageStyles,
        TemplateLoader<RegionStockTemplate> stocks
) {}
