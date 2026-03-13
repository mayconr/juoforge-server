# Próximo Sistema a Implementar no Shard (MVP)

Considerando que o shard já possui:

- Game loop
- Pacotes básicos implementados
- Criação de personagem e itens iniciais
- Skill Mining (MVP)

O próximo sistema deve:

1. Interagir com o que já existe (itens, skills, NPCs)
2. Permitir validar a infraestrutura do shard (game loop, pacotes, sessões)
3. Ser relativamente independente de sistemas complexos (como combate ou magias)

---

## Sugestões de Sistemas para MVP

### 1️⃣ NPC Vendors / Shop System
**Por que:** Permite criar NPCs que vendem e compram itens  
**O que testa da lib:**
- Sessões de player
- Envio/recebimento de pacotes
- Integração com inventário e itens

**Como validar:**  
Player inicia em cidade (ex: Minoc), compra uma pickaxe e sai para mineração.

---

### 2️⃣ Loot / Resource Node System (expandir Mining)
**Por que:** Continua o sistema de Mining e integra skill + itens:
- Veios de minério gerando diferentes ores
- Cooldown, respawn de veios
- Drop de itens automaticamente adicionados ao backpack

**O que testa da lib:**
- Sistema de skills
- Sistema de mundo / tiles
- Eventos de cooldown e timers

**Como validar:**  
Player minerando em diferentes veios, recebendo ores variados.

---

### 3️⃣ Movement + Map Interaction
**Por que:** Fundamental para validar o mundo e game loop:
- Walking / running em tiles
- Colisão e height/z-level
- Tile-based triggers (entrar em cidade, encontrar veios, triggers de quests simples)

**O que testa da lib:**
- Map blocks / LandTile / StaticTile
- Pacotes de movement (0x02, 0x78)
- Coordenação com NPCs e itens no chão

**Como validar:**  
Player andando, pegando itens do chão, interagindo com NPCs.

---

### 4️⃣ Simple Quest / Dialogue System (LLM integration)
**Por que:** Permite testar integração com LLM e diálogos dinâmicos:
- NPCs dão instruções simples
- Recompensas em itens ou gold

**O que testa da lib:**
- Interação entre NPC, player e inventário
- Persistência de estado do mundo

**Como validar:**  
Player recebe tarefa de minerar X ores e retorna ao NPC.

---

## ✅ Recomendação para Próximo Passo MVP
Começar pelo **NPC Vendor System** ou **Loot / Resource Node System**, pois:
- Interagem com **itens, inventário, skills e pacotes**
- Permitem criar um ciclo completo de ação do player (iniciar → pegar item → usar skill → voltar ao NPC)
- São simples de testar e dão métricas concretas do shard funcionando
