package com.github.mayconr.juoserver.game.flow;

import com.github.mayconr.juoserver.game.flow.DropItemFlowDefinition.DropItemContext.DropTarget;
import com.github.mayconr.juoserver.game.item.flow.drop.dropitem.*;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.DropItemContainerStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.DropItemGroundStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.StackItemStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.TransformToOrphanStep;
import com.github.mayconr.juoserver.game.item.flow.drop.validation.ValidateInteractionRangeStep;
import com.github.mayconr.juoserver.game.messaging.MessageModule;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowBuilder;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.DropItem;
import lombok.Getter;
import lombok.Setter;

/*
MoveItemFlow
 ├── ResolveIntentStep
 ├── EnsureSourceConsistencyStep
 ├── EnsureDestinationValidityStep
 ├── ValidateInteractionRangeStep
 ├── ValidateLineOfSightStep
 ├── EnsureAccessRightsStep
 ├── EnsureCapacityConstraintsStep
 ├── ValidateActionCooldownStep
 ├── EvaluateCheatRiskStep
 ├── AcquireItemLockStep
 ├── ApplyItemTransferAtomicallyStep
 ├── EnsureStateConsistencyStep
 └── EmitItemMovedEventStep
 ResolveIntentStep

Resolve e normaliza a intenção (player, sistema, script), enriquecendo o contexto com dados confiáveis do servidor.

- EnsureSourceConsistencyStep

Garante que o item realmente está na origem informada (container, chão, mobile), evitando falsificação de pacote.

- EnsureDestinationValidityStep

Valida se o destino é válido e compatível (container aceita, slot existe, tile permitido).

- ValidateInteractionRangeStep

Verifica se o player está dentro do alcance permitido para interagir com o item e o destino.

- ValidateLineOfSightStep

Confirma que há linha de visão entre player e item/destino, evitando interação através de obstáculos.

- EnsureAccessRightsStep

Valida permissões de acesso (ownership, loot, container, regras do shard).

- EnsureCapacityConstraintsStep

Garante que limites físicos são respeitados (peso do player, capacidade do container, slots disponíveis).

- ValidateActionCooldownStep

Impede ações rápidas demais, garantindo intervalo mínimo entre movimentos (anti-speed/anti-macro básico).

- EvaluateCheatRiskStep

Analisa padrões suspeitos (timing, repetição, comportamento) e atribui nível de risco sem bloquear diretamente.

- AcquireItemLockStep

Garante acesso exclusivo ao item durante o processamento, prevenindo concorrência e duplicação.

- ApplyItemTransferAtomicallyStep

Executa a transferência do item de forma atômica (remove da origem e adiciona ao destino sem inconsistência).

- EnsureStateConsistencyStep

Valida o estado final após o move, garantindo que o item não foi duplicado ou corrompido.

- EmitItemMovedEventStep

Publica eventos do movimento (e possíveis flags de risco) para sistemas externos (log, anti-cheat, scripts).
 */

public class DropItemFlowDefinition {
    private DropItemFlowDefinition() {
    }

    public static Flow<DropItemContext> build(RealmStorage storage, EventBus eventBus, MobileModule mobileModule, MessageModule messageModule) {
        return FlowFactory.<DropItemContext>builder()
                .appendGroup("DropItemPhase", dropItemPhase(storage))
                .appendGroup("ValidationPhase", validationPhase(mobileModule, messageModule))
                .appendGroup("ItemPlacementPhase", itemPlacementPhase(storage, eventBus))
                .build();
    }

    private static FlowBuilder<DropItemContext> dropItemPhase(RealmStorage storage) {
        return FlowFactory.<DropItemContext>builder()
                .step(new ResolveItemStep(storage), DropItemContext::isDroppedItem)
                .step(new ResolveDropTypeStep(), DropItemContext::isDroppedItem)
                .step(new ResolveGroundTargetStep(), ctx-> ctx.isDroppedItem(DropTarget.GROUND))
                .step(new ResolveMobileTargetStep(storage), ctx-> ctx.isDroppedItem(DropTarget.MOBILE))
                .step(new ResolveItemTargetStep(storage), ctx-> ctx.isDroppedItem(DropTarget.ITEM));
    }

    private static FlowBuilder<DropItemContext> validationPhase(MobileModule mobileModule, MessageModule messageModule) {
        return FlowFactory.<DropItemContext>builder()
                .step(new ValidateInteractionRangeStep(mobileModule, messageModule));
    }

    private static FlowBuilder<DropItemContext> itemPlacementPhase(RealmStorage storage, EventBus eventBus) {
        return FlowFactory.<DropItemContext>builder()
                .step(new TransformToOrphanStep(storage))
                .step(new DropItemGroundStep(storage, eventBus), DropItemContext::isGroundDrop)
                .step(new DropItemContainerStep(eventBus), DropItemContext::isContainerDrop)
                .step(new StackItemStep(storage, eventBus), DropItemContext::isStack);

    }

    @Getter
    @Setter
    public static class DropItemContext extends SyncFlowContext {
        private final UOPlayer player;

        private DropItem dropItem;
        private TargetContainer targetContainer;
        private TargetStack targetStack;
        private TargetGround  targetGround;

        private UOItem item;

        private DropTarget target;

        private DropItemContext(UOPlayer player) {
            this.player = player;
        }

        // FACTORIES
        public static DropItemContext ofDropItem(UOPlayer player, DropItem dropItem) {
            var context = new DropItemContext(player);
            context.setDropItem(dropItem);
            return context;
        }

        public static DropItemContext ofStack(UOPlayer player, TargetStack targetStack) {
            var context = new DropItemContext(player);
            context.setTargetStack(targetStack);
            return context;
        }

        public static DropItemContext ofGround(UOPlayer player, UOItem item,TargetGround ground) {
            var context = new DropItemContext(player);
            context.setItem(item);
            context.setTargetGround(ground);
            return context;
        }

        public static DropItemContext ofContainer(UOPlayer player, TargetContainer targetContainer) {
            var context = new DropItemContext(player);
            context.setTargetContainer(targetContainer);
            return context;
        }

        private boolean isDroppedItem(DropTarget type) {
            return type.equals(this.target) && isDroppedItem();
        }

        private boolean isDroppedItem() {
            return dropItem != null;
        }

        public boolean isStack() {
            return targetStack != null;
        }

        public boolean isContainerDrop() {
            return targetContainer != null;
        }

        public boolean isGroundDrop() {
            return targetGround != null;
        }

        public record TargetContainer(UOContainer container, Location location) { }

        public record TargetStack(UOItem itemStack) {}

        public record TargetGround(Location location) { }

        public enum DropTarget {
            /**
             * Item dropped on the ground
             */
            GROUND,
            /**
             * Item dropped on another item. It can be a stack or container
             */
            ITEM,
            /**
             * Item dropped on a mobile
             */
            MOBILE;
        }
    }
}
