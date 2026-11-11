# World properties

`WorldStorage` is the single entry point for loading and saving world state. Besides
mobiles and items, it owns a general-purpose key-value store exposed through
`WorldStorage.properties()`. Values are persisted as JSON by an adapter supplied by
the shard, but callers only handle typed Java values.

## Keys and values

Declare each key once, usually as a constant, binding its name to the value type:

```java
static final WorldKey<GameClockState> CLOCK =
        WorldKey.of("temporal.clock", GameClockState.class);

static final WorldKey<List<VeinState>> STATES =
        WorldKey.of("example.states", new DataType<>() {});
```

- Names must contain 1 to 100 characters and must not be blank. Prefix names with the
  owning module, such as `temporal.clock` or `mining.vein.<id>`.
- `put(key, value)` converts the value to JSON immediately; `get(key)` converts it back
  into a new object. Callers always work with copies, so changing a returned value has no
  effect until it is put again. Prefer records and other immutable values.
- `remove(key)` deletes the value. The deletion is written by the next world save.
- Unknown JSON fields are ignored when reading, so removing a field from a record does not
  invalidate stored values. A value that cannot be converted raises `IllegalStateException`.
- These operations only touch memory and are safe to call from any thread.

## Loading and saving

- Properties are loaded once, on the first access or during `WorldStorage.initialize()`,
  whichever happens first. Modules may therefore read them in `initialize(ModuleContext)`,
  even though the world initializes modules before loading mobiles and items.
- `saveWorld()` writes only the properties changed since the last successful save. Many
  `put` calls on the same key between two saves produce a single write.
- A change made while a save is running remains pending and is included in the next save.
  A failed save keeps every change pending and completes `saveWorld()` exceptionally.
- `saveMobiles()` and `saveItems()` never write properties.

Granularity is the main performance factor: a save writes whole values, so prefer one key
per independently changing entity instead of one large value containing every entity.

## Adapter

The shard configures the adapter with `WorldConfigurator.propertyStorage(...)`:

- `loadAll()` returns every stored property, indexed by name.
- `saveAll(upserts, deletes)` must apply the whole batch atomically. Names omitted from
  both arguments stay untouched, and a name never appears in both.
- Supplied JSON nodes are shared with the in-memory store and must not be modified.

Without an adapter, properties still work in memory, but `saveWorld()` fails while
changes are pending.

## Internals

The property classes live in `infrastructure.storage.property`. `CachedWorldStorage`
delegates to `PropertyStore`, which tracks a version per name to decide what is still
pending after a write. It takes part in initialization and world saves through the
`WorldPersistence` contract, so new kinds of persistent state can join the world storage
lifecycle without growing the `WorldStorage` API. Both types are public only because they
cross subpackages; they are internal to the world storage and not meant for module code.
