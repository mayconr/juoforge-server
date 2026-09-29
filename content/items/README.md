# Items

The `.json` files in this directory define the shard's item templates.
Each file contains an array of objects. The server loads these templates during
bootstrap; restart the server after editing definitions. Names must be unique
across files. Use decimal numbers in JSON values and exact enum names.

A **template** describes an item type. An **instance** (`UOItem`) represents an
item created in the world, with its own identity, amount, position, and attributes.
File names organize definitions; the identifier used by the API is `name`.

## Template properties

The current contract is [ItemTemplate](../../src/main/java/com/github/mayconr/juoserver/game/item/template/ItemTemplate.java).
There is no property named `props`: additional data belongs in `attr`.

| Property | Type | Default when omitted | Current use |
| --- | --- | --- | --- |
| `name` | string | `null` | Internal identifier used for creation, lookup, consumption, and trigger selection. Define a unique name. |
| `displayName` | string | `null` | Display name of the item. The template defines no fallback. |
| `modelId` | integer | `0` | Client graphic/art ID. Also supports template lookup by model. |
| `layer` | string/enum | `null` | Equipment slot. Must be appropriate for the item when equipped. |
| `movable` | boolean | `false` | Mobility state, checked by unequip validation, for example. It is not enforced universally across all flows. |
| `hue` | integer | `0` | Initial template color. Creation through `ItemRequest` overrides this value; see below. |
| `stockType` | string | `null` | Groups templates for vendor stock, queried through `getItemsTemplate(stockType)`. |
| `basePrice` | integer | `0` | Base price used by the economy's pricing strategy. |
| `flags` | array of enums | `[]` | Classifications used by item systems. |
| `attr` | JSON object | `{}` | Custom attributes copied into the instance's persistent attributes. |
| `containerGumpId` | integer | `0` | Gump used to open a container. This is a top-level template property. |
| `mountName` | string | `null` | Present in the contract but unused by the current mount flow. The active mapping is configured in `content/mounts/mounts.json`. |
| `weapon` | object | `null` | Weapon combat configuration: style, base damage, and radius. |

These defaults describe current loading behavior; they do not guarantee that an
incomplete definition works with every system. Explicitly provide the fields
required by the intended behavior.

### Flags

| Flag | Functionality |
| --- | --- |
| `STACKABLE` | Enables stacking attempts when creating items inside containers. |
| `WEAPON` | Classifies the item as a weapon. Also configure `weapon` for combat use. |
| `WEARABLE` | Classifies the item as equipment; checked by unequip validation. Also configure `layer`. |
| `CONTAINER` | Makes the mapper create a `UOContainer`, with contents and `containerGumpId`. |
| `MOUNT` | Marks a mount item; also considered when transferring items on death. |
| `CORPSE` | Makes the mapper create a `UOCorpse`; takes precedence over `CONTAINER`. |

Flags can be combined. They do not automatically define use behavior:
tools and books depend on triggers registered by the shard.

### Layers

Available values in [Layer](../../src/main/java/com/github/mayconr/juoserver/game/model/Layer.java):

```text
INVALID, ONE_HANDED, TWO_HANDED, SHOES, PANTS, SHIRT, HEAD, GLOVES,
RING, TALISMAN, NECK, HAIR, WAIST, INNER_TORSO, BRACELET, UNUSED_XF,
FACIAL_HAIR, MIDDLE_TORSO, EARRINGS, ARMS, CLOAK, BACKPACK, OUTER_TORSO,
OUTER_LEGS, INNER_LEGS, LAST_USER_VALID, MOUNT, SHOP_BUY_RESTOCK,
SHOP_BUY, SHOP_SELL, BANK
```

`INVALID`, `UNUSED_XF`, and `LAST_USER_VALID` are special enum values;
`LAST_USER_VALID` shares the code of `INNER_LEGS`. For ordinary equipment,
use the actual slot, such as `ONE_HANDED`, `HEAD`, or `BACKPACK`.

### Weapons

| Property | Type | Use |
| --- | --- | --- |
| `weapon.style` | enum | Determines combat type and animations, including mounted animations. |
| `weapon.baseDamage.min` | integer | Lower bound of base damage. |
| `weapon.baseDamage.max` | integer | Upper bound of base damage. |
| `weapon.radius` | integer | Radius used by the combat flow. |

Available styles: `AXE`, `PICKAXE`, `SWORD`, `KATANA`, `SPEAR`, `KRYSS`,
`STAFF`, `BOW`, `CROSSBOW`, and `WRESTLING`.
`BOW` and `CROSSBOW` are ranged; `WRESTLING` uses the combat type of the same name;
the remaining styles are melee. Provide the complete object: combat consumers
access `weapon`, `style`, and `baseDamage` directly in their respective paths.

```json
[
  {
    "name": "training_katana",
    "displayName": "Training Katana",
    "modelId": 5118,
    "layer": "ONE_HANDED",
    "movable": true,
    "hue": 0,
    "flags": ["WEARABLE", "WEAPON"],
    "weapon": {
      "style": "KATANA",
      "baseDamage": { "min": 10, "max": 15 },
      "radius": 1
    },
    "attr": { "shard.training": true }
  }
]
```

### Containers

```json
[
  {
    "name": "adventurer_backpack",
    "displayName": "Backpack",
    "modelId": 3701,
    "layer": "BACKPACK",
    "movable": true,
    "flags": ["WEARABLE", "CONTAINER"],
    "containerGumpId": 60
  }
]
```

Double-clicking a container loads its contents and publishes `ContainerOpenedEvent`.
The Netty session sends the window and items to the client. This path is handled
before `ItemUseTrigger`, so containers do not reach the current item use triggers.

Use `containerGumpId`, not `attr.gumpId`: the mapper and `DrawContainer` use the
dedicated field. Some older definitions still contain `attr.gumpId`, but that
attribute does not replace `containerGumpId`.

## Creating items through the API

```java
var item = world.createItem(
        ItemRequest.byName("katana")
                .withAmount(1)
                .withHue(0)
                .withDirection(Direction.NORTH),
        ItemTarget.dropAt(player)
);
```

`ItemRequest` requires exactly one selector: `byName(name)`, `byModelId(modelId)`,
or `byTemplate(template)`. The amount must be greater than zero. Model lookup
uses the first matching template, so prefer `name` when multiple definitions
share a `modelId`.

| Request option | Default | Effect |
| --- | --- | --- |
| `withAmount(int)` | `1` | Instance amount. |
| `withHue(int)` | `0` | Overrides the color defined by the template. |
| `withDirection(Direction)` | `null` | Overrides the initial direction, even when omitted. |

Although `ItemTemplate.toData()` initializes direction to `NORTH`, the creation
step subsequently applies `request.direction()`. Set the direction explicitly
when a consumer requires it. Likewise, provide `withHue(...)` to retain a desired
color: omitting this option applies `0`, even for colored templates.

| Target | Result |
| --- | --- |
| `ItemTarget.dropAt(location)` | Creates on the ground at the supplied location. |
| `ItemTarget.equip(mobile)` | Creates equipped on the mobile, using the item's layer. |
| `ItemTarget.container(container)` | Creates inside a `UOContainer`, attempting to stack by default. |
| `ItemTarget.dropAt(container, options -> options.tryStack(false))` | Creates inside the container without attempting to stack. |
| `ItemTarget.orphan()` | Creates without placing on the ground, equipping, or adding to a container. |

Currently, the container creation step only supports containers directly equipped
on a mobile. Creating items in containers on the ground or inside other containers
is not yet supported by that step.

Stacking during creation checks `STACKABLE` and compares item `name` values;
it does not compare `hue` or `attr`. Do not assume variants with the same name
will remain separate. Stacking attempts can be disabled through `tryStack`.

## Instance state and attributes

Besides template values, `UOItem` has `id` (UUID), `serialId`, `amount`,
`x`, `y`, `z`, `direction`, `hidden`, `unitWeight`, `currentLocation`, and
`previousLocation`. These fields are not additional `ItemTemplate` properties.
Creation generates a UUID and serial, and starts with `hidden = false` and
amount `1` before applying the request. `unitWeight` exists in the model but
cannot be configured directly through the current template.

Location types are `GroundLocation`, `EquippedLocation` (owner serial),
`ContainerLocation` (container serial), and `OrphanLocation`.
Containers also track their item serials; corpses have specific data,
such as the equipment represented on the body.

`attr` is a shard extension map with no fixed schema:

```json
{
  "shard.quality": "exceptional",
  "shard.usesRemaining": 50
}
```

```java
item.persistentAttributes().set("shard.usesRemaining", 49);
item.runtimeAttributes().set("shard.inUse", true);
```

`persistentAttributes()` is part of the persistence data; saving it depends on
the shard's save/storage lifecycle. `runtimeAttributes()` exists only in memory.
A custom key has an effect only when code interprets it. The template registry
also recognizes `attr.npcName` for an NPC index, but the current mount flow
uses the dedicated mount configuration.

Changing an instance through a setter does not automatically send an update to
the client. Module operations publish the corresponding events; prefer the world
API for actions such as creation, removal, and consumption.

## Using items with a double click

The only current `Trigger` value is `DOUBLE_CLICK`. For non-container items,
the flow calls `ItemUseService` and searches `ItemUseRegistry` for the first
trigger whose `supports(context)` returns `true`. Only that trigger executes;
registration order matters. If none matches, the service logs a debug message.

The context provides `player()`, `item()`, and `trigger()`. Register behavior
in the shard configuration; it is not a JSON property:

```java
cfg.addItemTrigger(runtime -> new ItemUseTrigger() {
    @Override
    public boolean supports(ItemUseContext ctx) {
        return ctx.trigger() == Trigger.DOUBLE_CLICK
                && "spellbook".equals(ctx.item().getName());
    }

    @Override
    public void execute(ItemUseContext ctx) {
        runtime.world().openSpellBook(
                ctx.player(), ctx.item(), SpellbookType.MAGERY, -1L);
    }
});
```

The shard already registers this behavior through `SpellbookUseTrigger`; the example
illustrates registration and does not need to be added again. `pickaxe` also has
a registered trigger that starts mining. Having a trigger class in the codebase
is not enough: it must be registered through `cfg.addItemTrigger(...)`.

### Spellbook

The [spellbooks.json](spellbooks.json) template uses `WEARABLE` and `ONE_HANDED`,
without `CONTAINER`, allowing the double click to reach the trigger.
The current trigger opens Magery with all 64 spells (`spellMask = -1L`).
Type and mask are API arguments, not item template fields.

`openSpellBook` delegates to `SpellModule`, which validates parameters and
access to the book, including its container chain. The book must belong to the
player or be on the ground, directly or through its containers, within 2 tiles
(also respecting the height check in `GameMath.isInRange`).
Finally, the flow publishes `SpellBookOpened`; Netty sends the contents and opening
packets. Opening a book does not cast a spell: `castSpell` has its own flow and triggers.

## Other available operations

```java
world.deleteItem(item);
world.deleteItem(item.getSerialId());
var result = world.consumeItem(containerSerial, "gold", 10, true);
```

Consumption searches by `name`, checks the available amount, and can search
nested containers with `searchNestedContainers = true`. Creation, equipment,
movement, and removal systems maintain their own flows/handlers and events.
Template flags and properties do not replace their validation rules.

Implementation references:

- [ItemRequest](../../src/main/java/com/github/mayconr/juoserver/game/item/ItemRequest.java)
- [ItemTarget](../../src/main/java/com/github/mayconr/juoserver/game/model/ItemTarget.java)
- [UOItem](../../src/main/java/com/github/mayconr/juoserver/game/model/UOItem.java)
- [ItemUseTrigger](../../src/main/java/com/github/mayconr/juoserver/game/item/trigger/ItemUseTrigger.java)
- [WorldActions](../../src/main/java/com/github/mayconr/juoserver/game/world/WorldActions.java)
