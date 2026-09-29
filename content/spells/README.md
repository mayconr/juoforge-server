# Spell templates

`magery.json` contains the shard's spell definitions, loaded at startup.
Each entry has a unique `key` (`namespace:name`, lowercase letters, digits and
underscores), a nonblank `name`, an optional `clientSpellId` (0-65535), and an
optional `metadata` object for future spell settings. Restart the server after editing.
Duplicate keys and duplicate non-null client IDs are rejected at startup.

The network handler resolves `clientSpellId` from `SpellSelectionExtendedCommand`
(0xBF/0x001C) to the internal key before calling `world.castSpell(caster, key)`.
Client IDs are used without subtracting one or applying a spellbook offset.
The file maps all 64 Magery spells (client IDs 1-64, keys such as `magery:clumsy`) from
[ClassicUO SpellsMagery.cs](https://github.com/ClassicUO/ClassicUO/blob/main/src/ClassicUO.Client/Game/Data/SpellsMagery.cs),
retrieved on 2026-09-22. Extend or adjust this mapping for the shard's client.
Metadata includes `school`, `circle` (1-8), `iconId` (decimal), `powerWords`,
`targetType`, and `reagents`. Names, target types, and reagent identifiers preserve
the source spelling; circles are derived from the eight groups of eight spells.
Unmapped IDs are logged as unknown and do not execute anything.

Custom spells do not need a client ID. Add an entry such as:

```json
{
  "key": "shard:arcane_nova",
  "clientSpellId": null,
  "name": "Arcane Nova",
  "metadata": { "school": "custom" }
}
```

A gump handler, NPC, or shard command can call
`world.castSpell(caster, "shard:arcane_nova")` directly. The `clientSpellId` field
may also be omitted. Spells without this mapping cannot be selected through the
client spell-ID packet path.

## Shard triggers

Register behavior during shard configuration:

```java
cfg.addSpellTrigger(runtime -> new SpellCastTrigger() {
    @Override
    public boolean supports(SpellCastContext context) {
        return "shard:arcane_nova".equals(context.spell().key());
    }

    @Override
    public void execute(SpellCastContext context) {
        // Start the shard's spell behavior here using runtime.world(), etc.
    }
});
```

`SpellCastTrigger` and `SpellCastContext` are in
`com.github.mayconr.juoserver.game.spell.trigger`.
Factories run once during bootstrap with the server runtime. They should construct
triggers, not initiate casts. Triggers are ready before world updates are enabled.
The module resolves and logs each request, then executes only the first supporting
trigger in registration order. Unknown keys never reach triggers; registered spells
without a matching trigger produce a warning. Exceptions propagate to the caller
and do not cause a fallback trigger to run.

Triggers run synchronously on the caller's thread and may receive concurrent
requests. Returning from `execute` means the request was handled, not that casting
has completed. Do not call `castSpell` recursively for the same request.

No built-in spell effects, targeting, costs, or casting time are implemented.
Metadata has gameplay meaning only when the shard's behavior uses it.
