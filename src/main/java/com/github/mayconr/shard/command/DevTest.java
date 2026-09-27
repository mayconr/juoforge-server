package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.SpellbookType;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;

public class DevTest extends AbstractCommand {
    private final World world;

    public DevTest(World world) {
        super("devtest");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        if (event.arguments()[0].equals("1")) {
            var item = world.createItem(ItemRequest.byName("spellbook"), ItemTarget.dropAt(event.player()));
            System.out.println(item.getSerialId()+" gerado");
        } else {
            var id = Integer.parseInt(event.arguments()[0]);
            world.getItemBySerialId(id).ifPresent(book ->
                    world.openSpellBook(event.player(), book, SpellbookType.MAGERY, -1L));
        }
    }
}
