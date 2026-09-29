# Shard content

The shard owns this directory. The engine receives its content sources through
`WorldCfg.content(WorldContent)`; it does not assume paths on disk.
`ShardContentLoader` defines the layout, and `ShardImplConfiguration` supplies it
during bootstrap.

| Directory | Content |
| --- | --- |
| `items/` | Item definitions, grouped by kind; `resources/` contains raw materials |
| `npcs/` | NPC definitions and their behavior parameters |
| `npc-profiles/stats/` | Complete NPC stat profiles, without inheritance |
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

NPCs can reference a `statProfile`, for example `"statProfile": "undead_basic"`.
Each profile defines all six positive values: `strength`, `dexterity`,
`intelligence`, `maxHitpoints`, `maxStamina` and `maxMana`. Profiles do not
reference or inherit from other profiles.

An explicit stat on the NPC overrides that field in the profile. Omitted or null
stats use the profile value. NPCs without a profile must provide all six values;
there is no implicit default of 100. Unknown profiles, missing stats and
non-positive values fail validation during bootstrap, before the world starts.

`NpcStatsResolver` produces immutable resolved stats. `ResolveNpcStatsStep` supplies
them to `NpcTemplate.toData(..., stats)` during creation. New NPCs start with full
hitpoints, stamina and mana. The skeleton uses `undead_basic` (all values 50);
animals and vendors use explicit profiles preserving their previous values of 100.
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
