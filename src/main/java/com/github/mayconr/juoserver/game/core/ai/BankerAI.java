package com.github.mayconr.juoserver.game.core.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.mayconr.juoserver.game.core.ai.ollama.OllanaClient;
import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.event.HandlerResult;
import com.github.mayconr.juoserver.game.core.event.MobileSpoke;
import com.github.mayconr.juoserver.game.core.gameloop.IntervalGameTask;
import com.github.mayconr.juoserver.game.core.model.Container;
import com.github.mayconr.juoserver.game.core.model.UOContainer;
import com.github.mayconr.juoserver.game.core.model.UOPlayer;
import com.github.mayconr.juoserver.game.core.session.game.GameSession;
import com.github.mayconr.juoserver.game.core.session.npc.NpcSession;
import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.storage.WorldService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BankerAI extends IntervalGameTask implements NpcAI {

    private static final String VAULT_ATTRIBUTE = "VAULT";
    private final WorldService worldService;
    private final OllanaClient ollanaClient;
    private final EventBus eventBus;
    private GameSession gameSession;
    private NpcSession npcSession;

    private static final List<OllanaClient.Message> OLLAMA_CONTEXT =
            List.of(
                    new OllanaClient.Message(
                            "system",
                            "You are a banker in the town of Minoc, in the world of Ultima Online. Your duty is to securely guard the belongings and gold of the citizens. You speak in a polite and medieval manner, like a true NPC. Avoid any mention of the modern world, technology, or artificial intelligence. Only respond based on the universe of Ultima Online, and always be ready to open the bank when the client says \"bank\". You respect Lord British and follow the Virtues of Honor and Honesty."),
                    new OllanaClient.Message("system", "Minoc is known as the city of mining."),
                    new OllanaClient.Message("system", "You are speaking to a man."),
                    new OllanaClient.Message("system", "Ensure your responses in english"),
                    new OllanaClient.Message("system", "do not include accents in the answer"),
                    new OllanaClient.Message(
                            "system",
                            "answer with a json in the following: {\"chat\":\"\",\"action\":\"\"} where chat is your answer to the client and action is a command for the server that called you."),
                    new OllanaClient.Message(
                            "system",
                            "when you decide to open the bank use the action openClientBank"),
                    new OllanaClient.Message(
                            "system",
                            "when you feel you are in dagerous, use the action callThePollice"),
                    new OllanaClient.Message("system", "you never ask for credentials"),
                    new OllanaClient.Message(
                            "system",
                            "Always respond with a maximum of 6 tokens. The answer must be short and objective."));

    public BankerAI(WorldService worldService, OllanaClient ollanaClient, EventBus eventBus) {
        super(10);
        this.worldService = worldService;
        this.ollanaClient = ollanaClient;
        this.eventBus = eventBus;
    }

    @Override
    public void initialize(GameSession gameSession, NpcSession session) {
        this.gameSession = gameSession;
        this.npcSession = session;
        // eventBus.register(MobileSpeech.class, this::onMobileSpeech);
        log.info("AI initialized for NPC " + session.getNpc().getName());
        // sk-ee998c407c534c54acac93a2858cba2d
    }

    @Override
    public void execute() {
        // this.npcSession.move(Direction.NORTH);
    }

    public HandlerResult onMobileSpeech(MobileSpoke speech) {
        final var mobile = speech.mobile();

        // speaker must be a player
        if (!(mobile instanceof UOPlayer player)) {
            return HandlerResult.CONTINUE;
        }

        final var npc = npcSession.getNpc();
        final var playerSession = gameSession.getPlayerSession(player);
        final var speechText = speech.message();

        if (speechText.startsWith("move")) {
            /*switch (speechText) {
                case "move n" -> npcSession.move(Direction.NORTH);
                case "move s" -> npcSession.move(Direction.SOUTH);
                case "move w" -> npcSession.move(Direction.SOUTHWEST);
                case "move e" -> npcSession.move(Direction.EAST);
            }*/
            System.out.println(npcSession.getNpc().getX() + " " + npcSession.getNpc().getY());
            npcSession.move(mobile);
        } else {
            final var contextKey = "CHAT_WITH_" + mobile.getSerialId();

            final var messages = npc.getAttribute(contextKey, new ArrayList<>(OLLAMA_CONTEXT));
            messages.add(new OllanaClient.Message("user", speech.message()));

            final var response = ollanaClient.chat(messages, 6);
            final var responseText = response.getChat();
            final var action = response.getAction() != null ? response.getAction() : "";

            messages.add(new OllanaClient.Message("assistant", responseText));
            npc.addAttribute(contextKey, messages);

            npcSession.speech(responseText);

            handleAction(player, playerSession, action);

            if (speech.message().startsWith("fecha")) {
                handleCloseVault(player);
            }
        }
        return HandlerResult.CONTINUE;
    }

    private void handleAction(UOPlayer player, PlayerSession playerSession, String action) {
        if (!"openClientBank".equals(action)) return;

        final var mobile = player; // alias

        if (!mobile.hasAttribute(VAULT_ATTRIBUTE)) {
            // Cria vault se não existir
            final var vault = gameSession.createItemAtLocation("Vault", player);
            vault.addAttribute(VAULT_ATTRIBUTE, true); // TODO: impedir abrir com double click
            mobile.addAttribute(VAULT_ATTRIBUTE, vault.getSerialId());
            playerSession.openContainerInRange((Container) vault);
        } else {
            // Vault já existe
            final var serialId = mobile.getAttribute(VAULT_ATTRIBUTE, -1);
            worldService.findContainerBySerialId(serialId)
                .exceptionally(throwable -> {
                    log.error("Unable to load container serial [{}]", serialId, throwable);
                    return Optional.empty();
                }).thenAccept(conOpt->{
                    conOpt.filter(container -> container instanceof UOContainer)
                        .map(UOContainer.class::cast)
                        .ifPresent(container -> {
                            gameSession.moveItem(container, mobile);
                            playerSession.openContainerInRange(container);
                        });
                });
        }
    }

    private void handleCloseVault(UOPlayer player) {
        final int vaultSerial = player.getAttribute(VAULT_ATTRIBUTE, -1);
        worldService.findItemBySerialId(vaultSerial)
            .exceptionally(throwable -> {
                log.error("Unable to load item serial [{}]", vaultSerial, throwable);
                return Optional.empty();
            })
            .whenComplete((itemOpt, throwable)->{
                itemOpt.ifPresent(gameSession::deleteItem);
            });
    }
}
