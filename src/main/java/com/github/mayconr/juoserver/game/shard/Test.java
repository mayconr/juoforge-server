package com.github.mayconr.juoserver.game.shard;

import static com.github.mayconr.juoserver.game.core.gump.DeclarativeGumpUI.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.mayconr.juoserver.game.core.event.*;
import com.github.mayconr.juoserver.game.core.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.core.gump.GumpSystem;
import com.github.mayconr.juoserver.game.core.model.CursorType;
import com.github.mayconr.juoserver.game.core.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.core.session.game.GameSession;
import com.github.mayconr.juoserver.game.storage.account.AccountStorage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Test {

    @Autowired private EventBus eventBus;
    @Autowired private GameSession gameSession;
    @Autowired private GumpSystem gumpSystem;
    @Autowired private AccountStorage accountStorage;

    @PostConstruct
    public void setUp() {

        accountStorage
                .findByUsername("admin")
                .thenAccept(
                        acct -> {
                            System.out.println(acct.get());
                        });

        // eventBus.register(MobileMove.class, this::onMove);
        eventBus.register(MobileSpoke.class, this::speech);
        eventBus.register(
                Prompt.class,
                this::createNpc,
                prompt -> prompt.name().equalsIgnoreCase("createnpc"));
        eventBus.register(
                Prompt.class,
                this::createItem,
                prompt -> prompt.name().equalsIgnoreCase("createitem"));
        /*eventBus.register(
        Prompt.class, this::move, prompt -> prompt.name().equalsIgnoreCase("goto"));*/
        eventBus.register(
                Prompt.class, this::where, prompt -> prompt.name().equalsIgnoreCase("where"));
        eventBus.register(
                Prompt.class, this::select, prompt -> prompt.name().equalsIgnoreCase("target"));
        eventBus.register(
                Prompt.class, this::mount, prompt -> prompt.name().equalsIgnoreCase("mount"));
        eventBus.register(
                Prompt.class, this::unmound, prompt -> prompt.name().equalsIgnoreCase("unmount"));
        eventBus.register(
                Prompt.class, this::sendGump, prompt -> prompt.name().equalsIgnoreCase("gump"));
        eventBus.register(SelectedObject.class, this::objectSelected);
        eventBus.register(SelectedStatics.class, this::staticSelected);
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

        gumpSystem.send(
                prompt.mobile(),
                gump,
                (ctx, selection) -> {
                    System.out.println("Recebido " + selection.getText(0));
                });
        return HandlerResult.CONTINUE;
    }

    public HandlerResult createItem(Prompt prompt) {
        gameSession.createItemAtLocation(prompt.arguments()[0], prompt.mobile());
        return HandlerResult.CONTINUE;
    }

    public HandlerResult onMove(MobileMove event) {
        System.out.println(event.mobile() + " andou");
        return HandlerResult.CONTINUE;
    }

    public HandlerResult speech(MobileSpoke spoke) {
        System.out.println("falou " + spoke.message());
        return HandlerResult.CONTINUE;
    }

    public HandlerResult createNpc(Prompt prompt) {
        gameSession.createNpcSession(prompt.arguments()[0], prompt.mobile());
        return HandlerResult.CONTINUE;
    }

    public HandlerResult move(Prompt prompt) {
        gameSession.getPlayerSession(prompt.mobile()).move(new PointInTheWorld(2516, 555, 0));
        return HandlerResult.CONTINUE;
    }

    public HandlerResult where(Prompt prompt) {
        log.info(
                "Estou em x={}, y={}, z={}",
                prompt.mobile().getX(),
                prompt.mobile().getY(),
                prompt.mobile().getZ());
        return HandlerResult.CONTINUE;
    }

    public HandlerResult select(Prompt prompt) {
        final var session = gameSession.getPlayerSession(prompt.mobile());
        session.sendTarget(CursorType.HELPFUL);
        return HandlerResult.CONTINUE;
    }

    public HandlerResult objectSelected(SelectedObject selectedObject) {
        System.out.println("Selecinou " + selectedObject);
        return HandlerResult.CONTINUE;
    }

    public HandlerResult staticSelected(SelectedStatics statics) {
        System.out.println("Selecinou statics " + statics);
        return HandlerResult.CONTINUE;
    }

    public void updateStatus(Prompt prompt) {}

    public HandlerResult mount(Prompt prompt) {
        gameSession.getPlayerSession(prompt.mobile()).mount(prompt.arguments()[0]);
        return HandlerResult.CONTINUE;
    }

    public HandlerResult unmound(Prompt prompt) {
        gameSession.getPlayerSession(prompt.mobile()).unmount();
        return HandlerResult.CONTINUE;
    }
}
