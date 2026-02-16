package com.github.mayconr.shard;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.HandlerResult;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.model.policy.DoubleClickPolicy;
import com.github.mayconr.juoserver.game.model.policy.DropItemGroundPolicy;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyRegistry;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyResult;
import com.github.mayconr.juoserver.game.world.module.item.trigger.ItemUseRegistry;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.game.world.module.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.shard.actions.GuildButton;
import com.github.mayconr.shard.actions.HelpRequested;
import com.github.mayconr.shard.skills.Anatomy;
import com.github.mayconr.shard.skills.SkillLockedHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.mayconr.juoserver.game.world.module.ui.gump.DeclarativeGumpUI.*;

@Component
@Slf4j
public class Test {

    @Autowired private EventBus eventBus;
    @Autowired private WorldInternal worldInternal;
    @Autowired private PolicyRegistry policyRegistry;
    @Autowired private ItemUseRegistry itemUseRegistry;

    @PostConstruct
    public void setUp() {
        policyRegistry.register(DoubleClickPolicy.class, (action)->{
            return PolicyResult.allow();
        });
        policyRegistry.register(DropItemGroundPolicy.class, policy->{
            return PolicyResult.allow();
        });

        /*itemUseRegistry.register(new ItemUseTrigger() {
            @Override
            public boolean supports(ItemUseContext ctx) {
                return ctx.item().getName().equals("hat");
            }

            @Override
            public void execute(ItemUseContext ctx) {
                final var playerSession = worldInternal.getPlayerSession(ctx.player());
                playerSession.sendTarget(CursorType.NEUTRAL, result->{
                    if (TargetType.OBJECT.equals(result.type())) {
                        worldInternal.getItemBySerialId(result.serialId())
                            .ifPresent(item->{
                                System.out.println("clicou em "+item);
                            });
                    }
                });
            }
        });*/

        eventBus.register(QuestButtonPressed.class, new com.github.mayconr.shard.actions.QuestButtonPressed());
        eventBus.register(HelpButtonPressed.class, new HelpRequested());
        eventBus.register(GuildButtonPressed.class, new GuildButton());
        eventBus.register(SkillLocked.class, new SkillLockedHandler());
        // eventBus.register(MobileMove.class, this::onMove);
        eventBus.register(MobileSpeech.class, this::speech);
        /*eventBus.register(
        Prompt.class, this::move, prompt -> prompt.name().equalsIgnoreCase("goto"));*/
        eventBus.register(
                Prompt.class, this::where, prompt -> prompt.name().equalsIgnoreCase("where"));
        eventBus.register(
                Prompt.class, this::select, prompt -> prompt.name().equalsIgnoreCase("target"));
        eventBus.register(
                Prompt.class, this::sendGump, prompt -> prompt.name().equalsIgnoreCase("gump"));
        eventBus.register(UseSkillRequested.class, new Anatomy(), s->s.skillId() == 1);
    }



    public HandlerResult sendGump(Prompt prompt) {
        // new DeclarativeGumpUI(Page(1));

        /*final var gump = new DeclarativeGumpUI(
            Page(1,
                Panel(300,220,5100,0,
                    Form(
                        Field(Label("Teste"), Button("teste", 1)),
                        Field(Label("Teste"), Button("Foi", 2)),
                        PageButton(4005, 4007, 2)
                    )
                )
            ),
            Page(2, Panel(300,220,5100,0,
                    Form(
                        Field(Label("Teste"), Button("Maycon", 1)),
                        PageButton(4005, 4007, 3)
                    )
            )),
            Page(3, Panel(300,220,5100,0,
                    Form(
                            Field(Label("Teste"), Button("Gregorio", 1)),
                            Field(Label("Icon"), ItemIcon(38960)),
                            PageButton(4005, 4007, 1)
                    )
            ))
        );*/

        // final var gump = new DeclarativeGumpUI(Page(1, Image(5599), Image(5593)));

        // final var gump = new DeclarativeGumpUI(Page(1, TextArea(2, 100, 50)));

        final var gump =
                new DeclarativeGumpUI(
                        Page(
                                1,
                                Panel(
                                        300,
                                        300,
                                        3000,
                                        Row(
                                                12,
                                                Label("Teste"),
                                                Label("maycon"),
                                                InlineField(Label("Name"), TextField(1, 50)),
                                                Button(2450, 2451, 2)))));

        worldInternal.sendGump(
                prompt.player(),
                gump,
                (ctx, selection) -> {
                    System.out.println("Recebido " + selection.getText(0));
                });
        return HandlerResult.CONTINUE;
    }

    public HandlerResult onMove(MobileMoved event) {
        System.out.println(event.mobile() + " andou");
        return HandlerResult.CONTINUE;
    }

    public HandlerResult speech(MobileSpeech spoke) {
        return HandlerResult.CONTINUE;
    }

    public HandlerResult move(Prompt prompt) {
        worldInternal.teleport(prompt.player(), new PointInTheWorld(2516, 555, 0));
        return HandlerResult.CONTINUE;
    }

    public HandlerResult where(Prompt prompt) {
        log.info(
                "Estou em x={}, y={}, z={}",
                prompt.player().getX(),
                prompt.player().getY(),
                prompt.player().getZ());
        return HandlerResult.CONTINUE;
    }

    public HandlerResult select(Prompt prompt) {
        return HandlerResult.CONTINUE;
    }

}
