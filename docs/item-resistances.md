# Item resistances

An item template can declare base contributions for five resistance types. Each
value is an integer from 0 to 100, inclusive. Missing channels, an omitted
`resistances` object, and a null object contribute zero. Invalid values reject
template loading. Use the spelling `poison`.

```json
{
  "name": "platemail",
  "displayName": "platemail",
  "layer": "INNER_TORSO",
  "modelId": 5141,
  "flags": ["WEARABLE"],
  "resistances": {
    "physical": 5,
    "fire": 3,
    "cold": 2,
    "poison": 3,
    "energy": 2
  }
}
```

These example values are shard balance choices. Resistance is a game mechanic;
it is not inferred from the graphic or equipment layer.

`ItemTemplate.toData` copies the immutable `ItemResistances` into `UOItemData`.
`UOItem` exposes it through `getResistances()` and includes it in saved data.
Saved items keep their contribution when their template changes. Older saved
items without resistance data contribute zero; template edits do not migrate
existing items.

The equip and unequip flows add or subtract the item's complete contribution.
New players and NPCs apply starter equipment after mobile creation. Item creation
with an equipment target, equipment transfer, rejected-drop restoration, death
transfer and unmounting also update the totals. Loading saved mobiles restores
saved totals without adding equipment contributions again.

`ResistanceType` identifies `PHYSICAL`, `FIRE`, `COLD`, `POISON` and `ENERGY`.
`WorldMobile.setResistance(mobile, type, value)` replaces only the selected
resistance. Other channels remain unchanged. Callers calculate the desired value
before invoking the setter; equipment flows include or exclude the relevant item
contribution for each type.
The setter clamps each supplied value to `GamePlaySettings.Mobile.minResistance`
and `maxResistance`, whose defaults are 0 and 100. Omitted or null limits use these
defaults. The minimum must not exceed the maximum.

```json
{
  "mobile": {
    "minResistance": 0,
    "maxResistance": 100
  }
}
```

The setter and equipment changes publish `MobileResistancesChanged` when an
effective resistance changes. The owning session sends `StatusBarInfo` (`0x11`)
with the current resistances and the configured maximum.

## Consulted references

- ModernUO `Projects/UOContent/Items/Armor/BaseArmor.cs`,
  `BasePhysicalResistance` and the five effective resistance properties:
  distinguishes item base values from modifiers.
- ModernUO `Projects/Server/Mobiles/Mobile.cs`, `ComputeResistances`:
  combines mobile base values, modifiers and equipment contributions. Its cap
  rules are reference-specific; Juoforge uses its configured limits.
- ClassicUO `src/ClassicUO.Client/Network/PacketHandlers.cs`, `CharacterStatus`:
  reads physical, fire, cold, poison and energy resistance from the status packet.

These files are under `docs/references/` and were consulted for behavior and
protocol semantics only.
