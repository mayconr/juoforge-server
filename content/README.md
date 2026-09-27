# Shard content

The shard owns this directory. The engine receives its content sources through
`WorldCfg.content(WorldContent)`; it does not assume paths on disk.
`ShardContentLoader` defines the layout, and `ShardImplConfiguration` supplies it
during bootstrap.

| Directory | Content |
| --- | --- |
| `items/` | Item definitions, grouped by kind; `resources/` contains raw materials |
| `npcs/` | NPC definitions and their behavior parameters |
| `mounts/` | Relationships between NPC and item identifiers |
| `players/bodies/` | Race and gender body definitions |
| `players/starting-kits/` | Items granted at character creation |
| `spells/` | Spell definitions, grouped by school |
| `skills/mining/` | Mining requirements and resulting item identifiers |
| `world/regions/` | Regions, boundaries and teleports |
| `economy/stocks/` | Initial stock associated with regions |
| `messaging/` | Message styles |

Each template JSON file contains an array. Each loaded directory and its
subdirectories must contain only one template type. File names organize content;
existing identifiers remain stable when files move. Restart after editing.

NPCs accept positive `maxHitpoints`, `maxStamina`, `maxMana`, `strength`, `dexterity` and
`intelligence` values. New NPCs start with full hitpoints, stamina and mana, and the
configured strength, dexterity and intelligence.
Each omitted or null value defaults to 100.
Changing a template affects newly created NPCs, not already persisted NPCs.

## Configuration and development

- `../config/gameplay.json` is an object containing rules and balance settings.
- `../config/server.json` is an object containing the game-loop rate and local
  client-data path. The shard combines both into the engine's `GamePlaySettings`.
- `../dev/content/items/` contains development markers. These are excluded by
  default; enable them with Spring property `shard.development-content=true`.
  Duplicate item names are rejected, including across normal and dev content.
- `shard.root` selects the directory containing `config/`, `content/` and
  `dev/`. It defaults to the working directory (`.`). For example, use
  `--shard.root=C:/shards/local --shard.development-content=true` as application
  arguments.

Production content must not reference development-only items. Items used by kits
or NPCs remain in their normal item categories; kits and NPCs reference their names.
The shard content tests verify these references, including region stock and mounts,
without starting the database, network or client file reader.
