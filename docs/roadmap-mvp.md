# Roadmap do MVP

Análise em 25/10/2026, baseada no HEAD `861bc1a`. Recorte principal: os oito commits mais recentes, de 11/09 a 03/10/2026; histórico anterior consultado para contexto.

## Base de escopo

Não foi localizada uma especificação formal do MVP nos arquivos pesquisados. `content/npcs/vendors.json` define NPCs, equipamentos, perfis e categorias de estoque; não define critérios de entrega do produto. `next.md` contém um roadmap anterior de combate. Portanto, este documento é uma proposta inicial baseada nessas referências, pendente de alinhamento com a definição preexistente do MVP.

Hipótese de trabalho: entregar uma experiência pequena e persistente em Moonglow, na qual o jogador entra, movimenta-se, obtém recursos, compra ferramentas, enfrenta um inimigo, progride e recupera seu estado após reconectar. Banco e uma magia funcional são candidatos ao escopo por já haver conteúdo e infraestrutura, mas sua obrigatoriedade precisa ser confirmada. Venda de recursos ao NPC é proposta para fechar a geração de renda; não está demonstrada como requisito formal.

## Implementações dos commits recentes

| Commit | Entrega observada | Limite relevante |
| --- | --- | --- |
| `4a6ca79` — 11/09 — #12 | Combate básico, animação, cálculo de dano e suporte a armas. | Não equivale a todas as regras de combate prontas. |
| `bb65775` — 14/09 — #13 | Correções nos pacotes de morte. | Validar a experiência completa de morte e recuperação no cliente. |
| `e586f67` — 21/09 — #14 | Tentativas de ganho de skills no combate, política extensível e ajustes no ganho fracionário. | Cap total e política de redução de skills ainda precisam de definição. |
| `622ff62` — 27/09 — #15 | Templates de magias, spellbook, seleção, validação e despacho de cast para triggers e flows do shard. | Catálogo de magias não representa efeitos implementados; Heal atualmente apenas valida o caster e envia mensagem. |
| `7519f50` — 29/09 — #16 | Organização em `content/`, perfis de stats, resolução e validação de atributos na criação de NPCs; separação de conteúdo de desenvolvimento. | Alterações de template afetam NPCs novos, não automaticamente os persistidos. |
| `a2a7494` — 02/10 — #17 | Fachada de mundo dividida em módulos de combate, NPCs, itens, economia, magia, storage e outros. | É base arquitetural, sem representar um novo ciclo jogável completo. |
| `abba591` — 03/10 — #18 | IA de combate com percepção, seleção de alvo, perseguição e ataque; perfis de IA, ativação por proximidade, intervalos e proteções no combate. | Sobrevivência, fuga e recuperação possuem etapas ainda vazias. |
| `861bc1a` — 03/10 — #19 | Ajustes da IA agressiva, configuração de políticas via mundo e migração de `stock_type` de NPCs. | Validar migração em base existente e compatibilidade do conteúdo persistido. |

O histórico anterior também registra movimentação por pipeline, colisão de mobiles, índice de referências, teleporte e suporte a dano/morte. Esses componentes servem de base; esta revisão não certifica todos os seus comportamentos em execução.

## Situação dos vendors usados como referência

| NPC | Configuração atual | Situação real e próxima entrega |
| --- | --- | --- |
| Gary, banker | `vendor_basic`, `banker_dialogue`; palavras `bank`, `balance`, `withdraw`, `deposit`. | Palavras-chave não implementam operações bancárias. Criar saldo/armazenamento bancário e handlers se banco fizer parte do MVP. |
| Jay, blacksmith | Role `vendor`, `vendor_ore`, `stockType: ORE`, estilo de fala próprio. | Lista minérios por categoria; compra efetiva precisa ser concluída e protegida. O nome blacksmith não implica crafting ou reparo existentes. |
| Olaf, miner | Role `vendor`, `vendor_mining_tool`, `stockType: MINING_TOOL`, pickaxe equipada. | Há pickaxe no estoque de Moonglow; concluir entrega ao comprador e atualização de estoque. Equipar a ferramenta no NPC não abastece o catálogo por si só. |

Evidências prioritárias no código atual:

- `EconomyModuleImpl.completeVendorPurchase`: criação dos itens comprados está comentada. O caminho de resolução pode consumir ouro antes de publicar conclusão com lista vazia de itens.
- `VendorHandler.resolveVendorPurchase`: calcula custo e retira ouro, mas precisa de regras explícitas para quantidade, estoque disponível, repetição de linhas, validade da sessão e limites aritméticos.
- `VendorStep`: trata somente `buy`. Equipamentos chamados `vendor_buy`/`vendor_sell` não comprovam suporte à venda do jogador.
- `PhisycalGoldWallet`: saldo e retirada operam sobre ouro físico na mochila; `deposit` está vazio. Isso não constitui um banco.
- `content/economy/stocks/moonglow.json`: contém minérios e pickaxe. O estoque de Britannia contém minérios, sem pickaxe; o catálogo depende da resolução regional, não apenas do vendor.

## Roadmap por marcos

As prioridades representam ordem de execução, sem estimativa de calendário. Cada marco exige evidências de aceite antes de ser marcado como concluído.

### M0 — P0 — Estabelecer uma base confiável

1. Conferir o MVP original e ajustar esta hipótese de escopo; fixar um cenário de entrada, uma região, um vendedor de ferramentas e um inimigo.
2. Revisar o tempo de combate: `CombatHitFrameDelayStep` usa scheduler, mas `Flow.resolve` espera com `join()`. A evidência atual aponta para espera bloqueante do chamador; não permite repetir como fato a hipótese antiga de continuação assíncrona com swings sobrepostos. Verificar o impacto no game loop e implementar agendamento sem bloquear o loop, mantendo no máximo um swing pendente por sessão.
3. Preservar as validações de sessão, participantes e distância após o delay e antes do dano. Testar cancelamento, troca de alvo, morte e saída de alcance durante o swing.
4. Validar a criação das tabelas com `sql/schema_creation.sql`, incluindo a coluna `mobiles.stock_type`, e conferir a gravação e a carga dos NPCs. A base será recriada; não há migration incremental para esta alteração.

Aceite: durante um swing longo, movimentação e outros jogadores continuam sendo processados; cancelamento/morte impedem dano posterior; nenhuma sessão acumula swings; NPC persistido mantém sua categoria de estoque após migração e reinício.

### M1 — P0 — Concluir compra de ferramentas e minérios

Depende de M0.

1. Concluir a criação dos itens e sua entrega na mochila com a quantidade comprada.
2. Validar quantidades positivas, somas e multiplicações, linhas repetidas, saldo, estoque e identificação/validade da sessão e do vendor.
3. Tornar débito, entrega e baixa de estoque consistentes, com reversão em falha e encerramento da sessão para impedir reaproveitamento indevido.
4. Conferir catálogo por região e categoria: Olaf oferece pickaxe; Jay oferece os minérios previstos. Definir reposição mínima de ferramentas.

Aceite: `buy` abre o catálogo correto; uma compra debita o valor exato, entrega a quantidade exata e reduz o estoque; saldo insuficiente, excesso de quantidade, requisição inválida ou repetida não provocam perda de ouro nem duplicação.

### M2 — P1 — Fechar obtenção de recursos e renda

Depende de M1. Venda ao NPC é uma proposta de escopo a confirmar.

1. Validar o percurso já existente de mineração com a pickaxe comprada: alvo permitido, alcance, ferramenta, recurso e ganho de skill.
2. Implementar venda de recursos ao vendor, se aprovada no MVP: itens aceitos, quantidade, preço, transferência/consumo e crédito de ouro. Completar a operação de depósito da carteira física necessária ao pagamento.
3. Definir preços iniciais e reposição para que haja acesso recorrente às ferramentas. Explicitar como o jogador obtém o ouro inicial.

Aceite: partindo do kit inicial definido, o jogador compra uma pickaxe, minera, vende o recurso e consegue recomprar a ferramenta; falhas não criam nem removem itens/ouro indevidamente.

### M3 — P1 — Consolidar combate PvE e progressão

Depende de M0; pode avançar independentemente de M2 após a estabilização da base.

1. Validar um inimigo agressivo com percepção, perseguição, ataque, cancelamento e perda de alvo. Definir limite de perseguição e retorno para evitar perseguição indefinida.
2. Escolher explicitamente se fuga/recuperação entram no MVP. `EvaluateSurvivalStep`, `FleeStep` e `RecoverStep` ainda são placeholders; não bloquear o marco por esses comportamentos se estiverem fora do escopo.
3. Definir cap total, locks e regras de ganho em hit/miss; preservar frações ao salvar/recarregar. O mapper já usa `double`, portanto a tarefa é validar o ciclo completo, não presumir ausência de persistência.
4. Corrigir a fonte de dano quando ranged estiver no escopo; `ApplyDamageStep` fixa `DamageSourceKind.MELEE`. Se o MVP for apenas melee, documentar a restrição.
5. Definir e concluir morte, cadáver, acesso aos itens e recuperação/ressurreição conforme o MVP. Loot e respawn devem ter escopo explícito, sem presumir implementação completa pelo suporte a morte.

Aceite: jogador e inimigo causam dano com as regras escolhidas; alvo morto ou cancelado não recebe hits pendentes; skill respeita limites e sobrevive à reconexão; o jogador consegue retomar a experiência depois da morte sem comando administrativo, se esse for o fluxo do MVP.

### M4 — P1 condicional — Banco funcional

Depende de M1 e das regras de persistência. Obrigatoriedade pendente de confirmação do MVP.

1. Definir se banco guarda ouro, itens ou ambos, separado da mochila.
2. Implementar handlers para os comandos anunciados pelo banker, autorização por proximidade, saldo e limites de movimentação.
3. Persistir operações com consistência entre origem e destino.

Aceite: depositar, consultar saldo e sacar produz valores corretos após reconectar/reiniciar; quantidades inválidas e saldo insuficiente não alteram os saldos; o mesmo ouro não aparece em duas carteiras.

### M5 — P2 condicional — Uma magia com efeito real

Depende de M0 e da definição das regras de combate. Obrigatoriedade pendente de confirmação do MVP.

1. Transformar Heal em efeito de cura com alvo, limite de vida, custo, tempo de cast, alcance e cancelamento definidos pelo shard.
2. Usar o fluxo de cast existente, mantendo o ciclo de magia separado do swing físico.
3. Expor ao jogador somente as magias funcionais previstas para o MVP; os 64 registros de Magery são catálogo, não 64 efeitos entregues.

Aceite: Heal altera HP sem ultrapassar o máximo; custo e tempo são aplicados conforme regra; alvo inválido, cancelamento e recurso insuficiente têm resultados determinísticos.

### M6 — P1 — Aceite integrado e entrega do MVP

Depende de M0–M3 e dos marcos condicionais incluídos no escopo aprovado.

1. Executar o cenário completo em cliente: entrada → movimentação → compra → mineração → renda, se incluída → combate → morte/recuperação → reconexão.
2. Conferir persistência de posição, itens, ouro, skills e banco, quando incluído, após salvar e reiniciar.
3. Validar com dois jogadores disputando o mesmo estoque e interagindo com o mesmo inimigo; verificar ausência de duplicação e bloqueio do loop.
4. Documentar instalação, migração, conteúdo necessário, comandos de operação e limitações conhecidas.

Aceite: cenário reproduzível sem intervenções administrativas durante o percurso; estado preservado após reinício; verificações automatizadas relevantes passando e evidências da execução no cliente registradas.

## Revisão do roadmap anterior

`next.md` permanece como registro anterior, mas exige atualização de diagnóstico:

- Revalidação após o delay e antes do dano já aparece em `CombatExecutionFlow` e `ApplyDamageStep`; manter e testar, em vez de tratar como totalmente ausente.
- A espera assíncrona é resolvida com `join()` em `Flow`; investigar bloqueio do loop antes de afirmar sobreposição de swings.
- Infraestrutura de magia já foi entregue em #15; falta o efeito real e suas regras de execução.
- Cap individual, lock UP e mapeamento de frações existem; cap total, política DOWN e round-trip de persistência seguem como trabalho a definir/validar.
- Origem de dano fixa em MELEE continua presente e deve ser tratada conforme o escopo de ranged.

## Depois do MVP

Propostas para posterior priorização: crafting/forja e reparos reais para o blacksmith, outras escolas/efeitos de magia, ranged completo quando não incluído, IA com fuga/recuperação, mais skills defensivas, novas regiões e economia mais sofisticada. Evitar ampliar o catálogo antes de concluir os ciclos existentes.

## Limites desta análise

Revisão estática do histórico, conteúdo, código e presença de testes. Não foram executados testes, servidor, banco ou cliente; itens descritos como implementados significam código identificado, não homologação de funcionamento. Não foram modificados `vendors.json`, regras de jogo ou o roadmap anterior.
