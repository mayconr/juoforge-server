# Regions

Regions are named areas of the world, organized as a tree. They are loaded by
`RegionSystemImpl` from JSON templates (`RegionTemplate`) through a `JsonTemplateLoader`,
which reads every `.json` file under the configured directory. Each file holds a JSON array
of regions, and Java (`//`) and YAML (`#`) comments are allowed.

When several regions contain a location, `RegionSystem.getRegion(location)` returns the
deepest one in the tree.

## Example

```json
[
  {
    "name": "britain-city",
    "displayName": "Britain",
    "parentName": "britannia",
    "type": "CITY",
    "aiActivationScope": true,
    "area": {
      "type": "rectangle",
      "x": 1416,
      "y": 1498,
      "width": 324,
      "height": 192
    },
    "properties": {
      "cliloc": 1075072
    },
    "lore": {
      "description": "The capital of the realm, guarded day and night.",
      "history": "Founded around the castle of the ruling lord.",
      "reputation": "Safe for travelers and honest merchants.",
      "secrets": "Old sewers connect the docks to the castle cellars."
    }
  }
]
```

## Properties

| Property            | Type    | Required | Description |
|---------------------|---------|----------|-------------|
| `name`              | string  | yes      | Unique identifier of the region. Used by `parentName`, by `RegionSystem.getRegion(name)` and by other templates, such as region stocks. Duplicate names fail the load. |
| `displayName`       | string  | yes      | Name shown to players, for example in the starting location list of the character screen. |
| `parentName`        | string  | no       | `name` of the parent region. Omit it for a root region. Loading fails when the parent does not exist. |
| `type`              | string  | yes      | One of the `RegionType` values, described below. |
| `area`              | object  | yes      | Tiles covered by the region, described below. |
| `properties`        | object  | no       | Free key/value map. Defaults to an empty map when omitted. |
| `teleport`          | object  | for `TELEPORT` | Teleport configuration, described below. |
| `aiActivationScope` | boolean | no       | Defaults to `false`. When `true`, the region is the scope in which NPC AI is activated and deactivated as players enter and leave it. A region without it uses its nearest ancestor that has it. |
| `lore`              | object  | no       | Narrative information about the region, described below. |

### `type`

| Value               | Meaning |
|---------------------|---------|
| `PROVINCE`          | Large area that groups cities and other regions. |
| `CITY`              | City or town. |
| `DISTRICT`          | Part of a city. |
| `BUILDING`          | Single building. |
| `DUNGEON`           | Dungeon. |
| `DUNGEON_LEVEL`     | Level of a dungeon. |
| `STARTING_LOCATION` | Location offered to new characters. Its area center is the spawn point and its parent `displayName` is shown as the city. |
| `TELEPORT`          | Moves a mobile that enters it to the destination given in `teleport`. |

### `area`

| Property | Type   | Used by     | Description |
|----------|--------|-------------|-------------|
| `type`   | string | all         | `rectangle` or `polygon` (case-insensitive). |
| `x`      | int    | `rectangle` | X of the top-left tile. |
| `y`      | int    | `rectangle` | Y of the top-left tile. |
| `width`  | int    | `rectangle` | Width in tiles. Must be greater than zero. |
| `height` | int    | `rectangle` | Height in tiles. Must be greater than zero. |
| `points` | array  | `polygon`   | At least three `{ "x": int, "y": int }` vertices. Vertices are tile corners: tile `(x, y)` spans from `(x, y)` to `(x + 1, y + 1)`. A tile belongs to the area when its center is inside the polygon (even-odd rule), and the polygon must cover at least one tile. |

Areas are two-dimensional; heights are resolved from the map.

### `properties`

Free map of values attached to the region. `RegionNode.resolveProperty(key)` looks up a key
in the region and then in its ancestors, so children inherit their parents' values. Keys
currently read by the server:

| Key                     | Type | Description |
|-------------------------|------|-------------|
| `cliloc`                | int  | Cliloc description of a `STARTING_LOCATION`, sent in the character list. Defaults to `0`. |

### `teleport`

Each `TELEPORT` region must declare its own `teleport` object with a nonblank
`kind` and a `destination` containing integer `x`, `y` and `z` coordinates.
For other region types, omit the object.

```json
"teleport": {
  "kind": "DUNGEON",
  "destination": { "x": 5587, "y": 631, "z": 30 }
}
```

### `lore`

Optional narrative information about the region, exposed by `RegionNode.getLore()`. The
object and every field in it are optional; a missing field is empty
(`RegionLore.findDescription()`, `findHistory()`, `findReputation()` and `findSecrets()`
return `Optional.empty()`). Lore belongs to the region that declares it and is not
inherited by its children.

| Property      | Type   | Description |
|---------------|--------|-------------|
| `description` | string | What the region looks like and what can be found there. |
| `history`     | string | Past events that shaped the region. |
| `reputation`  | string | How the region is seen by its inhabitants and by outsiders. |
| `secrets`     | string | Hidden facts about the region that are not common knowledge. |
